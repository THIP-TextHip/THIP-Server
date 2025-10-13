// cd ./src/test/java/konkuk/thip/k6
// k6 run --out influxdb=http://localhost:8086/k6 feed-like-concurrency-test.js
// k6 run feed-like-concurrency-test.js
import http from 'k6/http';
import { sleep,check } from 'k6'; // sleep 기능 사용 시 추가 (sleep(n) -> 지정한 n 기간 동한 VU 실행을 일시 중지)

const BASE_URL = 'http://localhost:8080';
const FEED_ID = 4; // 테스트할 피드 ID
const VUS = 2; // 원하는 VU 수

export let options = {
    vus: VUS, // 동시에 5명 접속
    duration: '30s', // 30초동안 테스트
};

// 테스트 전 사용자 별 토큰 발급
export function setup() {
    let tokens = [];
    let likeStatus = [];

    // 유저 ID에 대해 토큰을 미리 발급
    for (let userId = 1; userId <= VUS; userId++) {
        const res = http.get(`${BASE_URL}/api/test/token/access?userId=${userId}`);
        check(res, { 'token received': (r) => r.status === 200 && r.body.length > 0 });
        tokens.push(res.body);
        // 각 유저가 해당 피드에 대해 좋아요를 하지 않은 것으로 초기화
        likeStatus.push(false);
    }

    return { tokens, likeStatus };
}

export default function (data) {
    const vuIdx = __VU - 1;
    const token = data.tokens[vuIdx];

    // 현재 좋아요 요청상태 반전 --> 최초요청은 좋아요 하지않았을때 좋아요를 하는 요청
    data.likeStatus[vuIdx] = !data.likeStatus[vuIdx];

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

    // 응답 체크
    check(res, {
        'status 200': (r) => r.status === 200,
        'status 400': (r) => r.status === 400,
        'Internal server error': (r) => r.status === 500,
    });

    if (res.status !== 200) {
        console.error(`[VU${__VU}] ERROR status=${res.status} body=${res.body}`);
    }

    sleep(0.5); // 500ms 간격으로 요청, 일반적 사용자 환경 모의
}

// 테스트 결과 html 리포트로 저장
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
export function handleSummary(data) {
    return {
        "summary.html": htmlReport(data),
    };
}
