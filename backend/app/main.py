import logging
from uuid import uuid4

from fastapi import Depends, FastAPI, HTTPException, Request, status
from fastapi.responses import JSONResponse
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from .db import get_db
from .logging import configure_logging, logger
from .models import User
from .schemas import LoginRequest, SignupRequest, TokenResponse
from .security import (
    create_access_token,
    get_current_user_id,
    hash_password,
    is_18_or_older,
    verify_password,
)

configure_logging()
app = FastAPI(title="FocusForge API", version="0.1.0")


@app.middleware("http")
async def request_logging(request: Request, call_next):
    request_id = request.headers.get("X-Request-ID") or uuid4().hex
    try:
        response = await call_next(request)
        logger.info(
            "request method=%s path=%s status=%s request_id=%s",
            request.method,
            request.url.path,
            response.status_code,
            request_id,
        )
        response.headers["X-Request-ID"] = request_id
        return response
    except Exception:
        logger.exception(
            "request_failed method=%s path=%s request_id=%s",
            request.method,
            request.url.path,
            request_id,
        )
        raise


@app.exception_handler(Exception)
async def unhandled_exception(request: Request, exc: Exception):
    request_id = request.headers.get("X-Request-ID") or uuid4().hex
    logger.exception(
        "unhandled_exception method=%s path=%s request_id=%s",
        request.method,
        request.url.path,
        request_id,
    )
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error.", "request_id": request_id},
        headers={"X-Request-ID": request_id},
    )


@app.get("/api/v1/health")
def health():
    return {"status": "ok", "service": "focusforge-api"}


@app.post("/api/v1/auth/signup", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
def signup(p: SignupRequest, db: Session = Depends(get_db)):
    if not is_18_or_older(p.date_of_birth):
        raise HTTPException(403, "FocusForge requires users to be 18 or older.")

    email = p.email.lower()
    if db.scalar(select(User).where(User.email == email)):
        raise HTTPException(409, "An account with this email already exists.")

    user = User(
        email=email,
        password_hash=hash_password(p.password),
        date_of_birth=p.date_of_birth.isoformat(),
        terms_version=p.terms_version,
        privacy_version=p.privacy_version,
    )
    db.add(user)
    try:
        db.commit()
    except IntegrityError:
        db.rollback()
        raise HTTPException(409, "An account with this email already exists.")
    db.refresh(user)
    return TokenResponse(access_token=create_access_token(user.id))


@app.post("/api/v1/auth/login", response_model=TokenResponse)
def login(p: LoginRequest, db: Session = Depends(get_db)):
    user = db.scalar(select(User).where(User.email == p.email.lower()))
    if not user or not verify_password(p.password, user.password_hash):
        raise HTTPException(401, "Invalid credentials.")
    return TokenResponse(access_token=create_access_token(user.id))


@app.delete("/api/v1/auth/account", status_code=status.HTTP_204_NO_CONTENT)
def delete_account(
    user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    user = db.get(User, user_id)
    if user is None:
        raise HTTPException(status_code=404, detail="Account not found.")
    db.delete(user)
    db.commit()
    return None
