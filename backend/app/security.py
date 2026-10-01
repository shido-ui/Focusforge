from datetime import date, datetime, timedelta, timezone
import hashlib
import hmac
import secrets

import jwt
from argon2 import PasswordHasher
from fastapi import Depends, HTTPException
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.orm import Session

from .config import get_settings
from .db import get_db
from .models import AuthSession, User, utc_now

ph = PasswordHasher()
bearer = HTTPBearer(auto_error=False)
DUMMY_PASSWORD_HASH = ph.hash("focusforge-dummy-password-only-for-timing")


def hash_password(password: str) -> str:
    return ph.hash(password)


def verify_password(password: str, password_hash: str) -> bool:
    try:
        return ph.verify(password_hash, password)
    except Exception:
        return False


def is_18_or_older(dob: date, today: date | None = None) -> bool:
    today = today or date.today()
    return dob <= date(today.year - 18, today.month, today.day)


def create_access_token(sub: str) -> tuple[str, datetime]:
    settings = get_settings()
    now = datetime.now(timezone.utc)
    expires_at = now + timedelta(minutes=settings.access_token_minutes)
    token = jwt.encode(
        {"sub": sub, "exp": expires_at, "iat": now},
        settings.jwt_secret,
        algorithm="HS256",
    )
    return token, expires_at


def hash_refresh_secret(secret: str) -> str:
    return hashlib.sha256(secret.encode("utf-8")).hexdigest()


def issue_refresh_session(
    db: Session,
    user_id: str,
    device_label: str | None = None,
) -> tuple[str, AuthSession]:
    settings = get_settings()
    session = AuthSession(
        user_id=user_id,
        token_hash="pending",
        expires_at=utc_now() + timedelta(days=settings.refresh_token_days),
        device_label=device_label,
    )
    db.add(session)
    db.flush()
    secret = secrets.token_urlsafe(48)
    session.token_hash = hash_refresh_secret(secret)
    return f"{session.id}.{secret}", session


def parse_refresh_token(raw_token: str) -> tuple[str, str]:
    try:
        session_id, secret = raw_token.split(".", 1)
    except ValueError as exc:
        raise HTTPException(401, "Invalid or expired session.") from exc
    if len(session_id) != 36 or len(secret) < 32:
        raise HTTPException(401, "Invalid or expired session.")
    return session_id, secret


def get_current_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(bearer),
    db: Session = Depends(get_db),
) -> User:
    if credentials is None or credentials.scheme.lower() != "bearer":
        raise HTTPException(status_code=401, detail="Authentication required.")
    try:
        payload = jwt.decode(
            credentials.credentials,
            get_settings().jwt_secret,
            algorithms=["HS256"],
        )
        subject = payload.get("sub")
        if not isinstance(subject, str) or not subject:
            raise ValueError
    except (jwt.PyJWTError, ValueError):
        raise HTTPException(status_code=401, detail="Invalid or expired session.")
    user = db.get(User, subject)
    if user is None:
        raise HTTPException(status_code=401, detail="Invalid or expired session.")
    return user


def get_current_user_id(user: User = Depends(get_current_user)) -> str:
    return user.id


def rotate_refresh_token(
    db: Session,
    raw_token: str,
    device_label: str | None = None,
) -> tuple[str, str, datetime]:
    session_id, secret = parse_refresh_token(raw_token)
    session = db.get(AuthSession, session_id)
    now = utc_now()

    if session is None or session.revoked_at is not None:
        raise HTTPException(401, "Invalid or expired session.")
    if session.expires_at <= now:
        session.revoked_at = now
        db.commit()
        raise HTTPException(401, "Invalid or expired session.")
    if not hmac.compare_digest(session.token_hash, hash_refresh_secret(secret)):
        session.revoked_at = now
        db.commit()
        raise HTTPException(401, "Invalid or expired session.")

    new_raw, new_session = issue_refresh_session(
        db, session.user_id, device_label or session.device_label
    )
    session.revoked_at = now
    session.replaced_by_id = new_session.id
    session.rotation_counter += 1
    db.commit()
    access_token, access_expiry = create_access_token(session.user_id)
    return access_token, new_raw, access_expiry
