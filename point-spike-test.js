// 2. 스파이크 (Spike)
import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const requests = new Counter('http_reqs');

export let options = { stages: [
        { duration: '10s', target: 10 }, // 10초 동안 10 VUs로 시작
        { duration: '10s', target: 100 }, // 10초 동안 100 VUs로 급증
        { duration: '30s', target: 100 }, // 30초 동안 100 VUs 유지
        { duration: '10s', target: 0 },   // 10초 동안 0 VUs로 감소
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 이하
    },
};

// 검증할 url
const baseUrl = 'http://localhost:8080/points';

export default function () {
    const buyerId = Math.floor(Math.random() * 7) + 1; // 1부터 7까지의 랜덤 buyerId 선정
    const point = Math.floor(Math.random() * 1000) + 1; // 1부터 1000까지의 랜덤 잔액 선정

    const url = `${baseUrl}/${buyerId}?point=${point}`;
    const res = http.post(url);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}