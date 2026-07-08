# 기술 스택

- 언어: Java 17
- 프레임워크: Spring Boot 4.0.6
- 빌드 도구: Gradle
- ORM: Spring Data JPA / Hibernate
- 데이터베이스:
  - 로컬 / 테스트: H2
  - 개발 / 운영: MySQL 8.x
- DB 마이그레이션: Flyway
- 유틸리티: Lombok
- 공통 모듈:
  - `common-web:0.1.0-SNAPSHOT`
  - `common-logging:0.1.0-SNAPSHOT`
  - `common-core:0.1.0-SNAPSHOT` (`common-web` 전이 의존성)
- 보안 유틸리티: `spring-security-crypto`
- 테스트: JUnit 5, Mockito, AssertJ, Spring Boot Web MVC Test, Spring Boot Data JPA Test
- API 문서화: Spring REST Docs, Asciidoctor

## 기준

- Java 17의 현대 문법(Record, Sealed Class, Text Block 등)을 적극 활용한다.
