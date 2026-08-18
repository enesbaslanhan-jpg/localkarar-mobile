# LocalKarar Mobile API Inventory

This document maps the Fastify REST endpoints in the existing backend (`LocalAkademi_fixed`) to the mobile application's required features.

## 1. Authentication (`/auth`)
- `POST /auth/login`: Authenticates the user and returns a JWT token.
- `POST /auth/register`: Creates a new user and returns a JWT token.
- `GET /auth/me`: Validates token and returns current user context.
- `PUT /auth/password`: Changes user password.
- `PUT /auth/email`: Changes user email.
- `DELETE /auth/account`: Deletes the user account.

## 2. Workspaces & Business Profiles (`/workspaces`, `/business`)
- The mobile app will need to retrieve active workspace context, generally via `/workspaces` endpoints to access business profiles, contacts, documents, tracker, and settings.

## 3. Courses & Learning Progress (`/courses`)
- `GET /courses`: List courses (with pagination, filters, search).
- `GET /courses/:id`: Get detailed course information, including lessons, modules, and user's enrollment status.
- `GET /courses/:courseId/learn`: Fetches next/incomplete lesson for learning.
- `GET /courses/:courseId/lessons/:lessonId`: Get individual lesson data, including knowledge objects, quizzes, videos.
- `GET /enrollments`: Endpoints to handle enrollments.
- `GET /api/v1/learning-progress`: General learning progress.

## 4. Decision Tools & Checks (`/api/v1/decision-checks`)
- `GET /api/v1/decision-checks/sessions/me`: Get user's recent sessions.
- `GET /api/v1/decision-checks`: Get published decision tools.
- `GET /api/v1/decision-checks/:code`: Fetch definition of a decision check.
- `POST /api/v1/decision-checks/:code/start`: Start a new session.
- `GET /api/v1/decision-check-sessions/:id`: Get session state.
- `PATCH /api/v1/decision-check-sessions/:id/answers`: Update answers for a session.
- `POST /api/v1/decision-check-sessions/:id/complete`: Complete session and execute rules.
- `GET /api/v1/decision-check-sessions/:id/result`: Retrieve calculation/result output.

## 5. Financial Models (Model Lab) (`/financial-models`, `/workspaces/:workspaceId/financial-model-runs`)
- `GET /financial-models`: List available calculation models.
- `GET /financial-models/:code`: Fetch model definition (inputs, dependencies).
- `POST /financial-models/:code/validate`: Validate inputs.
- `POST /workspaces/:workspaceId/financial-models/:code/runs`: Execute a financial calculation run (returns checks, outputs).
- `GET /workspaces/:workspaceId/financial-model-runs`: Get run history for the workspace.
- `GET /workspaces/:workspaceId/financial-model-runs/:runId`: Retrieve specific run results.

## 6. AI Mentor (`/mentor/conversations`)
- `GET /mentor/conversations`: Get user's conversation list.
- `POST /mentor/conversations`: Create a new conversation.
- `GET /mentor/conversations/:id`: Fetch messages in a conversation.
- `POST /mentor/conversations/:id/messages`: Send a message to the mentor (standard).
- `POST /mentor/conversations/:id/messages/stream`: Send a message to the mentor (Server-Sent Events streaming).
- `PATCH /mentor/conversations/:id/archive`: Archive conversation.
- `DELETE /mentor/conversations/:id`: Delete conversation.

## 7. News & Feeds (`/api/news`, `/api/v1/feed`)
- `GET /api/news`: Fetch global news feed based on categories.
- `GET /api/v1/feed`: Fetch personalized feed items for user dashboard.
- `POST /api/v1/feed/items/view`, `/dismiss`, `/action`: Log interactions on feed items.
