# Orion - API Specification

## 1. Overview

This document defines the REST API for Orion.

The API provides access to authentication, project management, task management, availability, and scheduling functionality.

The API uses:

- RESTful HTTP endpoints.
- JSON request and response bodies.
- UUIDs for resource identifiers.
- ISO 8601 format for timestamps.
- JWT-based authentication for protected endpoints.

The API is implemented by the Spring Boot backend and consumed by the React frontend.

## 2. Base URL

The API is exposed under:

```text
/api
```

Example:

```text
GET /api/projects
```

## 3. Request and Response Format

### Content Type

Requests and responses containing data use:

```text
Content-Type: application/json
```

### Identifiers

Resources are identified using UUIDs.

Example:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Timestamps

Timestamps use ISO 8601 format.

Example:

```text
2026-09-10T14:30:00Z
```

### Validation

Invalid request data must be rejected with an appropriate `4xx` response.

Validation includes:

- Required fields.
- Valid UUIDs.
- Valid date and time values.
- Positive durations.
- Priority values between 1 and 5.
- Valid resource statuses.
- Valid task dependencies.

# 4. Authentication

Authentication endpoints allow users to create accounts and obtain authentication credentials.

## 4.1 Register

```http
POST /api/auth/register
```

Creates a new user account.

### Request

```json
{
  "email": "user@example.com",
  "password": "secure-password"
}
```

### Response

**201 Created**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com"
}
```

### Errors

- `400 Bad Request` — invalid registration data.
- `409 Conflict` — email address already registered.

Passwords must never be returned by the API.

## 4.2 Login

```http
POST /api/auth/login
```

Authenticates a user.

### Request

```json
{
  "email": "user@example.com",
  "password": "secure-password"
}
```

### Response

**200 OK**

```json
{
  "token": "<JWT>"
}
```

### Errors

- `400 Bad Request` — invalid request.
- `401 Unauthorized` — invalid credentials.

# 5. Authentication Requirements

Protected endpoints require a valid JWT.

The token is supplied using:

```http
Authorization: Bearer <JWT>
```

The authenticated user's identity is obtained from the token rather than from request parameters.

The API must not allow users to access or modify resources belonging to another user.

# 6. Projects

Projects represent collections of related tasks.

## 6.1 List Projects

```http
GET /api/projects
```

Returns projects belonging to the authenticated user.

### Response

**200 OK**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Project",
    "description": "Complete project",
    "deadline": "2026-10-20T23:59:00Z",
    "priority": 5,
    "status": "ACTIVE"
  }
]
```

## 6.2 Create Project

```http
POST /api/projects
```

Creates a project for the authenticated user.

### Request

```json
{
  "name": "Project",
  "description": "Complete project",
  "deadline": "2026-10-20T23:59:00Z",
  "priority": 5
}
```

### Response

**201 Created**

Returns the created project.

## 6.3 Get Project

```http
GET /api/projects/{id}
```

Returns a specific project belonging to the authenticated user.

### Response

**200 OK**

Returns the project.

### Errors

- `401 Unauthorized` — authentication required.
- `403 Forbidden` — project belongs to another user.
- `404 Not Found` — project does not exist.

## 6.4 Update Project

```http
PUT /api/projects/{id}
```

Updates an existing project.

### Request

```json
{
  "name": "Project",
  "description": "Updated description",
  "deadline": "2026-10-20T23:59:00Z",
  "priority": 5,
  "status": "ACTIVE"
}
```

### Response

**200 OK**

Returns the updated project.

## 6.5 Delete Project

```http
DELETE /api/projects/{id}
```

Deletes a project belonging to the authenticated user.

### Response

**204 No Content**

### Errors

- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`

# 7. Tasks

Tasks represent individual units of work that can be scheduled.

## 7.1 List Tasks

```http
GET /api/tasks
```

Returns tasks belonging to the authenticated user.

Optional filtering may be supported using query parameters.

Example:

```http
GET /api/tasks?status=TODO
```

### Response

**200 OK**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "projectId": "650e8400-e29b-41d4-a716-446655440000",
    "title": "Task",
    "description": "Define task",
    "estimatedMinutes": 120,
    "deadline": "2026-09-20T23:59:00Z",
    "priority": 5,
    "status": "TODO"
  }
]
```

## 7.2 Create Task

```http
POST /api/tasks
```

Creates a task within a project belonging to the authenticated user.

### Request

```json
{
  "projectId": "650e8400-e29b-41d4-a716-446655440000",
  "title": "Task",
  "description": "Define task",
  "estimatedMinutes": 120,
  "deadline": "2026-09-20T23:59:00Z",
  "priority": 5
}
```

### Response

**201 Created**

Returns the created task.

### Errors

- `400 Bad Request` — invalid task data.
- `403 Forbidden` — project belongs to another user.
- `404 Not Found` — project does not exist.

## 7.3 Get Task

```http
GET /api/tasks/{id}
```

Returns a specific task belonging to the authenticated user.

### Response

**200 OK**

Returns the task.

## 7.4 Update Task

```http
PUT /api/tasks/{id}
```

Updates an existing task.

### Request

```json
{
  "title": "Task",
  "description": "Updated description",
  "estimatedMinutes": 150,
  "deadline": "2026-09-20T23:59:00Z",
  "priority": 5,
  "status": "IN_PROGRESS"
}
```

### Response

**200 OK**

Returns the updated task.

Changes affecting scheduling constraints may require the current schedule to be recalculated.

## 7.5 Delete Task

```http
DELETE /api/tasks/{id}
```

Deletes a task belonging to the authenticated user.

### Response

**204 No Content**

Deleting a task must also remove or invalidate its associated dependencies and completion records according to the database deletion rules.

## 7.6 Complete Task

```http
POST /api/tasks/{id}/complete
```

Marks a task as completed.

### Request

```json
{
  "actualMinutes": 135
}
```

### Response

**200 OK**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "completedAt": "2026-09-10T16:30:00Z"
}
```

Completing a task must prevent it from being scheduled again.

The completion record should preserve the actual work performed.

# 8. Task Dependencies

Dependencies define prerequisite relationships between tasks.

## 8.1 Add Dependency

```http
POST /api/tasks/{id}/dependencies
```

Adds a prerequisite to a task.

### Request

```json
{
  "dependsOnTaskId": "650e8400-e29b-41d4-a716-446655440000"
}
```

This means:

```text
Task {id}
    depends on
Task {dependsOnTaskId}
```

### Response

**201 Created**

```json
{
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "dependsOnTaskId": "650e8400-e29b-41d4-a716-446655440000"
}
```

### Errors

- `400 Bad Request` — invalid dependency.
- `403 Forbidden` — task belongs to another user.
- `404 Not Found` — task does not exist.
- `409 Conflict` — dependency would create a cycle or already exists.

## 8.2 List Dependencies

```http
GET /api/tasks/{id}/dependencies
```

Returns the dependencies of a task.

### Response

**200 OK**

```json
[
  {
    "taskId": "550e8400-e29b-41d4-a716-446655440000",
    "dependsOnTaskId": "650e8400-e29b-41d4-a716-446655440000"
  }
]
```

## 8.3 Remove Dependency

```http
DELETE /api/tasks/{id}/dependencies/{dependencyId}
```

Removes a dependency.

### Response

**204 No Content**

# 9. Availability

Availability defines when Orion may schedule work.

## 9.1 List Availability

```http
GET /api/availability
```

Returns the authenticated user's availability periods.

### Response

**200 OK**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "dayOfWeek": "MONDAY",
    "startTime": "09:00",
    "endTime": "17:00"
  }
]
```

## 9.2 Create Availability

```http
POST /api/availability
```

Creates a recurring availability period.

### Request

```json
{
  "dayOfWeek": "MONDAY",
  "startTime": "09:00",
  "endTime": "17:00"
}
```

### Response

**201 Created**

Returns the created availability period.

### Validation

`startTime` must precede `endTime`.

## 9.3 Update Availability

```http
PUT /api/availability/{id}
```

Updates an availability period.

### Request

```json
{
  "dayOfWeek": "MONDAY",
  "startTime": "10:00",
  "endTime": "18:00"
}
```

### Response

**200 OK**

Returns the updated availability period.

Changes to availability may require schedule recalculation.

## 9.4 Delete Availability

```http
DELETE /api/availability/{id}
```

Deletes an availability period.

### Response

**204 No Content**

Removing availability may make the current schedule invalid or infeasible. Orion should identify this during schedule recalculation.

# 10. Schedules

Schedules represent generated plans for allocating tasks to available time.

## 10.1 Generate Schedule

```http
POST /api/schedules/generate
```

Generates a new schedule using the authenticated user's current tasks, dependencies, availability, and progress.

### Request

No request body is required initially.

### Response

**201 Created**

```json
{
  "scheduleId": "750e8400-e29b-41d4-a716-446655440000",
  "periodStart": "2026-09-21T00:00:00",
  "periodEnd" : "2026-09-28T00:00:00",
  "generatedAt" : "2026-09-21T09:00:00",
  "status": "ACTIVE",
  "blocks": [
    {
      "id": "850e8400-e29b-41d4-a716-446655440000",
      "taskId": "550e8400-e29b-41d4-a716-446655440000",
      "startTime": "2026-09-11T09:00:00Z",
      "endTime": "2026-09-11T10:30:00Z"
    }
  ],
  "unscheduledTasks": [],
  "riskInformation": [],
  "explanation": "Schedule generated successfully."
}
```

The generated schedule must satisfy all hard scheduling constraints.

If a feasible schedule cannot be generated, the response must identify the infeasible tasks and relevant constraints rather than silently violating them.

# 11. Current Schedule

## 11.1 Get Current Schedule

```http
GET /api/schedules/current
```

Returns the currently active schedule.

### Response

**200 OK**

```json
{
  "scheduleId": "750e8400-e29b-41d4-a716-446655440000",
  "status": "ACTIVE",
  "generatedAt": "2026-09-10T17:00:00Z",
  "periodStart": "2026-09-10T17:00:00Z",
  "periodEnd": "2026-09-20T23:59:00Z",
  "blocks": [
    {
      "id": "850e8400-e29b-41d4-a716-446655440000",
      "taskId": "550e8400-e29b-41d4-a716-446655440000",
      "startTime": "2026-09-11T09:00:00Z",
      "endTime": "2026-09-11T10:30:00Z"
    }
  ]
}
```

If no active schedule exists:

**404 Not Found**

# 12. Recalculate Schedule

## 12.1 Recalculate

```http
POST /api/schedules/recalculate
```

Recalculates the schedule using the user's current state.

Recalculation may be triggered after:

- Task progress changes.
- A task is completed.
- A task is added or modified.
- Availability changes.
- Deadlines change.
- Dependencies change.
- Significant progress deviations occur.

### Response

**200 OK**

```json
{
  "scheduleId": "750e8400-e29b-41d4-a716-446655440000",
  "status": "ACTIVE",
  "changes": {
    "addedBlocks": [],
    "removedBlocks": [],
    "movedBlocks": []
  },
  "riskInformation": [],
  "explanation": "Schedule recalculated after task completion."
}
```

Recalculation should avoid unnecessary changes to existing schedule blocks where possible.

# 13. Scheduling Response States

Scheduling operations may return the following states:

| State | Meaning |
| --- | --- |
| `ON_TRACK` | Current progress is consistent with the schedule. |
| `AT_RISK` | Progress indicates an increased risk of missing a deadline. |
| `INFEASIBLE` | Available time is insufficient to satisfy required constraints. |
| `COMPLETED` | Required work has been completed. |

These states describe the current scheduling health rather than HTTP status codes.

# 14. HTTP Status Codes

The API uses standard HTTP status codes.

| Status | Meaning |
| --- | --- |
| `200 OK` | Request completed successfully |
| `201 Created` | Resource successfully created |
| `204 No Content` | Request succeeded without a response body |
| `400 Bad Request` | Request data is invalid |
| `401 Unauthorized` | Authentication is missing or invalid |
| `403 Forbidden` | User is not permitted to access the resource |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | Request conflicts with the current domain state |
| `422 Unprocessable Entity` | Request is structurally valid but violates domain validation |
| `500 Internal Server Error` | Unexpected server-side failure |

# 15. Error Response

Errors should use a consistent JSON structure.

Example:

```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "Adding this dependency would create a cycle.",
  "timestamp": "2026-09-10T17:00:00Z",
  "path": "/api/tasks/550e8400-e29b-41d4-a716-446655440000/dependencies"
}
```

The API must not expose:

- Passwords.
- Password hashes.
- JWT secrets.
- Internal database credentials.
- Stack traces.
- Other sensitive implementation details.

# 16. Resource Ownership

All protected resources must belong to the authenticated user.

The API must verify ownership before performing operations.

A user may only access tasks belonging to their own projects.

Ownership checks apply to:

- Projects.
- Tasks.
- Task dependencies.
- Availability.
- Schedules.
- Schedule blocks.
- Task completion records.

An attempt to access another user's resource must not expose the resource's data.

# 17. API and Scheduling Responsibilities

The API is responsible for receiving requests, validating input, enforcing authorisation, and invoking domain services.

The scheduling service is responsible for:

- Calculating remaining work.
- Evaluating dependencies.
- Considering availability.
- Applying scheduling strategies.
- Enforcing hard constraints.
- Optimising soft constraints.
- Detecting infeasible schedules.
- Identifying schedule risk.
- Producing a `ScheduleResult`.

The controller should not contain scheduling logic.

The intended flow is:

```text
HTTP Request
     ↓
Controller
     ↓
Service
     ↓
Domain / Scheduler
     ↓
Repository
     ↓
PostgreSQL
```

# 18. API Design Principles

The API follows these principles:

1. Resource-oriented endpoints
   URLs represent domain resources rather than implementation details.

2. Stateless authentication
   JWT authentication is used for protected requests.

3. Consistent responses
   Resources and errors follow predictable JSON structures.

4. Validation at the boundary
   Invalid input is rejected before reaching domain logic.

5. Authorisation by ownership
   Users may only access their own resources.

6. Domain logic outside controllers
   Controllers delegate business operations to services.

7. Explicit scheduling operations
   Schedule generation and recalculation are separate operations from ordinary CRUD.

8. Stable identifiers
   UUIDs are used consistently for resource identifiers.

# 19. API Summary

| Area | Method | Endpoint | Purpose |
| --- | --- | --- | --- |
| Auth | POST | `/api/auth/register` | Register user |
| Auth | POST | `/api/auth/login` | Authenticate user |
| Projects | GET | `/api/projects` | List projects |
| Projects | POST | `/api/projects` | Create project |
| Projects | GET | `/api/projects/{id}` | Get project |
| Projects | PUT | `/api/projects/{id}` | Update project |
| Projects | DELETE | `/api/projects/{id}` | Delete project |
| Tasks | GET | `/api/tasks` | List tasks |
| Tasks | POST | `/api/tasks` | Create task |
| Tasks | GET | `/api/tasks/{id}` | Get task |
| Tasks | PUT | `/api/tasks/{id}` | Update task |
| Tasks | DELETE | `/api/tasks/{id}` | Delete task |
| Tasks | POST | `/api/tasks/{id}/complete` | Complete task |
| Dependencies | POST | `/api/tasks/{id}/dependencies` | Add dependency |
| Dependencies | GET | `/api/tasks/{id}/dependencies` | List dependencies |
| Dependencies | DELETE | `/api/tasks/{id}/dependencies/{dependencyId}` | Remove dependency |
| Availability | GET | `/api/availability` | List availability |
| Availability | POST | `/api/availability` | Create availability |
| Availability | PUT | `/api/availability/{id}` | Update availability |
| Availability | DELETE | `/api/availability/{id}` | Delete availability |
| Scheduling | POST | `/api/schedules/generate` | Generate schedule |
| Scheduling | GET | `/api/schedules/current` | Get active schedule |
| Scheduling | POST | `/api/schedules/recalculate` | Recalculate schedule |
