package com.kresna.aitopicapp.util

import com.kresna.aitopicapp.domain.model.ContentType
import com.kresna.aitopicapp.domain.model.Difficulty

object PromptBuilder {

    const val SYSTEM_PROMPT =
        "You are an expert teacher who writes accurate, engaging study material. " +
            "You always answer with valid JSON only: no markdown, no commentary."

    private val CODE_SNIPPET_INSTRUCTION = """
        Code: if an item involves programming, algorithms, data structures or another concept best shown with code,
        add a "codeSnippet" field with a short, correct, commented example and a "codeLanguage" field
        (e.g. "python", "kotlin", "javascript", "sql"). Omit both fields when code does not help.
    """.trimIndent()

    fun build(
        contentType: ContentType,
        topic: String,
        context: String,
        difficulty: Difficulty,
        count: Int,
        existingItems: List<String> = emptyList()
    ): String {
        val task = when (contentType) {
            ContentType.MCQ -> mcqTask(count)
            ContentType.QA -> qaTask(count)
            ContentType.FUN_FACT -> funFactTask(count)
            ContentType.MIX -> mixTask()
        }

        return buildString {
            appendLine("Topic: $topic")
            if (context.isNotBlank()) appendLine("Learner's focus / context: $context")
            appendLine("Level: ${difficulty.promptHint}")
            appendLine()
            appendLine(task)
            appendLine()
            appendLine("General rules:")
            appendLine("- Everything must be factually accurate; do not invent facts, quotes or sources.")
            appendLine("- Each item must teach something different; cover a broad range of sub-topics instead of rephrasing one idea.")
            appendLine("- Keep text concise and self-contained so it reads well on a phone screen.")
            appendLine()
            appendLine(CODE_SNIPPET_INSTRUCTION)
            val avoid = buildExistingItemsSection(existingItems)
            if (avoid.isNotEmpty()) {
                appendLine()
                appendLine(avoid)
            }
            appendLine()
            append("Return ONLY the JSON array, starting with [ and ending with ]. Escape newlines inside strings as \\n.")
        }
    }

    private fun mcqTask(count: Int) = """
        Write exactly $count multiple choice questions.
        - Exactly 4 options, exactly one correct; distractors must be plausible misconceptions, not jokes.
        - Vary which option is correct.
        - "explanation" says why the correct option is right AND briefly why the others are wrong.
        - Prefer questions that test understanding or application over pure recall.

        JSON format:
        [
          {
            "question": "What is Big O notation used for?",
            "options": ["A. A sorting algorithm", "B. Describing how an algorithm's cost grows with input size", "C. A data structure", "D. A programming paradigm"],
            "correctIndex": 1,
            "explanation": "Big O describes the growth rate of time or space. A and C are concrete algorithms/structures, D is unrelated."
          }
        ]
        "correctIndex" is the 0-based index of the correct option.
    """.trimIndent()

    private fun qaTask(count: Int) = """
        Write exactly $count question and answer pairs.
        - Mix "what", "why" and "how" questions; include real-world examples where useful.
        - Answers are clear and complete in 2-5 sentences.

        JSON format:
        [
          {
            "question": "Why does a hash map offer O(1) average lookups?",
            "answer": "It computes an index from the key's hash and jumps straight to that bucket. Collisions are rare with a good hash and load factor, so on average only a few entries are checked."
          }
        ]
    """.trimIndent()

    private fun funFactTask(count: Int) = """
        Write exactly $count surprising, little-known but true facts.
        - Each fact should teach something memorable, not just trivia.
        - "source" names where the fact comes from or its context (a person, organisation, paper or event).

        JSON format:
        [
          {
            "fact": "The first computer 'bug' was a real moth found in the Harvard Mark II in 1947.",
            "source": "Grace Hopper's team log book, Smithsonian"
          }
        ]
    """.trimIndent()

    private fun mixTask() = """
        Write a mixed set: exactly ${Constants.MixContent.MCQ_COUNT} multiple choice questions,
        ${Constants.MixContent.QA_COUNT} question and answer pairs and ${Constants.MixContent.FUN_FACT_COUNT} fun facts, in shuffled order.
        Every item has a "type" field: "MCQ", "QA" or "FUN_FACT".
        - MCQ: "question", "options" (exactly 4 strings), "correctIndex" (0-based), "explanation" (why right, why others wrong)
        - QA: "question", "answer" (2-5 sentences)
        - FUN_FACT: "fact", "source"

        JSON format:
        [
          {"type": "MCQ", "question": "...", "options": ["A. ...", "B. ...", "C. ...", "D. ..."], "correctIndex": 2, "explanation": "..."},
          {"type": "QA", "question": "...", "answer": "..."},
          {"type": "FUN_FACT", "fact": "...", "source": "..."}
        ]
    """.trimIndent()

    private fun buildExistingItemsSection(existingItems: List<String>): String {
        val recent = existingItems
            .filter { it.isNotBlank() }
            .takeLast(Constants.EXISTING_QUESTIONS_LIMIT)
        if (recent.isEmpty()) return ""
        val list = recent.joinToString("\n") { "- " + it.replace('\n', ' ').take(Constants.EXISTING_QUESTION_MAX_CHARS) }
        return "Already covered (do NOT repeat or rephrase these; pick new angles and sub-topics):\n$list"
    }
}
