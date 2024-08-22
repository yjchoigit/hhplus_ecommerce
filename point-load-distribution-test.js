// 3. 부하 분산 테스트 (Load Distribution Test)
import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const requests = new Counter('http_reqs');

export let options = {
    stages: [
        { duration: '1m', target: 25 }, // 1분 동안 25 VUs로 증가
        { duration: '3m', target: 50 }, // 3분 동안 50 VUs 유지
        { duration: '1m', target: 0 },  // 1분 동안 0 VUs로 감소
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