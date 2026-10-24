#!/usr/bin/env bash
# PostToolUse hook for Edit/Write/MultiEdit.
# Adds a reminder to Claude's context when a file that is part of a fragile contract changes.
# No dependencies beyond bash + sed, so it works on macOS, Linux and Git Bash.

input="$(cat)"
file="$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n1)"

[ -z "$file" ] && exit 0

msg=""
case "$file" in
  */data/local/entity/*.kt)
    msg="Room entity changed. Bump AppDatabase.version, add MIGRATION_N_N+1, and register it in DatabaseModule.addMigrations (see room-migration skill). fallbackToDestructiveMigration is on, so a missing migration silently wipes user data."
    ;;
  */util/PromptBuilder.kt)
    msg="PromptBuilder changed. ContentParser must accept exactly the fields and type names the prompt asks for. Update the table in .claude/rules/ai-contract.md and run the ai-contract-checker agent."
    ;;
  */util/ContentParser.kt)
    msg="ContentParser changed. Keep it in sync with PromptBuilder, read optional fields null-safely (JsonNull.asString throws), never turn a parse failure into an empty success. Run the ai-contract-checker agent."
    ;;
  */domain/model/ContentType.kt)
    msg="ContentType changed. Every when(contentType) must handle the new value: PromptBuilder, ContentParser, ContentRepositoryImpl, ContentPagerScreen, ShareButton. Follow the add-content-type skill."
    ;;
  */ui/*.kt)
    msg="UI file changed. Check .claude/rules/ui-compose.md: explicit loading/empty/error states, user-safe error text, per-item state keyed by content id, Undo for destructive actions. Consider the ux-reviewer agent."
    ;;
esac

[ -z "$msg" ] && exit 0

printf '{"hookSpecificOutput":{"hookEventName":"PostToolUse","additionalContext":"%s"}}\n' "$msg"
exit 0
