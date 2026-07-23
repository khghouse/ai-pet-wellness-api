# 반려동물 도메인

반려견 정보, 견종 기준 데이터, 회원과 반려견의 관계, 체중 이력을 관리한다.

## 엔티티

### Breed

- **역할:** 반려견 등록 시 선택하는 견종 기준 데이터
- **필드:**
  - `id`: 견종 식별자
  - `name`: 견종명, 유니크
  - `active`: 신규 등록 시 선택 가능 여부
  - `createdAt`: 생성일시, `BaseEntity`에서 관리
  - `updatedAt`: 수정일시, `BaseEntity`에서 관리
- **정책:**
  - 견종은 직접 입력하지 않고 기준 데이터에서 선택한다.
  - 비활성 견종은 신규 등록 시 선택할 수 없지만, 기존 반려견의 견종 이력은 유지한다.
  - 견종 기준 데이터는 삭제하지 않고 `active` 상태로 관리한다.
  - 초기 견종 데이터는 Flyway 마이그레이션으로 등록한다.

### Pet

- **역할:** 서비스에서 관리하는 반려견 프로필
- **필드:**
  - `id`: 반려견 식별자
  - `name`: 반려견 이름
  - `birthDate`: 생년월일
  - `gender`: 성별
  - `breed`: 견종
  - `neuteredStatus`: 중성화 상태
  - `deleted`: 삭제 여부
  - `createdAt`: 생성일시, `BaseEntity`에서 관리
  - `updatedAt`: 수정일시, `BaseEntity`에서 관리
- **상태값:**
  - `gender`: `MALE`, `FEMALE`, `UNKNOWN`
  - `neuteredStatus`: `NEUTERED`, `NOT_NEUTERED`, `UNKNOWN`

### PetWeight

- **역할:** 반려견 체중의 추가 전용 이력
- **필드:**
  - `id`: 체중 이력 식별자
  - `pet`: 반려견
  - `weight`: 체중, kg 단위, 소수점 한 자리
  - `measuredAt`: 실제 체중 측정 시각
  - `createdAt`: 체중 이력 등록 시각
- **정책:**
  - 체중을 기록할 때마다 새 이력을 생성한다.
  - 반려견 등록 시 입력한 체중은 첫 번째 이력으로 생성한다.
  - 현재 체중은 가장 최근의 `measuredAt`, 동일 시각이면 가장 큰 `id`를 가진 이력으로 판단한다.
  - 현재 체중을 `Pet`에 중복 저장하지 않는다.

### PetMembership

- **역할:** 회원과 반려견의 관계 및 권한
- **필드:**
  - `id`: 멤버십 식별자
  - `member`: 회원
  - `pet`: 반려견
  - `role`: 관계 역할
  - `status`: 멤버십 상태
  - `createdAt`: 생성일시, `BaseEntity`에서 관리
  - `updatedAt`: 수정일시, `BaseEntity`에서 관리
- **상태값:**
  - `role`: `OWNER`, `FAMILY`
  - `status`: `ACTIVE`, `LEFT`
- **정책:**
  - 동일 회원과 동일 반려견의 멤버십은 하나만 존재한다.
  - 반려견당 `OWNER` 멤버십은 하나만 존재한다.
  - 반려견 등록 회원은 `OWNER`, `ACTIVE` 멤버십으로 생성된다.

## 관계

- `Breed` 1 : N `Pet`
- `Pet` 1 : N `PetWeight`
- `Member` 1 : N `PetMembership`
- `Pet` 1 : N `PetMembership`
- `Member`와 `Pet`은 `PetMembership`을 통해 관계를 맺으며, 직접 연관관계를 두지 않는다.

## 초기 견종 데이터

초기 견종 데이터는 모두 `active = true`로 등록한다.

- 믹스견
- 견종 미상
- 말티즈
- 푸들
- 포메라니안
- 비숑 프리제
- 시추
- 요크셔테리어
- 치와와
- 닥스훈트
- 미니어처 슈나우저
- 프렌치 불도그
- 퍼그
- 골든 리트리버
- 래브라도 리트리버
- 웰시 코기
- 비글
- 보더 콜리
- 사모예드
- 진돗개
- 시베리안 허스키
- 독일 셰퍼드
- 코커 스패니얼
- 셔틀랜드 쉽독

## 응답 설계 원칙

- 응답 DTO는 엔티티별 공통 DTO 하나가 아닌 API 목적별 DTO로 설계한다.
- 재사용이 필요한 경우 `PetSummaryResponse`, `BreedResponse`처럼 작은 응답 DTO를 조합한다.
- 요청 경로나 인증 정보로 이미 알 수 있는 리소스는 중복해 응답하지 않는다.
- 반려견 등록 응답은 요청 회원 정보 대신 생성된 반려견과 `membershipRole`을 반환한다.

## 향후 정책

- 반려견 등록과 일반 산책, 미션 참여는 반려견 검증 없이 허용한다.
- 경제적 가치가 있는 리워드 수령은 사용자 본인인증과 반려견 검증 완료를 모두 요구한다.
- 반려견 검증의 외부 연동과 검증 상태 모델은 별도 요구사항에서 정의한다.

## 관련 요구사항

- [REQ-006: 반려견 등록 및 소유자 멤버십 생성](REQ-006-pet-registration.md)
