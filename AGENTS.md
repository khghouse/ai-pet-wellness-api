# AGENTS.md

Behavioral guidelines to reduce common Codex coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

## 프로젝트 컨텍스트 작업 흐름

코드 변경을 시작하기 전에 이 파일과 `docs/` 아래의 관련 문서를 먼저 확인한다.

문서는 다음 기준으로 참고한다.

- `docs/requirements.md`: 기능 범위와 완료 기준
- `docs/architecture/`: 기술 스택, 패키지 구조, 레이어 규칙
- `docs/conventions/`: 코딩, 테스트, 예외 처리, API 응답, 네이밍 규칙
- `docs/context/tasks.md`: 진행하기로 결정한 작업
- `docs/context/backlog.md`: 후보 작업과 미룬 이슈
- `docs/context/progress.md`: 완료 작업, 검증 결과, 인수인계 메모
- `docs/adr/`: 승인된 아키텍처 결정

기능 구현이나 의미 있는 변경을 진행할 때는 다음 흐름을 따른다.

1. `docs/requirements.md` 또는 사용자 요청에서 작업 단위를 도출한다.
2. 작업을 계획하거나 시작할 때 `docs/context/tasks.md`에 항목을 추가하거나 갱신한다.
3. 다음처럼 이후 구현 방향에 계속 영향을 주는 결정은 `docs/adr/`에 기록한다.
   - 프레임워크, 데이터베이스, 인증 방식, 외부 연동 방식 선택
   - 패키지 구조, 레이어 규칙, 모듈 경계 변경
   - 공통 응답 형식, 예외 처리 방식, 테스트 전략 변경
   - 되돌리기 어렵거나 여러 기능에 영향을 주는 기술 선택
4. 지금 처리하지 않는 후속 이슈나 아이디어는 `docs/context/backlog.md`에 기록한다.
5. 검증이 끝나면 `docs/context/progress.md`에 완료 작업과 검증 결과를 기록한다.
6. 작업을 마치면 변경 내용에 맞는 한글 Conventional Commit 메시지를 추천한다.

코드 변경을 완료하기 전에 `./gradlew check`를 실행한다. 포맷 위반이 있으면 `./gradlew spotlessApply`로 수정한 뒤 다시 검증한다.
