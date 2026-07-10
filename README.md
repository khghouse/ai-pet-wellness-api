# Pet Wellness API

반려동물 건강관리 서비스를 위한 백엔드 API 프로젝트입니다.

현재는 AI 에이전트 기반 개발 흐름과 하네스 엔지니어링을 학습하고, 그 위에서 기능을 점진적으로 구현하는 데 초점을 둡니다.

## 프로젝트 방향

- 하네스 엔지니어링 학습 및 적용
- AI 에이전트가 참고할 요구사항과 프로젝트 컨텍스트 정리
- 테스트, 문서화, 빌드 검증을 통한 AI 생성 코드 품질 관리
- 반려동물 건강관리 도메인의 백엔드 API 점진적 구현

## 기술 스택

- Java 17
- Spring Boot 4.0.6
- Gradle
- Spring Data JPA / Hibernate
- MySQL 8.x
- H2
- Flyway
- Spring Security / JWT
- Redis
- Lombok
- Spring REST Docs / Asciidoctor
- JUnit 5, Mockito, AssertJ, Testcontainers
- Spotless
- GitHub Actions

## 검증 하네스

이 프로젝트는 AI 에이전트가 생성한 결과물을 검증하기 위해 다음 하네스를 사용합니다.

- `./gradlew check`: 테스트와 포맷 검증
- `./gradlew build`: 전체 빌드, 테스트, API 문서 생성, 패키징 검증
- Spotless: Java 코드 포맷 검증 및 적용
- Spring REST Docs: 테스트 기반 API 문서 조각 생성
- Flyway: DB 스키마 변경 이력 관리
- GitHub Actions: PR 및 `main` 브랜치 push 시 CI 빌드 검증

## 문서 구조

프로젝트 컨텍스트는 `docs/` 아래에서 관리합니다.

- `docs/requirements.md`: 기능 요구사항과 완료 기준
- `docs/architecture/`: 기술 스택과 레이어 구조
- `docs/conventions/`: 코딩, 테스트, 예외, API 응답, REST Docs 규칙
- `docs/context/`: 작업 목록, 진행 기록, 백로그
- `docs/adr/`: 아키텍처 결정 기록

AI 에이전트 작업 규칙은 `AGENTS.md`에서 관리합니다.

## 시작하기

### 환경 변수

JWT 인증과 Redis 연결에는 `.env.example`에 정의된 환경 변수가 필요합니다.

```dotenv
JWT_SECRET=replace-with-a-random-secret-at-least-32-bytes-long
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password
```

`JWT_SECRET`은 32바이트 이상의 충분히 긴 랜덤 값으로 교체합니다. 예시 값을 실제 환경에서 그대로 사용하지 않습니다.

Spring Boot는 `.env`를 자동으로 읽지 않으므로 실행 전에 환경 변수를 로드합니다.

```bash
set -a
source .env
set +a
./gradlew bootRun
```

인증 통합 테스트는 Testcontainers Redis를 사용하므로 로컬 실행 시 Docker Desktop이 필요합니다.

### 테스트

```bash
./gradlew test
```

### 검증

```bash
./gradlew check
```

### 빌드

```bash
./gradlew build
```

### 포맷 적용

```bash
./gradlew spotlessApply
```

### API 문서 생성

```bash
./gradlew asciidoctor
```

생성된 문서는 `build/docs/asciidoc/index.html`에서 확인할 수 있습니다.

## GitHub Packages 인증

현재 일부 공통 모듈은 GitHub Packages를 통해 참조합니다.

private 패키지를 사용하는 환경에서는 다음 인증 정보 중 하나가 필요합니다.

- Gradle property: `gpr.user`, `gpr.key`
- Environment variable: `GITHUB_USERNAME`, `GITHUB_PACKAGES_TOKEN`
- GitHub Actions secret: `COMMON_MODULES_TOKEN`

## 개발 흐름

기능 개발은 다음 흐름을 기준으로 진행합니다.

1. 요구사항과 관련 컨텍스트 문서를 확인한다.
2. 작업 브랜치를 생성한다.
3. 테스트를 먼저 작성하거나 검증 기준을 명확히 한다.
4. 작은 단위로 구현한다.
5. 문서와 작업 기록을 갱신한다.
6. `./gradlew check`와 `./gradlew build`로 검증한다.
7. PR을 생성하고 CI 결과를 확인한다.
