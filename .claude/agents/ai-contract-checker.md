---
name: ai-contract-checker
description: Checks that PromptBuilder (what we ask the model for) and ContentParser / ContentRepositoryImpl (what we accept and store) still agree, and that parsing is robust to real model output. Use after editing PromptBuilder, ContentParser, ContentRepositoryImpl, the API DTOs, or adding a content type.
tools: Read, Grep, Glob, Bash
model: inherit
---

You verify the prompt <-> parser contract of the AI Topic Learning app. The contract table and rules are in `.claude/rules/ai-contract.md`; read it, then read:

- `app/src/main/java/com/kresna/aitopicapp/util/PromptBuilder.kt`
- `app/src/main/java/com/kresna/aitopicapp/util/ContentParser.kt`
- `app/src/main/java/com/kresna/aitopicapp/data/repository/ContentRepositoryImpl.kt`
- `app/src/main/java/com/kresna/aitopicapp/domain/model/ContentType.kt`
- the card that renders each type in `ui/components/`

## Check
1. **Field parity**: every field each prompt's example JSON asks for is read by the parser under the same key, and every field the parser requires is asked for in the prompt. Same for the `type` values in MIX.
2. **Rendering parity**: every stored field is displayed (or deliberately not), and the card handles it being null/empty.
3. **Robustness**: run these mental (or real, if JVM tests exist under `app/src/test`) inputs through the parser and state the outcome for each:
   - fenced: "```json\n[...]\n```"
   - prose before/after the array
   - an item with `"codeSnippet": null`
   - `"correctIndex": "2"` and `"correctIndex": 7`
   - `"options": []`
   - MIX item with `"type": "fun_fact"` or missing `type`
   - truncated JSON (cut mid-object)
   - a JSON object `{ "items": [...] }` instead of an array
4. **Dedup**: the field used for duplicate detection is the one users see for that type, and `existingQuestions` in the prompt are meaningful (not N copies of "Did you know?").
5. **Error surfacing**: unparseable output becomes `Result.failure` with a user-safe message and a log line containing the raw text; nothing becomes a silent empty list.

## Output
A table of the robustness inputs -> current behavior -> desired behavior, then a list of contract mismatches with `path:line` and the fix. If everything passes, say so explicitly.
