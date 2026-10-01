# FocusForge

**Your phone, rebuilt for studying.**

FocusForge is an Android-first productivity and JEE study platform.

## Current phase
**P1 — Foundation**

P0-A device-control validation is deferred until a completed APK can be installed and tested on the target OPPO K13 Turbo Pro 5G. P0-B, the golden-PDF knowledge benchmark, remains a release gate.

See `docs/SPEC.md`, `docs/ROADMAP.md`, and the active GitHub issues.

## Layout
- `android/` — Kotlin/Compose app
- `backend/` — FastAPI service, database, migrations, tests
- `p0/android-kiosk-proof/` — isolated Device Owner/Lock Task proof harness; not production code
- `.github/workflows/` — CI

## Backend
From `backend/`:
```bash
python -m venv .venv
source .venv/bin/activate
pip install -e ".[test]"
cp .env.example .env
alembic upgrade head
uvicorn app.main:app --reload
```
Set `FOCUSFORGE_JWT_SECRET` to a long random development secret. Never commit secrets or user data.

## Android
Open `android/` in Android Studio with JDK 17. The API endpoint is supplied through the Gradle property `focusforgeApiBaseUrl` (for example, `-PfocusforgeApiBaseUrl=https://your-tunnel.example`). The default placeholder intentionally does not point at a real service. P0 kiosk assumptions remain unvalidated until real-device testing.

## Rules
1. Follow `docs/ROADMAP.md` phase boundaries.
2. Keep AI solution verification states explicit.
3. Test Android behavior on real hardware.
4. Keep secrets server-side.
5. Update the spec/roadmap when approved requirements change.


## CI release prerequisites

The Android release build is intentionally fail-closed. Configure these GitHub repository settings before expecting the Android CI job to become green:

- Repository variable: `FOCUSFORGE_RELEASE_API_BASE_URL` — HTTPS production API base URL.
- Repository secrets: `FOCUSFORGE_RELEASE_KEYSTORE_B64`, `FOCUSFORGE_RELEASE_STORE_PASSWORD`, `FOCUSFORGE_RELEASE_KEY_ALIAS`, `FOCUSFORGE_RELEASE_KEY_PASSWORD`.

The release keystore is never committed to the repository. Debug builds use the controlled development endpoint `http://10.0.2.2:8080` unless `-PfocusforgeApiBaseUrlDebug=...` is supplied. Local-device HTTP endpoints are permitted only by the debug build's network-security policy; release builds require HTTPS.
