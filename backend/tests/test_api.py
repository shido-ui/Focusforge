from datetime import date, datetime, timezone
from uuid import uuid4

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


def create_account(client: TestClient):
    email = f"{uuid4()}@example.com"
    response = client.post("/api/v1/auth/signup", json=signup_payload(email))
    assert response.status_code == 201
    return email, response.json()


def test_health_and_readiness():
    client = TestClient(app)
    health = client.get("/api/v1/health")
    assert health.status_code == 200
    assert health.json()["status"] == "ok"
    assert health.headers["X-Request-ID"]

    ready = client.get("/api/v1/readiness")
    assert ready.status_code == 200
    assert ready.json()["status"] == "ready"


def test_request_id_is_validated_and_regenerated():
    client = TestClient(app)
    valid = client.get("/api/v1/health", headers={"X-Request-ID": "safe.request-id"})
    assert valid.headers["X-Request-ID"] == "safe.request-id"

    invalid = client.get("/api/v1/health", headers={"X-Request-ID": "bad\nrequest"})
    assert invalid.headers["X-Request-ID"]
    assert invalid.headers["X-Request-ID"] != "bad\nrequest"


def test_under_18_and_future_dob_are_rejected():
    client = TestClient(app)
    assert client.post(
        "/api/v1/auth/signup",
        json=signup_payload(f"{uuid4()}@example.com", date(2008, 10, 2)),
    ).status_code == 403
    assert client.post(
        "/api/v1/auth/signup",
        json=signup_payload(f"{uuid4()}@example.com", date(2099, 1, 1)),
    ).status_code == 422


def test_exact_18_boundary():
    client = TestClient(app)
    today = date.today()
    boundary = date(today.year - 18, today.month, today.day)
    before = client.post(
        "/api/v1/auth/signup",
        json=signup_payload(f"{uuid4()}@example.com", boundary),
    )
    assert before.status_code == 201


def test_email_normalization_and_whitespace_rejection():
    client = TestClient(app)
    email = f"{uuid4()}@example.com"
    assert client.post("/api/v1/auth/signup", json=signup_payload(email.upper())).status_code == 201
    duplicate = client.post("/api/v1/auth/signup", json=signup_payload(email))
    assert duplicate.status_code == 409
    whitespace = client.post("/api/v1/auth/signup", json=signup_payload(f" {uuid4()}@example.com"))
    assert whitespace.status_code == 422


def test_login_errors_are_uniform():
    client = TestClient(app)
    email, _ = create_account(client)
    wrong = client.post("/api/v1/auth/login", json={"email": email, "password": "wrong-password"})
    missing = client.post(
        "/api/v1/auth/login",
        json={"email": f"{uuid4()}@example.com", "password": "wrong-password"},
    )
    assert wrong.status_code == missing.status_code == 401
    assert wrong.json()["code"] == missing.json()["code"] == "invalid_credentials"


def test_refresh_rotation_and_reuse_rejection():
    client = TestClient(app)
    _, tokens = create_account(client)
    old_refresh = tokens["refresh_token"]
    rotated = client.post("/api/v1/auth/refresh", json={"refresh_token": old_refresh})
    assert rotated.status_code == 200
    new_refresh = rotated.json()["refresh_token"]
    assert new_refresh != old_refresh

    reused = client.post("/api/v1/auth/refresh", json={"refresh_token": old_refresh})
    assert reused.status_code == 401

    second = client.post("/api/v1/auth/refresh", json={"refresh_token": new_refresh})
    assert second.status_code == 401


def test_deleted_account_token_is_rejected():
    client = TestClient(app)
    _, tokens = create_account(client)
    auth = {"Authorization": f"Bearer {tokens['access_token']}"}
    assert client.get("/api/v1/account/me", headers=auth).status_code == 200
    assert client.delete("/api/v1/auth/account", headers=auth).status_code == 204
    assert client.get("/api/v1/account/me", headers=auth).status_code == 401


def test_account_deletion_cleans_owned_auth_and_sync_data():
    client = TestClient(app)
    _, tokens = create_account(client)
    auth = {"Authorization": f"Bearer {tokens['access_token']}"}
    event_id = str(uuid4())
    payload = {
        "client_event_id": event_id,
        "event_type": "focus.session.completed",
        "payload": {"duration_ms": 1000},
        "client_created_at": datetime.now(timezone.utc).isoformat(),
    }
    assert client.post("/api/v1/sync/events", json=payload, headers=auth).status_code == 200
    assert client.delete("/api/v1/auth/account", headers=auth).status_code == 204
    assert client.post("/api/v1/auth/refresh", json={"refresh_token": tokens["refresh_token"]}).status_code == 401


def test_sync_event_is_idempotent():
    client = TestClient(app)
    _, tokens = create_account(client)
    auth = {"Authorization": f"Bearer {tokens['access_token']}"}
    event = {
        "client_event_id": str(uuid4()),
        "event_type": "usage.sample",
        "payload": {"seconds": 42},
        "client_created_at": datetime.now(timezone.utc).isoformat(),
    }
    first = client.post("/api/v1/sync/events", json=event, headers={**auth, "Idempotency-Key": "event-1"})
    second = client.post("/api/v1/sync/events", json=event, headers={**auth, "Idempotency-Key": "event-1"})
    conflict_event = {**event, "payload": {"seconds": 43}}
    conflict = client.post("/api/v1/sync/events", json=conflict_event, headers={**auth, "Idempotency-Key": "event-1"})
    assert first.status_code == 200 and first.json()["duplicate"] is False
    assert second.status_code == 200 and second.json()["duplicate"] is False
    assert conflict.status_code == 409


def test_sync_duplicate_client_event_is_harmless():
    client = TestClient(app)
    _, tokens = create_account(client)
    auth = {"Authorization": f"Bearer {tokens['access_token']}"}
    event = {
        "client_event_id": str(uuid4()),
        "event_type": "usage.sample",
        "payload": {"seconds": 42},
        "client_created_at": datetime.now(timezone.utc).isoformat(),
    }
    first = client.post("/api/v1/sync/events", json=event, headers=auth)
    second = client.post("/api/v1/sync/events", json=event, headers=auth)
    assert first.status_code == second.status_code == 200
    assert first.json()["duplicate"] is False
    assert second.json()["duplicate"] is True


def test_cors_is_explicit():
    client = TestClient(app)
    response = client.get("/api/v1/health", headers={"Origin": "http://localhost:3000"})
    assert response.status_code == 200
    assert response.headers["access-control-allow-origin"] == "http://localhost:3000"
