from __future__ import annotations

import sqlite3
from datetime import datetime, timezone
from pathlib import Path

from app.config import get_settings


def backup_sqlite() -> Path:
    settings = get_settings()
    source = Path(settings.database_url.removeprefix("sqlite:///"))
    if not source.exists():
        raise FileNotFoundError(source)
    destination_dir = Path(settings.backup_directory)
    destination_dir.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    destination = destination_dir / f"focusforge-{stamp}.db"

    source_connection = sqlite3.connect(source)
    destination_connection = sqlite3.connect(destination)
    try:
        source_connection.backup(destination_connection)
        destination_connection.execute("PRAGMA wal_checkpoint(TRUNCATE)")
        destination_connection.commit()
    finally:
        destination_connection.close()
        source_connection.close()
    return destination


if __name__ == "__main__":
    print(backup_sqlite())
