# Architecture Decision Records

Architecture Decision Record, ADR은 중요한 기술적 결정과 아키텍처 결정을 기록하기 위해 사용한다.

하나의 결정은 하나의 파일로 작성하고, 파일 번호는 순차적으로 부여한다.

## 파일명 규칙

```text
0001-use-spring-boot.md
0002-use-mysql.md
0003-use-domain-oriented-packages.md
```

## 작성 형식

```md
# 0001. 결정 제목

## 상태

제안됨 | 승인됨 | 폐기됨 | 대체됨

## 배경

이 결정을 하게 된 문제, 제약, 배경을 설명한다.

## 결정

무엇을 결정했는지 명확히 설명한다.

## 결과

이 결정으로 인한 결과, 트레이드오프, 장점, 비용을 설명한다.

## 검토한 대안

- 대안:
  - 이유:
```
