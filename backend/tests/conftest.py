import pytest
from sqlalchemy import delete

from app.db import SessionLocal
from app.main import limiter
from app.models import AuthSession, IdempotencyRecord, SyncEvent, User


@pytest.fixture(autouse=True)
def clean_database():
    with SessionLocal() as db:
        db.execute(delete(SyncEvent))
        db.execute(delete(IdempotencyRecord))
        db.execute(delete(AuthSession))
        db.execute(delete(User))
        db.commit()
    limiter._hits.clear()
    yield
