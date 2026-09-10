# ADR-002: PostgreSQL

## Decision

Orion will use PostgreSQL as its primary relational database.

## Context

Orion manages structured and relational data including users, projects, tasks, dependencies, availability, schedules, schedule blocks, and task completion records.

The system requires strong data integrity, transactions, relationships between entities, and support for constraints such as task dependencies and ownership.

The database should also be free to use, locally reproducible, mature, and suitable for future analytics and optimisation workloads.

## Alternatives

### MySQL

A mature relational database with strong SQL support and a large ecosystem.

### MongoDB

A document-oriented database providing a flexible schema and good support for document-based data models.

### PostgreSQL

A mature open-source relational database with strong support for transactions, constraints, complex queries, and extensibility.

## Chosen Approach

Orion will use PostgreSQL as its primary database.

The application will interact with PostgreSQL through the backend persistence layer rather than accessing the database directly from the frontend.

Database constraints will be used where appropriate to enforce data integrity, while business rules such as dependency-cycle detection will remain in the application layer.

## Reason

PostgreSQL is well suited to Orion's relational domain and scheduling requirements.

It provides:

- Strong transactional consistency
- Foreign keys and relational constraints
- Powerful SQL capabilities
- Mature indexing and query optimisation
- Good support for complex relational queries
- Open-source and free local development
- A mature ecosystem and strong Java/Spring integration
- A suitable foundation for future analytics and optimisation

MongoDB's flexible document model is less appropriate because Orion's core data has well-defined relationships and integrity requirements.

MySQL would also satisfy the initial requirements, but PostgreSQL provides a strong general-purpose foundation and is the preferred choice for the project's relational and analytical requirements.

## Consequences

### Positive

- Strong data integrity
- Reliable transaction management
- Clear relational data model
- Powerful querying capabilities
- Suitable for scheduling-related data
- Free and straightforward to run locally
- Good compatibility with Spring Boot
- Suitable foundation for future analytics

### Negative

- Requires a database server rather than an embedded database
- Relational schema changes require controlled migrations
- Database configuration adds some local development complexity
- PostgreSQL-specific features may reduce portability to other database systems

These trade-offs are acceptable because data consistency and relational integrity are important to Orion.

## Future Review

This decision should be revisited if Orion develops requirements that PostgreSQL cannot efficiently satisfy, such as significant changes to the data model, specialised workload requirements, or demonstrated scalability limitations.