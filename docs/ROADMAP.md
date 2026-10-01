# FocusForge — Master Development Roadmap

> **Purpose:** Persistent project context and execution map.
>
> This file records what FocusForge is supposed to become, what is being built now, what must wait, and the acceptance criteria for every phase.
>
> **Source of truth:** `docs/SPEC.md` defines the product/architecture specification. This file defines the execution order and phase boundaries.

---

# 0. Non-negotiable development rules

1. Work on **one phase at a time**.
2. Do not silently implement future-phase features.
3. Do not redesign working architecture without documenting the reason.
4. Every phase must have tests.
5. Every phase must have measurable exit criteria.
6. Real Android hardware testing is required for Android-specific behavior.
7. AI/document behavior must be tested against a persistent golden dataset.
8. Never treat an AI-generated answer as authoritative without the verification state required by the specification.
9. Keep the product scope aligned with `docs/SPEC.md`.
10. If a new requirement conflicts with this roadmap, update the roadmap/spec explicitly before changing implementation.
11. When continuing work in a later conversation, read this file and `docs/SPEC.md` before making implementation decisions.
12. **P0 normally blocks broader development; explicit project-control decisions may defer hardware validation while keeping P0 open and release-blocking.**

---

# 1. Product North Star

## FocusForge

**Tagline:** “Your phone, rebuilt for studying.”

FocusForge is an Android-first productivity and JEE learning platform.

The long-term system combines:

- Focus OS
- Custom study launcher
- Scheduled lockdown
- Usage intelligence
- AI document understanding
- Automatic study-material organization
- Solved question bank
- Fast Mode
- Tests
- Adaptive learning
- Mistake intelligence
- Spaced repetition
- Knowledge graph
- AI Study Copilot
- Focus Score
- Competition/leaderboards

Core loop:

```
Student material
    ↓
AI understands it
    ↓
AI organizes it
    ↓
Questions become structured
    ↓
Every question receives a Solution object
    ↓
Solutions are verified/flagged
    ↓
Student practices
    ↓
Results become learning data
    ↓
Weaknesses are detected
    ↓
Future practice adapts
    ↓
Focus environment controls distractions
    ↓
Progress is measured
    ↺
```

---

# 2. v1 Launch Scope

The first public product should contain only:

- Focus lockdown
- Scheduling
- FocusForge launcher
- Usage statistics
- Productive/distraction classification
- PDF/document ingestion
- Automatic JEE organization
- Question extraction
- Diagram extraction
- Table extraction
- Equation/LaTeX extraction
- Source-solution extraction
- AI-generated missing solutions
- Solution verification
- Provenance
- Library
- Fast Mode
- Basic tests
- Simple leaderboard

Do NOT block v1 on:

- advanced adaptive learning
- full Hard Mode
- Knowledge Graph
- AI Study Copilot
- advanced anti-cheat
- sophisticated SRS

These are later phases.

---

# 3. Current Status

## Current phase: P1 — Foundation

**Status:** ACTIVE

### P0 validation debt

P0-A physical-device validation is explicitly deferred until a completed APK is available for installation/testing on the target OPPO K13 Turbo Pro 5G. P0-A is **not passed**.

P0-B benchmark evidence is also still required before release acceptance. P0 remains open as a release gate; moving to P1 is a documented project-control exception so implementation can proceed to a testable APK.

**GitHub tracking issue:** #1

P0 has two gates:

- P0-A — Device Control
- P0-B — Knowledge Engine

No broader feature development should begin until both gates are proven.

---

# 4. P0 — Risk Validation

## Goal

Kill the two biggest architectural/product risks before spending significant development time.

---

## P0-A — Device Control

### Objective

Prove what level of Android lockdown FocusForge can actually provide on the target physical device.

### Build/test

- Device Owner provisioning
- Lock Task mode
- FocusForge launcher proof
- Scheduled focus session
- Allowed application behavior
- Restricted application behavior
- Session state persistence
- Process/app restart recovery
- Phone reboot recovery
- Safe recovery path
- Emergency/recovery procedure
- Android version limitations
- OEM-specific limitations

### Test matrix

Record:

- device model
- Android version
- OEM
- provisioning method
- device-management state
- lock behavior
- launcher behavior
- reboot behavior
- recovery behavior
- observed limitations

### Exit criteria

P0-A passes only when:

- Device Owner feasibility is proven or definitively rejected for the target device/setup.
- Lock Task behavior is demonstrated.
- Focus session state survives the tested failure/reboot scenarios.
- Recovery path is documented and tested.
- Unsupported guarantees are documented.
- Architecture decision is recorded.

---

## P0-B — Knowledge Engine

### Objective

Prove that real study material can reliably become structured learning content.

### Benchmark PDFs

At minimum:

1. Clean text PDF
2. Scanned PDF
3. Diagram-heavy PDF
4. Equation-heavy PDF
5. Cross-page question material
6. Questions with source solutions
7. Questions without source solutions

### Pipeline

```
PDF
 ↓
SHA-256
 ↓
quota check
 ↓
page rendering
 ↓
text layer extraction
 ↓
embedded image/vector extraction
 ↓
Gemini structured extraction
 ↓
question assembly
 ↓
option linking
 ↓
diagram linking
 ↓
solution linking
 ↓
missing-solution generation
 ↓
verification
 ↓
taxonomy classification
 ↓
deduplication
 ↓
publish/review
```

### Measure

- question extraction precision
- question extraction recall
- question assembly accuracy
- diagram association
- table extraction
- equation/LaTeX integrity
- source solution extraction
- AI-generated solution correctness
- verification tier distribution
- processing time
- API cost
- failure/retry rate

### Exit criteria

P0-B passes only when:

- benchmark results are recorded
- failure cases are visible
- missing-solution flow works
- verification states work
- unverified questions are prevented from silently entering graded tests
- processing is resumable/idempotent
- measured quality is acceptable for v1

---

# 5. P1 — Foundation

## Goal

Create the stable application/backend foundation.

### Android

- Kotlin
- Jetpack Compose
- Material 3
- Hilt
- Room
- DataStore
- WorkManager
- Coroutines/Flow
- baseline project structure
- design system
- navigation
- error handling
- logging
- build configuration

### Backend

- Python 3.12
- FastAPI
- SQLAlchemy
- Alembic
- Pydantic v2
- SQLite WAL
- authentication
- API versioning
- environment configuration
- structured logging

### Infrastructure

- Termux deployment
- Cloudflare Tunnel
- development environment
- test environment
- CI
- database migrations
- health checks

### Authentication

- 18+ DOB gate
- Terms acceptance
- Privacy acceptance
- account creation
- login
- session handling
- account deletion

### Exit criteria

- Android app builds.
- Backend runs reproducibly.
- Database migrations work.
- CI passes.
- Real Android device can authenticate through the configured tunnel.
- Secrets are not embedded in the APK.

---

# 6. P2 — Focus Core

## Goal

Build the usable everyday focus environment.

### Features

- Focus profiles
- Schedules
- Soft enforcement
- Strong enforcement where supported
- Focus launcher
- allowlist configuration
- session state machine
- countdown
- session start/end
- recovery
- foreground/session monitoring
- watchdog
- OEM setup guidance

### Launcher

- home
- study shortcuts
- allowed-apps surface
- timer
- subject widgets
- theme
- dark mode
- basic animation/haptics

### Exit criteria

- User can create a focus schedule.
- Session starts and ends correctly.
- Launcher activates correctly.
- Allowed apps behave correctly.
- Session state persists through tested app/process failures.
- Tested device limitations are documented.

---

# 7. P3 — Usage Analytics

## Goal

Create trustworthy screen-time analytics.

### Features

- UsageStats collection
- daily aggregation
- app classification
- productive/distraction/neutral categories
- user overrides
- local storage
- offline queue
- synchronization
- dashboard
- daily/weekly summaries

### Privacy

Default to aggregated category totals rather than unnecessary raw activity uploads.

### Exit criteria

- Usage data is collected reliably.
- Category totals match device-level measurements within an agreed tolerance on tested devices.
- Offline events synchronize correctly.
- User overrides persist.
- No unnecessary raw usage data is uploaded.

---

# 8. P4 — Knowledge Engine

## Goal

Turn legitimate user-provided study material into structured learning content.

### Features

- upload
- SHA-256 dedupe
- document metadata
- page rendering
- text extraction
- image/vector extraction
- Gemini extraction
- strict schemas
- equations
- LaTeX
- diagrams
- tables
- charts
- question extraction
- option extraction
- cross-page assembly
- source solution extraction
- taxonomy classification
- provenance
- job progress UI
- resumable jobs
- per-user deduplication
- review queue
- completion summary

### Completion summary

Example:

```
12 chapters
87 topics
1,284 questions
1,102 source solutions
182 AI-generated solutions
347 diagrams
92 tables
```

### Exit criteria

Golden-set benchmark meets the quality thresholds established during P0.

---

# 9. P5 — Solution Engine

## Goal

Guarantee that every question has a traceable solution object while maintaining trust boundaries.

### Features

- source solution detection
- source solution extraction
- AI solution generation
- structured SolutionStep[]
- LaTeX steps
- final answer
- verification
- verification tiers
- subject-specific validators
- report-wrong-answer button
- review queue
- model/prompt versioning

### Verification

Tier 1:
Source Verified

Tier 2:
Computed

Tier 3:
Independent Agreement + applicable consistency checks

Tier 4:
Unverified

### Hard rule

Unverified solutions:

- remain clearly marked
- can be reviewed
- cannot silently become authoritative
- are excluded from graded tests by default

### Exit criteria

- Solution generation works on benchmark questions.
- Verification states are persisted.
- Incorrect/unverified solutions are correctly excluded from graded tests.
- Known-answer benchmark results are measured.
- Reported errors can be tracked and downgraded.

---

# 10. P6 — v1 Practice

## Goal

Turn the structured library into an enjoyable usable study system.

### Library

- Subject
- Chapter
- Topic
- Subtopic
- Theory
- Formulae
- Questions
- Solutions
- Diagrams
- Tables
- Revision

### Fast Mode

```
Question
→ Think
→ Answer
→ Result
→ Explanation
→ Next
```

### Filters

- scope
- difficulty
- question type
- new
- wrong
- weak
- unattempted
- revision

### Tests

- chapter
- mixed
- custom
- basic JEE Main scoring
- basic JEE Advanced-style sections
- locked mock where supported

### Exit criteria

- User can practice offline.
- Questions render correctly.
- Diagrams display with questions.
- LaTeX renders correctly.
- Results are persisted.
- Tests calculate configured scoring correctly.
- Full basic test works from start to result.

---

# 11. P7 — Adaptive Intelligence

## Goal

Personalize practice.

### Features

- weakness map
- adaptive selection
- mistake intelligence
- spaced repetition
- recommendation engine
- algorithm versioning

Initial weakness model:

```
0.5 × (1 - accuracy)
+
0.3 × recency_decay
+
0.2 × recent_mistake_rate
```

Maintain exploration so the system does not become a narrow repetition loop.

### Mistake categories

- conceptual
- calculation
- formula
- unit
- misread
- careless
- guess

AI may suggest; student confirms.

### Exit criteria

Simulation and real-user tests show that the feed actually prioritizes weak areas without eliminating useful exploration.

---

# 12. P8 — Knowledge Graph

## Goal

Move from simple taxonomy to actual concept relationships.

### Model

```
Subject
→ Chapter
→ Topic
→ Subtopic

Concept ↔ Concept

Theory → Concept
Question → Concept
Mistake → Concept
Formula → Concept
```

### Capabilities

- concept weakness detection
- prerequisite relationships
- related-question retrieval
- explanation retrieval
- targeted revision

### Exit criteria

The system can reliably associate benchmark questions/theory/mistakes with concepts and use those relationships for retrieval.

---

# 13. P9 — AI Study Copilot

## Goal

Create an AI tutor grounded in the student's own learning context.

### Example capabilities

“Why did I get this wrong?”

Uses:

- question
- student answer
- correct answer
- solution
- mistake history
- relevant theory

“Give me similar questions.”

Uses:

- concept
- difficulty
- history
- mistakes
- source library

“Explain this concept using my notes.”

Uses the student's own indexed material.

### Requirements

- grounded retrieval
- source references
- no pretending unsupported claims are from the user's material
- clear AI-generated labeling where appropriate

### Exit criteria

Grounded responses are consistently tied to the student's indexed content and known context.

---

# 14. P10 — Hard Mode

## Goal

Ship the strongest supported device-management experience.

### Features

- Device Owner setup wizard
- Lock Task
- managed configuration
- boot handling
- session auto-resume
- supported device restrictions
- OEM-specific guidance
- safe recovery

### Safety

Every Hard session has:

- explicit end time
- recovery procedure
- tested recovery path
- no intentional device bricking behavior

### Exit criteria

Hard Mode works reliably on the documented supported-device matrix.

---

# 15. P11 — Competition & Anti-Cheat

## Goal

Make competition meaningful without pretending client devices are perfectly trustworthy.

### Features

- Focus Score
- daily/weekly/monthly boards
- friends
- global board
- verified/unverified status
- signed reports
- server sanity checks
- replay detection
- impossible-event detection
- privacy controls

### Exit criteria

Known fake/replayed reports are rejected in automated tests and verified sessions receive the appropriate state.

---

# 16. P12 — Performance & Polish

## Goal

Turn the functional product into a premium Android experience.

### Performance

Target:

- cold start <1.2s
- lock engagement <300ms
- 60 FPS+ scrolling
- battery <3%/hour target during lock
- APK <30MB target
- non-AI API p95 <300ms
- 50-page PDF <6min target
- crash-free >99.5%

### UI/UX

- AMOLED dark-first
- subject accents
- spring transitions
- shared-element transitions
- haptics
- skeleton loading
- gesture support
- accessibility
- reduced-motion support
- no animation that slows study

### Android optimization

- Baseline Profiles
- memory profiling
- battery profiling
- frame-time profiling
- startup profiling
- database query profiling

---

# 17. P13 — Beta

## Goal

Validate the real product with a small group before broad release.

Target:

20–50 adult JEE students.

Measure:

- crash rate
- ANR rate
- retention
- focus-session completion
- PDF extraction quality
- solution error rate
- Fast Mode usage
- test completion
- user feedback
- API cost
- server load

### Exit criteria

Review:

- Day-7 retention
- crash-free sessions
- critical bug count
- extraction quality
- AI solution quality
- cost per active user
- hardware compatibility

Then make a go/no-go decision.

---

# 18. P14 — Scale Infrastructure

Only after usage justifies it.

### Migration path

```
Phone + Termux
→ VPS
→ PostgreSQL
→ object storage
→ background workers
→ scalable infrastructure
```

Do not prematurely introduce distributed infrastructure.

---

# 19. Future Feature Parking Lot

Ideas that may be considered after the core product is stable:

- richer study plans
- advanced exam simulation
- more sophisticated recommendation algorithms
- richer social features
- study groups
- collaborative revision
- deeper concept visualization
- advanced AI generation
- additional competitive modes
- cloud sync enhancements
- additional educational curricula

A parked feature is not automatically part of the current phase.

---

# 20. Definition of Done

A phase is **not done** because code exists.

A phase is done only when:

1. Implementation exists.
2. Tests exist.
3. Relevant real-device tests are complete.
4. Failure cases are tested.
5. Documentation is updated.
6. Acceptance criteria are checked.
7. No known P0/P1 blocker remains.
8. The phase is reviewed.
9. The next phase is explicitly started.

---

# 21. Context Recovery Protocol

When continuing FocusForge work after a conversation break:

1. Read `docs/SPEC.md`.
2. Read `docs/ROADMAP.md`.
3. Check the active GitHub Issue.
4. Check recent commits/PRs.
5. Determine the current phase and unfinished checklist.
6. Do not infer that a phase is complete from conversation history alone.
7. Continue only from the repository's recorded state.

If the repository and conversation disagree:

> **Repository state + explicit latest approved spec takes precedence.**

---

# 22. Current Immediate Task

**P1 only.**

P0-A and P0-B remain open validation gates. P0-A hardware proof will be performed against the completed APK on the target device. P0-B will be run against the persistent golden PDF benchmark before release acceptance.

### Current work

Build the stable Android/backend foundation required to produce a real APK and exercise the deferred P0 validation safely.

Do not silently pull P2+ features into P1.

---

# 23. Master Architecture

```
                         FOCUSFORGE
                              |
              +---------------+---------------+
              |               |               |
          FOCUS OS      KNOWLEDGE ENGINE   PRACTICE
              |               |               |
          Lockdown          PDFs          Fast Mode
          Launcher        Extraction        Tests
          Scheduler       Solutions        Results
          Profiles        Diagrams         Adaptive
          Usage           Organization      later
              |               |               |
              +---------------+---------------+
                              |
                         INTELLIGENCE
                              |
                 Analytics / Recommendations
                              |
                           BACKEND
                              |
                    FastAPI + SQLite + Gemini
                              |
                       Future Cloud Scale
```

---

# 24. Master Principle

FocusForge should always optimize for:

```
RELIABILITY
>
CORRECTNESS
>
SECURITY
>
USER TRUST
>
PERFORMANCE
>
FEATURE COUNT
```

A flashy feature that compromises the reliability of the core study loop does not belong in the current release.

**The current goal remains P0.**


## Phase Execution Protocol

Each phase is implemented as a single batched implementation pass before CI debugging begins.

1. Read the phase scope and all relevant existing code.
2. Implement the complete phase scope in one coherent batch, without stopping after individual sub-items.
3. Commit the phase implementation to `main`.
4. Wait for all triggered CI workflows/runs to become available and finish.
5. Inspect every available job, step, failure log, warning, and build artifact.
6. Fix all discovered issues in a dedicated debugging pass.
7. Re-run/verify CI until the phase is technically clean or an external/manual validation dependency remains.
8. Only after the phase gate is satisfied, begin the next phase.

CI status must never be inferred from an empty status response. A phase is not called green until actual workflow/job evidence is available. Hardware/manual gates remain explicit and cannot be simulated by CI.
