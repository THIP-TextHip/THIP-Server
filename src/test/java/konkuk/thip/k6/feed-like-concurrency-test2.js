import http from 'k6/http';
import { sleep,check } from 'k6'; // sleep 기능 사용 시 추가 (sleep(n) -> 지정한 n 기간 동한 VU 실행을 일시 중지)

const BASE_URL = 'http://localhost:8080';
const FEED_ID = 4; // 테스트할 피드 ID
const VUS = 1; // 한 명 사용자가 연타 테스트
const ITERATIONS = 60; // 연속 호출 횟수 각 구간마다 20번

export let options = {
    vus: VUS,
    iterations: ITERATIONS,
};

export function setup() {
    const res = http.get(`${BASE_URL}/api/test/token/access?userId=1`);
    check(res, { 'token received': (r) => r.status === 200 && r.body.length > 0 });

    // 최초 좋아요 상태 false로 초기화
    const likeStatus = false;

    return { token: res.body, likeStatus };
}

export default function (data) {
    const token = data.token;

    // 요청마다 좋아요 상태 변경
    data.likeStatus = !data.likeStatus;

    const payload = JSON.stringify({
        type: data.likeStatus
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
        },
    };

    const res = http.post(`${BASE_URL}/feeds/${FEED_ID}/likes`, payload, params);

    check(res, {
        'status 200': r => r.status === 200,
        'status 400': r => r.status === 400,
        'Internal server error': r => r.status === 500,
    });

    if (res.status === 400) {
        console.error(`[VU${__VU}] 400 Bad Request at iteration ${__ITER} body=${res.body}`);
    }
    if (res.status === 500) {
        console.error(`[VU${__VU}] 500 Internal Server Error at iteration ${__ITER} body=${res.body}`);
    }

    // iteration 번호로 구간 구분하여 sleep 시간 변경
    let sleepTime;
    if (__ITER <= 20) {
        sleepTime = 0.01; // 10ms
    } else if (__ITER <= 40) {
        sleepTime = 0.05; // 50ms
    } else {
        sleepTime = 0.1;  // 100ms
    }

    sleep(sleepTime);
}

// 테스트 결과 html 리포트로 저장
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
export function handleSummary(data) {
    return {
        "summary.html": htmlReport(data),
    };
}
