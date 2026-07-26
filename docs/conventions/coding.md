# 코딩 원칙

## 일반 원칙

- 가독성을 최우선으로 한다.
- 메서드는 단일 책임을 가지며 20줄 이내를 권장한다.
- 매직 넘버와 매직 스트링은 상수 또는 Enum으로 추출한다.
- `null` 반환을 지양하고 `Optional`을 적절히 활용한다.
- 생성자 주입을 원칙으로 하며 필드 주입은 사용하지 않는다.
- Lombok은 `@Data` 대신 필요한 어노테이션만 선택적으로 사용한다.
- 생성자 주입은 `final` 필드와 Lombok `@RequiredArgsConstructor`를 사용한다.

## 코드 포맷

- Java 코드는 Spotless와 Google Java Format의 AOSP 스타일을 사용한다.
- 들여쓰기는 탭 문자 대신 스페이스 4칸을 사용한다.
- 포맷 검사는 `./gradlew spotlessCheck`로 실행한다.
- 포맷 자동 수정은 `./gradlew spotlessApply`로 실행한다.
- 작업 완료 전 `./gradlew check`를 실행하여 테스트와 포맷 검사를 함께 검증한다.

## Controller

- 반환 타입은 반드시 `ApiResponse<T>`를 사용한다.
- `ResponseEntity`로 감싸는 것은 허용하지 않는다.
- 헤더 제어가 필요한 경우에도 반환 타입은 유지하고 `HttpServletResponse`를 직접 사용한다.
- `@RequestBody` Request DTO는 Service에 직접 전달하지 않고 ServiceRequest로 변환한다.
- `@PathVariable`, `@RequestParam` 단일 파라미터는 직접 전달한다.
- 여러 `@RequestParam`은 ServiceRequest로 묶는다.

## DTO / Request

- DTO와 Entity는 반드시 분리한다.
- Entity -> DTO 변환은 DTO 내부의 `from()` 정적 팩토리 메서드를 사용한다.
- Request DTO는 등록/수정 등 용도별로 분리한다.
- Request -> ServiceRequest 변환은 ServiceRequest 내부의 `from()`을 사용한다.
- ServiceRequest는 `dto/request/` 패키지에 위치시킨다.

## Service

- 트랜잭션은 Service 레이어에서 관리한다.
- 조회 메서드에는 `@Transactional(readOnly = true)`를 적용한다.
- 회원 생성과 비밀번호 변경 시 비밀번호 암호화는 Service 레이어에서 처리한다.
- Entity는 암호화된 비밀번호를 전달받아 도메인 규칙만 검증하고 저장한다.

## JPA / 영속성

- 연관관계 편의 메서드는 연관관계의 주인 쪽에 작성한다.
- N+1 문제를 주의하고 필요 시 `fetch join` 또는 `@EntityGraph`를 사용한다.
- Entity와 공통 Entity는 getter 메서드를 직접 작성하지 않고 Lombok `@Getter`를 사용한다.
- Entity의 기본 생성자는 Lombok `@NoArgsConstructor(access = AccessLevel.PROTECTED)`를 사용한다.

## Repository 조회 구현 규칙

- 조회 구현은 다음 순서로 선택한다.
  - 메서드 이름만으로 조회 의도를 명확히 표현할 수 있고, `fetch join`이나 DTO Projection이 필요하지 않으면 Spring Data JPA 쿼리 메서드를 사용한다.
  - 그 외 커스텀 조회는 QueryDSL을 사용한다.
- 쿼리 메서드 사용 예시는 다음과 같다.
  - 예: `findByEmailAndDeletedFalse`
  - 예: `existsByPetIdAndMemberIdAndStatus`
- QueryDSL 사용 예시는 다음과 같다.
  - `fetch join`이 필요한 조회
  - 여러 엔티티를 조인하는 조회
  - DTO Projection 조회
  - 조건이 선택적으로 조합되는 동적 조회
  - 쿼리 메서드 이름이 길어져 조회 의도를 명확히 표현하기 어려운 조회
  - 복잡한 정렬, 집계, 그룹화가 필요한 조회
- `@Query`를 사용한 JPQL 작성은 지양한다.
- 네이티브 SQL이 꼭 필요한 경우에는 QueryDSL로 표현하기 어려운 이유와 사용 범위를 검토한 뒤 별도로 결정한다.

## JPA 공통 엔티티

- 엔티티의 생성일시와 수정일시는 공통 `BaseEntity`에서 관리한다.
- `BaseEntity`는 `createdAt`, `updatedAt`만 포함한다.
- 삭제 여부나 삭제일시는 모든 엔티티에 공통으로 두지 않는다.
- 삭제 정책이 필요한 엔티티는 `deleted`, `deletedAt` 등의 필드를 개별 엔티티에서 정의한다.

## 날짜와 시간

- 서비스의 날짜와 시간 기준은 `Asia/Seoul`이다.
- 날짜만 필요한 값은 `LocalDate`와 `yyyy-MM-dd` 형식을 사용한다.
- 시각이 필요한 값은 `LocalDateTime`과 ISO-8601 형식을 사용하며, 소수 초를 허용한다.
- 시각 API 값의 예시는 `2026-07-25T14:30:00`, `2026-07-25T14:30:00.123456`이다.
- 애플리케이션 JVM, H2 테스트 DB, MySQL 연결 세션, CI는 모두 `Asia/Seoul` 기준으로 설정한다.
- MySQL의 신규 시각 컬럼은 `DATETIME(6)`을 사용한다. 기존 Flyway 마이그레이션의 `TIMESTAMP(6)` 컬럼 전환은 별도 마이그레이션으로 관리한다.
