# Orion Architecture

## 1. Overview

Orion is implemented as a modular monolith consisting of a React frontend, a Spring Boot backend, and a PostgreSQL database.

The architecture separates presentation, application, domain, scheduling, security, and persistence responsibilities while keeping the system simple to develop, test, and run locally.

Components may be extracted or replaced in the future where there is a demonstrated technical requirement.

## 2. Architecture Goals

The architecture is designed to provide:

- Separation of responsibilities.
- Low coupling between application components.
- Testable business and scheduling logic.
- Secure access to user data.
- Reliable persistence.
- Replaceable scheduling strategies.
- Consistent API behaviour.
- Reproducible local development.

## 3. System Components

The initial system consists of three primary components:

### Frontend

The frontend is implemented using React and TypeScript.

It is responsible for:

- User interaction.
- Presentation of application data.
- Client-side state management.
- Displaying tasks, projects, schedules, and scheduling results.
- Communicating with the backend through the REST API.

The frontend must not contain critical business or scheduling rules.

### Backend

The backend is implemented using Spring Boot and provides the REST API and core application logic.

It is responsible for:

- Authentication and authorisation.
- Request validation.
- Application logic.
- Domain rules.
- Scheduling.
- Persistence coordination.
- Error handling.

### Database

PostgreSQL is used as the primary persistent data store.

It stores users, projects, tasks, dependencies, availability, schedules, schedule blocks, and task completion records.

The database design is defined in `database.md`.

## 4. Backend Structure

The backend is organised into logical layers and modules.

```text
backend/src/main/java/com/orion/
├── controller/
├── service/
├── model/
├── dto/
├── repository/
├── scheduler/
├── security/
└── exception/
```

The package structure represents logical responsibilities rather than independently deployed services.

### Controller Layer

The controller layer handles HTTP requests and responses.

Responsibilities include:

- Receiving API requests.
- Validating request data at the API boundary.
- Calling application services.
- Mapping application results to API responses.

Controllers must not contain core business or scheduling logic.

### Service Layer

The service layer coordinates application operations.

Responsibilities include:

- Applying application-level business rules.
- Coordinating domain operations.
- Managing transactions.
- Calling repositories.
- Invoking scheduling operations.
- Coordinating changes across multiple domain objects.

### Domain Layer

The domain layer represents Orion's core concepts and rules.

Responsibilities include:

- Representing domain entities.
- Enforcing domain invariants.
- Representing relationships between domain entities.
- Providing domain behaviour independent of infrastructure.

The domain must not depend on HTTP, frontend implementation, or database-specific details.

### Scheduler

The scheduler is responsible for generating and evaluating schedules.

Responsibilities include:

- Calculating remaining work.
- Evaluating task dependencies.
- Considering user availability.
- Applying scheduling strategies.
- Generating schedule blocks.
- Enforcing hard constraints.
- Optimising soft constraints.
- Detecting infeasible schedules.
- Evaluating scheduling risk.
- Supporting schedule recalculation.

Scheduling logic must remain independent of HTTP concerns and should be testable without the REST API.

The detailed scheduling design is defined in `scheduling.md`.

### Repository Layer

The repository layer provides persistence access.

Responsibilities include:

- Retrieving persisted data.
- Saving domain state.
- Updating persisted data.
- Deleting persisted data.
- Abstracting database access from application logic.

Repositories must not contain core scheduling or application business rules.

### Security Layer

The security layer provides authentication and authorisation.

Responsibilities include:

- Password handling.
- JWT authentication.
- Endpoint protection.
- User identity management.
- Resource ownership checks.

Detailed security requirements are defined in `security.md`.

### DTO Layer

Data Transfer Objects define the data exchanged through the API.

DTOs prevent internal domain and persistence representations from being exposed directly to API consumers.

The API layer is responsible for mapping between DTOs and internal application objects.

### Exception Handling

Exception handling is centralised to provide consistent API error responses.

The exception handling mechanism must:

- Map known application and domain errors to appropriate HTTP status codes.
- Provide consistent error structures.
- Prevent internal implementation details from being exposed.
- Distinguish expected domain failures from unexpected system failures.

## 5. Frontend Structure

The frontend is organised by shared responsibilities and application features.

```text
frontend/src/
├── components/
├── pages/
├── services/
├── hooks/
├── types/
├── utils/
└── features/
    ├── auth/
    ├── projects/
    ├── tasks/
    ├── calendar/
    └── scheduling/
```

### Components

Contains reusable user interface components.

### Pages

Contains top-level application views.

### Services

Contains API communication logic.

Frontend services are responsible for communicating with the backend and must not implement backend business rules.

### Hooks

Contains reusable React state and behaviour logic.

### Types

Contains TypeScript representations of API and frontend data.

### Features

Contains feature-specific components, logic, and state.

## 6. Application Boundaries

The frontend and backend communicate through the REST API defined in `api.md`.

The backend is the authoritative source for:

- Authentication.
- Authorisation.
- Domain validation.
- Business rules.
- Scheduling.
- Infeasibility detection.
- Risk evaluation.
- Persistent state.

The frontend may perform client-side validation for usability, but equivalent validation must be enforced by the backend.

The frontend must never communicate directly with PostgreSQL.

## 7. Request Processing

A typical application request follows these stages:

1. The frontend sends an HTTP request to the REST API.
2. The controller validates the request structure.
3. The security layer authenticates and authorises the request where required.
4. The controller delegates the operation to an application service.
5. The service applies the relevant application and domain rules.
6. The service interacts with the scheduler or repository where required.
7. Persistence operations are performed through the repository layer.
8. The result is mapped to an API response.
9. The response is returned to the frontend.

This separation ensures that HTTP handling, application logic, domain rules, scheduling, and persistence remain independently testable.

## 8. Scheduling Integration

Scheduling is treated as a distinct application capability within the backend.

The scheduling service provides the application boundary for schedule generation and recalculation.

The scheduler receives the current information required to produce a schedule, including:

- Incomplete tasks.
- Remaining work.
- Deadlines.
- Priorities.
- Dependencies.
- User availability.
- Existing schedule information.
- Task progress.

The scheduler returns a scheduling result containing the generated schedule, unscheduled work, scheduling status, risk information, and an explanation of the result.

Scheduling strategies must be replaceable without requiring changes to controllers, persistence, or the frontend.

The detailed scheduling algorithms and constraints are defined in `scheduling.md`.

## 9. Persistence

PostgreSQL is the authoritative persistent data store for the initial system.

All database access is performed through the backend repository layer.

The database is responsible for maintaining persistent state and enforcing constraints that can be represented at the database level.

Application-level constraints that require domain knowledge, such as dependency cycle detection, are enforced by the backend.

The complete database schema and persistence constraints are defined in `database.md`.

## 10. Transaction Management

Operations that modify multiple related pieces of persistent state must use appropriate transaction boundaries.

Examples include:

- Creating or modifying task dependencies.
- Completing a task and recording its completion.
- Generating and activating a schedule.
- Recalculating a schedule and superseding the previous schedule.

A transaction must either complete successfully or leave the persistent state consistent with its previous valid state.

An invalid or incomplete schedule must not become the active schedule.

## 11. Module Boundaries

The backend is a modular monolith.

Modules are logically separated but execute within the same application process and share the same database.

The principal areas of responsibility are:

- Authentication and security.
- Project and task management.
- Availability management.
- Scheduling.
- Progress tracking.
- Persistence.

Modules should interact through defined application or domain interfaces rather than relying on internal implementation details of other modules.

The modular structure is intended to minimise coupling and allow individual capabilities to evolve independently.

## 12. Dependency Direction

Dependencies should follow the application's architectural boundaries.

Controllers depend on application services.

Application services may depend on domain logic, scheduling components, and repositories.

The domain and scheduling logic must not depend on controllers, frontend code, or HTTP-specific concerns.

Persistence implementations must remain behind the repository boundary.

This dependency direction allows the core application logic to be tested independently of external interfaces and infrastructure.

## 13. Data Ownership

User-owned resources are associated with an authenticated user either directly or through their parent entity.

The backend is responsible for enforcing ownership boundaries.

Operations involving projects, tasks, dependencies, availability, schedules, schedule blocks, and completion records must verify that the authenticated user is authorised to access the relevant resource.

Ownership enforcement is part of the security boundary and must not rely solely on frontend behaviour.

## 14. Error Handling

The application distinguishes between expected domain outcomes and unexpected system failures.

Expected outcomes include:

- Invalid input.
- Authentication failure.
- Authorisation failure.
- Missing resources.
- Domain conflicts.
- Invalid dependencies.
- Infeasible schedules.

An infeasible schedule is a valid scheduling outcome and must be represented explicitly rather than treated as an unexpected server failure.

Unexpected failures must be handled without exposing sensitive implementation details.

## 15. Configuration

Application configuration is externalised from source code.

Environment-specific values are provided through configuration files or environment variables.

Sensitive configuration must not be committed to source control.

Sensitive values include:

- Database credentials.
- JWT signing secrets.
- Environment-specific credentials.
- Other authentication secrets.

`.env.example` documents required environment variables without containing real credentials.

## 16. Testing

The architecture supports testing at multiple levels.

### Unit Testing

Unit tests should cover:

- Domain rules.
- Service behaviour.
- Scheduling strategies.
- Scheduling constraints.
- Risk evaluation.
- Validation logic.

### Integration Testing

Integration tests should cover:

- Repository behaviour.
- Database interactions.
- Service and repository integration.
- Authentication and authorisation.
- REST API behaviour.

### End-to-End Testing

End-to-end tests should cover critical user workflows across the frontend and backend.

Scheduling correctness should receive particular testing attention because scheduling is a core capability of Orion.

## 17. Initial Deployment

The initial system is designed to run locally without requiring paid infrastructure.

The required runtime components are:

- React frontend.
- Spring Boot backend.
- PostgreSQL database.

Development and test environments should be reproducible using documented setup instructions.

Cloud infrastructure, container orchestration, distributed messaging, and external worker services are not required for the initial implementation.

## 18. Future Scalability

The modular architecture provides a foundation for future extraction of individual components if justified by system requirements.

Potential future architectural changes include:

- Asynchronous processing using a message broker.
- Separate scheduling workers.
- Separate analytics or notification workers.
- A dedicated prediction service.
- Redis caching where performance measurements justify its use.
- Independent deployment of components.
- Container orchestration where operational requirements justify it.

These technologies are intentionally excluded from the initial implementation.

Future infrastructure should be introduced only when there is a clear requirement such as independent scaling, asynchronous processing, fault isolation, or measurable performance constraints.

## 19. Architectural Principles

The architecture follows the following principles:

1. Modular monolith first
   The initial system avoids distributed complexity until it is justified by actual requirements.

2. Separation of concerns
   Each architectural layer has a clearly defined responsibility.

3. Domain independence
   Core domain and scheduling logic remain independent of infrastructure and presentation concerns.

4. Backend authority
   Critical business, security, and scheduling rules are enforced by the backend.

5. Replaceable scheduling strategies
   Scheduling algorithms can evolve without requiring changes to unrelated components.

6. Testability
   Core logic can be tested independently of external interfaces and infrastructure.

7. Explicit boundaries
   Components communicate through defined interfaces and application boundaries.

8. Measured scalability
   Additional infrastructure is introduced in response to demonstrated requirements rather than anticipated scale.

## 20. Architecture Summary

The initial Orion architecture is a modular monolith comprising a React and TypeScript frontend, a Spring Boot backend, and a PostgreSQL database.

The backend separates controllers, application services, domain logic, scheduling, security, repositories, DTOs, and exception handling.

The design supports future evolution of Orion's scheduling algorithms and infrastructure without introducing unnecessary complexity into the initial implementation.
