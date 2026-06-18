# 0002. Spring Boot 4 사용

## 상태

승인됨

## 배경

초기 기술 스택은 익숙한 Java 17과 Spring Boot 3.3.x를 기준으로 정했다. 그러나 Spring Boot 3.3.x는 오픈소스 지원이 종료되어 신규 공개 프로젝트의 기반 버전으로 유지하기에는 장기적인 보안과 유지보수 측면에서 적절하지 않다.

이 프로젝트는 아직 애플리케이션 코드가 없어 메이저 버전 전환 비용이 낮다. Spring Boot 4.0.6은 Java 17과 Gradle 8.14를 지원하므로 기존 Java와 빌드 도구 버전을 유지할 수 있다.

## 결정

- Spring Boot 4.0.6을 사용한다.
- Java 17과 Gradle 8.14는 유지한다.
- Spring MVC 의존성은 Spring Boot 4에서 권장하는 `spring-boot-starter-webmvc`를 사용한다.

## 결과

- 지원 중인 Spring Boot 버전을 기반으로 기능 개발을 시작할 수 있다.
- Spring Framework 7과 Jakarta EE 11 기반 기술 스택을 사용한다.
- Spring Boot 3 기반 예제나 라이브러리를 참고할 때 4.0 호환성을 확인해야 한다.
- 향후 추가하는 starter와 테스트 의존성은 Spring Boot 4의 모듈화된 구조를 따른다.

## 검토한 대안

- Spring Boot 3.3.x 유지:
  - 이유: 익숙한 버전이지만 오픈소스 지원이 종료되어 채택하지 않는다.
- Spring Boot 3.5.x 사용:
  - 이유: 3.x 호환성을 유지할 수 있지만, 애플리케이션 코드가 없는 현재는 4.0으로 바로 전환하는 비용이 낮다.
