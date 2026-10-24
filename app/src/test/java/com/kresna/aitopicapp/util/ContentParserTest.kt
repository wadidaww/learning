package com.kresna.aitopicapp.util

import com.kresna.aitopicapp.domain.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentParserTest {

    private val mcqJson = """
        [
          {"question": "Q1?", "options": ["A. one", "B. two", "C. three", "D. four"], "correctIndex": 1, "explanation": "because"},
          {"question": "Q2?", "options": ["A. one", "B. two", "C. three", "D. four"], "correctIndex": 3}
        ]
    """.trimIndent()

    @Test
    fun `parses plain MCQ array`() {
        val items = ContentParser.parse(mcqJson, ContentType.MCQ, topicId = 7)
        assertEquals(2, items.size)
        assertEquals("Q1?", items[0].question)
        assertEquals(1, items[0].correctIndex)
        assertEquals("because", items[0].explanation)
        assertEquals(7, items[0].topicId)
    }

    @Test
    fun `strips markdown fences and surrounding prose`() {
        val text = "Sure! Here are your questions:\n```json\n$mcqJson\n```\nHope this helps."
        assertEquals(2, ContentParser.parse(text, ContentType.MCQ, 1).size)
    }

    @Test
    fun `strips reasoning blocks`() {
        val text = "<think>Let me think about [arrays] and {objects}...</think>\n$mcqJson"
        assertEquals(2, ContentParser.parse(text, ContentType.MCQ, 1).size)
    }

    @Test
    fun `unterminated reasoning yields nothing`() {
        assertTrue(ContentParser.parse("<think>still thinking [", ContentType.MCQ, 1).isEmpty())
    }

    @Test
    fun `accepts array wrapped in an object`() {
        val text = """{"questions": $mcqJson}"""
        assertEquals(2, ContentParser.parse(text, ContentType.MCQ, 1).size)
    }

    @Test
    fun `salvages complete items from truncated output`() {
        val truncated = """
            [
              {"question": "Q1?", "answer": "A1 with a } brace and \"quote\""},
              {"question": "Q2?", "answer": "A2"},
              {"question": "Q3?", "answ
        """.trimIndent()
        val items = ContentParser.parse(truncated, ContentType.QA, 1)
        assertEquals(listOf("Q1?", "Q2?"), items.map { it.question })
        assertEquals("A1 with a } brace and \"quote\"", items[0].answer)
    }

    @Test
    fun `salvages truncated output inside an unterminated fence`() {
        val text = "```json\n[{\"fact\": \"F1\", \"source\": \"S\"}, {\"fact\": \"F2"
        val items = ContentParser.parse(text, ContentType.FUN_FACT, 1)
        assertEquals(listOf("F1"), items.map { it.answer })
    }

    @Test
    fun `null and missing fields do not break the whole batch`() {
        val text = """
            [
              {"question": null, "answer": "orphan"},
              {"question": "Valid?", "answer": "Yes", "codeSnippet": null},
              {"question": "No answer"}
            ]
        """.trimIndent()
        val items = ContentParser.parse(text, ContentType.QA, 1)
        assertEquals(1, items.size)
        assertEquals("Valid?", items[0].question)
        assertEquals(null, items[0].codeSnippet)
    }

    @Test
    fun `resolves loosely typed correct answers`() {
        fun correctFor(value: String) = ContentParser.parse(
            """[{"question": "Q", "options": ["A. red", "B. green", "C. blue", "D. black"], "correctIndex": $value}]""",
            ContentType.MCQ,
            1
        ).singleOrNull()?.correctIndex

        assertEquals(2, correctFor("2"))
        assertEquals(2, correctFor("\"2\""))
        assertEquals(1, correctFor("\"B\""))
        assertEquals(1, correctFor("\"B. green\""))
        assertEquals(2, correctFor("\"blue\""))
        assertEquals(3, correctFor("4")) // 1-based index equal to the option count
        assertEquals(null, correctFor("9"))
    }

    @Test
    fun `drops MCQs without enough options`() {
        val text = """[{"question": "Q", "options": ["only"], "correctIndex": 0}]"""
        assertTrue(ContentParser.parse(text, ContentType.MCQ, 1).isEmpty())
    }

    @Test
    fun `mix response resolves types including inferred ones`() {
        val text = """
            [
              {"type": "MCQ", "question": "M?", "options": ["a", "b", "c", "d"], "correctIndex": 0},
              {"type": "Q&A", "question": "Q?", "answer": "A"},
              {"type": "fun_fact", "fact": "F", "source": "S"},
              {"question": "Inferred QA?", "answer": "yes"},
              {"type": "POEM", "text": "unsupported"}
            ]
        """.trimIndent()
        val items = ContentParser.parse(text, ContentType.MIX, 1)
        assertEquals(
            listOf(ContentType.MCQ, ContentType.QA, ContentType.FUN_FACT, ContentType.QA),
            items.map { it.contentType }
        )
        assertEquals("F", items[2].answer)
        assertEquals(ContentParser.FUN_FACT_TITLE, items[2].question)
    }

    @Test
    fun `garbage yields empty list`() {
        assertTrue(ContentParser.parse("I cannot help with that.", ContentType.MCQ, 1).isEmpty())
        assertTrue(ContentParser.parse("", ContentType.MCQ, 1).isEmpty())
    }
}
