# FocusForge — Master Product & Engineering Specification v4

> **Your phone, rebuilt for studying.**

## 1. Product definition

FocusForge is an Android-first productivity and JEE learning platform that transforms a student's phone into a controlled study environment.

It combines:

- Focus OS — scheduled lockdown + custom launcher
- AI Knowledge Engine — PDF → structured, organized, solved question bank
- Practice Engine — Fast Mode, tests and basic practice
- Productivity Intelligence — usage analytics and leaderboard

Core loop:

```
Material
→ AI understands it
→ automatically organizes it
→ every question receives a Solution object
→ student practices
→ mistakes/results become data
→ progress is measured
→ future practice improves
```

## 2. v1 scope

Build only:

- Focus lockdown and scheduling
- Custom FocusForge launcher
- Usage statistics and productivity/distraction analytics
- AI-powered PDF/document ingestion
- Automatic chapter/topic organization
- Question, answer, solution, diagram and table extraction
- AI-generated solutions for questions without source solutions
- Solution verification and provenance
- Library
- Fast Mode
- Basic JEE-style tests
- Simple leaderboard

Defer advanced adaptive practice, full Hard Mode, knowledge graph, AI Study Copilot and advanced anti-cheat.

## 3. P0 gate — mandatory for release acceptance

> **Project-control decision (2026-10-01):** P0 hardware validation is deferred until a completed APK is available for installation on the target device. P0 remains open and release-blocking; this is an explicit sequencing exception, not a P0 pass.

Nothing else is worth building until both real-hardware risks are proven.

### P0-A — Device control

Prove on a real Android device:

1. Device Owner provisioning feasibility
2. Lock Task
3. FocusForge launcher
4. Scheduled session
5. Allowed-app behavior
6. Restricted-app behavior
7. Process/app restart recovery
8. Reboot recovery
9. Persisted session state
10. Safe recovery path

Document Android/OEM limitations instead of claiming impossible lockdown.

### P0-B — Knowledge engine

Run real material through:

1. Clean PDF
2. Scanned PDF
3. Diagram-heavy PDF
4. Equation-heavy/cross-page PDF
5. Questions with source solutions
6. Questions without source solutions

Measure:

- extraction precision
- extraction recall
- question assembly accuracy
- diagram association
- table extraction
- equation/LaTeX integrity
- source-solution extraction
- AI-generated solution correctness
- verification tier
- processing time
- Gemini/API cost

## 4. Focus OS

### Enforcement tiers

**Soft**

- Custom launcher
- UsageStats
- session monitoring

**Strong**

- Supported additional Android enforcement
- foreground monitoring
- watchdog

**Hard — later**

- Device Owner
- Lock Task
- managed-device controls

FocusForge must never claim stronger control than Android/device policy actually provides.

### Session state

```
IDLE → ARMED → LOCKED → ENDING → IDLE
```

Persist session state. Use a monotonic clock for duration. Use server-time offset where server validation matters.

Essential device functionality remains available according to Android capabilities and configured policy.

## 5. Custom launcher

Support:

- widgets
- countdowns
- exam countdown
- subject shortcuts
- progress
- themes
- backgrounds
- typography
- icon packs
- haptics
- responsive animation

Rule: animation must never slow a study action.

## 6. AI Knowledge Engine

Pipeline:

```
Upload
→ SHA-256 dedupe
→ page rendering
→ text/image/vector extraction
→ Gemini structured extraction
→ question assembly
→ option/diagram/solution linking
→ missing-solution engine
→ validation
→ taxonomy classification
→ per-user dedupe
→ publish/review
```

Every stage must be idempotent and resumable.

### Automatic organization

```
Subject → Chapter → Topic → Subtopic
```

AI selects IDs from a fixed JEE taxonomy rather than inventing uncontrolled official chapter names.

### Hybrid diagram extraction

Combine document-native extraction with Gemini spatial understanding. Apply padding and quality checks. Low-confidence crops enter a user review flow.

### Cross-page questions

Support continuation and merging across pages.

## 7. Every question gets a Solution object

Hard product rule:

> Every extracted question must have a Solution object.

This does **not** mean every solution is trusted.

Possible states:

- source
- AI-generated
- AI-verified
- unverified

Unverified solutions remain clearly marked and are excluded from graded tests by default.

### Missing-solution pipeline

```
Question
→ understand variables/concepts/equations
→ solve
→ verify
→ structured SolutionStep[]
→ store
```

Store:

- explanation
- LaTeX
- intermediate steps
- final answer
- source
- verification tier

## 8. Solution verification

**Tier 1 — Source Verified**

Source answer key exists and extracted solution agrees.

**Tier 2 — Computed**

Independent mathematical/numerical validation succeeds.

**Tier 3 — Independent Agreement**

Independent solution paths agree and applicable consistency checks pass. Agreement alone is not proof.

**Tier 4 — Unverified**

Insufficient evidence. Visible with warning and excluded from graded tests by default.

### Specialized validation

- Mathematics: symbolic/numerical validation where applicable
- Physics: numerical + dimensional/unit checks where applicable
- Physical Chemistry: numerical/unit/equation checks
- Organic/Inorganic Chemistry: stronger source-backed or review-oriented validation for uncertain claims

## 9. Provenance

Store on every extracted object:

- document_id
- page_no
- bounding box
- source text
- extraction confidence as an internal routing signal, not a calibrated truth probability
- solution_source
- verification_tier
- model_version
- prompt_version
- pipeline_version

Every object should be traceable back to its source.

## 10. AI cost and reliability

- Page-hash cache
- Fast/cheap model by default where appropriate
- Escalate difficult pages
- Per-user quotas
- Concurrency limits
- Retries/backoff
- Track calls, time, failures and estimated cost

Use a paid Gemini/API configuration whose current data-use terms are appropriate for private student uploads. Verify the exact deployed service terms before launch.

Never embed the Gemini API key in the Android APK.

## 11. Library

Automatically organize:

- Theory
- Concepts
- Formulae
- Examples
- Questions
- Solutions
- Diagrams
- Tables
- Revision

Uploaded commercial material remains private to its owner. Do not create a shared repository of copyrighted books or extracted question banks.

## 12. Fast Mode

```
Question → Think → Answer → Result → Explanation → Next
```

Filters:

- subtopic/topic/chapter/multiple chapters/full syllabus
- easy/medium/hard/JEE Main/JEE Advanced/mixed
- new/wrong/weak/unattempted/revision/mixed
- MCQ/numerical/integer/multi-correct/assertion-reason/conceptual

## 13. Tests

v1 supports:

- chapter tests
- mixed tests
- custom tests
- basic JEE Main scoring behavior
- basic JEE Advanced-style configurable sections
- locked mock mode inside supported Focus sessions

## 14. Usage analytics

Use Android UsageStats as the primary on-device source.

Classify applications as:

- productive
- distracting
- neutral

Unknown applications may be server-classified and cached. User overrides always win.

Upload category totals by default rather than unnecessary raw activity.

## 15. Focus Score and leaderboard

Transparent, versioned formula:

```
Focus Score =
completed focus minutes
+ practice volume × accuracy
+ consistency
− distraction during focus sessions
```

Support:

- daily
- weekly
- monthly
- global
- friends
- public/anonymous/private participation
- verified/unverified states

Never claim perfect anti-cheat integrity on user-controlled devices.

## 16. Android stack

- Kotlin
- Jetpack Compose
- Material 3
- Hilt
- Room
- DataStore
- WorkManager
- Coroutines/Flow
- UsageStatsManager
- DevicePolicyManager where supported
- Baseline Profiles
- AGSL where useful
- KaTeX with cached rendering

## 17. Backend stack

- Python 3.12
- FastAPI
- SQLAlchemy
- Alembic
- Pydantic v2
- PyMuPDF
- SymPy
- google-genai
- Argon2id
- JWT
- SQLite WAL initially

PostgreSQL can replace SQLite when scale justifies it. Redis and microservices are not required for v1.

## 18. Phone-hosted deployment

```
Android phone
→ Termux
→ FastAPI
→ SQLite WAL
→ Cloudflare Tunnel
→ external Android clients
```

Use environment variables for secrets/configuration.

Use a storage abstraction so local storage can later become R2/S3.

The Android client must use an API endpoint so backend hosting can later move to a VPS/cloud without rewriting the app.

## 19. Offline-first

- Room is local source of truth for cached/user-operational data.
- Study content and recent activity remain usable offline.
- Events queue locally and synchronize later.
- Client-generated UUIDs make retries idempotent.
- Server is authoritative for accounts and global scores.

## 20. Security and privacy

- Gemini credentials server-side only
- Argon2id
- short-lived access tokens
- rotating refresh tokens
- HTTPS
- rate limiting
- per-user quotas
- signed/validated uploads
- daily backups
- account deletion
- data deletion
- clear Terms and Privacy policy
- private user libraries

## 21. Age gate

Signup blocks users under 18 using a date-of-birth check and requires Terms and Privacy acceptance.

The age gate does not replace a full legal/privacy review of the final deployment, jurisdictions and data practices.

## 22. Development phases

### P0 — Risk validation

Prove Device Owner/Lock Task feasibility and the PDF pipeline on real hardware.

### P1 — Foundation

Android shell, FastAPI, authentication, DB, CI, Termux/tunnel.

### P2 — Focus Core

Launcher, schedules, Soft/Strong locking, recovery.

### P3 — Usage Analytics

Collector, classification, dashboard and synchronization.

### P4 — Knowledge Engine

Extraction, diagrams, taxonomy, provenance and organization.

### P5 — Solution Engine

Generated solutions, validators and verification.

### P6 — v1 Practice

Library, Fast Mode and basic Tests.

### P7+

Adaptive practice, SRS, Knowledge Graph, Hard Mode, Copilot and advanced anti-cheat.

## 23. Performance targets

Targets to measure on representative real hardware:

- Cold start < 1.2 s
- Lock engagement < 300 ms
- Battery < 3%/hour target during lock
- 60 FPS+ scrolling
- APK < 30 MB target
- Non-AI API p95 < 300 ms
- 50-page PDF < 6 min target
- Crash-free sessions > 99.5%

These are benchmarks, not guarantees.

## 24. Golden PDF benchmark

Maintain a regression set containing:

- clean PDFs
- scanned PDFs
- low-quality scans
- diagram-heavy PDFs
- tables/charts
- equation-heavy pages
- cross-page questions
- questions with solutions
- questions without solutions
- Physics
- Mathematics
- Physical Chemistry
- Organic Chemistry
- Inorganic Chemistry

Every significant pipeline change must be tested against the same benchmark.

## 25. GitHub workflow

This file is the source of truth:

```
docs/SPEC.md
```

For each phase, create a GitHub Issue with explicit acceptance criteria.

Implementation instruction:

```
Read docs/SPEC.md.

Implement ONLY Phase P{N}.

Follow the architecture decisions exactly.
Do not redesign unrelated systems.
Do not modify future phases.
Add tests for all new behavior.
Run relevant existing tests.
Provide a checklist against the phase exit criteria.

If an architectural/data-model change is required,
STOP and explain it before changing the model.
```

## 26. Final definition

FocusForge is a personal study operating environment, not merely an app blocker.

It controls the study environment, understands legitimate student material, automatically organizes it, creates or validates solutions, turns material into practice, records outcomes and feeds those outcomes back into future study.

**v1 succeeds only when the core loop is trustworthy:**

```
Reliable supported focus state
+
Reliable real-PDF → structured-learning conversion
```

**Immediate action: P1 Foundation.** P0-A hardware validation remains deferred until the completed APK is available; P0-B benchmark validation remains a release gate.
