---
name: debug-android
description: Debug the AI Topic Learning app on a device or emulator - logcat filters, crash capture, inspecting the Room database and DataStore settings, capturing the raw AI response, and resetting app state. Use when reproducing a bug, investigating a crash, or when generated content looks wrong.
---

# Debugging the app on a device

App id: `com.kresna.aitopicapp` · DB file: `aitopicapp.db` · DataStore: `files/datastore/settings.preferences_pb`

All `run-as` commands need a **debug** build (`./gradlew installDebug`).

## 1. Check the device
```bash
adb devices -l                     # must list exactly one device, or pass -s <serial>
adb shell pidof com.kresna.aitopicapp
```

## 2. Logcat
```bash
adb logcat -c                                                         # clear first, then reproduce
adb logcat --pid=$(adb shell pidof com.kresna.aitopicapp) -v time      # only this app
adb logcat -v time ContentParser:V OkHttp:V AndroidRuntime:E '*:S'     # parser + HTTP + crashes
adb logcat -b crash -d                                                # last crash buffer
```
Current log tags: `ContentParser` (parse failures, skipped items). `OkHttp` appears only once an `HttpLoggingInterceptor` is wired in debug builds (not yet; see "Gaps").

Save evidence to a file so it can be attached to a bug: `adb logcat -d -v time > /tmp/aitopic-$(date +%s).log`.

## 3. Inspect the Room database
```bash
# Copy DB (+ WAL) out of the app sandbox
adb exec-out run-as com.kresna.aitopicapp cat databases/aitopicapp.db     > aitopic.db
adb exec-out run-as com.kresna.aitopicapp cat databases/aitopicapp.db-wal > aitopic.db-wal 2>/dev/null
sqlite3 aitopic.db "PRAGMA user_version;"                                 # schema version (AppDatabase.version)
sqlite3 aitopic.db "SELECT id,name,defaultContentType FROM topics;"
sqlite3 aitopic.db "SELECT topicId,contentType,COUNT(*) FROM generated_content GROUP BY 1,2;"
sqlite3 aitopic.db "SELECT id,contentType,substr(question,1,60),correctIndex,length(options) FROM generated_content WHERE topicId=1 ORDER BY createdAt DESC LIMIT 20;"
# Orphaned content (topic deleted, content left behind):
sqlite3 aitopic.db "SELECT COUNT(*) FROM generated_content WHERE topicId NOT IN (SELECT id FROM topics);"
```
Android Studio's **App Inspection > Database Inspector** shows the same live, and lets you run queries against the running app.

## 4. Inspect settings (DataStore)
```bash
adb exec-out run-as com.kresna.aitopicapp cat files/datastore/settings.preferences_pb | strings
```
Shows `api_key`, `model_id`, `region` values. Look for leading/trailing spaces in the key. **Never paste the API key into chat, a PR, or an issue.**

## 5. Capture the raw AI response
When content is missing or wrong, the question is always "what did the model actually return?".
- Today: `ContentParser` logs only the exception, not the text. Temporarily add, in `ContentRepositoryImpl.generateContent` after reading `text`:
  `if (BuildConfig.DEBUG) Log.d("ContentRepo", "raw[${text.length}]: ${text.take(4000)}")`
  (requires `buildFeatures { buildConfig = true }` in `app/build.gradle.kts`).
- Then reproduce with: `adb logcat -v time ContentRepo:D ContentParser:V '*:S'`.
- Feed the captured text into a JVM unit test for `ContentParser` so the bug stays fixed.

## 6. Reset state
```bash
adb shell pm clear com.kresna.aitopicapp          # wipe DB + settings
adb shell am force-stop com.kresna.aitopicapp
adb shell am start -n com.kresna.aitopicapp/.MainActivity
```

## 7. UI debugging
- Screenshot: `adb exec-out screencap -p > screen.png` (then Read the PNG).
- Layout bounds: `adb shell setprop debug.layout true && adb shell service call activity 1599295570`.
- Dark mode: `adb shell cmd uimode night yes|no`. Font scale: `adb shell settings put system font_scale 2.0`.
- If the `mobile` MCP server from `.mcp.json` is enabled, prefer its screenshot / tap / swipe tools.
- Compose recomposition counts: Android Studio Layout Inspector.

## Gaps (worth fixing in app code)
- No `HttpLoggingInterceptor` in debug builds (dependency already present).
- Raw AI response is never logged; parse failures become an empty list.
- No `buildConfig` feature enabled, no debug-only settings screen (clear DB, show raw last response).
- `exportSchema = false`, so there is no schema history to diff.
