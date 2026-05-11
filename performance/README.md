# Performance Tests

This directory contains k6 performance tests for the Qazaq Learn API.

## Test Scenarios

- `k6-login.js`: Tests user login endpoint
- `k6-courses.js`: Tests course listing endpoint
- `k6-enrollment.js`: Tests course enrollment endpoint

## Running Tests

```bash
# Login test
k6 run performance/k6-login.js

# Courses test
k6 run performance/k6-courses.js

# Enrollment test
k6 run performance/k6-enrollment.js
```

## Environment Variables

- `BASE_URL`: API base URL (default: http://localhost:8080)
- `USER_EMAIL`: Test user email (default: student@example.com)
- `USER_PASSWORD`: Test user password (default: Password123)
- `COURSE_ID`: Course ID for enrollment test

## Performance Targets

- 100 virtual users
- 5 minutes duration
- P95 response time < 500ms
- Error rate < 1%