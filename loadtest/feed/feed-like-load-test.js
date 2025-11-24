// feed-like-load-test.js
import http from 'k6/http';
import { sleep,check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const FEED_ID = 1; // 테스트할 피드 ID
const USERS_START   = 1;   // 토큰 발급 시작 userId
const USERS_COUNT   = 5000;     // 총 사용자 = VU 수
const TOKEN_BATCH   = 200;     // 토큰 발급 배치 크기
const BATCH_PAUSE_S = 0.2;     // 배치 간 대기 (for 토큰 발급 API 병목 방지)
const START_DELAY_S = 5;       // 테스트 시작 전 대기 (for 방 참여 요청 동시 시작)

// ===== 커스텀 메트릭 =====
const likeLatency = new Trend('feed_like_latency'); // 참여 API 지연(ms)
const http5xx     = new Counter('feed_like_5xx');   // 5xx 개수
const http2xx     = new Counter('feed_like_2xx');   // 2xx 개수
const http4xx     = new Counter('feed_like_4xx');   // 4xx 개수

// 실패 원인 분포 파악용(응답 JSON의 code 필드 기준)
const token_issue_failed = new Counter('token_issue_failed');
const fail_POST_ALREADY_LIKED = new Counter('fail_POST_ALREADY_LIKED');
const fail_POST_NOT_LIKED_CANNOT_CANCEL = new Counter('fail_POST_NOT_LIKED_CANNOT_CANCEL');
const fail_POST_LIKE_COUNT_UNDERFLOW     = new Counter('fail_POST_LIKE_COUNT_UNDERFLOW');
const fail_OTHER_4XX                    = new Counter('fail_OTHER_4XX');

const ERR = {   // THIP error code
    POST_ALREADY_LIKED: 185001,
    POST_NOT_LIKED_CANNOT_CANCEL: 185002,
    POST_LIKE_COUNT_UNDERFLOW: 185000
};

function parseError(res) {
    try {
        const j = JSON.parse(res.body || '{}'); // BaseResponse 구조
        return {
            code: Number(j.code),              // 정수 코드
            message: j.message || '',
            requestId: j.requestId || '',
            isSuccess: !!j.isSuccess
        };
    } catch (e) {
        return { code: NaN, message: '', requestId: '', isSuccess: false };
    }
}

// ------------ 시나리오 ------------
// 특정 시점에 한 게시물 (인기 작가,인플루언서가 작성한)에 좋아요 요청이 몰리는 상황 가정
export let options = {
    scenarios: {
        // 각 VU가 "정확히 1회" 실행 → 1 VU = 1명 유저
        feed_like_once: {
            executor: 'per-vu-iterations',
            vus: USERS_COUNT,
            iterations: 1,
            startTime: '0s',         // 모든 VU가 거의 동시에 스케줄링
            gracefulStop: '5s',
        },
    },
    thresholds: {
        feed_like_5xx:     ['count==0'],     // 서버 오류는 0건이어야 함
        feed_like_latency: ['p(95)<500'],   // p95 < 500ms
    },
};

// 테스트 전 사용자 별 토큰 배치 발급
export function setup() {
    const userIds = Array.from({ length: USERS_COUNT }, (_, i) => USERS_START + i);
    const tokens = [];

    for (let i = 0; i < userIds.length; i += TOKEN_BATCH) {
        const slice = userIds.slice(i, i + TOKEN_BATCH);
        const reqs = slice.map((uid) => [
            'GET',
            `${BASE_URL}/api/test/token/access?userId=${uid}`,
            null,
            { tags: { phase: 'setup_token_issue', feed: `${FEED_ID}` } },
        ]);

        const responses = http.batch(reqs);
        for (const r of responses) {
            if (r.status === 200 && r.body) {
                tokens.push(r.body.trim());
            }
            else {
                tokens.push(''); // 실패한 자리도 인덱스 유지
                token_issue_failed.add(1);
            }
        }
        sleep(BATCH_PAUSE_S);
    }
    if (tokens.length > USERS_COUNT) tokens.length = USERS_COUNT;

    const startAt = Date.now() + START_DELAY_S * 1000;    // 동시 시작 시간

    return { tokens, startAt };
}

// VU : 각자 자기 토큰으로 참여 호출 & 각자 1회만 실행
export default function (data) {
    const vuIdx = __VU - 1;
    const token = data.tokens[vuIdx];

    // 동기 시작: startAt까지 대기 → 모든 VU가 거의 같은 타이밍에 시작
    const now = Date.now();
    if (now < data.startAt) {
        sleep((data.startAt - now) / 1000);
    }

    if (!token) {     // 토큰 발급 실패 -> 스킵
        return;
    }

    const headers = {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
    };

    // 동시에 모든 유저가 인기 게시물에 대해 좋아요 요청
    const body = JSON.stringify({ type: 'true' });
    const url = `${BASE_URL}/feeds/${FEED_ID}/likes`;

    const res = http.post(url, body, { headers, tags: { phase: 'like', feed: `${FEED_ID}` } });

    // === 커스텀 메트릭 기록 ===
    likeLatency.add(res.timings.duration);
    if (res.status >= 200 && res.status < 300) http2xx.add(1);
    else if (res.status >= 400 && res.status < 500) {
        http4xx.add(1);
        const err = parseError(res);
        switch (err.code) {
            case ERR.POST_ALREADY_LIKED:
                fail_POST_ALREADY_LIKED.add(1);
                break;
            case ERR.POST_NOT_LIKED_CANNOT_CANCEL:
                fail_POST_NOT_LIKED_CANNOT_CANCEL.add(1);
                break;
            case ERR.POST_LIKE_COUNT_UNDERFLOW:
                fail_POST_LIKE_COUNT_UNDERFLOW.add(1);
                break;
            default:
                fail_OTHER_4XX.add(1);
        }
    } else if (res.status >= 500) {
        http5xx.add(1);
    }

    // === 검증 ===
    check(res, {
        'like responded': (r) => r.status !== 0,
        'like 200 or expected 4xx': (r) => r.status === 200 || (r.status >= 400 && r.status < 500),
    });
}

// 테스트 결과 html 리포트로 저장
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
export function handleSummary(data) {
    return {
        "summary.html": htmlReport(data),
    };
}
