import os
from datetime import date
from uuid import uuid4
os.environ["FOCUSFORGE_JWT_SECRET"]="ci-test-secret"
from fastapi.testclient import TestClient
from app.main import app

def test_health():
    r=TestClient(app).get("/api/v1/health")
    assert r.status_code==200 and r.json()["status"]=="ok" and r.headers["X-Request-ID"]

def test_request_id_is_preserved():
    request_id="test-request-id"
    r=TestClient(app).get("/api/v1/health", headers={"X-Request-ID":request_id})
    assert r.status_code==200 and r.headers["X-Request-ID"]==request_id

def test_account_deletion():
    client=TestClient(app); email=f"{uuid4()}@example.com"
    signup=client.post("/api/v1/auth/signup",json={"email":email,"password":"a-strong-password-123","date_of_birth":date(2000,1,1).isoformat(),"terms_version":"v1","privacy_version":"v1"})
    assert signup.status_code==201
    token=signup.json()["access_token"]
    deleted=client.delete("/api/v1/auth/account",headers={"Authorization":f"Bearer {token}"})
    assert deleted.status_code==204
