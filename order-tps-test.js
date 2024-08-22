// 1. 동시 다발적인 요청 (High Throughput Test)
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const requests = new Counter('http_reqs');

// 검증할 url
const BASE_URL = 'http://localhost:8080/orders/payment/kafka';

export let options = {
    stages: [
        { duration: '1m', target: 100 }, // 1분 동안 100 VUs로 증가
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 이하
    },
};

export default function () {
    const buyerId = Math.floor(Math.random() * 7) + 1; // 1부터 7까지의 랜덤 buyerId 선정
    const orderId = Math.floor(Math.random() * 107) + 1; // 1부터 107까지의 랜덤 잔액 선정
    const PAYLOAD = JSON.stringify({
        buyerId: buyerId,
        orderId: orderId
    });


    const res = http.post(BASE_URL, PAYLOAD, { headers: { 'Content-Type': 'application/json' } });

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    sleep(1);
}