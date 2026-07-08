# REST Docs 규칙

## 테스트 패키지 구조

```text
src/test/java/{패키지 경로}/{프로젝트명}/
├── support/
│   └── RestDocsSupport.java
└── domain/
    └── {도메인명}/
        └── controller/
            └── {도메인명}ControllerDocsTest.java
```

### RestDocsSupport

- `@WebMvcTest` 없이 `@ExtendWith(RestDocumentationExtension.class)`만 사용한다.
- `MockMvcBuilders.standaloneSetup(initController())`으로 대상 Controller만 등록한다.
- 스니펫 경로는 `{class-name}/{method-name}` 패턴을 사용한다.
- `CharacterEncodingFilter("UTF-8", true)`를 추가한다.
- Controller Advice는 공통 예외 응답 검증을 위해 `GlobalExceptionHandler`를 등록한다.
- `ObjectMapper`는 Spring Boot 4의 Jackson 3 환경에 맞춰 `tools.jackson.databind.ObjectMapper`를 사용한다.

#### RestDocsSupport 직렬화 기준

```java
protected ObjectMapper objectMapper = new ObjectMapper();

this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
        .setControllerAdvice(new GlobalExceptionHandler())
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .apply(documentationConfiguration(provider))
        .alwaysDo(document)
        .build();
```

## REST Docs descriptor 기준

- `pathParameters`, `requestFields`, `responseFields`의 모든 descriptor에는 `.type()`을 명시한다.
- 필수 필드는 `.attributes(key("required").value("true"))`
- 선택 필드는 `.optional().attributes(key("required").value("false"))`
- 문서에서 필수/선택 여부는 description 본문에 `(필수)`, `(선택)`으로 섞어 쓰지 않고 별도 `Required` 컬럼으로 표현한다.
- `LocalDateTime` 필드는 descriptor에서 `JsonFieldType.STRING`으로 명시하고, 설명에 `ISO-8601` 문자열 형식임을 드러낸다.

## REST Docs 섹션 순서

- 문서 섹션은 아래 순서를 기본으로 유지한다.
- `HTTP request`
- `Path Parameters` 또는 `Request Parameters`가 있으면 그 다음에 배치한다.
- `Request fields`가 있으면 입력 파라미터 섹션 뒤에 배치한다.
- `HTTP response`
- `Response fields`
- 즉, 최종 순서는 `HTTP request -> Path Parameters/Request Parameters -> Request fields -> HTTP response -> Response fields`를 따른다.

## AsciiDoc 문서 구조

- `src/docs/asciidoc/index.adoc`는 문서 진입점으로만 사용하고, 개별 리소스/공통 문서는 별도 `.adoc` 파일로 분리한다.
- 기능별 문서는 `src/docs/asciidoc/sections/` 아래에 생성하고, `index.adoc`에서 `include::...[]`로 조합한다.
- 공통 응답 예시처럼 snippet 의존성이 낮고 고정 포맷이 필요한 내용은 include snippet 대신 하드코딩된 JSON 예시를 사용한다.
- Spring REST Docs 4 환경에서는 Asciidoctor 확장 호환성을 위해 `operation::` 매크로 대신 개별 snippet `include::`를 사용한다.
- 문서 파일이 비대해지면 `overview`, `common-response`, `todo`처럼 주제별로 분리하는 것을 기본 원칙으로 한다.

### index.adoc 구성 원칙

```adoc
= Pet Wellness API Documentation

include::src/docs/asciidoc/sections/overview.adoc[]
include::src/docs/asciidoc/sections/member.adoc[]
```
