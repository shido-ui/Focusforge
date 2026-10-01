from __future__ import annotations

import shutil
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
    shutil.copy2(source, destination)
    return destination


if __name__ == "__main__":
    print(backup_sqlite())
