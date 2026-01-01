// 80%는 상세조회(GET), 20%는 좋아요 변경(POST) 요청
import http from 'k6/http';
import { sleep,check } from 'k6';

const BASE_URL = 'http://localhost:8080';
const FEED_ID = 1; // 테스트할 피드 ID

export let options = {
    scenarios: {
        read_scenario: {
            executor: 'constant-vus',
            vus: 160, // 전체 200명 중 160명은 상세 조회 전담
            duration: '2m',
            exec: 'readFeed',
        },
        write_scenario: {
            executor: 'constant-vus',
            vus: 40, // 전체 200명 중 20명은 좋아요 변경 전담
            duration: '2m',
            exec: 'likeFeed',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
    },
};

// 테스트 전 사용자 별 토큰 발급
export function setup() {
    // 최대 VU 수 계산
    const maxVUs = 200;
    let tokens = [];

    // 유저 ID에 대해 토큰을 미리 발급
    for (let userId = 1; userId <= maxVUs; userId++) {
        const res = http.get(`${BASE_URL}/api/test/token/access?userId=${userId}`);
        check(res, { 'token received': (r) => r.status === 200 && r.body.length > 0 });
        tokens.push(res.body);
    }

    return {tokens};
}

// 상세조회만 실행
export function readFeed(data) {
    let vuIdx = __VU - 1;
    let token = data.tokens[vuIdx];
    let params = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        }
    };

    let res = http.get(`${BASE_URL}/feeds/${FEED_ID}`, params);
    check(res, {
        'feed detail 200': (r) => r.status === 200,
        'feed detail status 400': (r) => r.status === 400,
        'feed detail Internal server error': (r) => r.status === 500,
    });

    if (res.status !== 200) {
        console.error(`[VU${__VU}] ERROR status=${res.status} body=${res.body}`);
    }

    sleep(Math.random()); // 0~1초 내 랜덤 대기(실사용 패턴 반영)
}

// 좋아요 변경만 실행
export function likeFeed(data) {
    let vuIdx = __VU - 1;
    let token = data.tokens[vuIdx];
    let params = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        }
    };

    // 상세 조회로 좋아요 상태 확인
    let getRes = http.get(`${BASE_URL}/feeds/${FEED_ID}`, params);
    let isLiked = false;
    if (getRes.status === 200) {
        try {
            let body = JSON.parse(getRes.body);
            isLiked = body.data.isLiked;
        } catch (e) {
            console.error(`[VU${__VU}] 상세조회 파싱 오류:`, getRes.body);
        }
    }

    // 상태 반대로 좋아요 또는 취소 요청
    let payload = JSON.stringify({ type: !isLiked });
    let res = http.post(`${BASE_URL}/feeds/${FEED_ID}/likes`, payload, params);

    check(res, {
        'feed like 200': (r) => r.status === 200,
        'feed like status 400': (r) => r.status === 400,
        'feed like Internal server error': (r) => r.status === 500,
    });

    if (res.status !== 200) {
        console.error(`[VU${__VU}] ERROR status=${res.status} body=${res.body}`);
    }

    sleep(Math.random() + 0.5); // 0.5~1.5초 랜덤 대기
}

// 테스트 결과 html 리포트로 저장
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
export function handleSummary(data) {
    return {
        "summary.html": htmlReport(data),
    };
}