---
name: add-content-type
description: Add a new learning content type (e.g. flashcard, true/false, fill-in-the-blank) end to end - enum, prompt, parser, storage, card UI, share/copy, pager routing. Use when the user asks for a new kind of generated content.
---

# Adding a content type

Every place that switches on `ContentType` must handle the new value. Find them all first:
```bash
grep -rn "ContentType\.\(MCQ\|QA\|FUN_FACT\|MIX\)" app/src/main/java | sort
```

## Touch points (in order)
1. `domain/model/ContentType.kt` — add the enum entry with a `displayName`. Note `fromString` falls back to `MCQ` for unknown strings.
2. `util/PromptBuilder.kt` — add `buildXxxPrompt(topic, context, count, existingQuestions)` with an explicit JSON example; add the type to `buildMixPrompt` (counts live in `Constants.MixContent`).
3. `util/ContentParser.kt` — add `parseXxx` (null-safe field access; validate; skip bad items with `Log.w`), route it in `parseItemByType`, and accept its `"type"` string in `parseMixResponse`.
4. `data/repository/ContentRepositoryImpl.kt` — add the prompt branch in the `when (contentType)`; make sure the dedup key for the new type is a field that varies per item.
5. Storage — map fields onto existing `ContentEntity` columns if possible (`question`, `answer`, `options`, `correctIndex`, `explanation`, `codeSnippet`, `codeLanguage`). If a new column is needed, follow the `room-migration` skill.
6. `ui/components/XxxCard.kt` — wrap in `ContentCardWrapper`; per-item state with `remember(content.id)`; include copy action and `CodeBlock` when `codeSnippet != null`.
7. `ui/screens/content/ContentPagerScreen.kt` — route the type in the `when` inside `HorizontalPager`.
8. `ui/components/ShareButton.kt` — add a `buildShareText` branch.
9. Chips in `TopicDialog` / `TopicDetailScreen` iterate `ContentType.entries`, so they pick it up automatically — check the label fits.
10. Update the contract table in `.claude/rules/ai-contract.md` and `CLAUDE.md` if needed.

## Verify
- Run the `ai-contract-checker` agent.
- Add a parser unit test with a realistic model response for the new type.
- Generate on a device for a coding topic and a non-coding topic; check the card in light and dark mode.
