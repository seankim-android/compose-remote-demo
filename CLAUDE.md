# CLAUDE.md -- compose-remote-demo

A small runnable demo of Jetpack Compose Remote: Ktor server emits a remote composition, Android client renders it. Edit the layout server-side, no client rebuild.

## Tech stack

- Server: Kotlin, Ktor 3.x (Netty), kotlinx.serialization, Gradle (Kotlin DSL)
- Client: Kotlin, Android (Compose), AGP, Gradle (Kotlin DSL)
- Compose Remote: AndroidX Compose Remote `1.0.0-alpha05` (alpha; APIs will move)

## Layout

```
compose-remote-demo/
├── server/    # Ktor server. Emits remote compositions over HTTP.
├── android/   # Android client. Fetches and renders.
├── shared/    # Optional shared types/schemas.
└── docs/      # Notes, diagrams, screenshots.
```

## Harness loop

Engineer -> QA -> Architect -> loop. Any stage can reject back to Engineer with specific feedback.

| Stage | Focus |
|-------|-------|
| Engineer | Minimal code that solves the problem. Run tests before and after. |
| QA | Failing test first, make it pass, refactor. Full suite must pass. |
| Architect | Correctness, simplicity, bloat check, conventions, prior feedback addressed. |

## Principles

- Understand the goal before writing code.
- Failing test first, then implementation, then refactor.
- Debug systematically: reproduce, isolate, root-cause, fix.
- Never say "done" without running tests and showing output.
- Ship in small, verifiable pieces.
- Do not build what was not asked for.

## Quality gates

- [ ] All existing tests pass
- [ ] New tests for new functionality
- [ ] No hardcoded secrets or tokens
- [ ] Architect review passed
- [ ] Changes are minimal

## Style

- No em dashes anywhere in code, comments, UI copy, or docs
- Direct, understated prose with short sentences
- No corporate or AI-sounding language
- No hedging, filler, or excessive transition words
- All markdown files and user-facing strings must pass an anti-AI-slop check. Run `/humanizer` on written content before finalizing. No "serves as", "tapestry", "landscape" (abstract), "foster", "leverage", "delve", "underscore", "highlight" (verb), "crucial", "pivotal", rule-of-three lists, superficial -ing phrases, or promotional puffery. Write like a person, not a press release.

## Notes

- Compose Remote is alpha; pin upstream deps loosely and expect churn.
- `bootstrap.sh` exists for forks that want to regenerate scaffolds against current Ktor/AGP.
- App talks to server at `http://10.0.2.2:8080` from the emulator.
