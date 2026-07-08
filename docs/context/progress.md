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
