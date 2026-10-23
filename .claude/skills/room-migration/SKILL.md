---
name: room-migration
description: Safely change the Room schema (add/rename a column, add a table, index, or foreign key) with a version bump and a hand-written migration so user data survives. Use whenever an @Entity, AppDatabase, or DatabaseModule changes.
---

# Room schema change checklist

Files: `data/local/entity/*.kt`, `data/local/AppDatabase.kt`, `di/DatabaseModule.kt`.
Current: `version = 4`, migrations `MIGRATION_2_3`, `MIGRATION_3_4`; **no 1->2**; `fallbackToDestructiveMigration()` is on, so any missing path wipes data silently.

## Steps
1. Edit the entity. New columns: nullable, or with a Kotlin default **and** a matching SQL `DEFAULT`.
2. Bump `version` in `@Database` by exactly 1 (N -> N+1).
3. Add the migration to `AppDatabase.companion`:
   ```kotlin
   val MIGRATION_4_5 = object : Migration(4, 5) {
       override fun migrate(db: SupportSQLiteDatabase) {
           db.execSQL("ALTER TABLE generated_content ADD COLUMN difficulty TEXT")
       }
   }
   ```
4. Register it: `.addMigrations(AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5)` in `DatabaseModule`.
5. SQL must produce exactly what Room expects (column type affinity, NOT NULL, defaults, indices). Room validates on open and throws `IllegalStateException: Migration didn't properly handle ...` with expected vs found — read both carefully.

## Foreign keys / table rebuilds
SQLite cannot `ALTER TABLE ... ADD CONSTRAINT`. To add e.g. `topicId -> topics(id) ON DELETE CASCADE`:
```sql
CREATE TABLE generated_content_new (... same columns ...,
  FOREIGN KEY(topicId) REFERENCES topics(id) ON DELETE CASCADE);
INSERT INTO generated_content_new SELECT ... FROM generated_content WHERE topicId IN (SELECT id FROM topics);
DROP TABLE generated_content;
ALTER TABLE generated_content_new RENAME TO generated_content;
CREATE INDEX index_generated_content_topicId ON generated_content(topicId);
```
and declare `foreignKeys = [ForeignKey(...)]`, `indices = [Index("topicId")]` on the entity.

## Strongly recommended (one-time)
- Turn on schema export: `exportSchema = true` and in `app/build.gradle.kts`
  `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`, commit `app/schemas/`.
- Add `androidTestImplementation("androidx.room:room-testing:<room version>")` and a `MigrationTestHelper` test per migration.
- Consider replacing `fallbackToDestructiveMigration()` with `fallbackToDestructiveMigrationFrom(1)` so only the known-missing path is destructive.

## Verify
- On a device with the *old* build installed and data present: install the new build over it (`./gradlew installDebug`, no uninstall), open the app, confirm data is still there (see `debug-android` skill, section 3: `PRAGMA user_version` should be the new N).
