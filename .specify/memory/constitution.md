<!--
Sync Impact Report
==================
Version change: (uninitialized template) → 1.0.0
Bump rationale: MAJOR — 템플릿 placeholder를 처음으로 구체 원칙/규약으로 확정. 이전 버전과의 호환성 개념이 없는 초기 비준.

Modified principles:
- [PRINCIPLE_1_NAME] → I. 계층형 아키텍처 (Layered Architecture)
- [PRINCIPLE_2_NAME] → II. 커버리지 게이트 테스트 (Coverage-Gated Testing)
- [PRINCIPLE_3_NAME] → III. API 계약 안정성 (API Contract Stability)
- [PRINCIPLE_4_NAME] → IV. 보안 우선 (Security First)
- [PRINCIPLE_5_NAME] → V. 관찰 가능성 (Observability)
- (신규) → VI. 성능 가드 (Performance Discipline)

Added sections:
- Technology Constraints (기술 스택/도구 제약)
- Development Workflow (개발/리뷰 워크플로우)
- Governance (거버넌스)

Removed sections: 없음 (초기 비준)

Templates requiring updates:
- ✅ .specify/templates/plan-template.md (Constitution Check 자리표시자 호환, 추가 변경 불필요)
- ✅ .specify/templates/spec-template.md (호환)
- ✅ .specify/templates/tasks-template.md (호환)
- ✅ .specify/templates/checklist-template.md (호환)

Deferred items (TODO):
- TODO(JACOCO_THRESHOLD): JaCoCo INSTRUCTION/BRANCH 커버리지 최소값 미정. 결정 시 jacoco.gradle violationRules와 본 문서 동시 갱신.
- TODO(K6_THRESHOLDS): k6 부하 임계치(예: p95 응답시간, 에러율, 최소 RPS) 도메인별 미정. 결정 시 loadtest/*.js 및 본 문서 동시 갱신.
-->

# THIP-Server Constitution

본 문서는 THIP-Server 프로젝트의 불변 원칙과 운영 규약을 정의한다. 본 문서는 다른 모든 관행·문서·합의에 우선하며, 모든 변경 사항(PR, 설계, 구현, 리뷰)은 본 문서와의 정합성을 검증해야 한다.

## Core Principles

### I. 계층형 아키텍처 (Layered Architecture)

Spring 표준 계층(Controller → Service → Repository) 분리를 유지한다. 다음을 MUST 한다.

- Controller는 HTTP/요청 검증/응답 매핑만 담당하며 비즈니스 로직을 포함하지 않는다.
- 도메인 로직은 Service 계층에 위치하며, 영속성 접근은 Repository를 통해서만 한다.
- 도메인 간 직접 의존을 줄이고, 횡단 관심사는 AOP·공통 모듈로 분리한다.
- DTO/Request/Response 계열은 외부 경계(Controller, 외부 API)에서만 사용하고, Service 계층은 도메인 객체를 다룬다.
- Querydsl, MapStruct 등 생성 코드는 도메인 책임을 침범하지 않는 범위에서만 사용한다.

**근거:** THIP은 backend 3인이 병렬 작업하는 도메인 다수(feed/follow/room/book 등)를 가진다. 책임 경계가 흐려지면 회귀 비용이 급격히 늘어난다.

### II. 커버리지 게이트 테스트 (Coverage-Gated Testing)

TDD는 권장이며 강제하지 않는다. 대신 자동화된 커버리지 게이트로 품질을 보증한다.

- 모든 Service 계층은 단위 또는 슬라이스 테스트를 가진다.
- 핵심 사용자 경로(인증, 핵심 도메인 read/write)는 통합 테스트로 검증한다.
- JaCoCo 커버리지 검증(`jacocoTestCoverageVerification`)이 CI의 `check` 단계에 연결되어 있으며, 임계치 미달 시 빌드를 실패시킨다. (TODO(JACOCO_THRESHOLD): 구체 수치 결정 필요)
- 동시성·경합 시나리오 테스트는 `@Tag('concurrency')`로 분리하고, CI 환경에서는 제외한다(로컬/스케줄 잡으로 실행).
- 생성 코드(`generated/`, `Q*.java`), Config, DTO/Request/Response, Application 부트스트랩은 커버리지 산정에서 제외한다(현행 jacoco.gradle와 일치).

**근거:** Test-First를 일률 강제하면 속도가 줄지만 커버리지 게이트만 강제하면 작성 시점은 유연하면서도 품질 회귀를 막을 수 있다.

### III. API 계약 안정성 (API Contract Stability)

외부 노출 API는 명시적 계약으로 다룬다.

- 모든 REST API는 공통 응답 포맷을 따르며, 신규 응답 타입을 임의로 추가하지 않는다.
- 에러 코드는 전역 유일성을 가진다. 중복 에러 코드 신설은 MUST NOT (참고: 과거 PR #336에서 중복 에러 코드 제거 이력 있음).
- 모든 공개 엔드포인트는 OpenAPI(`springdoc-openapi`) 스펙에 자동 노출되어야 하며, 응답/요청 모델은 Swagger UI에서 검증 가능해야 한다.
- Breaking change(URL, 메서드, 응답 필드 제거/타입 변경)는 PR 설명에 호환성 영향과 클라이언트 영향 범위를 명시해야 한다.

**근거:** 백엔드와 별도의 클라이언트 팀(웹/앱)이 존재. 계약이 흔들리면 모든 다운스트림이 영향을 받는다.

### IV. 보안 우선 (Security First)

보안은 추가 기능이 아니라 기본 조건이다.

- 인증은 JWT(`jjwt`) + OAuth2(`spring-security-oauth2-client`)를 통해서만 한다. 자체 세션 도입 금지.
- 시크릿(application.yml, Firebase 키, AWS 자격증명 등)은 레포에 커밋하지 않으며, 운영 환경 변수 또는 GitHub Secrets로 주입한다(현행 CI 워크플로 준수).
- Spring Security 설정에서 인증(Authentication)과 인가(Authorization)는 명시적으로 분리되어야 하며, 새 엔드포인트는 기본적으로 보호되어야 한다(`permitAll`은 명시적 의도일 때만).
- 외부 API 호출(Firebase, OpenAI 등)에 사용되는 키와 토큰은 코드에 하드코딩 금지.

**근거:** 사용자 데이터(독서 기록, 감상)를 보관하는 서비스. 사고 시 신뢰 회복 비용이 매우 크다.

### V. 관찰 가능성 (Observability)

운영 중인 시스템은 항상 관찰 가능해야 한다.

- 모든 서비스는 Spring Boot Actuator를 노출하며, 핵심 지표는 Prometheus 포맷(`micrometer-registry-prometheus`)으로 수집된다.
- 애플리케이션 로그는 Logstash Logback Encoder를 통해 구조화 로그(JSON)로 출력한다.
- 핵심 사용자 경로(인증, 결제·외부 호출 등)의 진입/오류는 식별 가능한 trace context와 함께 기록되어야 한다.
- 새로운 외부 통신(WebFlux, REST, Spring Retry 등)을 추가할 때는 성공/실패/지연 메트릭을 함께 노출한다.

**근거:** 부하 테스트와 운영 모니터링이 모두 메트릭 기반. 보이지 않는 것은 고칠 수 없다.

### VI. 성능 가드 (Performance Discipline)

성능은 기능이며, 회귀를 코드로 막는다.

- 핵심 도메인(현재 feed, follow, room)은 k6 부하 시나리오를 `loadtest/` 하위에 유지한다.
- 핵심 도메인을 수정하는 PR은 관련 k6 시나리오 실행 결과 또는 결과가 불필요한 사유를 PR 본문에 명시한다.
- 도메인별 부하 임계치(p95 응답시간, 에러율, 최소 RPS)는 별도 정의하며, 임계치 미달 시 머지 가능 여부는 리뷰어 합의로 결정한다. (TODO(K6_THRESHOLDS): 도메인별 구체 임계치 정의 필요)
- 동시성 회귀를 막기 위한 시나리오(예: `feed_like_concurrency_test*.js`)는 폐기하지 않으며, 동등 시나리오로만 교체한다.

**근거:** 좋아요·피드·팔로우 같은 핫경로는 작은 회귀가 큰 영향을 만든다. 사후 발견 대신 사전 가드가 비용 효율적.

## Technology Constraints

본 프로젝트는 다음 스택을 기준으로 한다. 스택 추가/변경은 거버넌스 절차를 따른다.

- 언어/런타임: Java 17 (Gradle toolchain)
- 프레임워크: Spring Boot 3.5.x (Web, WebFlux, Data JPA, Data Redis, Security, Validation, Actuator, AOP, Retry)
- 영속성: MySQL(운영) + H2(테스트), Flyway 마이그레이션, Querydsl 5.0.0
- 캐시/메시징: Redis
- 보조: Lombok, MapStruct 1.5.5, Google Guava, springdoc-openapi 2.8.8
- 보안: Spring Security, jjwt 0.12.3, OAuth2 Client
- 외부 연동: AWS S3(spring-cloud-aws), Firebase Admin 9.3.0, Spring AI(OpenAI starter)
- 모니터링/로깅: Micrometer Prometheus, Logstash Logback Encoder 7.4
- 빌드/CI: Gradle 8.x, GitHub Actions (`.github/workflows/ci-workflow.yml`), JaCoCo 0.8.12
- 부하: k6 (`loadtest/`)

신규 의존성 추가는 PR에서 도입 이유, 대안 비교, 라이선스 확인을 본문에 포함해야 한다.

## Development Workflow

- 메인 브랜치: `develop`. 운영 릴리스 브랜치 정책은 본 문서가 다루지 않는다.
- 모든 변경은 PR로 머지하며, 머지 조건은 **리뷰어 1인 이상 승인 + CI green** 이다.
- CI(`ci-workflow.yml`)는 `./gradlew build`를 통과해야 하며, JaCoCo coverage verification이 포함된 `check` 태스크가 동반된다.
- 동시성 테스트(`@Tag('concurrency')`)는 CI에서 제외되며, 별도 절차(로컬 실행 또는 스케줄 잡)에서 검증한다.
- 본 문서를 변경하는 PR은 본 문서의 "Governance" 절차를 따른다(아래).
- 새로운 spec/feature 작업은 Spec Kit 워크플로우(`/speckit-specify` → `/speckit-clarify`(선택) → `/speckit-plan` → `/speckit-tasks` → `/speckit-implement`)를 따른다.

## Governance

- 본 문서는 다른 모든 관행·합의·문서에 우선한다.
- 변경(개정·삭제·신설)은 PR로 제안하며, 백엔드 멤버 **3명 전원 승인** 시 머지된다.
- 모든 변경 PR은 다음 항목을 본문에 포함해야 한다.
  1. 변경 요약과 동기
  2. 영향 받는 원칙·섹션
  3. 다른 산출물(plan/spec/tasks 템플릿, 가이드 문서, jacoco.gradle, loadtest 등) 영향 범위
- 버전 정책은 시맨틱 버저닝(MAJOR.MINOR.PATCH)을 따른다.
  - **MAJOR**: 기존 원칙의 호환성 깨는 변경(원칙 삭제·재정의·정반대 전환)
  - **MINOR**: 새 원칙/섹션 추가 또는 가이드의 실질적 확장
  - **PATCH**: 문구 수정, 오탈자, 비의미적 정리
- 본 문서와 충돌하는 코드 변경은 머지 불가. 예외가 필요한 경우 Complexity Tracking(`plan-template.md`)에 정당화 사유를 기록한다.
- 본 문서의 `TODO(...)` 항목은 만료 시한 없이 보류하지 않으며, 결정되는 즉시 다음 PATCH/MINOR 개정에 반영한다.

**Version**: 1.0.0 | **Ratified**: 2026-05-17 | **Last Amended**: 2026-05-17
