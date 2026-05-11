import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const USER_EMAIL = __ENV.USER_EMAIL || 'student@example.com';
const USER_PASSWORD = __ENV.USER_PASSWORD || 'Password123';

export const options = {
  vus: 100,
  duration: '5m',
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const res = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email: USER_EMAIL,
    password: USER_PASSWORD,
  }), { headers: { 'Content-Type': 'application/json' } });

  check(res, {
    'login succeeded': (r) => r.status === 200,
    'received token': (r) => r.json('accessToken') !== undefined,
  });

  sleep(1);
}