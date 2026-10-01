from datetime import date

from app.security import create_access_token, get_current_user_id, is_18_or_older, verify_password


def test_adult_boundary():
    assert is_18_or_older(date(2008, 10, 1), date(2026, 10, 1))
    assert not is_18_or_older(date(2008, 10, 2), date(2026, 10, 1))


def test_password_hash_round_trip():
    from app.security import hash_password
    password = "a-strong-password-123"
    hashed = hash_password(password)
    assert hashed != password
    assert verify_password(password, hashed)
    assert not verify_password("wrong-password", hashed)


def test_access_token_contains_expected_subject():
    token = create_access_token("user-123")
    import jwt
    from app.security import get_settings
    payload = jwt.decode(token, get_settings().jwt_secret, algorithms=["HS256"])
    assert payload["sub"] == "user-123"
    assert "exp" in payload


def test_missing_authentication_is_rejected():
    from fastapi import HTTPException
    try:
        get_current_user_id(None)
        assert False
    except HTTPException as exc:
        assert exc.status_code == 401
