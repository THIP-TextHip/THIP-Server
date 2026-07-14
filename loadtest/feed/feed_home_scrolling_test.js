//100명의 사용자가 동시에 각자 다른 속도로 홈 피드를 1페이지부터 3페이지까지 탐색하는 시나리오
import http from 'k6/http';
import { sleep,check } from 'k6';

const BASE_URL = 'http://localhost:8000';
const MAX_VUS = 500;

export let options = {
    stages: [
        { duration: '20s', target: 200 }, // 20초 동안 100명까지 증가
        { duration: '40s',  target: MAX_VUS }, // 40초 동안 300명까지 증가하며 피크 부하
        { duration: '20s', target: 0 },   // 20초 동안 0명으로 하강
    ],
    thresholds: {
        http_req_duration: ['p(95)<80'],
        http_req_failed: ['rate<0.01'],
    },
};

// 테스트 전 사용자 별 토큰 발급
export function setup() {
    let tokens = [];

    // 유저 ID에 대해 토큰을 미리 발급
    for (let userId = 1; userId <= MAX_VUS; userId++) {
        const res = http.get(`${BASE_URL}/api/test/token/access?userId=${userId}`);
        check(res, { 'token received': (r) => r.status === 200 && r.body.length > 0 });
        tokens.push(res.body);
    }

    return { tokens: tokens };
}

export default function (data) {
    const vuIdx = __VU - 1;
    const token = data.tokens[vuIdx];

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
        },
    };

    let currentCursor = null;

    // 모든 유저가 정확히 3번의 요청(1~3페이지) 수행
    for (let i = 1; i <= 3; i++) {
        let url = `${BASE_URL}/feeds`;
        if (currentCursor) {
            url += `?cursor=${encodeURIComponent(currentCursor)}`;
        }

        let res = http.get(url, params);

        if (check(res, { 'status is 200': (r) => r.status === 200 })) {
            const responseData = res.json().data;
            currentCursor = responseData.nextCursor;

            // 만약 3페이지가 되기 전에 데이터가 끝났다면 루프 탈출
            if (responseData.isLast || !currentCursor) break;
        } else {
            // 요청 실패 시 해당 유저 시나리오 중단
            break;
        }

        sleep(Math.random() * 1 + 0.5); // 0.5초 ~ 1.5초 사이 랜덤하게 쉬기
    }
}