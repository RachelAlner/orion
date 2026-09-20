# Orion - Domain Model

## 1. Overview

The domain model defines the core concepts and relationships within Orion.

Orion models work as tasks belonging to projects, subject to deadlines, priorities, dependencies, and user availability. The scheduling domain uses this information to allocate work to available time and adapt the plan as progress changes.

## 2. Domain Entities

### User

Represents an individual Orion user.

#### Responsibilities:

- Own projects, tasks, availability, and schedules.
- Define recurring weekly availability.
- Access only resources belonging to the user.

### Project

Represents a collection of related tasks.

#### Attributes:

- Name
- Description
- Deadline
- Priority
- Status

#### Relationships:

- Belongs to one `User`.
- Contains one or more `Task` entities.

#### Status:

- `ACTIVE`
- `COMPLETED`
- `ARCHIVED`

### Task

Represents a unit of work that can be scheduled.

#### Attributes:

- Title
- Description
- Estimated duration
- Deadline
- Priority
- Status

#### Relationships:

- Belongs to one `Project`.
- May depend on other `Task` entities.
- May have multiple `ScheduleBlock` entities.
- May have multiple `TaskCompletion` records.

#### Status:

- `TODO`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

A task may be divided across multiple schedule blocks.

### Task Dependency

Represents a prerequisite relationship between two tasks.

If task `B` depends on task `A`, task `A` must be completed before work on task `B` can be scheduled.

#### Invariants:

- A task cannot depend on itself.
- Dependency cycles are not permitted.
- Both tasks must belong to the same user's data.

### Availability

Represents a period during which a user is available to work.

#### Attributes:

- Day of week
- Start time
- End time

#### Relationships: 

- Belongs to one `User`. 

#### Invariants:

- Start time must precede end time.
- Multiple availability periods may exist for the same day.
- Availability periods for the same user must not overlap.

### Schedule

Represents a generated plan for allocating work to available time.

#### Attributes:

- Generation time
- Validity period
- Scheduling algorithm
- Status

#### Relationships:

- Belongs to one `User`.
- Contains multiple `ScheduleBlock` entities.

#### Status:

- `ACTIVE`
- `SUPERSEDED`

A newly generated schedule may supersede the currently active schedule.

### Schedule Block

Represents a specific allocation of time to a task.

#### Attributes:

- Start time
- End time
- Task

#### Relationships:

- Belongs to one `Schedule`.
- References one `Task`.

#### Invariants:

- Start time must precede end time.
- The block must fall within user availability.
- Blocks must not overlap.
- The associated task must be schedulable.
- Task dependencies must be satisfied.
- The block must not violate the task deadline.

### Task Completion

Represents a recorded period of actual work performed on a task.

A task may have multiple completion records.

This allows Orion to distinguish between:

- Estimated work
- Scheduled work
- Actual work
- Remaining work

This distinction is required for adaptive scheduling and future duration prediction.

## 3. Domain Relationships

### Relationship Cardinality

| Relationship | Cardinality |
| --- | --- |
| User → Project | 1 : N |
| User → Availability | 1 : N |
| User → Schedule | 1 : N |
| Project → Task | 1 : N |
| Task → Task Dependency | N : N |
| Task → Schedule Block | 1 : N |
| Schedule → Schedule Block | 1 : N |
| Task → Task Completion | 1 : N |


## 4. Domain Rules

### Ownership

All user-owned resources must be associated with exactly one user, either directly or through their parent entity.

Users must not be able to access or modify resources belonging to another user.

### Task Progress

Planned and actual work are separate concepts.

Actual work is represented by `TaskCompletion` records rather than a single value on `Task`.

### Dependencies

Dependencies establish ordering constraints between tasks.

A dependent task cannot be scheduled until all of its incomplete prerequisites have been satisfied.

### Deadlines

Tasks may have deadlines which constrain when their remaining work can be scheduled.

Projects may also have deadlines. The relationship between project and task deadlines is defined by the scheduling rules.

### Priority

Priority represents the relative importance of a task or project.

The initial priority range is:

1 = Lowest
5 = Highest

Priority is a scheduling preference rather than a hard constraint.

## 5. Scheduling Domain

Scheduling transforms the current state of the domain into a set of planned work periods.

### Scheduling Inputs

The scheduler considers:

- Incomplete tasks
- Remaining work
- Deadlines
- Priorities
- Dependencies
- User availability
- Existing schedule
- Actual progress

### Hard Constraints

The following constraints must not be violated:

1. Work must occur within user availability.
2. Schedule blocks must not overlap.
3. Task dependencies must be respected.
4. Completed and cancelled tasks must not be scheduled.
5. Schedule blocks must have a positive duration.
6. Task deadlines must be respected where a feasible schedule exists.

If these constraints cannot be satisfied, the schedule is considered infeasible.

### Soft Constraints

The scheduler may optimise for:

- Deadline urgency
- Task priority
- Schedule stability
- Reduced context switching
- Reduced fragmentation of work

## 6. Schedule State

Orion classifies task and schedule health using the following states:

| State | Meaning |
| --- | ---- |
| `ON_TRACK` | Current progress is consistent with the plan. |
| `AT_RISK` | Current progress indicates an increased likelihood of missing a deadline. |
| `INFEASIBLE` | The available time is insufficient to satisfy the required constraints. |
| `COMPLETED` | All required work has been completed. |

Risk is derived from the current state of tasks, progress, availability, deadlines, and the generated schedule.

## 7. Replanning

A schedule may be recalculated when the current plan no longer represents the user's state accurately.

Replanning may be triggered by:

- Changes in actual task duration.
- Missed scheduled work.
- Changes in user availability.
- Changes to deadlines.
- New or modified tasks.
- Changes to task dependencies.
- Significant deviations from expected progress.

Replanning should adapt the schedule while avoiding unnecessary changes to existing allocations.

## 8. Infeasible Schedules

A schedule is infeasible when the available time cannot satisfy the required work and domain constraints.

Orion must report the infeasibility rather than generating a schedule that violates a hard constraint.

## 9. Domain Invariants

The following invariants apply across the domain:

- Every project belongs to exactly one user.
- Every task belongs to exactly one project.
- Tasks cannot depend on themselves.
- Dependency cycles are not permitted.
- Availability periods must have valid time ranges.
- Schedule blocks must have positive durations.
- Schedule blocks must not overlap.
- Schedule blocks must respect availability.
- Schedule blocks must respect task dependencies.
- Completed and cancelled tasks cannot be scheduled.
- Invalid schedules must not be considered active.
- User-owned resources must not be accessible by other users.

## 10. Domain Boundaries

The initial implementation is a modular monolith. Domain responsibilities are separated logically rather than deployed as independent services.

The scheduling domain should remain sufficiently isolated that scheduling strategies can be changed without requiring changes to the core project and task model.

## 11. Future Domain Extensions

The following concepts may be introduced as the system evolves:

- Prediction — estimates task duration using historical completion data.
- Notification — represents system notifications related to deadlines or schedule changes.
- Analytics — represents aggregated information about task and scheduling behaviour.
- Audit Log — records significant changes to domain state.

These concepts are intentionally excluded from the initial implementation until their requirements are established.

## 12. Domain Design Principles

The domain model follows these principles:

1. Separation of concerns
   Domain concepts should not depend on infrastructure or persistence details.

2. Explicit constraints
   Scheduling constraints must be represented explicitly rather than implicitly.

3. Planned vs actual work
   Estimated, scheduled, and actual work must remain distinguishable.

4. Valid domain state
   Invalid dependencies, schedules, and allocations must be rejected.

5. Replaceable scheduling strategies
   Scheduling algorithms should be replaceable without changing the core domain model.

6. Adaptive planning
   The domain must support schedules being recalculated as task progress and constraints change.

7. Explainability
   Scheduling decisions should be capable of being represented in terms of the constraints and priorities that influenced them.

## 13. Domain Model Summary

The core domain consists of:

```text
User
Project
Task
Task Dependency
Availability
Schedule
Schedule Block
Task Completion
```

The central domain workflow is:

```text
Tasks
  ↓
Constraints
  ↓
Available Time
  ↓
Schedule
  ↓
Actual Progress
  ↓
Replanning
  ↓
Updated Schedule
```

This model provides the foundation for the database schema, API design, and scheduling specification.
