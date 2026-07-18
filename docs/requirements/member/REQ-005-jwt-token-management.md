# REQ-005: JWT 로그인 및 토큰 관리

- **목표:** 사용자는 이메일과 비밀번호로 인증하고 JWT Access Token과 Refresh Token을 발급받을 수 있다.
- **행위자:** 사용자
- **범위:**
    - common-auth 모듈 연동
    - JWT 로그인 및 토큰 발급
    - Refresh Token 재발급
    - 로그아웃
    - Redis 기반 Refresh Token 저장 및 Access Token 블랙리스트 처리
    - JWT 기반 내 회원 정보 조회 전환
- **제외 범위:**
    - 소셜 로그인
    - 이메일 인증
    - 로그인 실패 횟수 제한
    - 계정 잠금
    - 권한별 기능 인가 정책
    - 관리자 역할 및 역할 관리
    - 다중 기기 로그인 정책
    - 실제 운영 환경의 Redis 고가용성 및 백업 구성

- **인증 API:**
    - `POST /api/auth/login`: 로그인 및 Access/Refresh Token 발급
    - `POST /api/auth/reissue`: Refresh Token 기반 토큰 재발급
    - `POST /api/auth/logout`: 로그아웃
    - `GET /api/v1/members/me`: 인증된 회원 정보 조회

- **변경 사항:**
    - 기존 `POST /api/v1/members/login` API를 제거한다.
    - 기존 `GET /api/v1/members/{memberId}` API를 제거하고 `GET /api/v1/members/me`로 전환한다.
    - 기존 `DELETE /api/v1/members/{memberId}` API를 제거하고 `DELETE /api/v1/members/me`로 전환한다.

- **로그인 입력값:**
    - `loginId`: 회원 이메일
    - `password`: 비밀번호

- **로그인 응답값:**
    - `accessToken`: Access Token
    - `refreshToken`: Refresh Token

- **재발급 입력값:**
    - `refreshToken`: 기존 Refresh Token

- **재발급 응답값:**
    - `accessToken`: 새 Access Token
    - `refreshToken`: 새 Refresh Token

- **로그아웃 입력값:**
    - `Authorization`: `Bearer {accessToken}`

- **내 회원 정보 응답값:**
    - `id`: 회원 식별자
    - `email`: 로그인/회원 식별 이메일
    - `status`: 회원 상태

- **회원 인증 정책:**
    - common-auth의 `AuthUserReader`를 프로젝트에서 구현한다.
    - `loginId`는 회원 이메일에 매핑한다.
    - 삭제 여부가 `false`인 회원만 인증 사용자로 조회한다.
    - 회원 권한은 현재 모든 회원에게 `ROLE_MEMBER`를 고정 부여한다.
    - 별도 역할 컬럼은 추가하지 않는다.
    - 회원 비밀번호는 이미 저장된 BCrypt 기반 해시 값을 사용한다.
    - 현재 회원 상태는 `ACTIVE`, `WITHDRAWN`만 사용한다.

- **토큰 정책:**
    - Access Token 유효기간은 30분으로 설정한다.
    - Refresh Token 유효기간은 14일로 설정한다.
    - Access Token에는 회원 식별자, 권한 목록, 토큰 타입을 포함한다.
    - Refresh Token 재발급 시 기존 Refresh Token을 삭제하고 새 토큰 쌍을 발급한다.
    - 로그아웃 시 Refresh Token을 삭제한다.
    - 로그아웃한 Access Token은 남은 유효기간 동안 Redis 블랙리스트에 저장한다.
    - JWT Secret은 HMAC 서명에 사용할 수 있도록 32바이트 이상의 값으로 설정한다.

- **보안 정책:**
    - 기본적으로 모든 API는 JWT 인증을 요구한다.
    - 아래 경로는 인증 없이 접근할 수 있다.
        - `POST /api/v1/members`
        - `POST /api/auth/login`
        - `POST /api/auth/reissue`
        - `/docs/**`
        - `/h2-console/**` (로컬 및 테스트 환경)
    - `GET /api/v1/members/me`는 JWT에서 추출한 회원 식별자로만 조회한다.
    - 탈퇴한 회원은 로그인, 토큰 재발급, 내 회원 정보 조회를 할 수 없다.

- **Redis 및 환경 변수:**
    - Refresh Token과 Access Token 블랙리스트는 Redis에 저장한다.
    - 개발 및 포트폴리오 시연 환경에서는 Redis Cloud 무료 플랜을 사용한다.
    - 실제 운영 환경 전환 시에는 TLS를 지원하는 Redis 플랜 또는 제공자를 사용한다.
    - 민감한 환경 변수는 `.env`에서 관리하고 Git에 포함하지 않는다.
    - `.env.example`은 Git에 포함해 필요한 환경 변수를 안내한다.
    - Spring Boot 실행 전 `.env`를 셸에서 로드하거나 IDE 환경 변수로 등록한다.

```dotenv
JWT_SECRET=replace-with-a-random-secret-at-least-32-bytes-long
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password
```

- **테스트 정책:**
    - 로컬 테스트와 CI에서는 Redis Cloud에 연결하지 않는다.
    - Testcontainers Redis를 사용해 독립된 Redis 컨테이너에서 인증 흐름을 검증한다.
    - 테스트 프로필에는 테스트 전용 JWT Secret을 사용한다.
    - 로그인, 재발급, 로그아웃, JWT 인증 필터, 내 회원 정보 조회를 검증한다.

- **흐름:**
    1. 사용자가 이메일을 loginId로, 비밀번호와 함께 로그인 요청한다.
    2. 시스템은 이메일로 삭제되지 않은 회원을 조회한다.
    3. 시스템은 비밀번호를 검증한다.
    4. 시스템은 ROLE_MEMBER 권한을 포함한 Access Token과 Refresh Token을 발급한다.
    5. 시스템은 Refresh Token을 Redis에 저장한다.
    6. 사용자는 Access Token을 Authorization 헤더에 담아 보호된 API를 호출한다.
    7. 시스템은 Access Token을 검증하고 인증 정보를 구성한다.
    8. 토큰 재발급 요청 시 시스템은 기존 Refresh Token을 검증하고 새 토큰 쌍을 발급한다.
    9. 로그아웃 요청 시 시스템은 Refresh Token을 삭제하고 Access Token을 블랙리스트에 저장한다.

- **완료 기준:**
    - [ ] 이메일과 비밀번호가 일치하면 Access Token과 Refresh Token이 발급된다.
    - [ ] 존재하지 않는 이메일 또는 일치하지 않는 비밀번호면 로그인에 실패한다.
    - [ ] 탈퇴한 회원은 로그인에 실패한다.
    - [ ] 유효한 Refresh Token으로 새 토큰 쌍을 발급받을 수 있다.
    - [ ] 재발급 후 기존 Refresh Token은 사용할 수 없다.
    - [ ] 로그아웃 후 Refresh Token은 사용할 수 없다.
    - [ ] 로그아웃한 Access Token으로 보호된 API를 호출하면 실패한다.
    - [ ] Access Token 없이 보호된 API를 호출하면 실패한다.
    - [ ] 유효한 Access Token으로 GET /api/v1/members/me를 호출하면 본인 정보만 조회된다.
    - [ ] 회원가입, 로그인, 토큰 재발급, API 문서 경로는 인증 없이 접근할 수 있다.
    - [ ] 테스트와 CI는 외부 Redis Cloud 연결 없이 실행된다.

- **예외:**
    - 로그인 인증 실패
    - 토큰 누락
    - 유효하지 않은 토큰
    - 만료된 토큰
    - 토큰 타입 불일치
    - 블랙리스트 토큰
    - Refresh Token 불일치 또는 만료
    - 탈퇴 회원 인증 시도
