from datetime import date

import pytest
import jwt
from fastapi import HTTPException
from pydantic import ValidationError

from app.config import Settings
from app.security import (
    create_access_token,
    get_current_user_id,
    hash_password,
    is_18_or_older,
    verify_password,
)


def test_adult_boundary():
    assert is_18_or_older(date(2008, 10, 1), date(2026, 10, 1))
    assert not is_18_or_older(date(2008, 10, 2), date(2026, 10, 1))


def test_password_hash_round_trip():
    password = "a-strong-password-123"
    hashed = hash_password(password)
    assert hashed != password
    assert verify_password(password, hashed)
    assert not verify_password("wrong-password", hashed)


def test_access_token_contains_expected_subject():
    token = create_access_token("user-123")
    payload = jwt.decode(token, __import__("app.config", fromlist=["get_settings"]).get_settings().jwt_secret, algorithms=["HS256"])
    assert payload["sub"] == "user-123"
    assert "exp" in payload


def test_missing_authentication_is_rejected():
    with pytest.raises(HTTPException) as exc:
        get_current_user_id(None)
    assert exc.value.status_code == 401


def test_production_rejects_short_jwt_secret():
    with pytest.raises(ValidationError):
        Settings(environment="production", jwt_secret="too-short")


def test_access_token_window_is_bounded():
    with pytest.raises(ValidationError):
        Settings(jwt_secret="test-secret", access_token_minutes=4)
    with pytest.raises(ValidationError):
        Settings(jwt_secret="test-secret", access_token_minutes=61)
