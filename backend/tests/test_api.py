import os
os.environ["FOCUSFORGE_JWT_SECRET"]="ci-test-secret"
from fastapi.testclient import TestClient
from app.main import app
def test_health():
 r=TestClient(app).get("/api/v1/health"); assert r.status_code==200 and r.json()["status"]=="ok"
