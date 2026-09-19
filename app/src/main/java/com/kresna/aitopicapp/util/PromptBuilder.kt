package com.kresna.aitopicapp.util

object PromptBuilder {

    private val CODE_SNIPPET_INSTRUCTION = """
        IMPORTANT: If the question, answer, or explanation involves code, programming, algorithms, data structures,
        or any computer science concept that can be demonstrated with code, include a "codeSnippet" field with a
        clean, runnable code example. Also include a "codeLanguage" field indicating the programming language
        (e.g., "python", "javascript", "kotlin", "java", "cpp", "rust", "go", "sql", "html", "css", etc.).
        The code snippet should be concise, well-commented, and demonstrate the concept clearly.
        If the topic is NOT code-related, omit the "codeSnippet" and "codeLanguage" fields entirely.
    """.trimIndent()

    fun buildMcqPrompt(
        topic: String,
        context: String,
        count: Int,
        existingQuestions: List<String> = emptyList()
    ): String {
        return """
            Generate $count unique multiple choice questions about: $topic

            Additional context from user: $context

            ${buildExistingQuestionsSection(existingQuestions)}

            Requirements:
            - Each question must be educational and factually accurate
            - Vary difficulty (easy, medium, hard)
            - Make questions interesting and thought-provoking
            - Each question has exactly 4 options (A, B, C, D)
            - Exactly one option is correct
            - Provide a brief explanation for the correct answer
            - Do NOT duplicate any of the already covered questions listed above

            $CODE_SNIPPET_INSTRUCTION

            Return ONLY a JSON array in this exact format:
            [
              {
                "question": "What is Big O notation used for?",
                "options": ["A. A sorting algorithm", "B. Describing algorithm complexity", "C. A data structure", "D. A programming paradigm"],
                "correctIndex": 1,
                "explanation": "Big O notation is used to describe the upper bound of an algorithm's growth rate."
              }
            ]

            For CS/coding topics, include the optional codeSnippet and codeLanguage fields:
            [
              {
                "question": "Which sorting algorithm has the best average-case time complexity?",
                "options": ["A. Bubble Sort", "B. Quick Sort", "C. Selection Sort", "D. Insertion Sort"],
                "correctIndex": 1,
                "explanation": "Quick Sort has an average-case time complexity of O(n log n), which is better than the O(n^2) of the other options.",
                "codeSnippet": "def quicksort(arr):\n    if len(arr) <= 1:\n        return arr\n    pivot = arr[len(arr) // 2]\n    left = [x for x in arr if x < pivot]\n    middle = [x for x in arr if x == pivot]\n    right = [x for x in arr if x > pivot]\n    return quicksort(left) + middle + quicksort(right)",
                "codeLanguage": "python"
              }
            ]

            Do not include any other text, just the JSON array.
        """.trimIndent()
    }

    fun buildQaPrompt(
        topic: String,
        context: String,
        count: Int,
        existingQuestions: List<String> = emptyList()
    ): String {
        return """
            Generate $count unique question and answer pairs about: $topic

            Additional context from user: $context

            ${buildExistingQuestionsSection(existingQuestions)}

            Requirements:
            - Questions should be educational and insightful
            - Answers should be clear, detailed, and easy to understand
            - Vary between conceptual and factual questions
            - Include real-world examples when relevant
            - Do NOT duplicate any of the already covered questions listed above

            $CODE_SNIPPET_INSTRUCTION

            Return ONLY a JSON array in this exact format:
            [
              {
                "question": "What is the difference between a stack and a queue?",
                "answer": "A stack is LIFO (Last In, First Out) - like stacking plates. A queue is FIFO (First In, First Out) - like a line at a store."
              }
            ]

            For CS/coding topics, include the optional codeSnippet and codeLanguage fields:
            [
              {
                "question": "How does a binary search work?",
                "answer": "Binary search divides a sorted array in half repeatedly, eliminating half the remaining elements each step, achieving O(log n) time complexity.",
                "codeSnippet": "def binary_search(arr, target):\n    low, high = 0, len(arr) - 1\n    while low <= high:\n        mid = (low + high) // 2\n        if arr[mid] == target:\n            return mid\n        elif arr[mid] < target:\n            low = mid + 1\n        else:\n            high = mid - 1\n    return -1",
                "codeLanguage": "python"
              }
            ]

            Do not include any other text, just the JSON array.
        """.trimIndent()
    }

    fun buildFunFactPrompt(
        topic: String,
        context: String,
        count: Int,
        existingQuestions: List<String> = emptyList()
    ): String {
        return """
            Generate $count unique fun facts about: $topic

            Additional context from user: $context

            ${buildExistingQuestionsSection(existingQuestions)}

            Requirements:
            - Facts should be surprising, interesting, or little-known
            - Each fact should be educational
            - Make them engaging and memorable
            - Include the source or context when relevant
            - Do NOT duplicate any of the already covered facts listed above

            $CODE_SNIPPET_INSTRUCTION

            Return ONLY a JSON array in this exact format:
            [
              {
                "fact": "The first computer bug was an actual bug! In 1947, Grace Hopper found a moth stuck in a Harvard Mark II computer.",
                "source": "Computer History Museum"
              }
            ]

            For CS/coding topics, include the optional codeSnippet and codeLanguage fields:
            [
              {
                "fact": "The XOR swap trick lets you swap two variables without a temporary variable using bitwise XOR.",
                "source": "Computer Science Fundamentals",
                "codeSnippet": "a, b = 5, 10\na ^= b\nb ^= a\na ^= b\n# Now a=10, b=5",
                "codeLanguage": "python"
              }
            ]

            Do not include any other text, just the JSON array.
        """.trimIndent()
    }

    fun buildMixPrompt(
        topic: String,
        context: String,
        count: Int = Constants.MixContent.TOTAL,
        existingQuestions: List<String> = emptyList()
    ): String {
        return """
            Generate a mix of content about: $topic

            Additional context from user: $context

            ${buildExistingQuestionsSection(existingQuestions)}

            Requirements:
            - Generate exactly ${Constants.MixContent.MCQ_COUNT} multiple choice questions, ${Constants.MixContent.QA_COUNT} question and answer pairs, and ${Constants.MixContent.FUN_FACT_COUNT} fun facts
            - Each item must have a "type" field: "MCQ", "QA", or "FUN_FACT"
            - MCQ items: "question", "options" (4 options as strings), "correctIndex" (0-3), "explanation"
            - QA items: "question", "answer"
            - FUN_FACT items: "fact", "source"
            - All content must be educational and factually accurate
            - Shuffle the items randomly so the types are mixed
            - Do NOT duplicate any of the already covered content listed above

            $CODE_SNIPPET_INSTRUCTION

            Return ONLY a JSON array in this exact format:
            [
              {
                "type": "MCQ",
                "question": "What is Big O notation used for?",
                "options": ["A. A sorting algorithm", "B. Describing algorithm complexity", "C. A data structure", "D. A programming paradigm"],
                "correctIndex": 1,
                "explanation": "Big O notation describes the upper bound of an algorithm's growth rate."
              },
              {
                "type": "QA",
                "question": "What is the difference between a stack and a queue?",
                "answer": "A stack is LIFO (Last In, First Out). A queue is FIFO (First In, First Out)."
              },
              {
                "type": "FUN_FACT",
                "fact": "The first computer bug was an actual bug found in 1947.",
                "source": "Computer History Museum"
              }
            ]

            For CS/coding topics, include the optional codeSnippet and codeLanguage fields in any item where code is relevant:
            [
              {
                "type": "QA",
                "question": "What is recursion?",
                "answer": "Recursion is when a function calls itself to solve smaller instances of the same problem.",
                "codeSnippet": "def factorial(n):\n    if n <= 1:\n        return 1\n    return n * factorial(n - 1)",
                "codeLanguage": "python"
              }
            ]

            Do not include any other text, just the JSON array.
        """.trimIndent()
    }

    private fun buildExistingQuestionsSection(existingQuestions: List<String>): String {
        if (existingQuestions.isEmpty()) return ""
        val limited = existingQuestions.takeLast(Constants.EXISTING_QUESTIONS_LIMIT)
        val questionList = limited.joinToString("\n") { "- $it" }
        return """
            IMPORTANT: The following questions/topics have already been covered. Generate completely different and unique content. Do NOT repeat or rephrase any of these:
            $questionList
        """.trimIndent()
    }
}
