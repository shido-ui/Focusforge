import json
import logging
import re
import threading
import time
from collections import defaultdict, deque
from uuid import uuid4

from fastapi import Depends, FastAPI, HTTPException, Request, Response, status
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session
from starlette.exceptions import HTTPException as StarletteHTTPException

from .config import get_settings
from .db import get_db
from .logging import configure_logging, logger
from .models import AuthSession, SyncEvent, User, utc_now
from .schemas import (
    AccountResponse,
    LoginRequest,
    RefreshRequest,
    SignupRequest,
    SyncEventRequest,
    SyncEventResponse,
    TokenResponse,
)
from .security import (
    DUMMY_PASSWORD_HASH,
    create_access_token,
    get_current_user,
    hash_password,
    issue_refresh_session,
    is_18_or_older,
    rotate_refresh_token,
    verify_password,
)

configure_logging()
settings = get_settings()
app = FastAPI(title="FocusForge API", version="0.2.0")

if settings.allowed_origins:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.allowed_origins,
        allow_credentials=True,
        allow_methods=["GET", "POST", "DELETE", "OPTIONS"],
        allow_headers=["Authorization", "Content-Type", "X-Request-ID", "Idempotency-Key"],
        max_age=600,
    )

_REQUEST_ID_RE = re.compile(r"^[A-Za-z0-9._:-]{1,64}$")


class SlidingWindowLimiter:
    def __init__(self):
        self._lock = threading.Lock()
        self._hits: dict[str, deque[float]] = defaultdict(deque)

    def check(self, key: str, limit: int, window_seconds: int = 60) -> int | None:
        now = time.monotonic()
        with self._lock:
            hits = self._hits[key]
            cutoff = now - window_seconds
            while hits and hits[0] <= cutoff:
                hits.popleft()
            if len(hits) >= limit:
                return max(1, int(hits[0] + window_seconds - now))
            hits.append(now)
            if len(self._hits) > 5000:
                stale = [k for k, v in self._hits.items() if not v or v[-1] <= cutoff]
                for stale_key in stale[:1000]:
                    self._hits.pop(stale_key, None)
        return None


limiter = SlidingWindowLimiter()


def client_ip(request: Request) -> str:
    # Do not trust X-Forwarded-For unless a trusted reverse proxy is explicitly configured.
    return request.client.host if request.client else "unknown"


def enforce_rate_limit(request: Request, scope: str, limit: int, identity: str = "") -> None:
    keys = [f"ip:{scope}:{client_ip(request)}"]
    if identity:
        keys.append(f"identity:{scope}:{identity}")
    retry_after = max((limiter.check(key, limit) or 0 for key in keys), default=0)
    if retry_after:
        raise HTTPException(
            status_code=429,
            detail={
                "code": "rate_limited",
                "message": "Too many requests. Try again later.",
                "retry_after": retry_after,
            },
            headers={"Retry-After": str(retry_after)},
        )


def request_id_for(request: Request) -> str:
    value = request.headers.get("X-Request-ID", "")
    return value if _REQUEST_ID_RE.fullmatch(value) else uuid4().hex


@app.middleware("http")
async def request_context(request: Request, call_next):
    request_id = request_id_for(request)
    request.state.request_id = request_id
    content_length = request.headers.get("content-length")
    if content_length:
        try:
            length = int(content_length)
        except ValueError:
            return JSONResponse(
                status_code=400,
                content={
                    "code": "invalid_content_length",
                    "message": "Invalid content length.",
                    "request_id": request_id,
                },
            )
        limit = settings.max_upload_bytes if request.url.path.startswith("/api/v1/documents") else settings.max_request_bytes
        if length > limit:
            return JSONResponse(
                status_code=413,
                content={
                    "code": "request_too_large",
                    "message": "Request is too large.",
                    "request_id": request_id,
                },
            )
    try:
        response = await call_next(request)
    except Exception:
        logger.exception(
            "request_failed method=%s path=%s request_id=%s",
            request.method,
            request.url.path,
            request_id,
        )
        raise
    response.headers["X-Request-ID"] = request_id
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["Referrer-Policy"] = "no-referrer"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["Cache-Control"] = "no-store" if request.url.path.startswith("/api/v1/auth") else "no-cache"
    if settings.environment == "production":
        response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
    logger.info(
        "request method=%s path=%s status=%s request_id=%s",
        request.method,
        request.url.path,
        response.status_code,
        request_id,
    )
    return response


@app.exception_handler(StarletteHTTPException)
async def http_exception_handler(request: Request, exc: StarletteHTTPException):
    request_id = getattr(request.state, "request_id", uuid4().hex)
    detail = exc.detail if isinstance(exc.detail, dict) else {
        "code": "http_error",
        "message": str(exc.detail or "Request failed."),
    }
    payload = {
        "code": detail.get("code", "http_error"),
        "message": detail.get("message", "Request failed."),
        "request_id": request_id,
        "retryable": exc.status_code in {408, 425, 429, 500, 502, 503, 504},
    }
    headers = {"X-Request-ID": request_id}
    if exc.headers:
        headers.update(exc.headers)
    return JSONResponse(status_code=exc.status_code, content=payload, headers=headers)


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    request_id = getattr(request.state, "request_id", uuid4().hex)
    return JSONResponse(
        status_code=422,
        content={
            "code": "validation_error",
            "message": "Request validation failed.",
            "request_id": request_id,
            "retryable": False,
        },
        headers={"X-Request-ID": request_id},
    )


@app.exception_handler(Exception)
async def unhandled_exception(request: Request, exc: Exception):
    request_id = getattr(request.state, "request_id", uuid4().hex)
    logger.exception(
        "unhandled_exception method=%s path=%s request_id=%s",
        request.method,
        request.url.path,
        request_id,
    )
    return JSONResponse(
        status_code=500,
        content={
            "code": "internal_error",
            "message": "Internal server error.",
            "request_id": request_id,
            "retryable": True,
        },
        headers={"X-Request-ID": request_id},
    )


@app.get("/api/v1/health")
def health():
    return {"status": "ok", "service": "focusforge-api"}


@app.get("/api/v1/readiness")
def readiness(db: Session = Depends(get_db)):
    db.execute(select(User).limit(1))
    return {"status": "ready", "service": "focusforge-api"}


def token_response(db: Session, user_id: str, device_label: str | None = None) -> TokenResponse:
    access_token, access_expiry = create_access_token(user_id)
    refresh_token, _ = issue_refresh_session(db, user_id, device_label)
    return TokenResponse(
        access_token=access_token,
        refresh_token=refresh_token,
        access_token_expires_at=access_expiry,
    )


@app.post("/api/v1/auth/signup", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
def signup(p: SignupRequest, request: Request, db: Session = Depends(get_db)):
    email = str(p.email)
    enforce_rate_limit(request, "signup", settings.signup_rate_limit_per_minute, email)
    if not is_18_or_older(p.date_of_birth):
        raise HTTPException(403, {"code": "age_restricted", "message": "FocusForge requires users to be 18 or older."})
    if db.scalar(select(User).where(User.email == email)):
        raise HTTPException(409, {"code": "account_exists", "message": "An account with this email already exists."})

    user = User(
        email=email,
        password_hash=hash_password(p.password),
        date_of_birth=p.date_of_birth.isoformat(),
        terms_version=p.terms_version,
        privacy_version=p.privacy_version,
    )
    db.add(user)
    try:
        db.flush()
        response = token_response(db, user.id)
        db.commit()
    except IntegrityError:
        db.rollback()
        raise HTTPException(409, {"code": "account_exists", "message": "An account with this email already exists."})
    return response


@app.post("/api/v1/auth/login", response_model=TokenResponse)
def login(p: LoginRequest, request: Request, db: Session = Depends(get_db)):
    email = str(p.email)
    enforce_rate_limit(request, "login", settings.login_rate_limit_per_minute, email)
    user = db.scalar(select(User).where(User.email == email))
    password_hash = user.password_hash if user else DUMMY_PASSWORD_HASH
    valid = verify_password(p.password, password_hash)
    if not user or not valid:
        raise HTTPException(401, {"code": "invalid_credentials", "message": "Invalid credentials."})
    response = token_response(db, user.id)
    db.commit()
    return response


@app.post("/api/v1/auth/refresh", response_model=TokenResponse)
def refresh(p: RefreshRequest, request: Request, db: Session = Depends(get_db)):
    enforce_rate_limit(request, "refresh", settings.refresh_rate_limit_per_minute)
    access, refresh_token, expiry = rotate_refresh_token(db, p.refresh_token, p.device_label)
    return TokenResponse(
        access_token=access,
        refresh_token=refresh_token,
        access_token_expires_at=expiry,
    )


@app.post("/api/v1/auth/logout", status_code=status.HTTP_204_NO_CONTENT)
def logout(p: RefreshRequest, request: Request, db: Session = Depends(get_db)):
    enforce_rate_limit(request, "logout", 10)
    try:
        session_id, _ = p.refresh_token.split(".", 1)
    except ValueError:
        return Response(status_code=204)
    session = db.get(AuthSession, session_id)
    if session is not None and session.revoked_at is None:
        session.revoked_at = utc_now()
        db.commit()
    return Response(status_code=204)


@app.get("/api/v1/account/me", response_model=AccountResponse)
def account_me(user: User = Depends(get_current_user)):
    return AccountResponse(id=user.id, email=user.email)


@app.delete("/api/v1/auth/account", status_code=status.HTTP_204_NO_CONTENT)
def delete_account(request: Request, user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    enforce_rate_limit(request, "account-delete", settings.account_delete_rate_limit_per_minute, user.id)
    db.delete(user)
    db.commit()
    return Response(status_code=204)


@app.post("/api/v1/sync/events", response_model=SyncEventResponse)
def sync_event(
    event: SyncEventRequest,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    existing = db.scalar(
        select(SyncEvent).where(
            SyncEvent.user_id == user.id,
            SyncEvent.client_event_id == event.client_event_id,
        )
    )
    if existing is not None:
        return SyncEventResponse(client_event_id=event.client_event_id, accepted=True, duplicate=True)
    record = SyncEvent(
        user_id=user.id,
        client_event_id=event.client_event_id,
        event_type=event.event_type,
        payload_json=json.dumps(event.payload, separators=(",", ":"), sort_keys=True),
        client_created_at=event.client_created_at,
    )
    db.add(record)
    try:
        db.commit()
    except IntegrityError:
        db.rollback()
        return SyncEventResponse(client_event_id=event.client_event_id, accepted=True, duplicate=True)
    return SyncEventResponse(client_event_id=event.client_event_id, accepted=True, duplicate=False)
