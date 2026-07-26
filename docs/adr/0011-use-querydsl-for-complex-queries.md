# 0011. 복잡한 Repository 조회에 QueryDSL 사용

## 상태

승인됨

## 배경

반려동물 목록처럼 연관 엔티티의 `fetch join`, 정렬, 상태 조건이 함께 필요한 조회는 Spring Data JPA 쿼리 메서드 이름만으로 의도를 표현하기 어렵다.

`@Query`에 JPQL 문자열을 직접 작성하면 엔티티 필드명 변경 시 컴파일 단계에서 오류를 확인할 수 없고, 조회가 복잡해질수록 가독성이 낮아질 수 있다.

## 결정

- QueryDSL을 Repository의 복잡한 조회 구현 도구로 사용한다.
- 고정된 단순 조건 조회와 존재 여부 확인은 Spring Data JPA 쿼리 메서드를 사용한다.
- `fetch join`, 여러 엔티티 조인, DTO Projection, 동적 조건, 복잡한 정렬·집계·그룹화는 QueryDSL로 구현한다.
- `@Query`를 사용한 JPQL 작성은 지양한다.
- QueryDSL 구현은 Repository fragment와 구현체로 분리한다.

## 결과

- 복잡한 조회를 타입 안전하게 작성하고, 엔티티 필드 변경을 컴파일 단계에서 확인할 수 있다.
- 단순 조회까지 QueryDSL로 작성하지 않아 Repository 코드량 증가를 제한한다.
- QueryDSL 의존성, Q 클래스 생성, `JPAQueryFactory` 설정이 추가된다.
- 네이티브 SQL이 필요한 경우에는 QueryDSL로 표현하기 어려운 이유와 사용 범위를 별도로 검토해야 한다.

## 검토한 대안

- Spring Data JPA 쿼리 메서드만 사용:
  - 이유: 단순 조회에는 적합하지만 조인과 정렬 조건이 늘어나면 메서드명이 길어져 조회 의도를 파악하기 어렵다.
- `@Query`로 JPQL 작성:
  - 이유: 추가 의존성 없이 작성할 수 있지만 문자열 기반 쿼리의 타입 안전성이 낮아 채택하지 않는다.
