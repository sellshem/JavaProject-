## Tech stack

- Java 17
- Spring Boot 3.x
- Spring Web
- Spring Security (JWT + RBAC)
- Spring Data JPA + PostgreSQL
- Flyway (database migrations)
- Redis (caching, rate limiting, account lockout)
- Kafka (asynchronous event streaming)
- Lombok
- MapStruct
- Validation
- springdoc OpenAPI / Swagger
- JUnit 5 + Mockito + Testcontainers
- Docker / Docker Compose
- k6 performance testing
- Kubernetes manifests (K8s)

## Architecture

The application follows a clean layered architecture:

- `controller` - HTTP entry points and request mapping
- `service` - business logic and authorization rules
- `repository` - persistence interfaces for data access
- `domain` - JPA entities, enums, and domain events
- `dto` - request and response payloads
- `mapper` - MapStruct mappers for DTO/entity conversion
- `security` - JWT authentication, rate limiting, and account lockout
- `config` - Kafka, Redis, encryption configuration
- `exception` - global exception handling

### Event-Driven Architecture

The system uses Kafka for asynchronous event publishing:

- `CourseCreatedEvent` - published when a new course is created
- `CoursePublishedEvent` - published when a course is published/unpublished
- `UserRegisteredEvent` - published on user registration/login
- `AssignmentSubmittedEvent` - published when student submits assignment
- `CourseEnrollmentEvent` - published on course enrollment

### Security Features

- **Authentication**: JWT access tokens (24h) + secure refresh tokens (7d, stored in httpOnly cookies recommended)
- **Authorization**: Role-Based Access Control (ADMIN, TEACHER, STUDENT) with method-level security (`@PreAuthorize`)
- **Rate Limiting**: IP-based rate limiting on auth endpoints (5 attempts/15min) and general API (100 req/min) via Redis
- **Account Lockout**: After 5 failed login attempts, account locked for 15 minutes (Redis)
- **Field Encryption**: Sensitive PII (fullName) encrypted at rest using AES/GCM/NoPadding
- **Audit Logging**: All important actions logged with actor, action, entity, timestamp, IP
- **Secrets Management**: Externalized configuration via environment variables

## Folder structure

```
src/main/java/kz/qazaqlearn
 ├── QazaqLearnApplication.java
 ├── config           # Kafka, Redis, Encryption, OpenAPI
 ├── controller
 ├── service
 ├── repository
 ├── domain
 │   └── events       # Domain events for Kafka
 ├── dto
 ├── mapper
 ├── security         # JWT, filters, rate limit, account lockout
 └── exception
```

## Run locally

1. Set environment variables or use defaults in `src/main/resources/application.yml`.
2. Start PostgreSQL, Redis, Kafka, Zookeeper using Docker Compose:

```bash
docker compose up -d
```

3. Build and run:

```bash
./mvnw clean package
java -jar target/qazaq-learn-0.0.1-SNAPSHOT.jar
```

The backend will be available at `http://localhost:8080`. Swagger UI at `/swagger-ui.html`.

## Run with Docker Compose

```bash
docker compose up --build
```

Includes: PostgreSQL, Redis, Kafka, Zookeeper, backend, frontend.

## API examples

### Register user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Aidos Nur","email":"aidos@example.com","password":"Password123","role":"STUDENT"}'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"aidos@example.com","password":"Password123"}'
```
Response includes `accessToken` and `refreshToken`.

### Refresh token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<your-refresh-token>"}'
```

### Create course (teacher)

```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"titleKk":"Kazakh 101","descriptionKk":"Nurturing Kazakh language learners."}'
```

## Security

- Passwords are hashed with BCrypt
- JWT tokens signed with HS256
- Refresh tokens stored hashed in DB, rotated on use
- Rate limiting & account lockout via Redis
- Sensitive fields encrypted (fullName)
- CORS configured for frontend origin

## Testing

```bash
# Unit tests
./mvnw test

# Integration tests with Testcontainers (PostgreSQL, Redis, Kafka)
./mvnw verify

# Coverage report
./mvnw jacoco:report
# Open target/site/jacoco/index.html
```

## Performance testing

```bash
# Install k6
k6 run performance/k6-login.js
k6 run performance/k6-courses.js
k6 run performance/k6-enrollment.js
```

Targets: 100 VUs, 5 min, p95 < 500ms, error rate < 1%

## Kubernetes

Manifests in `k8s/` include deployments, services, ingress, configmap, secret.

### Deploy

```bash
kubectl apply -f k8s/
```

Uses 2 replicas, resource limits, liveness/readiness probes.

## CI/CD

GitHub Actions workflow (`.github/workflows/ci.yml`) includes:

- Test with coverage (JaCoCo + Codecov)
- Docker image build & push to GHCR
- Deploy to staging (dev branch) and production (main branch)

Requires repository secrets:
- `CODECOV_TOKEN`
- `KUBE_CONFIG_STAGING` (base64 kubeconfig)
- `KUBE_CONFIG_PROD`

## Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | jdbc:postgresql://localhost:5432/qazaq_learn | PostgreSQL URL |
| `SPRING_DATASOURCE_USERNAME` | qazaq_user | DB user |
| `SPRING_DATASOURCE_PASSWORD` | qazaq_password | DB password |
| `SPRING_DATA_REDIS_HOST` | localhost | Redis host |
| `SPRING_DATA_REDIS_PORT` | 6379 | Redis port |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Kafka bootstrap |
| `JWT_SECRET` | change-me-in-production | JWT signing secret |
| `JWT_EXPIRATION_MS` | 86400000 | JWT TTL (24h) |
| `ENCRYPTION_KEY` | change-me-32-char-key | AES encryption key (16+ chars) |
| `SERVER_PORT` | 8080 | Server port |

## Auditing

Admin users can retrieve audit logs:

```bash
GET /api/admin/audit-logs
```

Captures: actor, action, entity type/id, timestamp, IP address.

## Folder structure

```
src/main/java/kz/qazaqlearn
 ├── QazaqLearnApplication.java
 ├── config
 ├── controller
 ├── service
 ├── repository
 ├── domain
 ├── dto
 ├── mapper
 ├── security
 └── exception
```

## Run locally

1. Set environment variables or use defaults in `src/main/resources/application.yml`.
2. Start PostgreSQL and Redis if not using Docker.
3. Build and run:

```bash
./mvnw clean package
java -jar target/qazaq-learn-0.0.1-SNAPSHOT.jar
```

## Run with Docker Compose

```bash
docker compose up --build
```

The backend will be available at `http://localhost:8080`. Docker Compose starts the application, PostgreSQL, and Redis.

## API examples

Register user:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Aidos Nur","email":"aidos@example.com","password":"Password123","role":"STUDENT"}'
```

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"aidos@example.com","password":"Password123"}'
```

Create course (teacher):

```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"titleKk":"Kazakh 101","descriptionKk":"Nurturing Kazakh language learners."}'
```

Create assignment (teacher):

```bash
curl -X POST http://localhost:8080/api/courses/$COURSE_ID/assignments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"titleKk":"Homework 1","descriptionKk":"Complete exercises 1-5.","deadline":"2024-12-31T23:59:59"}'
```

Submit assignment (student):

```bash
curl -X POST http://localhost:8080/api/assignments/$ASSIGNMENT_ID/submissions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"answerText":"My submission text."}'
```

Grade submission (teacher):

```bash
curl -X PATCH http://localhost:8080/api/submissions/$SUBMISSION_ID/grade \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"grade":95,"feedbackKk":"Excellent work!"}'
```

## Test command

```bash
./mvnw test
```

## Verify command

```bash
./mvnw clean verify
```

## Swagger / OpenAPI

After starting the application, the Swagger UI is available at:

```bash
http://localhost:8080/swagger-ui.html
```

## Kubernetes

The `k8s/` folder contains Kubernetes manifests for the Qazaq Learn application, PostgreSQL, and Redis.

### Deploy to Kubernetes

```bash
# Apply all manifests
kubectl apply -f k8s/

# Check deployments
kubectl get deployments

# Check services
kubectl get services

# Check pods
kubectl get pods
```

### Access the application

If using ingress, add to /etc/hosts:

```
127.0.0.1 qazaq-learn.local
```

Then access at http://qazaq-learn.local

## Performance testing

Use k6 to execute the performance tests:

```bash
# Login test
k6 run performance/k6-login.js

# Courses test
k6 run performance/k6-courses.js

# Enrollment test
k6 run performance/k6-enrollment.js
```

Targets: 100 VUs, 5 min, p95 < 500ms, error rate < 1%

## Audit logging

The application records audit events for lesson, enrollment, progress, assignment, and submission actions. Admin users can retrieve logs from:

```bash
GET /api/admin/audit-logs
```

## Assignment and submission flows

- Teachers can create, update, and delete assignments.
- Students can submit assignments, resubmit returned submissions, and teachers can grade them.
- Assignment submission activity is captured in the audit log.

### Create assignment (teacher):

```bash
curl -X POST http://localhost:8080/api/courses/$COURSE_ID/assignments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"titleKk":"Homework 1","descriptionKk":"Complete exercises 1-5.","deadline":"2024-12-31T23:59:59"}'
```

### Submit assignment (student):

```bash
curl -X POST http://localhost:8080/api/assignments/$ASSIGNMENT_ID/submissions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"answerText":"My submission text."}'
```

### Grade submission (teacher):

```bash
curl -X PATCH http://localhost:8080/api/submissions/$SUBMISSION_ID/grade \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"grade":95,"feedbackKk":"Excellent work!"}'
```

## Default environment variables

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`
- `SERVER_PORT`
