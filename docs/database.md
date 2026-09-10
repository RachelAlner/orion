# Orion - Database Design

## 1. Overview

This document defines the database design for Orion.

Orion uses PostgreSQL as its primary relational database. The database persists users, projects, tasks, task dependencies, availability, generated schedules, schedule blocks, and task completion records.

The database design is derived from the domain model and is responsible for:

- Persisting domain state.
- Enforcing data integrity where appropriate.
- Representing relationships between domain entities.
- Supporting retrieval of tasks, schedules, and user data.
- Providing a reliable source of persisted application state.

Domain rules that require application-level reasoning, such as dependency-cycle detection and schedule optimisation, are not delegated entirely to the database.

## 2. Database Principles

The database design follows these principles:

1. Relational integrity
   Foreign keys and database constraints are used to maintain valid relationships.

2. User data isolation
   User-owned resources are associated with a specific user and must not be accessible across users.

3. Normalisation
   Data is stored in separate tables according to its domain meaning, avoiding unnecessary duplication.

4. Explicit relationships
   Many-to-many and historical relationships are represented using dedicated tables.

5. UUID identifiers
   Entities use UUIDs as primary keys to avoid exposing sequential identifiers and to support possible future distributed components.

6. Application/domain separation
   Database constraints enforce structural validity, while complex domain rules remain in the application layer.

7. Temporal accuracy
   Timestamps are stored explicitly for scheduling, progress tracking, auditing, and possible future analytics.

8. Extensibility
   The schema should support future scheduling, prediction, analytics, and distributed-processing features without requiring unnecessary redesign of the core model.

## 3. Database Technology

### Database

#### PostgreSQL

PostgreSQL is selected because it provides:

- Strong relational integrity.
- Foreign key and check constraints.
- Transactions and ACID guarantees.
- Effective indexing.
- Good support for temporal data.
- Compatibility with Spring Boot and Java.
- A strong foundation for future analytical workloads.

## 4. Entity Overview

The initial database consists of the following tables:

```text
users
projects
tasks
task_dependencies
availability
schedules
schedule_blocks
task_completions
```

# 5. Table Definitions

## 5.1 `users`

Stores authentication and account information for Orion users.

| Column | Type | Constraints | Description |
| --- | ---| ---| --- |
| `id` | UUID | PK | Unique user identifier |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | User email address |
| `password_hash` | VARCHAR(255) | NOT NULL | Securely hashed password |
| `created_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | Account creation time |
| `updated_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | Last account update time |

### Constraints

- `id` is the primary key.
- `email` must be unique.
- `email` cannot be null.
- Plaintext passwords must never be stored.
- Password hashing is performed by the application layer.

### Indexes

The unique constraint on `email` provides an index suitable for authentication lookups.

## 5.2 `projects`

Represents a collection of related tasks.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `id` | UUID | PK | Unique project identifier |
| `user_id` | UUID | FK, NOT NULL | Owner of the project |
| `name` | VARCHAR(255) | NOT NULL | Project name |
| `description` | TEXT |  | Project description |
| `deadline` | TIMESTAMP WITH TIME ZONE |  | Project deadline |
| `priority` | INTEGER | NOT NULL | Project priority from 1–5 |
| `status` | VARCHAR(20) | NOT NULL | Project status |
| `created_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | Creation time |
| `updated_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | Last update time |

### Foreign Keys

```text
projects.user_id → users.id
```

Deleting a user should not leave orphaned projects.

### Constraints

- `name` cannot be null.
- `priority` must be between 1 and 5.
- `status` must represent a valid project status.
- `deadline`, when present, must represent a valid timestamp.

### Project statuses

```text
ACTIVE
COMPLETED
ARCHIVED
```

### Indexes

Recommended indexes:

```text
user_id
deadline
```

These support common queries such as retrieving a user's projects and finding projects approaching their deadlines.

## 5.3 `tasks`

Represents individual units of work that can be scheduled.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `id` | UUID | PK | Unique task identifier |
| `project_id` | UUID | FK, NOT NULL | Project containing the task |
| `title` | VARCHAR(255) | NOT NULL | Task title |
| `description` | TEXT |  | Task description |
| `estimated_minutes` | INTEGER | NOT NULL | Initial estimated work required |
| `deadline` | TIMESTAMP WITH TIME ZONE |   | Task deadline |
| `priority` | INTEGER | NOT NULL     | Task priority from 1–5 |
| `status` | VARCHAR(20) | NOT NULL | Current task status |
| `created_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | Creation time |
| `updated_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | Last update time |
| `completed_at` | TIMESTAMP WITH TIME ZONE |  | Completion time |

### Foreign Keys

```text
tasks.project_id → projects.id
```

A task cannot exist without a project.

### Constraints

- `title` cannot be null.
- `estimated_minutes` must be greater than zero.
- `priority` must be between 1 and 5.
- `status` must represent a valid task status.
- `completed_at` should only be populated for completed tasks.

### Task statuses

```text
TODO
IN_PROGRESS
COMPLETED
CANCELLED
```

### Indexes

Recommended indexes:

```text
project_id
deadline
status
```

These support common operations such as:

- Retrieving tasks belonging to a project.
- Finding tasks with approaching deadlines.
- Retrieving incomplete tasks for scheduling.

# 5.4 `task_dependencies`

Represents prerequisite relationships between tasks.

If task `B` depends on task `A`, then `A` must be completed before `B` can be scheduled.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `task_id` | UUID | PK, FK | Dependent task |
| `depends_on_task_id` | UUID | PK, FK | Required prerequisite |

The composite primary key is:

```text
(task_id, depends_on_task_id)
```

### Example

```text
Task A: Research topic
Task B: Write report

B depends on A
```

The database represents this as:

```text
task_id = B
depends_on_task_id = A
```

### Foreign Keys

```text
task_dependencies.task_id
    → tasks.id

task_dependencies.depends_on_task_id
    → tasks.id
```

### Constraints

A task must not depend on itself:

```text
task_id != depends_on_task_id
```

Duplicate dependencies are prevented by the composite primary key.

### Application-level constraints

Dependency cycles cannot be reliably prevented using a simple row-level database constraint.

For example:

```text
A → B
B → C
C → A
```

The application layer must validate the dependency graph before accepting a new dependency.

This validation should reject circular dependencies.

# 5.5 `availability`

Represents recurring periods during which a user is available to work.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `id` | UUID | PK | Unique availability identifier |
| `user_id` | UUID | FK, NOT NULL | User who owns the availability |
| `day_of_week` | SMALLINT | NOT NULL | Day represented by the availability period |
| `start_time` | TIME | NOT NULL | Start of available period |
| `end_time` | TIME | NOT NULL | End of available period |

### Foreign Keys

```text
availability.user_id → users.id
```

### Constraints

The following must hold:

```text
start_time < end_time
```

`day_of_week` must represent a valid day.

The application may use:

```text
1 = Monday
2 = Tuesday
3 = Wednesday
4 = Thursday
5 = Friday
6 = Saturday
7 = Sunday
```

### Example

```text
user_id      = ...
day_of_week  = 1
start_time   = 09:00
end_time     = 17:00
```

This represents availability from 09:00 to 17:00 every Monday.

### Important design decision

Availability represents a **recurring weekly pattern** in the initial implementation.

Specific exceptions, such as holidays or one-off unavailable periods, will be introduced later in future versions.

# 5.6 `schedules`

Represents a generated version of a user's plan.

A schedule contains the time allocations produced by the scheduling algorithm.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `id` | UUID | PK | Unique schedule identifier |
| `user_id` | UUID | FK, NOT NULL | Owner of the schedule |
| `generated_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | Time the schedule was generated |
| `valid_from` | TIMESTAMP WITH TIME ZONE | NOT NULL | Start of schedule validity |
| `valid_until` | TIMESTAMP WITH TIME ZONE | NOT NULL | End of schedule validity |
| `algorithm` | VARCHAR(50) | NOT NULL | Scheduling strategy used |
| `status` | VARCHAR(20) | NOT NULL | Schedule state |

### Foreign Keys

```text
schedules.user_id → users.id
```

### Schedule statuses

```text
ACTIVE
SUPERSEDED
```

Only a valid generated schedule should be considered active.

### Algorithm

The `algorithm` field records which scheduling strategy generated the schedule.

Examples include:

```text
EDF
PRIORITY
SPT
COMBINED
```

This allows Orion to compare scheduling strategies and analyse their behaviour in future versions.

### Validity

The following must hold:

```text
valid_from < valid_until
```

### Schedule lifecycle

When a new schedule is generated:

```text
Existing ACTIVE schedule
          ↓
      SUPERSEDED

New schedule
     ↓
   ACTIVE
```

This preserves the distinction between the current schedule and previously generated schedules.

# 5.7 `schedule_blocks`

Represents a specific allocation of time to a task.

A task can be divided across multiple schedule blocks.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `id` | UUID | PK | Unique block identifier |
| `schedule_id` | UUID | FK, NOT NULL | Schedule containing the block |
| `task_id` | UUID | FK, NOT NULL | Task being scheduled |
| `start_time` | TIMESTAMP WITH TIME ZONE | NOT NULL | Block start |
| `end_time` | TIMESTAMP WITH TIME ZONE | NOT NULL | Block end |

### Foreign Keys

```text
schedule_blocks.schedule_id → schedules.id
schedule_blocks.task_id → tasks.id
```

### Constraints

The following must hold:

```text
start_time < end_time
```

A block must have a positive duration.

The application layer must additionally verify that:

- The block falls within user availability.
- The task is not completed or cancelled.
- Dependencies are satisfied.
- The task deadline is not violated.
- Blocks belonging to the same schedule do not overlap.

### Indexes

Recommended indexes:

```text
schedule_id
task_id
start_time
```

These support retrieving a schedule, finding blocks belonging to a task, and ordering schedule blocks chronologically.

# 5.8 `task_completions`

Stores records of actual work performed on a task.

A task may have multiple completion records.

This allows Orion to distinguish between:

```text
Estimated Work
      ↓
Scheduled Work
      ↓
Actual Work
      ↓
Remaining Work
```

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `id` | UUID | PK | Unique completion record |
| `task_id` | UUID | FK, NOT NULL | Task worked on |
| `started_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | Start of recorded work |
| `completed_at` | TIMESTAMP WITH TIME ZONE | NOT NULL | End of recorded work |
| `actual_minutes` | INTEGER | NOT NULL | Actual time spent |

### Foreign Keys

```text
task_completions.task_id → tasks.id
```

### Constraints

```text
started_at < completed_at
actual_minutes > 0
```

### Indexes

Recommended indexes:

```text
task_id
completed_at
```

These support calculating historical task performance and retrieving completion records for analytics.

# 6. Relationships

The database relationships correspond directly to the domain model.

## Relationship Summary

| Relationship | Cardinality |
| --- | --- |
| User → Project | 1 : N |
| User → Availability | 1 : N |
| User → Schedule | 1 : N  |
| Project → Task | 1 : N |
| Task → Task Dependency | N : N |
| Task → Schedule Block | 1 : N |
| Schedule → Schedule Block | 1 : N |
| Task → Task Completion | 1 : N |

# 7. Referential Integrity

Foreign keys should be used to prevent orphaned records.

The primary relationships are:

```text
projects.user_id
    → users.id

tasks.project_id
    → projects.id

task_dependencies.task_id
    → tasks.id

task_dependencies.depends_on_task_id
    → tasks.id

availability.user_id
    → users.id

schedules.user_id
    → users.id

schedule_blocks.schedule_id
    → schedules.id

schedule_blocks.task_id
    → tasks.id

task_completions.task_id
    → tasks.id
```

Referential integrity ensures that child records cannot reference entities that do not exist.

# 8. Deletion Behaviour

Deletion behaviour should prevent accidental orphaned data.

The initial design uses the following conceptual rules:

| Parent | Child | Behaviour |
| --- | --- | --- |
| User | Projects | Cascade |
| User | Availability | Cascade |
| User | Schedules | Cascade |
| Project | Tasks | Cascade |
| Task | Schedule Blocks | Restrict or application-controlled |
| Task | Task Dependencies | Cascade |
| Task | Task Completions | Cascade |
| Schedule | Schedule Blocks | Cascade |

The exact deletion behaviour should be implemented consistently through database foreign-key actions and application-level validation.

Deleting a user should remove all user-owned data.

Deleting a project should remove its tasks and associated dependent records.

Historical schedules may require additional consideration if Orion later treats schedules as audit/history data. The initial implementation will use cascading deletion for simplicity.

# 9. Data Integrity Constraints

The database should enforce constraints wherever they can be expressed safely at the database level.

## Numeric constraints

```text
priority BETWEEN 1 AND 5
estimated_minutes > 0
actual_minutes > 0
```

## Temporal constraints

```text
availability.start_time < availability.end_time

schedule.valid_from < schedule.valid_until

schedule_block.start_time < schedule_block.end_time

task_completion.started_at < task_completion.completed_at
```

## Relationship constraints

```text
task_dependencies.task_id
    != task_dependencies.depends_on_task_id
```

## Required values

The following fields should not be nullable:

- User email
- Password hash
- Project owner
- Project name
- Task project
- Task title
- Task estimated duration
- Task priority
- Task status
- Availability owner
- Availability times
- Schedule owner
- Schedule generation time
- Schedule validity period
- Schedule algorithm
- Schedule status
- Schedule block task
- Schedule block times
- Completion task
- Completion timestamps
- Actual duration

Optional information, such as descriptions and deadlines where appropriate, may remain nullable.

# 10. Database Constraints vs Application Constraints

Not every domain rule should be implemented directly in PostgreSQL.

### Database-level constraints

The database should enforce structural rules such as:

- Primary keys.
- Foreign keys.
- Unique email addresses.
- Non-null required fields.
- Positive durations.
- Valid priority ranges.
- Valid time ranges.
- Self-dependency prevention.

### Application-level constraints

The application should enforce rules requiring domain logic, such as:

- Dependency-cycle detection.
- Whether a task's dependencies are satisfied.
- Whether a schedule is feasible.
- Whether schedule blocks overlap.
- Whether a schedule respects availability.
- Whether a task deadline can be satisfied.
- Whether a completed task should be rescheduled.
- Scheduler optimisation objectives.
- Schedule stability decisions.

This separation prevents the database from becoming responsible for complex scheduling behaviour.

# 11. User Data Isolation

All user-owned data must be associated with a user either directly or through a parent entity.

A task does not need its own `user_id` because its ownership can be derived through its project.

The application must verify ownership before allowing access to resources.

For example, a request for:

```text
GET /api/tasks/{id}
```

must not return a task belonging to another user.

Database relationships provide structural ownership, while the application layer performs authorisation checks.

# 12. Time and Date Handling

Orion is a scheduling application, so temporal data must be represented consistently.

### Timestamps

Timestamps representing real-world moments should use:

```text
TIMESTAMP WITH TIME ZONE
```

This includes:

- Account creation.
- Project deadlines.
- Task deadlines.
- Schedule generation.
- Schedule validity.
- Schedule blocks.
- Task completion records.

### Recurring availability

Recurring availability uses:

```text
TIME
```

for the start and end of the recurring period, together with a day-of-week value.

For example:

```text
Monday
09:00 → 17:00
```

### Time zones

The application should use a consistent time-zone strategy.

Stored timestamps should represent absolute points in time, while the user's configured or selected time zone can determine how schedules are displayed.

The initial implementation should avoid mixing local timestamps and UTC timestamps without an explicit conversion strategy.

# 13. Task Duration and Progress

Orion must distinguish between estimated, scheduled, actual, and remaining work.

### Estimated work

Stored on the task:

```text
tasks.estimated_minutes
```

This represents the initial estimate of the total work required.

### Scheduled work

Derived from the task's schedule blocks:

```text
SUM(schedule_block duration)
```

This represents how much time Orion has allocated to the task.

### Actual work

Stored through:

```text
task_completions.actual_minutes
```

Multiple completion records may contribute to the total actual work.

### Remaining work

Remaining work is derived from the current task state and recorded progress.

Conceptually:

```text
Remaining Work
    =
Required Work - Actual Work
```

The exact calculation may evolve as the scheduling and prediction systems become more sophisticated.

This distinction is essential for adaptive replanning.

# 14. Schedule Persistence

A generated schedule is persisted rather than being treated as a temporary calculation.

This provides:

- Reproducibility.
- Schedule history.
- Comparison between schedules.
- Schedule stability analysis.
- Future analytics.
- Debugging of scheduling decisions.

Each schedule records the algorithm used to generate it.

For example:

```text
Schedule A
algorithm = EDF
status = SUPERSEDED

Schedule B
algorithm = COMBINED
status = ACTIVE
```

This allows Orion to determine how the current plan differs from previous plans.

# 15. Schedule Block Persistence

Schedule blocks are the atomic persisted representation of planned work.

For example:

```text
Task: Complete database design

09:00 ───────── 10:30
       Schedule Block
```

A longer task can be split:

```text
09:00 ── 10:00    Block 1
14:00 ── 15:00    Block 2
16:00 ── 16:30    Block 3
```

This allows Orion to schedule work around fragmented availability.

Breaks do not need to be stored as separate database entities in the initial implementation. A gap between schedule blocks represents unavailable or unused time.

# 16. Indexing Strategy

Indexes should support the most common application queries without introducing unnecessary overhead.

Initial indexes:

| Table | Index | Purpose |
| --- | --- | --- |
| `projects` | `user_id` | Retrieve user's projects |
| `projects` | `deadline` | Find upcoming deadlines |
| `tasks` | `project_id` | Retrieve project tasks |
| `tasks` | `deadline` | Find urgent tasks |
| `tasks` | `status` | Retrieve incomplete tasks |
| `availability` | `user_id` | Retrieve user availability |
| `schedules` | `user_id` | Retrieve user schedules |
| `schedule_blocks` | `schedule_id` | Retrieve schedule blocks |
| `schedule_blocks` | `task_id` | Retrieve blocks for a task |
| `schedule_blocks` | `start_time` | Chronological schedule queries |
| `task_completions` | `task_id` | Retrieve task history |
| `task_completions` | `completed_at` | Historical queries |

Indexes should be added or modified based on measured query performance rather than prematurely optimising every query.

# 17. Transactions

Operations that modify multiple related records should use database transactions.

For example, generating a new schedule may involve:

```text
1. Create schedule
2. Create schedule blocks
3. Supersede previous schedule
4. Mark new schedule as active
```

These operations should be atomic.

If any operation fails, the transaction should roll back so that Orion does not leave the database in a partially updated state.

Similarly, operations involving task completion and replanning should maintain database consistency.

# 18. Concurrency Considerations

The initial implementation is a modular monolith, but the database design should account for concurrent requests.

Potential concurrent operations include:

- Updating a task.
- Completing a task.
- Generating a schedule.
- Recalculating a schedule.
- Updating availability.

The application should ensure that concurrent operations cannot produce invalid active schedules or inconsistent task state.

Database transactions and appropriate transaction isolation should be used where necessary.

More advanced concurrency mechanisms can be introduced if measured behaviour demonstrates a need.

# 19. Database Migrations

Database schema changes should be managed using version-controlled migrations.

Migrations provide a reproducible way to create and evolve the database schema across development, testing, and production environments.

The migration history should be stored in the repository and applied automatically by the application or deployment process.

Each migration should:

- Have a unique version.
- Make one logical schema change.
- Be committed to Git.
- Be tested before integration.
- Avoid modifying previously applied migrations.

The specific migration framework will be selected during implementation.

# 20. Normalisation

The initial schema follows a relational and predominantly normalised design.

For example, task completion data is stored separately from `tasks`. Rather than storing multiple completion records directly inside a task.

Similarly, task dependencies are stored in a separate relationship table.

This avoids storing variable-length collections inside individual task records and allows PostgreSQL to maintain relational integrity.

Denormalisation should only be introduced if profiling demonstrates a measurable performance requirement.

# 21. Future Database Extensions

The initial schema intentionally excludes concepts that are not required by the MVP.

Potential future tables include:

```text
notifications
audit_logs
prediction_results
task_duration_predictions
analytics_events
user_preferences
calendar_events
```

These should only be introduced when corresponding requirements are established.

Future scheduling or prediction functionality may also require additional historical data.

This data can eventually support task-duration prediction and personalised scheduling.

# 22. Initial Schema Summary

| Table | Purpose |
| --- | --- |
| `users` | User accounts and authentication data |
| `projects` | Groups related tasks |
| `tasks` | Units of schedulable work |
| `task_dependencies` | Prerequisite relationships between tasks |
| `availability` | Recurring user working periods |
| `schedules` | Generated schedule versions |
| `schedule_blocks` | Concrete allocations of work to time |
| `task_completions` | Historical records of actual work |

The initial schema deliberately focuses on the core Orion scheduling domain.

# 23. Design Decisions

The database design makes the following decisions:

### PostgreSQL

PostgreSQL is used as the primary relational database because the domain contains strong relationships, constraints, transactions, and temporal data.

### UUID primary keys

UUIDs are used for entity identifiers to provide non-sequential identifiers and support possible future distributed architecture.

### Separate completion records

Actual work is represented by `task_completions` rather than a single value on `tasks`, allowing Orion to retain historical work data.

### Separate schedule and schedule blocks

A `schedule` represents a generated plan, while `schedule_blocks` represent its individual time allocations.

### Recurring availability

Availability is initially represented as a weekly recurring pattern. One-off exceptions can be introduced later.

### Application-level dependency validation

Dependency cycles are detected by the application rather than relying on database constraints.

### No premature denormalisation

The initial schema prioritises correctness and maintainability. Performance-driven denormalisation can be introduced later based on measured requirements.

# 24. Database Design Invariants

The following invariants must hold:

1. Every project belongs to exactly one user.
2. Every task belongs to exactly one project.
3. Every availability record belongs to exactly one user.
4. Every schedule belongs to exactly one user.
5. Every schedule block belongs to exactly one schedule.
6. Every schedule block references exactly one task.
7. Every task completion belongs to exactly one task.
8. Tasks cannot depend on themselves.
9. Dependency cycles are rejected by the application.
10. Priorities must be between 1 and 5.
11. Task estimated durations must be positive.
12. Completion durations must be positive.
13. Availability periods must have a valid start and end time.
14. Schedule validity periods must have a valid start and end time.
15. Schedule blocks must have a positive duration.
16. Foreign-key relationships must remain valid.
17. User-owned data must remain isolated between users.

# 25. Future Review

The database design should be reviewed when:

- New domain entities are introduced.
- Scheduling requirements change.
- Historical scheduling data becomes necessary.
- Performance measurements identify database bottlenecks.
- Collaboration features are introduced.
- Calendar integration is introduced.
- Prediction or machine-learning features require additional data.
- Orion evolves towards distributed services.
