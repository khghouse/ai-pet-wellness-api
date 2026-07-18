# 회원 도메인

회원 가입, 인증, 회원 정보 관리와 탈퇴 기능을 관리한다.

## 엔티티

### Member

- **역할:** 서비스 이용 주체인 회원 계정
- **필드:**
  - `id`: 회원 식별자
  - `email`: 로그인/회원 식별 이메일
  - `password`: 단방향 해싱된 비밀번호
  - `status`: 회원 상태
  - `deleted`: 삭제 여부
  - `createdAt`: 생성일시, `BaseEntity`에서 관리
  - `updatedAt`: 수정일시, `BaseEntity`에서 관리
  - `deletedAt`: 삭제일시

## 공통 정책

- 이메일은 회원 식별자로 사용하며, 중복 가입할 수 없다.
- 비밀번호는 BCrypt 기반 단방향 해싱으로 저장한다.
- 회원 상태는 현재 `ACTIVE`, `WITHDRAWN`만 사용한다.
- 탈퇴 시 `status`는 `WITHDRAWN`, `deleted`는 `true`로 변경하고 `deletedAt`을 기록한다.

## 관련 요구사항

- [REQ-001: 회원 가입](REQ-001-signup.md)
- [REQ-002: 로그인](REQ-002-login.md) - REQ-005에 의해 대체됨
- [REQ-003: 회원 탈퇴](REQ-003-withdrawal.md)
- [REQ-004: 회원 정보 조회](REQ-004-member-information.md) - REQ-005에 의해 대체됨
- [REQ-005: JWT 로그인 및 토큰 관리](REQ-005-jwt-token-management.md)
