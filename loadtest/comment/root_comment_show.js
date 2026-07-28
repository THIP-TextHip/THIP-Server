import http from 'k6/http';
import { check } from 'k6';

// --- 환경 설정 ---
const BASE_URL = 'http://localhost:8080';
const TARGET_POST_ID = 1;
const POST_TYPE = 'FEED';

export const options = {
  // --- 시나리오 설정 (Constant Arrival Rate) ---
  scenarios: {
    // 1단계: 초당 50 요청 (Warm-up)
    warm_up: {
      executor: 'constant-arrival-rate',
      rate: 50,
      timeUnit: '1s',
      duration: '30s',
      preAllocatedVUs: 10,
      maxVUs: 50,
    },
    // 2단계: 초당 150 요청 (Target Load)
    load_test: {
      executor: 'constant-arrival-rate',
      rate: 150,
      timeUnit: '1s',
      duration: '1m',
      startTime: '30s',
      preAllocatedVUs: 30,
      maxVUs: 100,
    },
    // 3단계: 초당 300 요청 (Stress Test)
    stress_test: {
      executor: 'constant-arrival-rate',
      rate: 300,
      timeUnit: '1s',
      duration: '30s',
      startTime: '1m30s',
      preAllocatedVUs: 50,
      maxVUs: 200,
    },
  },

  // --- [핵심 수정] Thresholds 타겟팅 ---
  // setup() 단계의 요청은 무시하고, 'root_comment_show' 태그가 있는 요청만 평가합니다.
  thresholds: {
    // 1. 응답 시간: 해당 태그 요청의 95%가 50ms 이내여야 함
    'http_req_duration{name:root_comment_show}': ['p(95)<50'],

    // 2. 에러율: 해당 태그 요청의 실패율이 1% 미만이어야 함
    'http_req_failed{name:root_comment_show}': ['rate<0.01'],
  },
};

// --- Setup: 토큰 발급 (성능 측정 제외 대상) ---
export function setup() {
  const MAX_SETUP_VUS = 200;
  console.log(`🚀 토큰 ${MAX_SETUP_VUS}개 발급 시작...`);
  const tokens = [];

  for (let userId = 1; userId <= MAX_SETUP_VUS; userId++) {
    // *주의* 여기에는 tags를 붙이지 않습니다. 따라서 thresholds 평가에서 자동 제외됩니다.
    const res = http.get(`${BASE_URL}/api/test/token/access?userId=${userId}`);
    if (res.status === 200 && res.body.length > 0) {
      tokens.push(res.body);
    }
  }
  console.log(`✅ 토큰 ${tokens.length}개 발급 완료`);
  return { tokens };
}

// --- Main Logic: 루트 댓글 조회 (성능 측정 대상) ---
export default function (data) {
  // 토큰 랜덤 선택
  const token = data.tokens[Math.floor(Math.random() * data.tokens.length)];

  const params = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    // [중요] 이 태그(name)를 기준으로 thresholds가 작동합니다.
    tags: { name: 'root_comment_show' },
  };

  const res = http.get(`${BASE_URL}/comments/${TARGET_POST_ID}?postType=${POST_TYPE}`, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
  });
}