# Orion - Scheduling Design

## 1. Purpose

The scheduling system is responsible for generating and maintaining feasible task schedules based on deadlines, priorities, dependencies, available time, and task progress.

The scheduler is responsible for deciding:
- which tasks should be scheduled
- when each task should be scheduled 
- how much work should be allocated to each available period 
- how tasks should be split across multiple periods when necessary 
- how existing scheduled work should be preserved where possible 
- when a schedule cannot satisfy all constraints. 

The scheduling system is designed to be deterministic, explainable, testable, and independent of the API layer.

The scheduler does not manage tasks or availability directly. It consumes those domains as inputs and produces scheduling decisions.

## 2. Scheduling Inputs

The scheduler operates on the following inputs:

- Incomplete tasks
- Remaining work for each task
- Task deadlines
- Task priorities
- Task dependencies
- User availability
- Existing schedule
- Recorded task progress
- Previously scheduled work

Completed and cancelled tasks are excluded from scheduling.

### Task Information

Each schedulable task provides:

- Estimated duration
- Remaining duration
- Deadline
- Priority
- Completion status
- Dependencies
- Project association

Estimated duration represents the initial expected amount of work.

Remaining duration represents the amount of work that still needs to be scheduled and may differ from the original estimate following partial completion.

Only tasks that are eligible for scheduling may be allocated work.

### Task Eligibility 

A task is eligible for scheduling when all required conditions are satisfied. 

A task must: 
- Not be completed. 
- Not be cancelled. 
- Have a positive remaining duration. 
- Belong to the user. 
- Have all required dependencies satisfied. 

A task is blocked when one or more required dependencies remain incomplete. 

For example: 

```text
A → B → C 
```

If A is incomplete: 

```text
A = eligible 
B = blocked 
C = blocked 
```

After A is completed: 

```text
A = completed
B = eligible
C = blocked 
```

The scheduler must never schedule a blocked task before its dependencies are satisfied. 

### Dependencies 

Dependencies define ordering constraints between tasks. 

For a dependency:

```text
Task A → Task B
```

Task B cannot be scheduled before Task A has been completed sufficiently to satisfy the dependency. 

A task with incomplete prerequisites is considered blocked.

### Availability

Availability defines the periods during which work may be scheduled.

The scheduler must only allocate work within these periods and must not create overlapping schedule blocks.

### Exisitng Schedule 

When replanning, the scheduler may recieve an existing schedule. 

Existing work should be preserved where possible to avoid unnecessary changes. 

### Current Progress 

When replanning, the scheduler may recieve information about actual progress. 

Examples include: 
- Completed work. 
- Remaining task duration. 
- Work that took longer than estimated. 
- Work that was missed. 

This allows the scheduler to adjust future work rather than relying entirely on the original plan.

## 3. Scheduling Constraints

The scheduler distinguishes between hard constraints, which must never be violated, and soft constraints, which influence the quality of a feasible schedule.

### 3.1 Hard Constraints

The following constraints must always be satisfied:

1. Work may only be scheduled during user availability.
2. Schedule blocks must not overlap.
3. Completed tasks must not be scheduled.
4. Cancelled tasks must not be scheduled.
5. A task may only be scheduled after all of its dependencies have been completed.
6. Schedule blocks must have a positive duration.
7. A task must not be scheduled after its deadline.
8. Scheduled work must not exceed the task's remaining work.
9. All scheduled blocks must belong to valid tasks and schedules.
10. Dependency relationships must not contain cycles.

If all hard constraints cannot be satisfied, the scheduler must report the schedule as infeasible rather than violating a constraint.

### 3.2 Soft Constraints

When multiple feasible schedules exist, the scheduler should optimise for:

1. Deadline urgency
2. Task priority
3. Risk of falling behind
4. Schedule stability
5. Reduced fragmentation
6. Reduced context switching
7. Effective use of available time

Soft constraints may be traded against one another when necessary.

## 4. Available Time and Task Splitting

### Available Time 

The scheduler converts recurring availability into concrete scheduling windows for the period being scheduled. 

For example, if the user has: 

```text 
Monday 09:00-12:00
Monday 14:00-17:00
``` 

then the scheduler creates two independent windows: 

```text 
09:00-12:00
14:00-17:00
``` 

Work must never cross an unavailable period. 

The scheduler may divide a task accross multiple availability windows. 

### Task Splitting 

Tasks may be split across multiple schedule blocks. 

For example: 

Task duration: 150 minutes 

Availability: 
09:00-10:00
14:00-16:00

The scheduler may produce: 

09:00-10:00     60 minutes
14:00-15:30     90 minutes

The total allocated duration is: 

60 + 90 = 150 minutes 

A task should only be split when necessary or when doing so produces a better schedule. 

The scheduler should avoid creating unnecessarily small fragments. 

## 5. Scheduling Strategy

The initial scheduler will use a combination of established scheduling strategies rather than relying on a single ordering rule.

Eligible tasks are ordered using a scheduling score. 

The initial scheduler uses a deterministic score based on: 
- deadline urgency
- task priority
- remaining duration 

A task with an earlier deadline should generally recieve greater urgency. 

This reduces the likelihood of missing imminent deadlines.

A higher-priority task should generally recieve greater urgency when other factors are comparable. 

Tasks are assigned a priority from 1 to 5, where:

- `1` — Lowest priority
- `2` — Low priority
- `3` — Normal priority
- `4` — High priority
- `5` — Critical priority

Priority alone must not determine scheduling order. A lower-priority task with a substantially closer deadline may need to be scheduled before a higher-priority task.

The scheduler therefore considers multiple factors when determining which task should receive available time.

This allows users to identify work that is more important independently of its deadline.

Remaining duration is considered so that large tasks are not repeatedly postponed until insufficient time remains. 

When other factors are comparable, shorter tasks may be scheduled first.

This can increase the number of completed tasks and reduce the amount of unfinished work.

The score is an internal scheduling mechanism and is not exposed as a user-facing rating. 

When two tasks have equivalent scores, the scheduler uses deterministic tie-breaking rules. 

Tie-breaking order: 
1. earlier deadline 
2. higher priority
3. earlier task creation time 
4. task ID as the final deterministic tie-breaker

## 6. Schedule Generation

Schedule generation follows these general stages:

1. Select tasks that require scheduling.
2. Calculate remaining work for each task.
3. Validate dependencies and scheduling constraints.
4. Determine available scheduling periods.
5. Prioritise eligible tasks.
6. Allocate work to available periods.
7. Validate the resulting schedule against all hard constraints.
8. Identify unscheduled work.
9. Calculate schedule and task risk.
10. Produce the final schedule result.

Tasks may be divided across multiple schedule blocks when their remaining work does not fit into a single available period.

The scheduler must never allocate time beyond a task's remaining work.

## 7. Dependencies

A task with dependencies is not eligible for scheduling until all prerequisite tasks have been completed.

Dependencies must form a directed acyclic graph.

A dependency cycle makes the affected tasks unschedulable. The scheduler must identify the cycle and report the affected tasks rather than attempting to schedule them.

Dependencies must also be respected during schedule generation. A dependent task must not be scheduled before the completion of its prerequisites.

## 8. Infeasible Schedules

A schedule is considered infeasible when the scheduler cannot satisfy all hard constraints.

For example, if a user has:

- 12 hours of remaining work
- 8 hours of available time before the relevant deadlines

the scheduler must not create 12 hours of work within 8 hours of availability.

Instead, it should schedule the maximum feasible amount and report the remaining 4 hours as unscheduled work.

The result must identify:

- Affected tasks
- Unscheduled work
- Relevant constraint
- Reason the work could not be scheduled

Infeasibility must be explicit and must never be hidden by silently violating constraints.

## 9. Schedule Stability

Replanning should not unnecessarily change previously generated schedules.

When a task's progress changes or new information becomes available, the scheduler should preserve existing schedule blocks where doing so does not negatively affect feasibility or significantly increase deadline risk.

Schedule changes should therefore be minimised while still responding appropriately to meaningful changes.

The scheduler should distinguish between:

- Unchanged blocks
- Moved blocks
- Added blocks
- Removed blocks

This allows the application to explain how and why a schedule changed.

## 10. Replanning

A schedule may be recalculated when relevant inputs change.

Replanning may be triggered by:

- Recorded task progress
- Actual work differing significantly from estimates
- Missed scheduled work
- Changes to availability
- Changes to deadlines
- New tasks
- Modified task priorities
- Added or removed dependencies
- Significant changes in remaining work

Replanning operates on the remaining work rather than restarting completed work.

The resulting schedule should reflect the latest known state while preserving schedule stability where possible.

## 11. Risk and Schedule Health

Each task and schedule may be assigned a health state.

### `ON_TRACK`

The available time is sufficient to complete the required work without significant deadline risk.

### `AT_RISK`

The task or schedule remains feasible but has a meaningful risk of missing its deadline or requires substantially more work than currently planned.

### `INFEASIBLE`

The available constraints cannot accommodate the remaining work.

### `COMPLETED`

The required work has been completed and no further scheduling is required.

Risk assessment should initially use deterministic rules based on remaining work, available time, deadlines, and progress.

More advanced predictive risk models may be introduced later.

## 12. Schedule Objective

The scheduler should seek to minimise the overall cost of a schedule while satisfying all hard constraints.

The objective should account for:

- Deadline risk
- Priority violations
- Schedule instability
- Excessive fragmentation
- Context switching
- Unused available time where useful work exists

Hard constraint violations have effectively infinite cost and therefore cannot be traded against improvements in soft constraints.

The objective function should remain extensible so that more advanced optimisation techniques can be introduced later.

## 13. Scheduling Result

The scheduler should return a structured result containing:

- Generated schedule blocks
- Unscheduled tasks
- Schedule status
- Risk information
- Scheduling algorithm used
- Explanation of significant scheduling decisions

A schedule result must distinguish between successfully scheduled work and work that could not be scheduled.

This information is used by the application to present the schedule and communicate scheduling problems to the user.

## 14. Scheduler Interface

The scheduling implementation should be isolated behind a scheduler interface.

```java
Scheduler
    -> generateSchedule(
        List<Task>,
        List<Dependency>,
        List<Availability>,
        ExistingSchedule
    )
    -> ScheduleResult
```

The API and application services must depend on the scheduling abstraction rather than a specific scheduling algorithm.

This allows different scheduling strategies to be evaluated or replaced without changing the surrounding application.

## 15. Scheduler Invariants

Every generated schedule must satisfy the following invariants:

1. No schedule blocks overlap.
2. All blocks occur within user availability.
3. Dependencies are respected.
4. No block occurs after its task's deadline.
5. Every block has a positive duration.
6. Completed tasks are never scheduled.
7. Cancelled tasks are never scheduled.
8. Scheduled work does not exceed remaining work.
9. Dependency cycles are not accepted.
10. Every schedule block references a valid task.
11. The schedule accurately represents the result of the selected scheduling strategy.

These invariants must be verified through automated tests.

## 16. Future Extensions

The scheduling system is designed to support future improvements without changing its core responsibilities.

Potential extensions include:

- Historical duration prediction
- Personalised task duration estimates
- Advanced optimisation algorithms
- Automatic risk prediction
- Improved schedule stability scoring
- Learning-based prioritisation
- Event-driven schedule recalculation
- User-configurable scheduling preferences

These extensions are outside the initial scheduling implementation and should only be introduced when their benefits and implementation requirements are established.
