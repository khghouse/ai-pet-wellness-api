# 테스트 규칙

## 일반 원칙

- 기본 작성 대상은 Controller 슬라이스 테스트, Service 통합 테스트, Repository 슬라이스 테스트다.
- Service 레이어는 `IntegrationTestSupport`를 상속하여 실제 DB와 연동한 테스트를 작성한다.
- `domain/.../service/*Test`는 Service와 Repository를 함께 사용하는 통합 테스트를 작성한다.
- `integration/*Test`는 사용자가 명시적으로 요청한 경우에만 작성한다.
- 사용자가 통합 테스트를 요청한 경우 `MockMvc` 기반으로 HTTP 요청부터 Controller, Service, Repository까지 관통하는 전체 플로우를 검증한다.
- API 인수 시나리오 테스트는 핵심 사용자 흐름을 실제 HTTP 서버 기준으로 검증할 때 작성한다.
- Mock 기반 단위 테스트는 기본 선택지가 아니다.
- 테스트 픽스처는 별도 `fixture` 패키지 또는 `TestFixture` 클래스로 분리한다.
- 하나의 테스트 메서드에서는 하나의 동작만 검증한다.
- Controller 테스트의 정상 요청, 형식 오류, 정책 오류는 Request DTO와 `ObjectMapper`를 사용해 JSON을 생성한다.
- Spring Boot 4의 Jackson 3 환경에서는 `tools.jackson.databind.ObjectMapper`를 사용한다.
- JSON 문법 오류나 필드 누락 자체를 검증하는 테스트는 문자열 JSON을 사용한다.
- @DisplayName은 메서드명을 반복하지 않고 한글 문장으로 테스트 의도를 설명한다. 형식은 어떤 상황에서 어떤 결과를 반환한다 또는 어떤 조건이면 어떤 예외/응답이 발생한다를 권장한다.

```java
@DisplayName("RequestBody 검증에 실패하면 INVALID_INPUT_VALUE 응답을 반환한다")
@Test
void requestBodyValidation_blankName_throwsInvalidInputValue() throws Exception { }
```

## 테스트 패키지 구조

```text
src/test/java/{패키지 경로}/{프로젝트명}/
├── support/
│   ├── ControllerTestSupport.java
│   ├── IntegrationTestSupport.java
│   └── RepositoryTestSupport.java
├── architecture/
│   └── ArchitectureRuleTest.java
├── domain/
│   └── {도메인명}/
│       ├── controller/
│       │   └── {도메인명}ControllerTest.java
│       ├── service/
│       │   └── {도메인명}ServiceTest.java
│       ├── repository/
│       │   └── {도메인명}RepositoryTest.java
│       └── entity/
│           └── {도메인명}Test.java
└── integration/
    └── {도메인명}IntegrationTest.java  # 명시 요청 시에만 작성
└── acceptance/
    └── {시나리오명}AcceptanceTest.java
```

## API 인수 시나리오 테스트 기준

- 회원 가입부터 반려견 등록처럼 여러 도메인 API를 연결하는 핵심 사용자 흐름을 검증한다.
- `@SpringBootTest(webEnvironment = RANDOM_PORT)`로 실제 내장 서버를 실행하고 HTTP 클라이언트로 요청한다.
- 외부 인프라는 Testcontainers 또는 테스트 대역을 사용하며, 운영 환경의 외부 서비스에 연결하지 않는다.
- 상세 입력 검증과 개별 예외 분기는 Controller, Service, 통합 테스트에서 검증하고, 인수 시나리오 테스트는 핵심 흐름 중심으로 제한한다.
- 시나리오 문서는 `docs/test-scenarios/SCN-xxx-*.md`에 작성한다.

## 아키텍처 테스트 기준

- `architecture/ArchitectureRuleTest`에서 ArchUnit 기반 패키지 의존 규칙을 검증한다.
- Controller는 Repository를 직접 참조하지 않는다.
- Controller, Service, Repository 레이어 접근 방향을 검증한다.
- Service 간 의존은 허용한다.
- 다른 도메인의 Repository 직접 참조는 금지한다.

## Support 클래스 기준

| 클래스 | 용도 |
|---|---|
| `ControllerTestSupport` | 보안 필터를 제외한 Controller MVC 계약 테스트 |
| `IntegrationTestSupport` | Service + Repository 실제 연동 테스트 및 명시 요청된 MockMvc 기반 전체 플로우 통합 테스트 |
| `RepositoryTestSupport` | JPA / Querydsl Repository 슬라이스 테스트 |

### IntegrationTestSupport

- `@SpringBootTest` + `@ActiveProfiles("test")`
- 부모 클래스에 `@Transactional`을 선언하지 않는다.
- 자식 테스트 클래스에서 `@Transactional`을 명시한다.
- Service 테스트에서는 실제 Service와 Repository 연동을 검증한다.
- `integration` 패키지 테스트는 사용자가 명시적으로 요청한 경우에만 작성한다.
- 명시 요청된 `integration` 패키지 테스트에서는 `@AutoConfigureMockMvc`를 추가하여 HTTP 요청부터 Controller, Service, Repository까지 전체 애플리케이션 플로우를 검증한다.

### RepositoryTestSupport

- `@DataJpaTest` + `@ActiveProfiles("test")`
- Querydsl 사용 시 `@Import(QuerydslConfig.class)` 추가
- `@Import(JpaAuditingConfig.class)`를 반드시 포함한다.

### ControllerTestSupport

- `@WebMvcTest` 사용
- `@AutoConfigureMockMvc(addFilters = false)`로 보안 필터를 제외한다.
- 공통 보안/인프라 설정만 부모 클래스에서 관리한다.
- 도메인 서비스 `@MockBean`은 각 Controller 테스트 클래스에서 직접 선언한다.
- JWT 필터와 인증/인가 경계는 `integration/auth/*Test`에서 전체 애플리케이션 흐름으로 검증한다.

## Redis Testcontainers 기준

- 인증 통합 테스트는 외부 Redis Cloud에 연결하지 않는다.
- Testcontainers로 고정 버전의 Redis 이미지를 실행한다.
- Redis host와 port는 `@DynamicPropertySource`로 테스트 컨텍스트에 주입한다.
- 로컬에서 인증 통합 테스트를 실행하려면 Docker Desktop이 필요하다.
- Docker를 사용할 수 없는 환경에서는 인증 통합 테스트를 건너뛰고, Docker가 제공되는 CI에서 반드시 실행한다.
