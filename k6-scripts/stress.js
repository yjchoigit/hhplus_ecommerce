// import http from 'k6/http';
// import { sleep } from 'k6';
//
// // 기본적으로 K6는 export default function을 통해 실행할 수 있는 테스트가 있어야 합니다.
// export default function () {
//     http.get('https://test.k6.io');
//     sleep(1); // 1초 동안 대기
// }

import http from 'k6/http';
import {check, sleep} from 'k6';
import {Rate} from 'k6/metrics';

// 사용자 정의 메트릭
const errorRate = new Rate('errors');

export let options = {
    stages: [
        { duration: '1m', target: 10 }, // 1분 동안 VU(User) 10명으로 증가
        { duration: '2m', target: 50 }, // 2분 동안 VU(User) 50명으로 증가
        { duration: '3m', target: 100 }, // 3분 동안 VU(User) 100명으로 증가
        { duration: '2m', target: 50 }, // 2분 동안 VU(User) 50명으로 감소
        { duration: '1m', target: 0 }, // 1분 동안 VU(User) 0명으로 감소
    ],
    thresholds: {
        'http_req_duration': ['p(95)<500'], // 95%의 요청이 500ms 이하로 처리되어야 함
        'errors': ['rate<0.01'], // 에러율이 1% 미만이어야 함
    },
};

export default function () {
    let res = http.get('https://test.k6.io'); // 테스트할 URL

    // 응답이 정상적인지 체크
    const checkRes = check(res, {
        'status is 200': (r) => r.status === 200,
        'response time is less than 500ms': (r) => r.timings.duration < 500,
    });

    // 에러 발생 여부 기록
    if (!checkRes) {
        errorRate.add(1);
    }

    sleep(1); // 다음 요청 전 1초 대기
}