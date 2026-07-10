# 0006. common-auth 기반 JWT 인증 사용

## 상태

승인됨

## 배경

로그인 이후 보호된 API에서 현재 사용자를 식별하고, Access Token과 Refresh Token의 발급, 재발급, 로그아웃을 일관되게 처리할 인증 기반이 필요하다.

`common-auth`는 JWT 발급과 검증, Spring Security 필터, Refresh Token 저장, Access Token 블랙리스트와 인증 API를 제공한다. 회원 엔티티와 회원 정책은 서비스 프로젝트가 소유하고 `AuthUserReader` 계약으로 인증 정보를 제공할 수 있다.

## 결정

- `io.github.khghouse:common-auth:0.1.0-SNAPSHOT`을 사용한다.
- common-auth가 제공하는 로그인, 토큰 재발급, 로그아웃 API와 JWT 인증 필터를 사용한다.
- 프로젝트에서 `AuthUserReader`를 구현해 삭제되지 않은 회원을 이메일로 조회하고 `ROLE_MEMBER` 권한을 부여한다.
- Refresh Token과 로그아웃한 Access Token은 Redis에 저장한다.
- 개발 및 시연 환경은 Redis Cloud 무료 플랜을 사용하고, 실제 운영 전환 시 TLS 지원 Redis를 사용한다.
- 로컬 테스트와 CI는 외부 Redis에 연결하지 않고 Testcontainers Redis를 사용한다.
- 회원 정보 조회와 탈퇴는 JWT 인증 주체를 사용하는 `/api/v1/members/me` API로 제공한다.

## 결과

- 인증과 토큰 관리 구현을 서비스마다 중복하지 않는다.
- 보호된 API는 JWT에서 추출한 회원 식별자를 사용한다.
- 테스트 실행에는 Docker 환경이 필요하다.
- common-auth의 공개 API와 자동 설정 변경은 이 프로젝트의 통합 테스트로 호환성을 검증해야 한다.
- Redis Cloud 무료 플랜은 TLS를 지원하지 않으므로 실제 사용자 데이터를 다루는 운영 환경에는 사용하지 않는다.

## 검토한 대안

- 프로젝트 내부에 JWT 인증 직접 구현:
  - 이유: 서비스별 중복 구현과 인증 정책 불일치가 발생하므로 채택하지 않는다.
- 테스트와 CI에서 Redis Cloud 사용:
  - 이유: 외부 네트워크와 비밀값에 의존하고 테스트 데이터 격리가 어려우므로 채택하지 않는다.
- ID 기반 회원 조회와 탈퇴 API 유지:
  - 이유: 인증된 사용자가 다른 회원 ID를 지정할 수 있어 본인 소유권을 보장하지 못하므로 채택하지 않는다.
