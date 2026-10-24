---
name: verify-change
description: Verify a code change in this Android app before calling it done - compile, unit tests, lint, review agents, and an on-device check - adapting to whether an Android SDK/device is available (local machine) or not (cloud session, rely on CI). Use before committing or opening a PR.
---

# Verify a change

## 1. Decide where you can build
```bash
[ -n "$ANDROID_HOME" ] || [ -f local.properties ] && echo "SDK available" || echo "NO SDK"
adb devices 2>/dev/null | sed 1d | grep -w device && echo "device available"
```
- **SDK available**: run step 2 locally.
- **No SDK** (Claude Code cloud sessions): you cannot compile. Read every changed file carefully for missing imports, wrong types and unresolved references, push, and rely on the **Android CI** GitHub Actions workflow. State in the PR body that it was not built locally, and check the workflow result before saying it works.

## 2. Build, test, lint
```bash
./gradlew assembleDebug testDebugUnitTest --console=plain
./gradlew lintDebug --console=plain     # report: app/build/reports/lint-results-debug.html
```
Fix compile errors and test failures. For lint, fix new warnings in files you touched; don't mass-fix unrelated ones in the same PR.

## 3. Review
- Logic or state changes: run the `bug-hunter` agent on the diff.
- UI changes: run the `ux-reviewer` agent on the touched screens.
- Prompt/parser/repository changes: run the `ai-contract-checker` agent.
- Entity changes: confirm the `room-migration` checklist was followed.

## 4. On device (when available)
```bash
./gradlew installDebug && adb shell am start -n com.kresna.aitopicapp/.MainActivity
```
Walk the affected journey, take a screenshot (`adb exec-out screencap -p > after.png`), check dark mode, and check logcat for new warnings (`debug-android` skill).

## 5. Report
In the commit/PR: what changed, how it was verified (built locally / CI only / device-tested), and anything not verified.
