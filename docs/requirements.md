# Orion - Requirements 

## 1. Definition 
Orion is an adaptive planning and scheduling system that helps users manage projects, tasks, and deadlines.

The system processes task information, inlcuding estimated duration, deadline, priority, dependencies, and user availability. Based on this information, it produces a schedule that aims to maximise the user's available time and minimise missed deadlines. 

Unlike basic to-do lists, Orion is intended to actively manage the user's schedule. When a user completes work, spends longer than expected on a taks, misses a planned session, adds new work, or changes a deadline, Orion should be able to reassess the schedule and generate an updated plan. 

## 2. Problem 

Managing several projects at the same time becomes difficult when tasks have different deadlines, priorities, durations, and dependencies. 

A simple to-do list shows what needs to be done, but does not help a user organise their time. 

Orion aims to solve this by treating task planning as a scheduling and optimisation problem rather than simply storing a list of tasks. 

## 3. Goals 

The main goals of Orion are: 

1. Allow users to organise projects and tasks in one place. 
2. Automatically generate schedules based on the user's available time. 
3. Take deadlines, priorities, and dependencies into account when scheduling. 
4. Detect when a schedule is at risk or cannot be completed within the available time. 
5. Adapt the schedule when the user's progress changes. 
6. Keep the system reliable, secure, and maintainable as it grows. 

## 4. Users 

The initial version of Orion is designed for individual users who need to manage multiple pieces of work at the same time. 

The initial system will focus on a single-user experience. Multi-user collaboration is not a requirement for the first version. 

## 5. User Stories 

### Account and personal data 

#### US-001
As a user, I want to create an account so that my projects and schedules are associated with me. 

#### US-002
As a user, I want to log in securely so that other people cannot access my data. 

### Projects

#### US-003
As a user, I want to be able to create a project so that I can organise related tasks together. 

#### US-004
As a user, I want to give a project a deadline and priority so that Orion understands its importance.

#### US-005
As a user, I want to edit or delete a project when my plans change. 

#### US-006 
As a user, I want to see the progress of a project so that I know how much work remains. 

### Tasks

#### US-007
As a user, I want to create tasks within a project so that I can break larger pieces of work into smaller units. 

#### US-008
As a user, I want to give a task an estimated duration so that Orion can determine how much time it needs in the schedule. 

#### US-009
As a user, I want to assign a priority and deadline to a task so that important or urgent work can be scheduled appropriately. 

#### US-010
As a user, I want to mark a task as completed so that Orion can update my schedule. 

#### US-011
As a user, I want to record how long I actually spent on a task so that Orion can compare this with my original estimate. 

### Dependencies

#### US-012
As a user, I want to specify that one task depends on another so that tasks are scheduled in the correct order. 

#### US-013
As a user, I want Orion to prevent circular dependencies so that the scheduler does not recieve an impossible task structure. 

### Availability 

#### US-014
As a user, I want to define when I am available to work so that Orion does not schedule tasks when I am unavailable. 

#### US-015
As a user, I want to have different availability on different days so that the scheduler reflects my actual routine.

### Scheduling

#### US-016
As a user, I want Orion to automatically generate a schedule so that I do not have to manually decide when to complete every task.

#### US-017
As a user, I want the scheduler to consider deadlines, priorities, durations and dependencies so that the resulting schedule reflects the constraints of my work.

#### US-018
As a user, I want large tasks to be split across multiple work sessions so that they can fit around my availability.

#### US-019
As a user, I want Orion to tell me when my workload cannot realistically be completed before my deadlines.

#### US-020
As a user, I want Orion to identify tasks that are becoming risky so that I know where I need to take action.

### Adaptive scheduling

#### US-021
As a user, I want my schedule to be recalculated when my progress changes so that it remains useful.

#### US-022
As a user, I want Orion to take actual time spent on previous work into account when planning future work.

#### US-023
As a user, I want to understand why my schedule has changed so that the system does not feel like a black box.

## 6. Functional Requirements 

### 6.1 User Management

#### FR-001
The system shall allow a user to create an account using an email address and password.

#### FR-002
The system shall allow registered users to authenticate.

#### FR-003
The system shall securely store user passwords using a password hashing mechanism.

#### FR-004
The system shall associate user-owned data with the relevant user account.

#### FR-005
The system shall prevent authenticated users from accessing resources belonging to another user.

### 6.2 Project Management

#### FR-006
The system shall allow users to create projects.

#### FR-007
A project shall have a name.

#### FR-008
A project may have a description.

#### FR-009
A project shall support a deadline.

#### FR-010
A project shall support a priority.

#### FR-011
A project shall have a status.

#### FR-012
Users shall be able to update projects.

#### FR-013
Users shall be able to delete projects.

#### FR-014
The system shall associate each project with its owner.

### 6.3 Task Management

#### FR-015
The system shall allow users to create tasks within projects.

#### FR-016
A task shall have a title.

#### FR-017
A task may have a description.

#### FR-018
A task shall have an estimated duration measured in minutes.

#### FR-019
A task shall support a priority.

#### FR-020
A task may have a deadline.

#### FR-021
A task shall have a status.

#### FR-022
Users shall be able to update tasks.

#### FR-023
Users shall be able to delete tasks.

#### FR-024
Users shall be able to mark tasks as completed.

#### FR-025
Completed tasks shall not be included in newly generated schedules.

### 6.4 Task Dependencies

#### FR-026
The system shall allow a task to depend on another task.

#### FR-027
A task shall not be scheduled before its incomplete dependencies have been completed.

#### FR-028
The system shall reject circular task dependencies.

#### FR-029
The system shall detect invalid dependency relationships before generating a schedule.

### 6.5 Availability

#### FR-030
Users shall be able to define periods during which they are available to work.

#### FR-031
Availability shall support different working periods on different days.

#### FR-032
The system shall prevent invalid availability periods where the end time is before or equal to the start time.

#### FR-033
The scheduler shall only allocate work during available periods.

### 6.6 Scheduling

#### FR-034
The system shall generate a schedule from a user's incomplete tasks, availability and constraints.

#### FR-035
The scheduler shall consider task deadlines.

#### FR-036
The scheduler shall consider task priorities.

#### FR-037
The scheduler shall consider estimated task durations.

#### FR-038
The scheduler shall consider task dependencies.

#### FR-039
The scheduler shall consider the user's available working periods.

#### FR-040
The scheduler shall prevent overlapping schedule blocks.

#### R-041
The scheduler shall not schedule work outside the user's availability.

#### FR-042
The scheduler shall not schedule a task after its deadline when a feasible alternative exists.

#### FR-043
The scheduler shall support splitting a task across multiple schedule blocks.

#### FR-044
The scheduler shall allow gaps between schedule blocks.

#### FR-045
The system shall return information about tasks that could not be scheduled.

#### FR-046
The system shall identify when the requested workload cannot fit within the available time.

#### FR-047
The system shall distinguish between a feasible and infeasible schedule.

### 6.7 Schedule Recalculation

#### FR-048
The system shall allow a schedule to be recalculated.

#### FR-049
The scheduler shall be able to use the user's current task progress when recalculating.

#### FR-050
The system shall account for completed work when generating a new schedule.

#### FR-051
The system shall account for remaining work when generating a new schedule.

#### FR-052
The system should avoid unnecessary changes to existing schedule blocks when possible.

#### FR-053
The system shall identify significant changes between the previous and new schedules.

### 6.8 Progress and Actual Time

#### FR-054
The system shall allow users to record work sessions for tasks.

#### FR-055
A work session shall record when the session started and ended.

#### FR-056
A task shall be able to have multiple work sessions.

#### FR-057
The system shall calculate the total recorded time spent on a task.

#### FR-058
The system shall be able to compare estimated duration with actual recorded duration.

#### FR-059
The system should use historical duration information to improve future scheduling when sufficient data is available.

### 6.9 Schedule Risk

#### FR-060
The system shall identify tasks that are at risk of missing their deadlines.

#### FR-061
The system shall identify when the available time is insufficient to complete the remaining workload.

#### FR-062
The system shall distinguish between work that is on track, at risk and infeasible.

#### FR-063
The system should provide an explanation for why a task or project has been identified as at risk.

### 6.10 Explanations

#### FR-064
The system should provide an explanation for major scheduling decisions.

Examples include:

- a task was scheduled earlier because its deadline is approaching
- a high-priority task was scheduled before lower-priority work
- a task was moved because another task took longer than expected
- a task could not be scheduled because there was insufficient available time

## 7. Non-Functional Requirements 

### 7.1 Performance

#### NFR-001
Typical API requests should aim to complete within approximately 200ms under normal local development conditions.

#### NFR-002
The system should provide a measurable performance target for schedule generation based on the number of tasks and constraints being processed.

#### NFR-003
Performance should be measured using tests or benchmarks rather than assumed.

### 7.2 Security

#### NFR-004
Passwords must never be stored in plaintext.

#### NFR-005
Protected API endpoints shall require authentication.

#### NFR-006
Users shall only be able to access resources they own.

#### NFR-007
Secrets such as database passwords and authentication keys shall not be committed to the repository.

#### NFR-008
User input shall be validated before being processed.

#### NFR-009
The application should return appropriate errors without exposing sensitive implementation details.

### 7.3 Reliability

#### NFR-010
The system shall fail explicitly when an operation cannot be completed.

#### NFR-011
Scheduling failures shall not silently produce invalid schedules.

#### NFR-012
Database operations should maintain data consistency.

#### NFR-013
The scheduler should produce deterministic results for the same input and configuration where possible.

### 7.4 Maintainability

#### NFR-014
The system should be structured so that scheduling logic is separated from API and persistence logic.

#### NFR-015
The scheduling algorithm should be replaceable without requiring major changes to unrelated parts of the application.

#### NFR-016
Major architectural decisions should be documented using Architecture Decision Records.

#### NFR-017
The project should include automated tests for important business rules.

#### NFR-018
The codebase should follow consistent naming, formatting and project conventions.

### 7.5 Usability

#### NFR-019
A user should be able to create a project and its first tasks without needing to understand how the scheduling algorithm works.

#### NFR-020
The generated schedule should be understandable without requiring the user to inspect technical details.

#### NFR-021
Scheduling conflicts and infeasible plans should be communicated clearly.

#### NFR-022
Important schedule changes should be visible to the user.

### 7.6 Testability

#### NFR-023
Core scheduling rules shall be testable independently of the user interface.

#### NFR-024
The system shall include tests for scheduling constraints.

#### NFR-025
The system shall include tests for invalid dependencies and infeasible schedules.

#### NFR-026
The system should include integration tests for important API and database operations.

## 8. Success Criteria 

The first version of Orion will be considered successful if a user can:

1. Create a project.
2. Add tasks with deadlines, priorities and estimated durations.
3. Define when they are available to work.
4. Add dependencies between tasks.
5. Generate a schedule automatically.
6. See when and what they should work on.
7. Complete work and record actual time spent.
8. Recalculate the schedule when progress changes.
9. See when a deadline is at risk.
10. Receive a clear indication when the requested workload cannot fit within the available time.

