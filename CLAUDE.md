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
          components/ (cards, dialogs), navigation/NavGraph.kt, theme/,
          widget/ (home-screen "Daily card" widget, RemoteViews)
util/     PromptBuilder (prompt text), ContentParser (AI JSON -> domain), Constants
```

### Core flow (read these first when debugging generation)

1. `TopicDetailViewModel.generateContent()` or `ContentPagerViewModel.loadMoreContent()` (prefetch)
2. `GenerateContentUseCase` -> `ContentRepositoryImpl.generateContent()`
3. reads settings (`SettingsDataStore`), existing questions (`ContentDao`), builds prompt (`PromptBuilder`)
4. POST `chat/completions` (Retrofit built per call from region base URL; auth header added in `NetworkModule` interceptor)
5. `ContentParser.parseResponse` / `parseMixResponse` -> filter duplicates -> `ContentDao.insertAll`
6. `ContentPagerViewModel` observes the DAO `Flow`, lays it out with `FeedLayout` (runs of 10 cards + a `RunSummaryCard` + a trailing status page) and `ContentPagerScreen` renders it as a full-screen `VerticalPager` feed of `MCQCard` / `QACard` (flashcard) / `FunFactCard` (Spark)

### Home-screen widget

`ui/widget/DailyCardWidgetProvider` (Hilt `AppWidgetProvider`, classic RemoteViews, layout
`res/layout/widget_daily_card.xml`) shows one card a day from all topics. `GetDailyCardUseCase`
-> `DailyCardRepository` (candidates via `ContentDao.getDailyCandidates`, today's pick kept in
`shared_prefs/daily_card.xml`) -> `DailyCardPicker.choose` (pure, unit-tested: keeps the day's pick,
rotates topics, prefers unviewed cards). Refreshes: when placed, ~1 min after midnight (inexact
alarm), every 6 h fallback, time/zone change, and `MainActivity.onStop`. A tap launches
`MainActivity` with `DailyCardLink` extras -> `NavGraph(startCard)` -> pager route with
`startContentId`. Widget colours are XML resources mirroring `Color.kt` (values / values-night).

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
- Compose: screens take callbacks + `hiltViewModel()`; components are stateless where possible. Use `MaterialTheme` colors, and for accents `AppTheme.palette` (soft pastels at night, deeper tones by day, from `ui/theme/Color.kt`); never hard-code a hex colour in a component. Card-type accents and icons come from `ui/theme/CardStyle.kt`. The app follows the system light/dark setting (`AITopicLearningTheme(dynamicColor = false)`), so check both. Keep it easy on the eyes: no pure black or white, tints at about 0.12-0.16 alpha rather than saturated fills. Use `Icons.Rounded.*` (and `Icons.AutoMirrored.Rounded.*`) icons, not emoji.
- Feed cards are built on `FeedCard` (full-bleed, leaves room for the overlay header and the right-hand action rail); use `rememberFeedHaptics()` for answer feedback.
- New user-facing strings: keep them short, specific, and actionable (see `.claude/rules/ui-compose.md`).
- Keep `PromptBuilder` (what we ask for) and `ContentParser` (what we accept) in sync — they are one contract.
- The ModelArk model and region list lives in `domain/model/AiModelCatalog.kt` (source: https://ai.byteplus.com/ark/region:ap-southeast-1/docs/model-list; the docs.byteplus.com copy is JS-rendered and unreadable from the cloud). Settings, the settings store (which migrates old ids through `AiModelCatalog.resolve`) and `AiModelCatalogTest` all read from it. Model ids carry a `yymmdd` version suffix; never add one without it.
- Room schema changes require a version bump **and** a `Migration` added in `AppDatabase` + registered in `DatabaseModule`. `fallbackToDestructiveMigration()` is on, so a forgotten migration silently wipes user data.

## Known pitfalls

The parser, dedup, pager ordering and topic deletion bugs listed here before PR #1 merged are fixed on `main` (verified 2026-11-02). Things that still bite:

- Compose code can't be compiled in the cloud; a change that only "looks right" needs CI (and ideally a Paparazzi snapshot) before it is called done.
- `FeedLayout.build` and `FeedLayout.pageOf` must stay consistent: a card's page index is `contentIndex + contentIndex / FEED_RUN_SIZE` only while every full run is followed by exactly one summary page.
- Quiz options from the model may carry "A. " prefixes; show them through `OptionText.stripEnumerators` (card, clipboard and share all do) so they agree.

## Debugging

See the `debug-android` skill (`.claude/skills/debug-android/SKILL.md`) for logcat filters, adb DB/DataStore inspection and how to capture the raw AI response.
Existing log tags: `ContentParser`, `DailyCardWidget` (which card the widget shows, load failures). There is no HTTP logging yet (`okhttp-logging` is a dependency but not wired up).

## Claude tooling in this repo

- `.claude/rules/` — path-scoped rules loaded when editing matching files.
- `.claude/agents/` — `bug-hunter`, `ux-reviewer`, `android-debugger`, `ai-contract-checker`.
- `.claude/skills/` — `debug-android`, `room-migration`, `add-content-type`, `verify-change`.
- `.claude/settings.json` — permission allowlist + a PostToolUse hook (`.claude/hooks/kotlin-edit-guard.sh`) that reminds about migrations and prompt/parser sync.
- `.mcp.json` — optional `mobile-mcp` server to let Claude drive a local emulator/device (screenshots, taps) for GUI checks.
