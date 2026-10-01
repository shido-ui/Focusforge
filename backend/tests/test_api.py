import os
from datetime import date
from uuid import uuid4

os.environ["FOCUSFORGE_JWT_SECRET"] = "ci-test-secret-0123456789-0123456789"

from fastapi.testclient import TestClient

from app.main import app


def signup_payload(email: str, dob: date = date(2000, 1, 1)):
    return {
        "email": email,
        "password": "a-strong-password-123",
        "date_of_birth": dob.isoformat(),
        "terms_version": "v1",
        "privacy_version": "v1",
    }


def test_health():
    r = TestClient(app).get("/api/v1/health")
    assert r.status_code == 200
    assert r.json()["status"] == "ok"
    assert r.headers["X-Request-ID"]


def test_request_id_is_preserved():
    request_id = "test-request-id"
    r = TestClient(app).get("/api/v1/health", headers={"X-Request-ID": request_id})
    assert r.status_code == 200
    assert r.headers["X-Request-ID"] == request_id


def test_under_18_signup_is_rejected():
    r = TestClient(app).post(
        "/api/v1/auth/signup",
        json=signup_payload(f"{uuid4()}@example.com", date(2008, 10, 2)),
    )
    assert r.status_code == 403


def test_future_dob_is_rejected():
    r = TestClient(app).post(
        "/api/v1/auth/signup",
        json=signup_payload(f"{uuid4()}@example.com", date(2099, 1, 1)),
    )
    assert r.status_code == 422


def test_signup_login_and_case_normalization():
    client = TestClient(app)
    email = f"{uuid4()}@example.com"
    signup = client.post("/api/v1/auth/signup", json=signup_payload(email))
    assert signup.status_code == 201

    login = client.post(
        "/api/v1/auth/login",
        json={"email": email.upper(), "password": "a-strong-password-123"},
    )
    assert login.status_code == 200
    assert login.json()["token_type"] == "bearer"
    assert login.json()["access_token"]


def test_duplicate_signup_is_rejected():
    client = TestClient(app)
    email = f"{uuid4()}@example.com"
    assert client.post("/api/v1/auth/signup", json=signup_payload(email)).status_code == 201
    duplicate = client.post("/api/v1/auth/signup", json=signup_payload(email))
    assert duplicate.status_code == 409


def test_invalid_login_is_rejected():
    client = TestClient(app)
    email = f"{uuid4()}@example.com"
    assert client.post("/api/v1/auth/signup", json=signup_payload(email)).status_code == 201
    invalid = client.post(
        "/api/v1/auth/login",
        json={"email": email, "password": "wrong-password"},
    )
    assert invalid.status_code == 401


def test_account_deletion():
    client = TestClient(app)
    email = f"{uuid4()}@example.com"
    signup = client.post("/api/v1/auth/signup", json=signup_payload(email))
    assert signup.status_code == 201
    token = signup.json()["access_token"]
    deleted = client.delete(
        "/api/v1/auth/account",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert deleted.status_code == 204
