# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this is

**AI Topic Learning** — a single-module Android app (Kotlin, Jetpack Compose, Material3).
The user creates *topics*; the app asks a BytePlus ModelArk chat-completions model to
generate learning content (MCQ, Q&A, Fun Fact, or a Mix) as a JSON array, parses it,
stores it in Room, and shows it in a swipeable pager.

Package root: `app/src/main/java/com/kresna/aitopicapp/`

```
domain/   models, repository interfaces, use cases (pure Kotlin, no Android)
data/     Room (entities, DAOs, converters, AppDatabase), DataStore settings,
          Retrofit API + DTOs, repository implementations
di/       Hilt modules (DatabaseModule, NetworkModule, DispatcherModule)
ui/       screens/{home,topic,content,settings} (Screen + ViewModel each),
          components/ (cards, dialogs), navigation/NavGraph.kt, theme/
util/     PromptBuilder (prompt text), ContentParser (AI JSON -> domain), Constants
```

### Core flow (read these first when debugging generation)

1. `TopicDetailViewModel.generateContent()` or `ContentPagerViewModel.loadMoreContent()` (prefetch)
2. `GenerateContentUseCase` -> `ContentRepositoryImpl.generateContent()`
3. reads settings (`SettingsDataStore`), existing questions (`ContentDao`), builds prompt (`PromptBuilder`)
4. POST `chat/completions` (Retrofit built per call from region base URL; auth header added in `NetworkModule` interceptor)
5. `ContentParser.parseResponse` / `parseMixResponse` -> filter duplicates -> `ContentDao.insertAll`
6. `ContentPagerViewModel` observes the DAO `Flow`, lays it out with `FeedLayout` (runs of 10 cards + a `RunSummaryCard` + a trailing status page) and `ContentPagerScreen` renders it as a full-screen `VerticalPager` feed of `MCQCard` / `QACard` (flashcard) / `FunFactCard` (Spark)

### Navigation

`home` -> `topic/{topicId}` (generate) -> `content/{topicId}/{contentType}` (pager); `settings`.
Home "smart click": a topic that already has content jumps straight to the pager using the
*last generated* content type (`ContentDao.getLastContentTypeForTopic`).

## Commands

The Gradle wrapper targets Gradle 8.11.1 (AGP 8.7.2, Kotlin 2.0.21, JDK 17, compile/target SDK 34, min SDK 26).

```bash
./gradlew assembleDebug            # build debug APK
./gradlew installDebug             # install on connected device/emulator
./gradlew testDebugUnitTest        # JVM unit tests (app/src/test)
./gradlew lintDebug                # Android lint -> app/build/reports/lint-results-debug.html
./gradlew connectedDebugAndroidTest  # instrumented tests (needs device)
```

A build needs an Android SDK (`local.properties` with `sdk.dir=...` or `ANDROID_HOME`).
**Claude Code cloud sessions have no Android SDK** (and `dl.google.com` is blocked), so
they cannot compile. In the cloud, rely on the GitHub Actions workflow
(`.github/workflows/android.yml`) to compile, test and lint a PR, and say clearly in the PR
that the change was not built locally.

**Screenshots without a device:** `FeedScreenshotTest` (Paparazzi) renders feed cards to PNGs during
`testDebugUnitTest` (`app/build/reports/paparazzi/`); CI uploads them as the `screenshots` artifact.
Cloud sessions can't download artifacts, so on PRs CI also prints JPEG copies to the job log between
`=== SCREENSHOT-BEGIN <file>` / `=== SCREENSHOT-END` markers: fetch the log with the GitHub MCP
`get_job_logs` (`return_content=true`, large `tail_lines`; big results are saved to a file), strip the
timestamp prefix from each line and base64-decode the lines between the markers.

## Conventions

- Clean Architecture: UI -> use case -> repository interface (domain) -> impl (data). ViewModels never touch DAOs or Retrofit directly.
- State: each ViewModel exposes `StateFlow`s; one-shot messages go through a `MutableSharedFlow<String>` collected into a `SnackbarHostState`. Shared `UiState<T>` (`Loading` / `Success` / `Error`) lives in `ui/common/UiState.kt`.
- Repositories and use cases return `Result<T>`; do not throw across layers.
- Compose: screens take callbacks + `hiltViewModel()`; components are stateless where possible. Use `MaterialTheme` colors and the palette in `ui/theme/Color.kt`; card-type gradients come from `ui/theme/CardStyle.kt`. The app is dark-first (`AITopicLearningTheme(darkTheme = true, dynamicColor = false)`).
- Feed cards are built on `FeedCard` (full-bleed, leaves room for the overlay header and the right-hand action rail); use `rememberFeedHaptics()` for answer feedback.
- New user-facing strings: keep them short, specific, and actionable (see `.claude/rules/ui-compose.md`).
- Keep `PromptBuilder` (what we ask for) and `ContentParser` (what we accept) in sync — they are one contract.
- Room schema changes require a version bump **and** a `Migration` added in `AppDatabase` + registered in `DatabaseModule`. `fallbackToDestructiveMigration()` is on, so a forgotten migration silently wipes user data.

## Known pitfalls (verified on `main` as of 2026-10-23)

Open PR #1 (`claude/sleepy-carson-2gnu2r`) rewrites the parser, dedup, pager ordering and
topic deletion and addresses most of these. Once it merges, re-check this list and delete
the entries it fixed.

- `ContentParser` uses `jsonObject.get("x")?.asString`; an explicit JSON `null` is a `JsonNull`, and `asString` on it throws, failing the whole batch.
- `ContentParser.parseJsonArray` returns `Result.success(emptyList())` on malformed JSON, so parse failures look like "no new content".
- Fun facts all use `question = "Did you know?"`; the duplicate filter in `ContentRepositoryImpl` compares on `question`, so after the first fun-fact batch every new fun fact is dropped.
- The content DAO orders by `createdAt DESC`; when a prefetch inserts rows, the pager list is re-emitted newest-first and the current page shifts. Card state (`remember { }` in `MCQCard`/`QACard`) is keyed by page index, not content id.
- `generated_content` has no foreign key to `topics`; deleting a topic orphans its content.
- `HomeViewModel` sets `Success(emptyList())` before Room's first emission, so "No topics yet" flashes on launch.

## Debugging

See the `debug-android` skill (`.claude/skills/debug-android/SKILL.md`) for logcat filters, adb DB/DataStore inspection and how to capture the raw AI response.
Existing log tags: `ContentParser`. There is no HTTP logging yet (`okhttp-logging` is a dependency but not wired up).

## Claude tooling in this repo

- `.claude/rules/` — path-scoped rules loaded when editing matching files.
- `.claude/agents/` — `bug-hunter`, `ux-reviewer`, `android-debugger`, `ai-contract-checker`.
- `.claude/skills/` — `debug-android`, `room-migration`, `add-content-type`, `verify-change`.
- `.claude/settings.json` — permission allowlist + a PostToolUse hook (`.claude/hooks/kotlin-edit-guard.sh`) that reminds about migrations and prompt/parser sync.
- `.mcp.json` — optional `mobile-mcp` server to let Claude drive a local emulator/device (screenshots, taps) for GUI checks.
