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

### 검증

- `./gradlew check`
  - 결과: 성공
  - 목적: 컴파일 및 테스트 검증 생명주기 확인
- `./gradlew dependencies --configuration runtimeClasspath`
  - 결과: 성공
  - 목적: Spring Boot 및 런타임 의존성 해석 확인
- `./gradlew build`
  - 결과: 실패
  - 목적: 전체 빌드 생명주기 확인
  - 참고: 애플리케이션 진입 클래스가 없어 `bootJar`가 main class를 찾지 못함

### 인수인계 메모

- 첫 애플리케이션 구성 시 `@SpringBootApplication` 진입 클래스를 추가하고 `./gradlew build`를 다시 검증한다.

## 2026-06-17

### 완료

### 검증

### 인수인계 메모

- 다음 작업은 `docs/context/tasks.md`를 기준으로 선택한다.
- 후보 이슈는 `docs/context/backlog.md`에서 tasks.md로 승격한 뒤 진행한다.
- 설계 변경이 있으면 `docs/adr/`에 결정과 이유를 남긴다.
