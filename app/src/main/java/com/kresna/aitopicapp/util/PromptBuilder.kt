package com.kresna.aitopicapp.util

object PromptBuilder {

    private const val MIX_MCQ_COUNT = 4
    private const val MIX_QA_COUNT = 3
    private const val MIX_FUN_FACT_COUNT = 3

    fun buildMcqPrompt(topic: String, context: String, count: Int): String {
        return """
            Generate $count unique multiple choice questions about: $topic

            Additional context from user: $context

            Requirements:
            - Each question must be educational and factually accurate
            - Vary difficulty (easy, medium, hard)
            - Make questions interesting and thought-provoking
            - Each question has exactly 4 options (A, B, C, D)
            - Exactly one option is correct
            - Provide a brief explanation for the correct answer

            Return ONLY a JSON array in this exact format:
            [
              {
                "question": "What is Big O notation used for?",
                "options": ["A. A sorting algorithm", "B. Describing algorithm complexity", "C. A data structure", "D. A programming paradigm"],
                "correctIndex": 1,
                "explanation": "Big O notation is used to describe the upper bound of an algorithm's growth rate."
              }
            ]

            Do not include any other text, just the JSON array.
        """.trimIndent()
    }

    fun buildQaPrompt(topic: String, context: String, count: Int): String {
        return """
            Generate $count unique question and answer pairs about: $topic

            Additional context from user: $context

            Requirements:
            - Questions should be educational and insightful
            - Answers should be clear, detailed, and easy to understand
            - Vary between conceptual and factual questions
            - Include real-world examples when relevant

            Return ONLY a JSON array in this exact format:
            [
              {
                "question": "What is the difference between a stack and a queue?",
                "answer": "A stack is LIFO (Last In, First Out) - like stacking plates. A queue is FIFO (First In, First Out) - like a line at a store."
              }
            ]

            Do not include any other text, just the JSON array.
        """.trimIndent()
    }

    fun buildFunFactPrompt(topic: String, context: String, count: Int): String {
        return """
            Generate $count unique fun facts about: $topic

            Additional context from user: $context

            Requirements:
            - Facts should be surprising, interesting, or little-known
            - Each fact should be educational
            - Make them engaging and memorable
            - Include the source or context when relevant

            Return ONLY a JSON array in this exact format:
            [
              {
                "fact": "The first computer bug was an actual bug! In 1947, Grace Hopper found a moth stuck in a Harvard Mark II computer.",
                "source": "Computer History Museum"
              }
            ]

            Do not include any other text, just the JSON array.
        """.trimIndent()
    }

    fun buildMixPrompt(topic: String, context: String, count: Int = MIX_MCQ_COUNT + MIX_QA_COUNT + MIX_FUN_FACT_COUNT): String {
        return """
            Generate a mix of content about: $topic

            Additional context from user: $context

            Requirements:
            - Generate exactly $MIX_MCQ_COUNT multiple choice questions, $MIX_QA_COUNT question and answer pairs, and $MIX_FUN_FACT_COUNT fun facts
            - Each item must have a "type" field: "MCQ", "QA", or "FUN_FACT"
            - MCQ items: "question", "options" (4 options as strings), "correctIndex" (0-3), "explanation"
            - QA items: "question", "answer"
            - FUN_FACT items: "fact", "source"
            - All content must be educational and factually accurate
            - Shuffle the items randomly so the types are mixed

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

            Do not include any other text, just the JSON array.
        """.trimIndent()
    }
}
