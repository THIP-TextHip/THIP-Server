import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const BASE_URL      = 'http://localhost:8080';
const TARGET_USER_ID= 1;        // 팔로우/언팔 대상 사용자 ID
const USERS_START   = 10000;    // 토큰 발급 시작 userId
const USERS_COUNT   = 500;      // 총 사용자 = VU 수
const TOKEN_BATCH   = 200;      // 토큰 발급 배치 크기
const BATCH_PAUSE_S = 0.2;      // 배치 간 대기 (for 토큰 발급 API 병목 방지)
const START_DELAY_S = 5;        // 테스트 시작 전 대기 (for 동시 시작)

const followLatency = new Trend('follow_change_latency'); // API 지연(ms) - 네이밍 포맷 유지
const http5xx     = new Counter('follow_change_5xx');   // 5xx 개수
const http2xx     = new Counter('follow_change_2xx');   // 2xx 개수
const http4xx     = new Counter('follow_change_4xx');   // 4xx 개수

const token_issue_failed              = new Counter('token_issue_failed');
const fail_USER_ALREADY_FOLLOWED      = new Counter('fail_USER_ALREADY_FOLLOWED');
const fail_USER_ALREADY_UNFOLLOWED    = new Counter('fail_USER_ALREADY_UNFOLLOWED');
const fail_USER_CANNOT_FOLLOW_SELF    = new Counter('fail_USER_CANNOT_FOLLOW_SELF');
const fail_OTHER_4XX                  = new Counter('fail_OTHER_4XX');

const ERR = {
    USER_ALREADY_FOLLOWED:    70001,
    USER_ALREADY_UNFOLLOWED:  75001,
    USER_CANNOT_FOLLOW_SELF:  75002,
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
// [다수 유저가 동일 타깃(TARGET_USER_ID)에게 '팔로우' 요청을 동시에 보내는 상황]
export const options = {
    scenarios: {
        // 각 VU가 "정확히 1회" 실행 → 1 VU = 1명 유저
        join_once_burst: {
            executor: 'per-vu-iterations',
            vus: USERS_COUNT,
            iterations: 1,
            startTime: '0s',         // 모든 VU가 거의 동시에 스케줄링
            gracefulStop: '5s',
        },
    },
    thresholds: {
        follow_change_5xx:     ['count==0'],     // 서버 오류는 0건이어야 함
        follow_change_latency: ['p(95)<1000'],   // p95 < 1s
    },
};

/** ===== setup: 토큰 일괄 발급 ===== */
export function setup() {
    const userIds = Array.from({ length: USERS_COUNT }, (_, i) => USERS_START + i);
    const tokens = [];

    for (let i = 0; i < userIds.length; i += TOKEN_BATCH) {
        const slice = userIds.slice(i, i + TOKEN_BATCH);
        const reqs = slice.map((uid) => [
            'GET',
            `${BASE_URL}/api/test/token/access?userId=${uid}`,
            null,
            { tags: { phase: 'setup_token_issue', target: `${TARGET_USER_ID}` } },
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

// VU : 각자 자기 토큰으로 팔로우 호출 & 각자 1회만 실행
export default function (data) {
    const idx = __VU - 1;                 // VU <-> user 매핑(1:1)
    const token = data.tokens[idx];

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

    const body = JSON.stringify({ type: true });
    const url = `${BASE_URL}/users/following/${TARGET_USER_ID}`;

    const res = http.post(url, body, { headers, tags: { phase: 'follow_change', target: `${TARGET_USER_ID}` } });

    followLatency.add(res.timings.duration);
    if (res.status >= 200 && res.status < 300) {
        http2xx.add(1);
    }
    else if (res.status >= 400 && res.status < 500) {
        http4xx.add(1);
        const err = parseError(res);
        switch (err.code) {
            case ERR.USER_ALREADY_FOLLOWED:
                fail_USER_ALREADY_FOLLOWED.add(1);
                break;
            case ERR.USER_ALREADY_UNFOLLOWED:
                fail_USER_ALREADY_UNFOLLOWED.add(1);
                break;
            case ERR.USER_CANNOT_FOLLOW_SELF:
                fail_USER_CANNOT_FOLLOW_SELF.add(1);
                break;
            default:
                fail_OTHER_4XX.add(1);
        }
    }
    else if (res.status >= 500) {
        http5xx.add(1);
    }

    check(res, {
        'follow_change responded': (r) => r.status !== 0,
        // 이미 팔로우 상태에서 재요청 등 합리적 4xx 허용
        'follow_change 200 or expected 4xx': (r) => r.status === 200 || (r.status >= 400 && r.status < 500),
    });
}