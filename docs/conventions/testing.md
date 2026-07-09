# 테스트 규칙

## 일반 원칙

- 기본 작성 대상은 Controller 슬라이스 테스트, Service 통합 테스트, Repository 슬라이스 테스트다.
- Service 레이어는 `IntegrationTestSupport`를 상속하여 실제 DB와 연동한 테스트를 작성한다.
- `domain/.../service/*Test`는 Service와 Repository를 함께 사용하는 통합 테스트를 작성한다.
- `integration/*Test`는 사용자가 명시적으로 요청한 경우에만 작성한다.
- 사용자가 통합 테스트를 요청한 경우 `MockMvc` 기반으로 HTTP 요청부터 Controller, Service, Repository까지 관통하는 전체 플로우를 검증한다.
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
```

## 아키텍처 테스트 기준

- `architecture/ArchitectureRuleTest`에서 ArchUnit 기반 패키지 의존 규칙을 검증한다.
- Controller는 Repository를 직접 참조하지 않는다.
- Controller, Service, Repository 레이어 접근 방향을 검증한다.
- Service 간 의존은 허용한다.
- 다른 도메인의 Repository 직접 참조는 금지한다.

## Support 클래스 기준

| 클래스 | 용도 |
|---|---|
| `ControllerTestSupport` | Spring Security 포함 Controller 슬라이스 테스트 |
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
- 공통 보안/인프라 설정만 부모 클래스에서 관리한다.
- 도메인 서비스 `@MockBean`은 각 Controller 테스트 클래스에서 직접 선언한다.
- 부모 클래스는 `JwtAuthenticationFilter`, `JwtAuthenticationEntryPoint`, `MemberAuthenticationProvider`처럼 여러 Controller 테스트에서 반복되는 보안 의존성만 가진다.
