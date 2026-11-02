---
paths:
  - "app/src/main/java/com/kresna/aitopicapp/ui/**/*.kt"
---

# Compose UI rules (goal: zero user confusion)

## Every screen state is explicit
- Handle Loading, Success-with-data, Success-empty, and Error. Never show an empty state before the first real emission (seed flows with `Loading`, not `emptyList()`).
- An empty state says *why* it is empty and offers the next action as a button, not just text.
- Long-running work shows progress *and* what is happening ("Generating 10 questions…"), and the trigger is disabled while running.

## Errors the user can act on
- Never show raw exception text (`UnknownHostException`, `timeout`, `HTTP 401`). Map to a plain message + next step, e.g. "No internet connection. Check your network and try again." / "API key rejected. Open Settings to update it." Keep the raw message in logcat.
- Background failures (prefetch, mark-as-viewed) must be visible somewhere (snackbar or inline row with Retry) if they stop the user from getting more content.

## Destructive actions
- Deleting anything needs either a confirmation dialog or an Undo snackbar. Swipe-to-delete must offer Undo.
- Dialog copy must state what is deleted (e.g. "This also deletes 24 generated items").

## State keyed to data, not position
- In `HorizontalPager` / `LazyColumn`, pass a stable `key` (content id) and use `remember(content.id) { … }` / `rememberSaveable` for per-item state (selected option, revealed answer).
- Exception: the learning feed's `VerticalPager` has no keys on purpose, so that when a batch arrives while the user sits on the status page the first new card takes its place. Its cards key all per-item state by `content.id` instead, so page reuse is safe.
- Do not build `Flow`s inside composables without `remember(key)`; a new flow each recomposition resubscribes.

## Discoverability
- Hidden gestures (long-press to edit, swipe to delete) need a visible alternative (overflow menu / icon) or a one-time hint.
- Counters scale: use "3 / 42" text instead of one dot per page when the count can exceed ~8.

## Consistency
- Colors from `MaterialTheme.colorScheme`; accents (success, error, card types) from `AppTheme.palette`. The app follows the system theme, so check both light and dark.
- Icon buttons need a specific `contentDescription` ("Copy question", not "Copy"); decorative emoji/icons use `contentDescription = null` / `Modifier.clearAndSetSemantics {}`.
- Touch targets ≥ 48dp. Don't overlay icon buttons on top of text content.
- Pluralize counts correctly ("1 item", "2 items").
- New user-visible strings go to `res/values/strings.xml` (`stringResource(R.string.…)`); the feed components are done, older screens still hard-code and are migrated as they are touched.
