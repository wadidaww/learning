package com.kresna.aitopicapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiModelCatalogTest {

    /** ModelArk ids are lower-case words joined by dashes and end in a yymmdd version. */
    private val idShape = Regex("^[a-z0-9]+(-[a-z0-9]+)*-\\d{6}$")

    @Test
    fun `every id is unique and shaped like a ModelArk id`() {
        val ids = AiModelCatalog.allModels.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        ids.forEach { assertTrue("bad id: $it", idShape.matches(it)) }
    }

    @Test
    fun `default model is a current model`() {
        val default = AiModelCatalog.find(AiModelCatalog.DEFAULT_MODEL_ID)
        assertTrue(default != null && !default.isRetiring)
    }

    @Test
    fun `every retiring model points at a current model`() {
        AiModelCatalog.retiringModels.forEach { model ->
            val replacement = AiModelCatalog.find(model.replacementId!!)
            assertTrue("${model.id} -> ${model.replacementId}", replacement != null && !replacement.isRetiring)
        }
    }

    @Test
    fun `legacy ids resolve to known models and known ids pass through`() {
        AiModelCatalog.LEGACY_IDS.forEach { (legacy, target) ->
            assertEquals(target, AiModelCatalog.resolve(legacy))
            assertTrue("$legacy -> $target unknown", AiModelCatalog.find(target) != null)
        }
        assertEquals("seed-2-0-pro-260328", AiModelCatalog.resolve("seed-2-0-pro-260328"))
        assertEquals(AiModelCatalog.DEFAULT_MODEL_ID, AiModelCatalog.resolve(null))
        assertEquals(AiModelCatalog.DEFAULT_MODEL_ID, AiModelCatalog.resolve(""))
        assertEquals("ep-custom", AiModelCatalog.resolve("ep-custom"))
    }

    @Test
    fun `europe only offers the seed 2 family`() {
        val europe = AiModelCatalog.currentModelsFor(AiModelCatalog.EUROPE_REGION_ID)
        assertTrue(europe.isNotEmpty())
        europe.forEach { assertTrue(it.id, it.id.startsWith("seed-2-0-")) }
        assertEquals(AiModelCatalog.currentModels, AiModelCatalog.currentModelsFor(AiModelCatalog.DEFAULT_REGION_ID))
    }

    @Test
    fun `replacement is suggested for retiring, unknown and out-of-region models`() {
        assertNull(AiModelCatalog.suggestedReplacement("seed-2-0-lite-260428", AiModelCatalog.DEFAULT_REGION_ID))
        assertEquals(
            "seed-2-0-lite-260428",
            AiModelCatalog.suggestedReplacement("seed-1-6-250615", AiModelCatalog.DEFAULT_REGION_ID)?.id
        )
        // GLM 4.7's replacement is Dola Seed 2.1 Turbo, which Europe does not serve, so fall back to a Seed 2.0 model.
        val euReplacement = AiModelCatalog.suggestedReplacement("glm-4-7-251222", AiModelCatalog.EUROPE_REGION_ID)
        assertTrue(euReplacement != null && euReplacement.availableInEurope)
        assertFalse(AiModelCatalog.suggestedReplacement("deepseek-v4-pro-ga-260813", AiModelCatalog.EUROPE_REGION_ID) == null)
        assertTrue(AiModelCatalog.suggestedReplacement("not-a-model", AiModelCatalog.DEFAULT_REGION_ID) != null)
    }
}
