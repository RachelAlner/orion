# Orion - Security

## 1. Security Goals

Orion must protect user accounts, personal task data, schedules, and availability information from unauthorised access or modification.

The security design is based on:

- Authentication
- Authorisation
- Secure data handling
- Input validation
- Secure configuration
- Controlled error handling
- Security testing
- Least-privilege access

Security controls should be implemented at the backend rather than relying on the frontend to enforce them.

## 2. Authentication

Orion will use token-based authentication with JSON Web Tokens (JWT).

Users must authenticate before accessing protected resources.

Authentication requirements:

- Passwords must never be stored in plaintext.
- Passwords must be hashed using a secure password hashing algorithm.
- Successful authentication returns a JWT.
- Protected API requests must provide the JWT using the `Authorization: Bearer <JWT>` header.
- Invalid, expired, or missing tokens must result in an authentication error.
- JWT secrets must not be stored in source control.
- Tokens must contain only the claims required by the application.

Authentication endpoints:

- `POST /api/auth/register`
- `POST /api/auth/login`

## 3. Authorisation and Resource Ownership

Authentication establishes the identity of a user. Authorisation determines whether that user is permitted to access a resource.

Every user-owned resource must be associated with its owner.

The backend must verify resource ownership before allowing users to:

- View resources
- Modify resources
- Delete resources
- Generate schedules
- Recalculate schedules
- Record task progress

A user must never be able to access another user's projects, tasks, availability, schedules, or related data by modifying an identifier in an API request.

Authorisation checks must be performed server-side and must not depend on frontend restrictions.

## 4. Input Validation

All externally supplied input must be validated by the backend.

Validation must include:

- Required fields
- Field types
- String lengths
- Valid UUIDs
- Valid dates and timestamps
- Valid task durations
- Valid priority values
- Valid task and project states
- Valid availability periods
- Valid dependency relationships

Invalid input must be rejected before it reaches business logic or persistence operations.

Validation must not rely solely on database constraints or frontend validation.

## 5. Database Security

The application must use parameterised queries or the persistence framework's parameterised mechanisms to prevent SQL injection.

Database access must be performed through the backend persistence layer.

The frontend must never connect directly to PostgreSQL.

Database credentials must:

- Not be committed to source control
- Be provided through environment-specific configuration
- Use the minimum permissions required by the application

Foreign keys and database constraints should be used where appropriate to maintain data integrity.

## 6. Sensitive Configuration

Secrets and credentials must not be stored in the repository.

This includes:

- JWT signing secrets
- Database passwords
- API keys
- Deployment credentials
- Other authentication secrets

Sensitive configuration should be supplied through environment variables or an equivalent secure configuration mechanism.

Example environment files containing real secrets must be excluded from version control.

## 7. API Security

All endpoints that access user data must require authentication.

The backend must:

- Authenticate protected requests
- Authorise access to requested resources
- Validate request data
- Reject malformed requests
- Return appropriate HTTP status codes
- Avoid exposing sensitive implementation details

The API must not trust identifiers supplied by the client to establish ownership.

## 8. Common Web Security Risks

### SQL Injection

Database operations must use parameterised queries or safe ORM mechanisms. User input must never be concatenated directly into SQL statements.

### Cross-Site Scripting

User-provided content must be handled safely when rendered by the frontend. The application must not treat untrusted input as executable HTML or JavaScript.

### Insecure Direct Object References

Resource identifiers such as project, task, and schedule IDs must not provide access by themselves. The backend must verify that the authenticated user owns or is authorised to access the requested resource.

### Brute-Force Authentication

Authentication endpoints should use appropriate protections against repeated failed login attempts. Rate limiting may be introduced as required by the deployment environment.

### Cross-Site Request Forgery

The authentication mechanism and token storage strategy must be implemented consistently with the application's CSRF model. If authentication tokens are stored in cookies, appropriate CSRF protections must be applied.

## 9. Error Handling

API errors must not expose:

- Passwords
- Authentication tokens
- Database credentials
- Stack traces
- Internal file paths
- SQL statements
- Other sensitive implementation details

Clients should receive structured error responses containing only the information required to understand and handle the error.

Detailed technical information may be recorded in secure server-side logs where appropriate.

## 10. Logging

Security-relevant events should be logged sufficiently to support debugging and investigation.

Relevant events include:

- Authentication failures
- Authentication successes
- Authorisation failures
- Unexpected security-related errors

Logs must not contain passwords, JWTs, database credentials, or other sensitive secrets.

## 11. Transport Security

Production deployments must use HTTPS to protect credentials, authentication tokens, and user data while in transit.

HTTP should not be used for authenticated production traffic.

Local development may use HTTP where appropriate because it does not represent the production security boundary.

## 12. Security Testing

Security controls must be verified through automated and manual testing.

Tests should cover:

- Password hashing and authentication
- Invalid and expired JWTs
- Access to another user's resources
- Unauthenticated API requests
- Invalid input
- SQL injection attempts
- Dependency and ownership validation
- Protected endpoint access
- Sensitive information in error responses

Security tests should be included alongside functional tests as the application develops.

## 13. Security Principles

Orion follows these principles:

- Least privilege: components and database users receive only the permissions they require.
- Server-side enforcement: security decisions are made by the backend.
- Defence in depth: security does not depend on a single control.
- Secure by default: protected resources require authentication unless explicitly designated public.
- Minimise sensitive data: only necessary sensitive information is stored.
- Fail securely: invalid or unauthorised requests must not result in access to protected resources.
- No secrets in source control: credentials and signing keys must remain outside the repository.
