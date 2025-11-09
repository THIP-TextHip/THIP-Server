import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const BASE_URL      = 'http://localhost:8080';
const ROOM_ID       = 12345;
const USERS_START   = 10000;   // 토큰 발급 시작 userId
const USERS_COUNT   = 500;     // 총 사용자 = VU 수
const TOKEN_BATCH   = 200;     // 토큰 발급 배치 크기
const BATCH_PAUSE_S = 0.2;     // 배치 간 대기 (for 토큰 발급 API 병목 방지)
const START_DELAY_S = 5;       // 테스트 시작 전 대기 (for 방 참여 요청 동시 시작)

// ===== 커스텀 메트릭 =====
const joinLatency = new Trend('rooms_join_latency'); // 참여 API 지연(ms)
const http5xx     = new Counter('rooms_join_5xx');   // 5xx 개수
const http2xx     = new Counter('rooms_join_2xx');   // 2xx 개수
const http4xx     = new Counter('rooms_join_4xx');   // 4xx 개수

// 실패 원인 분포 파악용(응답 JSON의 code 필드 기준)
const token_issue_failed                = new Counter('token_issue_failed');
const fail_ROOM_MEMBER_COUNT_EXCEEDED   = new Counter('fail_ROOM_MEMBER_COUNT_EXCEEDED');
const fail_USER_ALREADY_PARTICIPATE     = new Counter('fail_USER_ALREADY_PARTICIPATE');
const fail_RESOURCE_LOCKED              = new Counter('fail_RESOURCE_LOCKED');  // 423 Locked error
const fail_OTHER_4XX                    = new Counter('fail_OTHER_4XX');

const ERR = {   // THIP error code
  ROOM_MEMBER_COUNT_EXCEEDED: 100006,
  USER_ALREADY_PARTICIPATE: 140005,
  RESOURCE_LOCKED: 50200,
};

function parseError(res) {
  try {
    const j = JSON.parse(res.body || '{}'); // BaseResponse 구조
    // BaseResponse: { isSuccess:boolean, code:number, message:string, requestId:string, data:any }
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
// [인기 작가가 만든 모임방에 THIP의 수많은 유저들이 '모임방 참여' 요청을 보내는 상황 가정]
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
    rooms_join_5xx:     ['count==0'],     // 서버 오류는 0건이어야 함
    rooms_join_latency: ['p(95)<1000'],   // p95 < 1s
  },
};

// setup: 토큰 배치 발급
// roomId 12345 방 & userId 10000 ~ 유저들은 사전에 만들어져 있어야 함
export function setup() {
  const userIds = Array.from({ length: USERS_COUNT }, (_, i) => USERS_START + i);
  const tokens = [];

  for (let i = 0; i < userIds.length; i += TOKEN_BATCH) {
    const slice = userIds.slice(i, i + TOKEN_BATCH);
    const reqs = slice.map((uid) => [
      'GET',
      `${BASE_URL}/api/test/token/access?userId=${uid}`,
      null,
      { tags: { phase: 'setup_token_issue', room: `${ROOM_ID}` } },
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
  const idx = __VU - 1;                 // VU <-> user 매핑(1:1)
  const token = data.tokens[idx];

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

  const body = JSON.stringify({ type: 'join' });
  const url = `${BASE_URL}/rooms/${ROOM_ID}/join`;

  const res = http.post(url, body, { headers, tags: { phase: 'join', room: `${ROOM_ID}` } });

  // === 커스텀 메트릭 기록 ===
  joinLatency.add(res.timings.duration);
  if (res.status >= 200 && res.status < 300) http2xx.add(1);
  else if (res.status >= 400 && res.status < 500) {
    http4xx.add(1);
    const err = parseError(res);
    switch (err.code) {
      case ERR.ROOM_MEMBER_COUNT_EXCEEDED:
        fail_ROOM_MEMBER_COUNT_EXCEEDED.add(1);
        break;
      case ERR.USER_ALREADY_PARTICIPATE:
        fail_USER_ALREADY_PARTICIPATE.add(1);
        break;
      case ERR.RESOURCE_LOCKED:
        fail_RESOURCE_LOCKED.add(1);
        break;
      default:
        fail_OTHER_4XX.add(1);
    }
  } else if (res.status >= 500) {
    http5xx.add(1);
  }

  // === 검증 ===
  check(res, {
    'join responded': (r) => r.status !== 0,
    'join 200 or expected 4xx': (r) => r.status === 200 || (r.status >= 400 && r.status < 500),
  });
}
