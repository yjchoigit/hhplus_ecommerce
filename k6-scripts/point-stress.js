import http from 'k6/http';
import {check, sleep} from 'k6';

// 설정: 가상 사용자 수와 테스트 지속 시간 설정
export let options = {
    vus: 10, // 가상 사용자 수
    duration: '30s', // 테스트 지속 시간
};

export default function () {
    // buyerId를 랜덤으로 생성 (예: 1부터 1000까지)
    let buyerId = Math.floor(Math.random() * 1000) + 1;
    let response = http.get(`http://host.docker.internal:3000/points/${buyerId}`);

    // 응답 검증
    check(response, {
        'status is 200': (r) => r.status === 200,
        'response time is less than 200ms': (r) => r.timings.duration < 200,
        'response body is an integer': (r) => {
            try {
                let responseBody = JSON.parse(r.body);
                return typeof responseBody === 'number';
            } catch (e) {
                return false;
            }
        },
    });

    sleep(1); // 각 요청 사이에 대기 시간
}
