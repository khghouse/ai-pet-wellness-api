# 진행 기록

> 완료된 작업과 검증 결과를 세션 간 이어받기 위해 기록한다.
> 다음 작업은 `tasks.md`, 후보 이슈는 `backlog.md`, 설계 결정은 `docs/adr/`를 기준으로 한다.

## 작성 규칙

`progress.md`는 다음 작업 목록을 관리하지 않는다.
완료된 작업, 검증 결과, 다음 세션에 필요한 인수인계만 기록한다.

### 작성 형식

```md
## YYYY-MM-DD

### 완료

- 작업명
  - 변경:
  - 검증:
  - 관련 문서:
  - 관련 커밋:

### 검증

- `검증 명령`
  - 결과: 성공/실패
  - 목적:

### 인수인계 메모

- 다음 세션에서 반드시 알아야 하는 주의사항
```

### 인수인계 메모 작성 기준

- 다음 세션에서 반드시 알아야 하는 주의사항만 적는다.
- 다음 작업 목록은 `tasks.md`에 적는다.
- 후보 이슈는 `backlog.md`에 적는다.
- 설계 결정은 `docs/adr/`에 적는다.

---

## 2026-07-11

### 완료

- REQ-005 JWT 로그인 및 토큰 관리 구현
  - 변경: `common-auth` 기반 로그인, 토큰 재발급, 로그아웃과 JWT 인증 필터 연동
  - 변경: 회원 인증 정보를 제공하는 `MemberAuthUserReader` 구현 및 `ROLE_MEMBER` 권한 부여
  - 변경: 회원 정보 조회와 탈퇴 API를 JWT 인증 주체를 사용하는 `/api/v1/members/me`로 전환
  - 변경: Redis Cloud 환경 변수 설정과 로컬 `.env.example` 추가
  - 변경: 테스트와 CI에서 외부 Redis 대신 Testcontainers Redis를 사용하도록 통합 테스트 구성
  - 변경: 인증 API REST Docs 테스트와 문서 추가
  - 검증: `common-auth`의 JWT 고유 식별자 수정본 재배포 후 Refresh Token Rotation 성공 확인
  - 관련 문서: `docs/requirements.md`, `docs/adr/0006-use-common-auth.md`

- REQ-005 코드 리뷰 반영
  - 변경: HTTP 요청·응답 로그의 비밀번호와 Access/Refresh Token 필드 마스킹 설정 추가
  - 변경: 재발급된 Access Token의 보호 API 호출과 Refresh Token의 연속 재발급 검증 추가
  - 변경: 로그아웃 후 기존 Refresh Token 재사용 실패 검증 추가
  - 변경: REQ-002와 REQ-004가 REQ-005에 의해 대체됐음을 명시
  - 변경: `.env.example`, README와 요구사항의 JWT Secret 예시를 32바이트 이상으로 수정
  - 검증: 인증 통합 테스트 로그에서 JWT 패턴 0건, `[MASKED]` 30건 확인
  - 관련 문서: `README.md`, `docs/requirements.md`

- common-modules 0.1.0 불변 버전 전환
  - 변경: `common-web`, `common-logging`, `common-auth` 의존성을 `0.1.0`으로 고정
  - 변경: `common-core:0.1.0` 전이 의존성 확인
  - 변경: 기술 스택과 ADR의 공통 모듈 버전 결정 갱신
  - 검증: 런타임 의존성 그래프에 공통 모듈 SNAPSHOT 버전이 없음을 확인
  - 관련 문서: `docs/architecture/tech-stack.md`, `docs/adr/0007-use-common-modules-release.md`

### 검증

- `./gradlew --refresh-dependencies dependencies --configuration runtimeClasspath`
  - 결과: 성공
  - 목적: 네 공통 모듈이 모두 `0.1.0`으로 해석되고 SNAPSHOT 전이 의존성이 없는지 확인
- `./gradlew --rerun-tasks test --tests '*AuthIntegrationTest' --tests '*CommonModulesAutoConfigurationTest'`
  - 결과: 성공, 인증 통합 테스트 10개 실행 및 `skipped=0` 확인
  - 목적: 불변 릴리스의 JWT 인증 흐름과 공통 모듈 자동 설정 호환성 확인
- `./gradlew test --tests '*AuthIntegrationTest' --tests '*CommonModulesAutoConfigurationTest'`
  - 결과: 성공
  - 목적: 토큰 마스킹 설정, 재발급 토큰 사용 가능 여부와 로그아웃 후 Refresh Token 폐기 확인
- `./gradlew --refresh-dependencies test --tests '*AuthIntegrationTest'`
  - 결과: 성공
  - 목적: 재배포된 `common-auth`를 사용한 로그인, 재발급, 로그아웃, JWT 인증과 Redis 토큰 관리 검증
- `./gradlew check`
  - 결과: 성공
  - 목적: 전체 테스트, Spotless 포맷과 아키텍처 규칙 검증
- `./gradlew build`
  - 결과: 성공
  - 목적: REST Docs HTML 생성과 Spring Boot JAR 패키징 검증

### 인수인계 메모

- 테스트와 CI는 Docker에서 `redis:7.4-alpine` 컨테이너를 실행하며 Redis Cloud에 연결하지 않는다.
- 로컬 실행 시 `.env`를 셸 환경 변수로 로드해야 하며 실제 값은 Git에 포함하지 않는다.
- `common-auth:0.1.0`은 JWT마다 고유한 `jti`를 발급하는 불변 릴리스를 사용한다.

## 2026-07-10

### 완료

- 회원 비활성 상태 제거
  - 변경: `MemberStatus`에서 `INACTIVE` 상태와 `Member.deactivate()` 메서드 제거
  - 변경: REQ-002의 비활성 회원 로그인 제한 정책 및 테스트 제거
  - 변경: 회원 상태를 현재 범위에 필요한 `ACTIVE`, `WITHDRAWN`으로 단순화
  - 관련 문서: `docs/requirements.md`

- REQ-004 회원 정보 조회 구현
  - 변경: 회원 식별자 기반 회원 정보 조회 API 추가
  - 변경: 탈퇴 회원 조회를 차단하는 `MEMBER_WITHDRAWN` 오류 코드 추가
  - 변경: 회원 정보 조회 Controller, Service, REST Docs 테스트 추가
  - 변경: 회원 API 문서에 회원 정보 조회 섹션 추가
  - 검증: 테스트를 먼저 작성해 컴파일 실패를 확인한 뒤 구현 후 성공 확인
  - 관련 문서: `docs/requirements.md`

- REQ-003 회원 탈퇴 구현
  - 변경: 회원 식별자 기반 탈퇴 API 추가
  - 변경: `MemberService.withdraw(Long)` 추가
  - 변경: 탈퇴 시 회원 상태 `WITHDRAWN`, 삭제 여부 `true`, 삭제일시 기록 처리 추가
  - 변경: 존재하지 않는 회원과 이미 탈퇴한 회원에 대한 에러 코드 추가
  - 변경: 회원 탈퇴 Controller, Service, REST Docs 테스트 추가
  - 변경: 회원 API 문서에 회원 탈퇴 섹션 추가
  - 검증: 테스트를 먼저 작성해 컴파일 실패를 확인한 뒤 구현 후 성공 확인
  - 관련 문서: `docs/requirements.md`

### 검증

- `./gradlew test --tests '*MemberServiceTest'`
  - 결과: 제거 전후 성공
  - 목적: 회원 상태 단순화 후 로그인과 탈퇴 정책 회귀 확인
- `./gradlew spotlessApply`
  - 결과: 성공
  - 목적: 상태 제거 코드 포맷 적용
- `./gradlew check`
  - 결과: 성공
  - 목적: 테스트와 포맷 검사를 포함한 전체 검증
- `./gradlew build`
  - 결과: 성공
  - 목적: REST Docs HTML 생성과 Spring Boot JAR 패키징 검증
- `./gradlew test --tests '*MemberControllerTest' --tests '*MemberServiceTest' --tests '*MemberControllerDocsTest'`
  - 결과: 구현 전 실패, 구현 후 성공
  - 목적: 회원 정보 조회 성공, 존재하지 않는 회원 및 탈퇴 회원 조회 실패, 민감 정보 미노출 확인
- `./gradlew spotlessApply`
  - 결과: 성공
  - 목적: 신규 회원 정보 조회 코드 포맷 적용
- `./gradlew check`
  - 결과: 성공
  - 목적: 테스트와 포맷 검사를 포함한 전체 검증
- `./gradlew build`
  - 결과: 성공
  - 목적: REST Docs HTML 생성과 Spring Boot JAR 패키징 검증
- `./gradlew test --tests '*MemberControllerTest' --tests '*MemberServiceTest'`
  - 결과: 구현 전 실패, 구현 후 성공
  - 목적: 회원 탈퇴 상태 변경, 중복 탈퇴 방지, 탈퇴 후 로그인 차단 확인
- `./gradlew test --tests '*MemberControllerTest' --tests '*MemberServiceTest' --tests '*MemberControllerDocsTest'`
  - 결과: 성공
  - 목적: 회원 탈퇴 API 문서 snippet 생성 확인
- `./gradlew spotlessApply`
  - 결과: 성공
  - 목적: 신규 회원 탈퇴 테스트 코드 포맷 적용
- `./gradlew check`
  - 결과: 성공
  - 목적: 테스트와 포맷 검사를 포함한 전체 검증
- `./gradlew build`
  - 결과: 성공
  - 목적: REST Docs HTML 생성과 Spring Boot JAR 패키징 검증

### 인수인계 메모

- 현재 회원 상태는 `ACTIVE`, `WITHDRAWN`만 사용하며, 탈퇴 시 `WITHDRAWN`과 `deleted == true`를 함께 기록한다.
- REQ-004는 JWT 인증 기반 현재 로그인 회원 식별을 포함하지 않고 `memberId`를 경로 변수로 받는다.
- JWT 인증 도입 시 회원 식별자를 인증 정보에서 추출하는 방식으로 변경한다.
- REQ-003은 JWT 인증 기반 본인 확인, 비밀번호 재확인, 연관 도메인 데이터 정리, 도메인 이벤트 발행을 포함하지 않는다.
- 탈퇴 성공 응답은 `ApiResponse<Void>`로 반환하며 `data` 필드는 포함하지 않는다.

## 2026-07-09

### 완료

- REQ-002 로그인 구현
  - 변경: 이메일과 비밀번호 기반 로그인 API 추가
  - 변경: Controller용 `MemberLoginRequest`와 Service용 `MemberLoginServiceRequest` 분리
  - 변경: `PasswordEncoder.matches()` 기반 비밀번호 검증 추가
  - 변경: 존재하지 않는 이메일, 비밀번호 불일치, 탈퇴 회원, 비활성 회원 로그인 실패 처리 추가
  - 변경: 로그인 API Controller, Service, REST Docs 테스트 추가
  - 변경: 회원 API 문서에 로그인 섹션 추가
  - 검증: 테스트를 먼저 작성해 컴파일 실패를 확인한 뒤 구현 후 성공 확인
  - 관련 문서: `docs/requirements.md`

### 검증

- `./gradlew test --tests '*MemberControllerTest' --tests '*MemberServiceTest'`
  - 결과: 구현 전 실패, 구현 후 성공
  - 목적: 로그인 요청 검증, credential 검증, 회원 상태 검증 확인
- `./gradlew test --tests '*MemberControllerTest' --tests '*MemberServiceTest' --tests '*MemberControllerDocsTest'`
  - 결과: 성공
  - 목적: 로그인 API 문서 snippet 생성 확인
- `./gradlew spotlessApply`
  - 결과: 성공
  - 목적: 신규 로그인 테스트 코드 포맷 적용
- `./gradlew check`
  - 결과: 성공
  - 목적: 테스트와 포맷 검사를 포함한 전체 검증
- `./gradlew build`
  - 결과: 성공
  - 목적: REST Docs HTML 생성과 Spring Boot JAR 패키징 검증

### 인수인계 메모

- REQ-002는 JWT 발급을 포함하지 않고 이메일/비밀번호 credential 검증까지만 처리한다.
- 로그인 실패 응답은 이메일 존재 여부와 비밀번호 불일치 여부를 구분하지 않고 `LOGIN_FAILED`로 통일한다.

## 2026-07-09

### 완료

- ArchUnit 아키텍처 검증 하네스 추가
  - 변경: ArchUnit JUnit 5 테스트 의존성 추가
  - 변경: Controller가 Repository를 직접 참조하지 못하게 검증하는 규칙 추가
  - 변경: Controller, Service, Repository 레이어 접근 방향 검증 규칙 추가
  - 변경: 다른 도메인의 Repository 직접 참조 금지 규칙 추가
  - 변경: 도메인 간 Service 의존은 허용하도록 규칙 조정
  - 변경: 기술 스택, 테스트 규칙, ADR 문서 갱신
  - 검증: ArchUnit 의존성 추가 전 컴파일 실패를 확인한 뒤 의존성 및 규칙 적용 후 성공 확인

### 검증

- `./gradlew test --tests '*ArchitectureRuleTest'`
  - 결과: 구현 전 실패, 구현 후 성공
  - 목적: ArchUnit 의존성과 아키텍처 규칙 검증 확인
- `./gradlew spotlessApply`
  - 결과: 성공
  - 목적: 신규 아키텍처 테스트 코드 포맷 적용
- `./gradlew check`
  - 결과: 성공
  - 목적: ArchUnit 규칙이 전체 테스트 생명주기에 포함되는지 확인
- `./gradlew build`
  - 결과: 성공
  - 목적: 전체 빌드와 패키징 검증

### 인수인계 메모

- 아키텍처 규칙은 `architecture/ArchitectureRuleTest`에서 관리한다.
- 새 레이어나 도메인 간 협업 방식이 추가되면 ArchUnit 규칙도 함께 검토한다.
- 현재 규칙은 도메인 간 Service 의존은 허용하고, 다른 도메인의 Repository 직접 참조는 금지한다.

## 2026-07-08

### 완료

- Flyway DB 마이그레이션 도구 추가
  - 변경: Flyway 및 MySQL 지원 의존성 추가
  - 변경: Spring Boot 4 Flyway 통합 모듈 추가
  - 변경: 회원 테이블 초기 마이그레이션 SQL 추가
  - 변경: 테스트 프로필의 JPA 스키마 전략을 `validate`로 변경
  - 변경: 기술 스택과 ADR 문서 갱신
  - 검증: 회원 서비스 테스트에서 Flyway 마이그레이션 후 Hibernate 스키마 검증이 통과하는지 확인

### 검증

- `./gradlew test --tests '*MemberServiceTest'`
  - 결과: 구현 전 실패, 구현 후 성공
  - 목적: Flyway 마이그레이션과 JPA 스키마 검증 연동 확인

### 인수인계 메모

- Spring Boot 4에서는 Flyway 자동 구성을 위해 `spring-boot-flyway` 모듈을 명시한다.
- 새 스키마 변경은 기존 마이그레이션 파일을 수정하지 않고 새 `V{버전}__{설명}.sql` 파일로 추가한다.

## 2026-07-08

### 완료

- REST Docs 하네스 추가
  - 변경: Spring REST Docs MockMvc 의존성과 Asciidoctor Gradle 플러그인 추가
  - 변경: `RestDocsSupport` 추가
  - 변경: 회원 가입 API REST Docs 테스트 추가
  - 변경: AsciiDoc 진입점과 회원 API 문서 섹션 추가
  - 변경: `bootJar`에 생성된 HTML 문서를 `static/docs`로 포함하도록 설정
  - 변경: 기술 스택과 REST Docs 규칙 문서 갱신
  - 검증: 회원 가입 API 문서 snippet 생성과 Asciidoctor HTML 생성 확인

### 검증

- `./gradlew test --tests '*MemberControllerDocsTest'`
  - 결과: 구현 전 실패, 구현 후 성공
  - 목적: REST Docs 의존성, Support 설정, 회원 가입 API snippet 생성 확인
- `./gradlew asciidoctor`
  - 결과: 성공
  - 목적: 생성된 snippet을 AsciiDoc HTML 문서로 조합하는지 확인
- `./gradlew check`
  - 결과: 성공
  - 목적: 테스트와 포맷 검사를 포함한 전체 검증
- `./gradlew build`
  - 결과: 성공
  - 목적: REST Docs HTML 생성과 Spring Boot JAR 패키징 검증
- `jar tf build/libs/pet-wellness-1.0-SNAPSHOT.jar | grep 'docs'`
  - 결과: 성공
  - 목적: 생성된 HTML 문서가 `static/docs` 경로로 JAR에 포함되는지 확인

### 인수인계 메모

- Spring REST Docs 4의 Asciidoctor 확장과 AsciidoctorJ 조합에서 `operation::` 매크로 호환 문제가 있어 개별 snippet `include::` 방식으로 문서를 구성한다.
- REST Docs 테스트는 `@WebMvcTest`가 아닌 standalone MockMvc 기반 `RestDocsSupport`를 상속한다.

## 2026-07-08

### 완료

- 테스트 하네스 Support 클래스 및 테스트 프로필 정리
  - 변경: `ControllerTestSupport`, `IntegrationTestSupport`, `RepositoryTestSupport` 추가
  - 변경: 회원 Controller 테스트와 회원 Service 테스트가 공통 Support를 상속하도록 정리
  - 변경: `application-test.yml`을 추가해 테스트 DB와 JPA 설정을 명시
  - 변경: Spring Boot 4 Data JPA 테스트 모듈 의존성 추가
  - 변경: 기술 스택과 작업 기록 문서 갱신

### 검증

- `./gradlew test --tests '*MemberControllerTest' --tests '*MemberServiceTest' --tests '*PetWellnessApplicationTest'`
  - 결과: 성공
  - 목적: Support 클래스 분리 후 기존 Controller, Service, 애플리케이션 컨텍스트 테스트 동작 확인

### 인수인계 메모

- Spring Boot 4에서는 Repository 슬라이스 테스트를 위해 `spring-boot-data-jpa-test` 테스트 의존성을 명시했다.
- `IntegrationTestSupport`는 부모 클래스에 `@Transactional`을 선언하지 않고, 필요한 테스트 클래스에서 직접 선언한다.

## 2026-07-02

### 완료

- REQ-001 회원 가입 구현
  - 변경: 이메일과 비밀번호 기반 회원 가입 API 추가
  - 변경: `Member`, `MemberStatus`, `BaseEntity`, `MemberRepository`, `MemberService` 추가
  - 변경: Controller용 `MemberSignupRequest`와 Service용 `MemberSignupServiceRequest` 분리
  - 변경: `spring-security-crypto` 기반 `PasswordEncoder` 설정 추가
  - 변경: 회원 가입 Controller 슬라이스 테스트와 서비스 통합 테스트 추가
  - 변경: 기술 스택 문서에 비밀번호 해싱 및 Boot 4 Web MVC 테스트 모듈 반영
  - 검증: 테스트를 먼저 작성해 컴파일 실패를 확인한 뒤 구현 후 성공 확인
  - 관련 문서: `docs/requirements.md`, `docs/architecture/tech-stack.md`

### 검증

- `./gradlew test --tests '*MemberServiceTest' --tests '*MemberControllerTest'`
  - 결과: 구현 전 실패, 구현 후 성공
  - 목적: 회원 가입 성공, 비밀번호 해싱, 이메일 중복, 입력값 검증 확인
- `./gradlew spotlessApply`
  - 결과: 성공
  - 목적: Java 코드 포맷 적용
- `./gradlew check`
  - 결과: 성공
  - 목적: 테스트와 포맷 검사를 포함한 전체 검증
- `./gradlew build`
  - 결과: 성공
  - 목적: 실행 가능한 Spring Boot JAR을 포함한 전체 빌드 검증

### 인수인계 메모

- Spring Boot 4에서는 Controller 슬라이스 테스트를 위해 `spring-boot-webmvc-test` 테스트 의존성을 명시했다.
- 로그인, JWT 발급, 소셜 로그인, 이메일 인증, 비밀번호 재설정은 REQ-001 범위에서 제외했다.

## 2026-07-01

### 완료

- CI GitHub Packages 인증 설정 개선
  - 변경: GitHub Actions에서 `COMMON_MODULES_TOKEN` Secret을 `GITHUB_PACKAGES_TOKEN` 환경 변수로 전달
  - 변경: Gradle GitHub Packages 인증 정보에서 `GITHUB_PACKAGES_TOKEN`을 기본 `GITHUB_TOKEN`보다 우선 사용하도록 조정
  - 검증: `./gradlew build`, `git diff --check`, GitHub Actions 워크플로 YAML 파싱

### 검증

- `./gradlew build`
  - 결과: 성공
  - 목적: private GitHub Packages 의존성 해석과 전체 빌드 검증
- `git diff --check`
  - 결과: 성공
  - 목적: 공백 오류 확인
- `ruby -e "require 'yaml'; YAML.load_file('.github/workflows/ci.yml'); puts 'workflow yaml ok'"`
  - 결과: 성공
  - 목적: GitHub Actions 워크플로 YAML 문법 확인

### 인수인계 메모

- `ai-pet-wellness-api` 저장소의 Actions Secret에 `COMMON_MODULES_TOKEN`이 등록되어 있어야 CI에서 private `common-modules` 패키지를 내려받을 수 있다.

## 2026-06-21

### 완료

- common-web 및 common-logging 연동
  - 변경: GitHub Packages 저장소와 `common-web:0.1.0-SNAPSHOT`, `common-logging:0.1.0-SNAPSHOT` 의존성 추가
  - 변경: CI에 `packages: read` 권한과 `GITHUB_TOKEN` 전달 설정 추가
  - 변경: 공통 웹 및 로깅 AutoConfiguration Bean 등록 통합 테스트 추가
  - 변경: 공통 모듈 사용 ADR과 기술 스택 갱신
  - 검증: 공통 모듈 추가 전 통합 테스트 실패와 추가 후 성공 확인
  - 검증: `common-core:0.1.0-SNAPSHOT` 전이 의존성 포함 확인
  - 관련 문서: `docs/adr/0003-use-common-modules.md`, `docs/architecture/tech-stack.md`

### 검증

- `./gradlew test --tests io.github.khghouse.petwellness.CommonModulesAutoConfigurationTest`
  - 결과: 의존성 추가 전 실패, 추가 후 성공
  - 목적: 공통 웹 및 로깅 자동 설정 Bean 등록 확인
- `./gradlew dependencyInsight --dependency common-web --configuration runtimeClasspath`
  - 결과: 성공
  - 목적: `common-web:0.1.0-SNAPSHOT` 의존성 해석 확인
- `./gradlew dependencyInsight --dependency common-logging --configuration runtimeClasspath`
  - 결과: 성공
  - 목적: `common-logging:0.1.0-SNAPSHOT` 의존성 해석 확인
- `./gradlew dependencyInsight --dependency common-core --configuration runtimeClasspath`
  - 결과: 성공
  - 목적: `common-web`을 통한 `common-core:0.1.0-SNAPSHOT` 전이 의존성 확인
- `./gradlew spotlessApply`
  - 결과: 성공
  - 목적: 새 통합 테스트 코드 포맷 적용
- `./gradlew check`
  - 결과: 성공
  - 목적: 테스트와 코드 포맷을 포함한 전체 검증
- `./gradlew build`
  - 결과: 성공
  - 목적: 실행 가능한 Spring Boot JAR을 포함한 전체 빌드 검증

### 인수인계 메모

- private GitHub Package를 CI에서 내려받으려면 `common-modules` 패키지 설정에서 이 저장소에 Actions 읽기 권한을 부여해야 한다.
- `0.1.0-SNAPSHOT`의 안정 버전 전환 작업은 `docs/context/backlog.md`에서 관리한다.
- `common-auth`는 회원과 인증 요구사항을 확정한 뒤 별도 작업으로 연동한다.

## 2026-06-18

### 완료

- 기술 스택 기반 Gradle 빌드 구성
  - 변경: Java 17 toolchain과 Spring Boot 3.3.13 플러그인 설정
  - 변경: Web, Validation, JPA, H2, MySQL, Lombok, Spring Boot Test 의존성 추가
  - 검증: `./gradlew check`, `./gradlew dependencies --configuration runtimeClasspath`
  - 관련 문서: `docs/architecture/tech-stack.md`, `docs/adr/0001-use-spring-boot.md`
- 에이전트 완료 흐름 및 IDE 제외 규칙 추가
  - 변경: 작업 완료 후 한글 Conventional Commit 메시지를 추천하도록 `AGENTS.md`에 명시
  - 변경: `.idea/` 전체를 Git 추적 대상에서 제외
  - 검증: `git check-ignore .idea/vcs.xml`, `git diff --check`
- Spring Boot 4.0.6 업그레이드
  - 변경: Spring Boot 플러그인을 3.3.13에서 4.0.6으로 변경
  - 변경: `spring-boot-starter-web`을 `spring-boot-starter-webmvc`로 변경
  - 변경: 기술 스택 문서 갱신 및 ADR 추가
  - 검증: `./gradlew check`, `./gradlew dependencies --configuration runtimeClasspath`
  - 관련 문서: `docs/architecture/tech-stack.md`, `docs/adr/0002-use-spring-boot-4.md`
- Spring Boot 애플리케이션 진입점 추가
  - 변경: `io.github.khghouse.petwellness.PetWellnessApplication` 추가
  - 변경: Spring 애플리케이션 컨텍스트 로딩 테스트 추가
  - 검증: 진입 클래스 추가 전 테스트 실패와 추가 후 성공 확인
  - 검증: `./gradlew check`, `./gradlew build`
  - 관련 문서: `docs/architecture/layers.md`, `docs/conventions/testing.md`
- Spotless 코드 포맷 검증 추가
  - 변경: Spotless 8.6.0과 Google Java Format 설정
  - 변경: 기존 Java 코드에 자동 포맷 적용
  - 변경: 코딩 컨벤션과 에이전트 검증 명령 갱신
  - 검증: 기존 코드의 포맷 위반 검출 후 `spotlessApply` 적용 확인
  - 검증: `./gradlew spotlessCheck`, `./gradlew check`, `./gradlew check --dry-run`
  - 관련 문서: `docs/conventions/coding.md`, `AGENTS.md`
- Java 들여쓰기 4칸 적용
  - 변경: Google Java Format AOSP 스타일 적용
  - 변경: 코딩 컨벤션에 스페이스 4칸 들여쓰기 명시
  - 검증: `./gradlew spotlessApply`, `./gradlew check`
  - 관련 문서: `docs/conventions/coding.md`
- AI 에이전트 Git 작업 승인 흐름 명시
  - 변경: 작업 브랜치 생성부터 빌드 검증까지 AI의 기본 수행 범위로 정의
  - 변경: 커밋, push, PR 생성은 사용자 승인 후 수행하도록 경계 명시
  - 변경: PR 최종 확인과 merge는 사용자가 담당하도록 명시
  - 검증: `git diff --check`, `./gradlew build`
  - 관련 문서: `AGENTS.md`

### 검증

- `./gradlew check`
  - 결과: 성공
  - 목적: 컴파일 및 테스트 검증 생명주기 확인
- `./gradlew dependencies --configuration runtimeClasspath`
  - 결과: 성공
  - 목적: Spring Boot 및 런타임 의존성 해석 확인
- `./gradlew check`
  - 결과: 성공
  - 목적: Spring Boot 4.0.6 적용 후 검증 생명주기 확인
- `./gradlew dependencies --configuration runtimeClasspath`
  - 결과: 성공
  - 목적: Spring Boot 4.0.6 및 변경된 Web MVC starter 의존성 해석 확인
- `./gradlew check`
  - 결과: 성공
  - 목적: 애플리케이션 진입점과 컨텍스트 로딩 테스트 검증
- `./gradlew build`
  - 결과: 성공
  - 목적: main class 해석, 실행 가능한 Spring Boot JAR 생성, 전체 빌드 검증
- `./gradlew spotlessCheck`
  - 결과: 성공
  - 목적: Java 코드 포맷 검증
- `./gradlew check`
  - 결과: 성공
  - 목적: 테스트와 Spotless 포맷 검사의 통합 검증
- `./gradlew check --dry-run`
  - 결과: 성공
  - 목적: `spotlessCheck`가 `check` 생명주기에 연결됐는지 확인
- `./gradlew check`
  - 결과: 성공
  - 목적: AOSP 스타일의 스페이스 4칸 들여쓰기 및 전체 검증
- `./gradlew build`
  - 결과: 실패
  - 목적: 전체 빌드 생명주기 확인
  - 참고: 애플리케이션 진입 클래스가 없어 `bootJar`가 main class를 찾지 못함

### 인수인계 메모

## 2026-06-17

### 완료

### 검증

### 인수인계 메모

- 다음 작업은 `docs/context/tasks.md`를 기준으로 선택한다.
- 후보 이슈는 `docs/context/backlog.md`에서 tasks.md로 승격한 뒤 진행한다.
- 설계 변경이 있으면 `docs/adr/`에 결정과 이유를 남긴다.
