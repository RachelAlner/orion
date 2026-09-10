# ADR-001: Modular Monolith First

## Decision

Orion will initially be developed as a modular monolith rather than a distributed microservices architecture.

## Context

Orion requires a backend responsible for user management, projects, tasks, scheduling, dependencies, availability, authentication, and persistence.

At the initial stage, the system does not have requirements that justify multiple independently deployed services or distributed infrastructure. Introducing such infrastructure prematurely would increase development and operational complexity without providing a corresponding benefit.

The architecture should nevertheless maintain clear separation between application responsibilities so that components can be extracted into separate services if future requirements justify doing so.

## Alternatives

### Microservices

Separate the application into independently deployable services that communicate through APIs or messaging.

### Serverless

Implement application functionality primarily through independently deployed cloud functions and managed services.

### Modular Monolith

Implement the system as a single deployable application with clearly separated internal modules and responsibilities.

## Chosen Approach

Orion will use a modular monolith architecture.

The backend will remain a single deployable application while maintaining separation between:

- API and controller logic
- Application and domain logic
- Scheduling
- Persistence
- Security
- Error handling

Dependencies between modules should be explicit and unnecessary coupling should be avoided.

## Reason

A modular monolith provides the simplest architecture that satisfies Orion's initial requirements.

It provides:

- Simpler development and debugging
- Simpler local setup and deployment
- Lower operational complexity
- Straightforward transaction management
- Easier integration and end-to-end testing
- Strong consistency within the application
- Clear internal boundaries without distributed-system overhead

Microservices would introduce additional complexity such as network communication, distributed transactions, service deployment, fault handling, and service coordination before there is a demonstrated need for these capabilities.

The modular structure also preserves the option of extracting individual components into separate services in the future.

## Consequences

### Positive

- Reduced development and infrastructure complexity
- Easier local development
- Simpler testing and debugging
- Simpler deployment
- Straightforward database transactions
- No requirement for distributed infrastructure initially
- Clear internal boundaries that support future evolution

### Negative

- Components share the same deployment lifecycle
- Components cannot be independently scaled initially
- A failure in the application may affect multiple modules
- Poorly enforced module boundaries could result in increased coupling

These disadvantages are acceptable for the initial system and will be mitigated through separation of concerns and explicit module boundaries.

## Future Review

The architecture should be reconsidered if there is a demonstrated requirement for:

- Independent scaling of specific components
- Independent deployment
- Fault isolation
- Asynchronous or distributed processing
- Workloads that cannot be efficiently handled within the monolith

If such requirements arise, individual modules may be extracted into separate services incrementally rather than redesigning the entire system.
