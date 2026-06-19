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
