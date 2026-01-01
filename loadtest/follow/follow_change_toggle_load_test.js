import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const BASE_URL        = 'http://localhost:8080';
const TARGET_USER_ID  = 1;          // 팔로우/언팔 대상 사용자
const USERS_START     = 10000;      // 토큰 발급 시작 userId
const USERS_COUNT     = 500;        // 동시 사용자 수 = VU 수
const TOKEN_BATCH     = 200;        // 토큰 발급 배치 크기
const BATCH_PAUSE_S   = 0.2;        // 배치 간 휴지 (발급 API 보호)
const START_DELAY_S   = 5;          // 전체 동기 시작 대기
const TOGGLE_ITER     = 20;         // 각 VU가 시도할 토글 횟수(팔로우/언팔 번갈아 최대 20회)
const TOGGLE_PAUSE_S  = 0.1;        // 한 번 호출 후 다음 토글까지 간격

const followLatency = new Trend('follow_change_latency');
const http5xx       = new Counter('follow_change_5xx');
const http2xx       = new Counter('follow_change_2xx');
const http4xx       = new Counter('follow_change_4xx');

const token_issue_failed            = new Counter('token_issue_failed');
const fail_USER_ALREADY_FOLLOWED    = new Counter('fail_USER_ALREADY_FOLLOWED');
const fail_USER_ALREADY_UNFOLLOWED  = new Counter('fail_USER_ALREADY_UNFOLLOWED');
const fail_USER_CANNOT_FOLLOW_SELF  = new Counter('fail_USER_CANNOT_FOLLOW_SELF');
const fail_OTHER_4XX                = new Counter('fail_OTHER_4XX');

const success_follow_200            = new Counter('success_follow_200');
const success_unfollow_200          = new Counter('success_unfollow_200');

const ERR = {
    USER_ALREADY_FOLLOWED:   70001,
    USER_ALREADY_UNFOLLOWED: 75001,
    USER_CANNOT_FOLLOW_SELF: 75002,
};

function parseError(res) {
    try {
        const j = JSON.parse(res.body || '{}');
        return {
            code: Number(j.code),
            message: j.message || '',
            requestId: j.requestId || '',
            isSuccess: !!j.isSuccess,
        };
    } catch (_) {
        return { code: NaN, message: '', requestId: '', isSuccess: false };
    }
}

/** ===== 시나리오 ===== */
export const options = {
    scenarios: {
        follow_toggle_spam: {
            executor: 'per-vu-iterations',
            vus: USERS_COUNT,
            iterations: TOGGLE_ITER,     // 각 VU가 TOGGLE_ITER번 default() 실행
            startTime: '0s',
            gracefulStop: '5s',
        },
    },
    thresholds: {
        follow_change_5xx:     ['count==0'],
        follow_change_latency: ['p(95)<1000'],
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
            if (r.status === 200 && r.body) tokens.push(r.body.trim());
            else {
                tokens.push('');
                token_issue_failed.add(1);
            }
        }
        sleep(BATCH_PAUSE_S);
    }
    if (tokens.length > USERS_COUNT) tokens.length = USERS_COUNT;

    const startAt = Date.now() + START_DELAY_S * 1000;
    return { tokens, startAt };
}

/** ===== 각 VU의 토글 상태 =====
 * false = 아직 팔로우 안 한 상태로 가정 → 다음 요청은 follow(type:true)
 * true  = 이미 팔로우 한 상태로 가정   → 다음 요청은 unfollow(type:false)
 */
let isFollowing = false;

/** ===== 실행 루프 ===== */
export default function (data) {
    const idx   = __VU - 1;
    const token = data.tokens[idx];

    // 동기 시작
    const now = Date.now();
    if (now < data.startAt) {
        sleep((data.startAt - now) / 1000);
    }

    if (!token) return;

    const headers = {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
    };

    // 현재 상태에 따라 요청 결정
    // 규칙: "이전 요청이 200일 때만" 상태를 뒤집는다 → 4xx/5xx면 상태 유지(같은 동작 재시도)
    const wantFollow = !isFollowing; // 현재 false면 follow, true면 unfollow
    const body = JSON.stringify({ type: wantFollow });
    const url  = `${BASE_URL}/users/following/${TARGET_USER_ID}`;

    const res = http.post(url, body, { headers, tags: { phase: 'follow_change', target: `${TARGET_USER_ID}`, want: wantFollow ? 'follow' : 'unfollow' } });

    followLatency.add(res.timings.duration);
    if (res.status >= 200 && res.status < 300) {
        http2xx.add(1);
        // 200일 때만 상태 반전
        isFollowing = !isFollowing;
        if (wantFollow) success_follow_200.add(1);
        else success_unfollow_200.add(1);
    } else if (res.status >= 400 && res.status < 500) {
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
    } else if (res.status >= 500) {
        http5xx.add(1);
    }

    check(res, {
        'follow_change responded': (r) => r.status !== 0,
        'follow_change 200 or expected 4xx': (r) =>
            r.status === 200 || (r.status >= 400 && r.status < 500),
    });

    // 토글 간격
    sleep(TOGGLE_PAUSE_S);
}