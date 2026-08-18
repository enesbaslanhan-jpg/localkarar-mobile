# LocalKarar Mobile Data Model Map

This document maps the backend PostgreSQL models (via Prisma) to the data structures that the mobile application will need to consume and manage.

## 1. User & Access
- `User`: The primary account holder.
  - Fields: `id`, `email`, `role`, `name`.
- `BusinessWorkspace`: Represents the business context for the user.
- `BusinessMember`: Links `User` to `BusinessWorkspace` with roles (`owner`, `viewer`, etc.).

## 2. Learning & Content
- `Course`: High-level learning paths.
- `Lesson`: Sub-unit of courses.
- `KnowledgeObject`: Underlying content fragments (used for flashcards, quizzes, text).
- `Enrollment`: Links a `User` to a `Course`.
- `LessonProgress`: Tracks how much of a `Lesson` a `User` has completed.

## 3. Decision Engine
- `DecisionCheck`: The published template for a decision flow (e.g. `DC-PROFIT-001`).
- `DecisionCheckVersion`: Versioned logic for the decision check.
- `DecisionCheckSession`: Tracks an active/completed session for a user.
- `DecisionCheckAnswer`: User's answers for a particular session.
- `DecisionCheckResult`: The final calculation output and findings of a completed session.
- `DecisionJournalEntry`: Saved context/results mapped to the user's business profile.

## 4. Model Lab (Calculations)
- `FinancialModel`: The published financial model (e.g., `CASHFLOW-001`).
- `FinancialModelRun`: Stores the execution trace, inputs, and outputs of a calculation.
- `ModelAssumption`: User overrides/inputs supplied to the run.

## 5. AI Mentor
- `Conversation`: Top-level thread container with a `title` and `contextSnapshot`.
- `ConversationMessage`: Individual user or assistant messages.
- `MentorSession` (Deprecated): Legacy AI system model. Mobile should use `Conversation` instead.
- `ConversationSummary`: Periodically generated background summaries of the chat thread.

## 6. Feed & Updates
- `NewsArticle`: Content displayed in the news feed section.
- `FeedInteraction`: User actions (view, dismiss) on dynamic feed items.

## Mobile Responsibility
The mobile app will implement Data Transfer Objects (DTOs) and Domain Models mirroring these structures. However, it will not replicate database relations locally—all structured requests and joins are executed via the API.
