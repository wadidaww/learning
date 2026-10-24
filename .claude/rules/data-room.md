---
paths:
  - "app/src/main/java/com/kresna/aitopicapp/data/local/**/*.kt"
  - "app/src/main/java/com/kresna/aitopicapp/di/DatabaseModule.kt"
---

# Room / DataStore rules

- Any change to an `@Entity` (column added/removed/renamed, type change, index, foreign key) requires:
  1. bump `version` in `AppDatabase`,
  2. add `MIGRATION_N_N+1` in `AppDatabase.companion`,
  3. register it in `DatabaseModule.provideDatabase().addMigrations(...)`,
  4. a migration test when test infra exists.
  Use the `room-migration` skill. `fallbackToDestructiveMigration()` is enabled, so forgetting this silently deletes all user topics and content.
- SQLite cannot add a foreign key with `ALTER TABLE`; adding one means create-new-table, copy, drop, rename inside the migration.
- New columns added by migration must be nullable or have a `DEFAULT` that matches the entity's Kotlin default.
- `ContentTypeConverter` and `ContentType.fromString` silently fall back to `MCQ` on unknown values; log a warning when that happens rather than hiding it.
- DAO queries that feed UI lists need a deterministic `ORDER BY` (add `id` as a tiebreaker; rows in one batch share almost the same `createdAt`).
- Settings defaults (`modelId`, `region`) are duplicated in `SettingsDataStore`, `AppSettings`, and `SettingsViewModel`; change all of them or, better, centralize them.
- The API key is stored in plain DataStore and `android:allowBackup="true"`; don't log it, and don't add it to share/copy text.
