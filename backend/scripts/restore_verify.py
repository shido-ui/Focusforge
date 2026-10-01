from __future__ import annotations

import sqlite3
import sys
from pathlib import Path


def verify_backup(path: str) -> None:
    db_path = Path(path)
    if not db_path.is_file():
        raise FileNotFoundError(db_path)
    connection = sqlite3.connect(db_path)
    try:
        integrity = connection.execute("PRAGMA integrity_check").fetchone()[0]
        foreign_keys = connection.execute("PRAGMA foreign_key_check").fetchall()
        if integrity != "ok" or foreign_keys:
            raise RuntimeError(f"Backup verification failed: integrity={integrity!r}, foreign_keys={foreign_keys!r}")
    finally:
        connection.close()


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit("usage: python -m scripts.restore_verify BACKUP.db")
    verify_backup(sys.argv[1])
    print("backup verification: ok")
