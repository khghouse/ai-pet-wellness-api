# SCN-002: 회원 탈퇴와 토큰 폐기

## 목적

인증된 사용자가 회원 탈퇴를 완료하면 해당 요청의 Access Token과 기존 Refresh Token을 즉시 사용할 수 없는 핵심 API 사용자 흐름을 검증한다.

## 사전 조건

- 테스트 프로필의 데이터베이스와 Redis Testcontainers를 사용한다.
- 테스트 전후 회원과 Redis 토큰 데이터를 정리한다.

## 시나리오

1. 이메일과 비밀번호로 회원 가입을 요청한다.
2. 동일한 이메일과 비밀번호로 로그인하여 Access Token과 Refresh Token을 발급받는다.
3. Access Token으로 회원 탈퇴를 요청한다.
4. 동일한 이메일과 비밀번호로 로그인을 다시 요청한다.
5. 기존 Access Token으로 내 회원 정보 조회를 요청한다.
6. 기존 Refresh Token으로 토큰 재발급을 요청한다.

## 기대 결과

- 회원 가입, 로그인, 회원 탈퇴 요청은 성공한다.
- 로그인 응답에 비어 있지 않은 Access Token과 Refresh Token이 포함된다.
- 회원 탈퇴 성공 응답에는 별도 데이터가 포함되지 않는다.
- 탈퇴한 회원이 같은 이메일과 비밀번호로 로그인하면 `401 Unauthorized`, `INVALID_CREDENTIALS` 오류를 반환한다.
- 기존 Access Token으로 내 회원 정보 조회 시 `401 Unauthorized`, `TOKEN_BLACKLISTED` 오류를 반환한다.
- 기존 Refresh Token으로 토큰 재발급 시 `401 Unauthorized`, `REFRESH_TOKEN_NOT_FOUND` 오류를 반환한다.
