// feed-like-concurrency-test12.js
// 피크 이후 점진적 하락 테스트 20 → 100(2분 유지) → 50(1분) → 10(이후 종료)
import http from 'k6/http';
import { sleep,check } from 'k6'; // sleep 기능 사용 시 추가 (sleep(n) -> 지정한 n 기간 동한 VU 실행을 일시 중지)

const BASE_URL = 'http://localhost:8080';
const FEED_ID = 1; // 테스트할 피드 ID

export let options = {
    stages: [
        { duration: '2m', target: 20 },   // 2분간 VU 20명 유지 (초기 피크 시작)
        { duration: '2m', target: 100 },  // 2분간 VU 100명 유지 (피크 최대)
        { duration: '1m', target: 50 },   // 1분간 VU 50명 유지 (감소 시작)
        { duration: '2m', target: 10 },   // 2분간 VU 10명 유지 (감소 계속)
        { duration: '1m', target: 0 }     // 1분간 VU 0명 (테스트 종료)
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
    },
};

// 테스트 전 사용자 별 토큰 발급
export function setup() {
    // 최대 VU 수 계산
    const maxVUs = 100;
    let tokens = [];
    let likeStatus = [];

    // 유저 ID에 대해 토큰을 미리 발급
    for (let userId = 1; userId <= maxVUs; userId++) {
        const res = http.get(`${BASE_URL}/api/test/token/access?userId=${userId}`);
        check(res, { 'token received': (r) => r.status === 200 && r.body.length > 0 });
        tokens.push(res.body);
        likeStatus.push(true); // 좋아요 요청
    }

    return { tokens, likeStatus };
}

export default function (data) {
    const vuIdx = __VU - 1;
    const token = data.tokens[vuIdx];

    if (data.lastStatusCode === 200) {
        data.likeStatus[vuIdx] = !data.likeStatus[vuIdx];
    }

    // FeedIsLikeRequest DTO에 맞는 요청 body
    const payload = JSON.stringify({
        type: data.likeStatus[vuIdx],
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
        },
    };

    const res = http.post(`${BASE_URL}/feeds/${FEED_ID}/likes`, payload, params);
    data.lastStatusCode = res.status;

    // 응답 체크
    check(res, {
        'status 200': (r) => r.status === 200,
        'status 400': (r) => r.status === 400,
        'Internal server error': (r) => r.status === 500,
    });

    if (res.status !== 200) {
        console.error(`[VU${__VU}] ERROR status=${res.status} body=${res.body}`);
    }

    // 0.5~1.5초 간격 임의 지연
    sleep(Math.random() + 0.5);
}

// 테스트 결과 html 리포트로 저장
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
export function handleSummary(data) {
    return {
        "summary.html": htmlReport(data),
    };
}
