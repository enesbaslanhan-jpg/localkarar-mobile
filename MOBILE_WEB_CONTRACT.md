# LocalKarar Mobile & Web Contract

This document acts as the foundational agreement for the development of **LocalKarar Mobile**.

## Primary Rule
LocalKarar Mobile is the mobile client for the existing LocalKarar Web product. It is **not** a separate product. The mobile application **must** use the same backend infrastructure, business logic, databases, and APIs as the web application.

## Core Principles
1. **Single Source of Truth**: The `LocalAkademi_fixed` workspace (the current backend for LocalKarar) contains the definitive source of truth for all business logic, authentication, and data structures.
2. **No Duplication of Logic**: Do not invent separate mobile business logic. All calculations (e.g., in Financial Models, Decision Checks) must be performed by the backend engine, not the mobile client.
3. **Shared Backend Resources**: The mobile app will use the exact same:
    - User accounts and authentication (`/auth/*` endpoints)
    - Database (PostgreSQL via Prisma on backend)
    - Business data and Workspaces (`/business`, `/workspaces`)
    - Courses and learning progress (`/courses`, `/enrollments`, `/lessons`)
    - Decision tools (`/api/v1/decision-checks`)
    - Calculation logic (Financial Models, `/financial-models`)
    - AI Mentor backend (`/mentor/conversations`)
    - Saved results and user progress

## Target Platforms
- Android (Primary focus of Phase 1, `compileSdk 35`)
- iOS (To be fully integrated when Compose Multiplatform constraints with iOS on the current SDK are resolved, target remains KMP)

## API Communication
The mobile application will consume the Fastify-based REST APIs exposed by the existing Node.js server. The application will manage state efficiently and leverage backend-generated summaries rather than recreating them locally.
