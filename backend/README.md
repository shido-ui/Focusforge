# FocusForge backend

Python 3.12 FastAPI service.

## Local setup

```bash
python -m venv .venv
source .venv/bin/activate
pip install -e ".[test]"
cp .env.example .env
```

Set `FOCUSFORGE_JWT_SECRET` to a long random development secret. Never commit secrets or user data.

## Database

```bash
alembic upgrade head
```

Start the API:

```bash
uvicorn app.main:app --reload
```

Health check:

```bash
curl http://127.0.0.1:8000/api/v1/health
```

Every response includes an `X-Request-ID` for tracing. Clients may provide one; otherwise the server generates it.

## Termux deployment

Termux is the intended early phone-hosted deployment.

- Keep FastAPI bound to localhost.
- Keep the JWT secret outside Git.
- Run Alembic migrations before startup.
- Do not expose Uvicorn directly to the public internet.
- Use Cloudflare Tunnel as the external HTTPS ingress.
- Configure the Android client with the tunnel HTTPS base URL.
- Do not commit tunnel tokens, domains containing private routing details, JWT secrets, or user databases.

Example local listener:

```bash
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

Before connecting real accounts, verify TLS, authentication, rate limiting, backup/recovery, and account deletion behavior.

## Testing

```bash
pytest
```

CI runs Alembic migrations before the backend tests.
