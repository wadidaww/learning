---
paths:
  - "app/src/main/java/com/kresna/aitopicapp/util/PromptBuilder.kt"
  - "app/src/main/java/com/kresna/aitopicapp/util/ContentParser.kt"
  - "app/src/main/java/com/kresna/aitopicapp/data/repository/ContentRepositoryImpl.kt"
  - "app/src/main/java/com/kresna/aitopicapp/data/remote/**/*.kt"
---

# AI prompt <-> parser contract

`PromptBuilder` defines the JSON shape we ask for; `ContentParser` defines what we accept. Treat them as one contract and change both together.

| Type | Required fields | Optional | Stored as |
|------|-----------------|----------|-----------|
| MCQ | `question`, `options` (4 strings), `correctIndex` (0-3) | `explanation`, `codeSnippet`, `codeLanguage` | `question`, `options`, `correctIndex`, `explanation` |
| QA | `question`, `answer` | `codeSnippet`, `codeLanguage` | `question`, `answer` |
| FUN_FACT | `fact` | `source`, `codeSnippet`, `codeLanguage` | `question="Did you know?"`, `answer=fact`, `explanation=source` |
| MIX | array of the above, each with `"type"` = `MCQ` / `QA` / `FUN_FACT` | | per-item type |

## Parser rules
- Models return messy output: code fences, leading prose, trailing commas, explicit `null`s, numbers as strings, lowercase types. Be lenient on shape, strict on meaning.
- Read optional fields null-safely: `el.takeIf { it != null && !it.isJsonNull }?.asString`. `JsonNull.asString` throws and fails the entire batch.
- Validate each item and skip (with a `Log.w` that includes the index and reason) instead of failing the batch: blank question/fact, MCQ without 2+ options, `correctIndex` out of range.
- A response that cannot be parsed at all must return `Result.failure` with a user-safe message, and log the first ~2KB of the raw text under tag `ContentParser`. Never turn a parse failure into `success(emptyList())`.

## Dedup rules
- Dedup key must be the content the user sees: `answer` for FUN_FACT, `question` otherwise. Compare normalized text (trim, lowercase, collapse whitespace).
- `existingQuestions` sent to the prompt should be the most recent N (needs `ORDER BY createdAt DESC LIMIT N` in the DAO), not an arbitrary N.

## Network
- Keep `max_tokens` large enough for `DEFAULT_BATCH_SIZE` items with code snippets; if `finish_reason == "length"` the JSON is truncated — surface that as "Response was cut off; try a smaller batch".
- Never log the `Authorization` header or the API key.

After editing any of these files, run the `ai-contract-checker` agent.
