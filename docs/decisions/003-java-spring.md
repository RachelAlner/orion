# ADR-003: Java and Spring Boot

## Decision

Orion will use Java with Spring Boot for its backend.

## Context

Orion requires a backend capable of providing a REST API, authentication, business logic, scheduling, persistence, validation, and integration with PostgreSQL.

The backend should support a maintainable modular architecture while remaining suitable for developing and testing the scheduling system.

## Alternatives

### Python

A widely used language with strong support for data processing, optimisation, and machine learning.

### Node.js with TypeScript

Provides a JavaScript/TypeScript-based backend with a large ecosystem and strong support for REST APIs.

### Java with Spring Boot

Provides a mature, strongly typed backend ecosystem with established support for web applications, security, persistence, testing, and modular application design.

## Chosen Approach

Orion will use Java with Spring Boot for the backend.

Spring Boot will provide the application framework and supporting infrastructure for:

- REST API development
- Dependency injection
- Authentication and security
- PostgreSQL persistence
- Validation
- Transaction management
- Testing

The scheduling logic will remain separated from HTTP and persistence concerns so that scheduling strategies can be developed and tested independently.

## Reason

Java is well suited to Orion's backend requirements and provides strong type safety and mature tooling.

Spring Boot provides established integrations for the core technologies used by Orion, including PostgreSQL, REST APIs, security, and automated testing.

Python may be considered for future specialised optimisation or machine learning components if a demonstrated requirement emerges, without requiring the initial backend architecture to depend on them.

## Consequences

### Positive

- Strong static typing
- Mature backend ecosystem
- Clear support for modular application design
- Strong integration with PostgreSQL
- Established security and authentication support
- Good testing and dependency-injection capabilities
- Suitable for implementing and testing scheduling algorithms
- Consistent technology stack for the initial backend

### Negative

- More verbose than some alternative languages
- Spring Boot introduces framework complexity
- JVM applications require more resources than lightweight alternatives
- Python may be more convenient for future machine learning or specialised optimisation work

These trade-offs are acceptable for the initial system.

## Future Review

This decision should be revisited if Orion develops requirements that Java and Spring Boot cannot efficiently satisfy, particularly for specialised optimisation, machine learning, or performance-critical workloads.