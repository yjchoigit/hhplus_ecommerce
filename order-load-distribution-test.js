// 3. 부하 분산 테스트 (Load Distribution Test)
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const requests = new Counter('http_reqs');

// 검증할 url
const BASE_URL = 'http://localhost:8080/orders/payment/kafka';

export const options = {
    vus: 50, // 동시 사용자 수
    duration: '5m', // 테스트 지속 시간
    thresholds: {
        'http_req_duration': ['p(95)<500'] // 95%의 응답 시간이 500ms 이하이어야 함
    }
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