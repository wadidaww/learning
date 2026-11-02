package com.kresna.aitopicapp.domain.model

/**
 * Chat models and regions of BytePlus ModelArk.
 *
 * Source: https://ai.byteplus.com/ark/region:ap-southeast-1/docs/model-list ("Text and code
 * generation" and "Retiring models"), checked 2026-11-02. Every model there is a deep-reasoning
 * model with tool use; the page says all of them are served in `ap-southeast-1` and only the
 * seed-2-0 family is also served in `eu-west-1`. When the page changes, update this list and
 * [LEGACY_IDS]; the UI, settings store and tests all read from here.
 */
object AiModelCatalog {

    const val DEFAULT_MODEL_ID = "dola-seed-2-1-turbo-260628"
    const val DEFAULT_REGION_ID = "ap-southeast-1"
    const val EUROPE_REGION_ID = "eu-west-1"

    /** Retirement date announced for every model in [retiringModels]. */
    const val RETIREMENT_DATE = "November 11, 2026"

    private const val BYTEDANCE = "ByteDance"
    private const val DEEPSEEK = "DeepSeek"
    private const val ZAI = "Z.ai"

    /** Models currently listed as active, strongest or newest first within each provider. */
    val currentModels: List<AiModel> = listOf(
        AiModel(
            id = DEFAULT_MODEL_ID,
            name = "Dola Seed 2.1 Turbo",
            description = "Newest Seed model. Fast, strong and the best default",
            provider = BYTEDANCE,
            contextWindow = "256K"
        ),
        AiModel(
            id = "seed-2-0-pro-260328",
            name = "Seed 2.0 Pro",
            description = "Most capable Seed 2.0 model",
            provider = BYTEDANCE,
            contextWindow = "256K",
            availableInEurope = true
        ),
        AiModel(
            id = "seed-2-0-lite-260428",
            name = "Seed 2.0 Lite",
            description = "Good balance of speed, quality and cost",
            provider = BYTEDANCE,
            contextWindow = "256K",
            availableInEurope = true
        ),
        AiModel(
            id = "seed-2-0-mini-260428",
            name = "Seed 2.0 Mini",
            description = "Smallest and cheapest Seed model",
            provider = BYTEDANCE,
            contextWindow = "256K",
            availableInEurope = true
        ),
        AiModel(
            id = "seed-2-0-code-preview-260328",
            name = "Seed 2.0 Code (preview)",
            description = "Tuned for programming topics",
            provider = BYTEDANCE,
            contextWindow = "256K",
            availableInEurope = true
        ),
        AiModel(
            id = "deepseek-v4-1-flash-261017",
            name = "DeepSeek V4.1 Flash",
            description = "Latest DeepSeek, fast with a huge context",
            provider = DEEPSEEK,
            contextWindow = "1M"
        ),
        AiModel(
            id = "deepseek-v4-1-flash-economy-261017",
            name = "DeepSeek V4.1 Flash Economy",
            description = "Same model at a lower price, may be slower",
            provider = DEEPSEEK,
            contextWindow = "1M"
        ),
        AiModel(
            id = "deepseek-v4-1-flash-260910",
            name = "DeepSeek V4.1 Flash (Sep 2026)",
            description = "Previous V4.1 Flash release",
            provider = DEEPSEEK,
            contextWindow = "1M"
        ),
        AiModel(
            id = "deepseek-v4-pro-ga-260813",
            name = "DeepSeek V4 Pro",
            description = "Largest DeepSeek V4 model",
            provider = DEEPSEEK,
            contextWindow = "1M"
        ),
        AiModel(
            id = "deepseek-v4-flash-ga-260731",
            name = "DeepSeek V4 Flash",
            description = "Fast DeepSeek V4 model",
            provider = DEEPSEEK,
            contextWindow = "1M"
        ),
        AiModel(
            id = "deepseek-v4-pro-260425",
            name = "DeepSeek V4 Pro (Apr 2026)",
            description = "Earlier V4 Pro release",
            provider = DEEPSEEK,
            contextWindow = "1M"
        ),
        AiModel(
            id = "deepseek-v4-flash-260425",
            name = "DeepSeek V4 Flash (Apr 2026)",
            description = "Earlier V4 Flash release",
            provider = DEEPSEEK,
            contextWindow = "1M"
        ),
        AiModel(
            id = "glm-5-3-flash-260828",
            name = "GLM 5.3 Flash",
            description = "Fast GLM model with image understanding",
            provider = ZAI,
            contextWindow = "1M"
        ),
        AiModel(
            id = "glm-5-2-260617",
            name = "GLM 5.2",
            description = "GLM text model",
            provider = ZAI,
            contextWindow = "1M"
        )
    )

    /** Still callable until [RETIREMENT_DATE]; kept so an existing selection keeps working and can be migrated. */
    val retiringModels: List<AiModel> = listOf(
        AiModel("seed-2-0-lite-260228", "Seed 2.0 Lite (Feb 2026)", "Retiring", BYTEDANCE, "256K", availableInEurope = true, replacementId = "seed-2-0-lite-260428"),
        AiModel("seed-2-0-mini-260215", "Seed 2.0 Mini (Feb 2026)", "Retiring", BYTEDANCE, "256K", availableInEurope = true, replacementId = "seed-2-0-mini-260428"),
        AiModel("seed-1-8-251228", "Seed 1.8", "Retiring", BYTEDANCE, "256K", replacementId = "seed-2-0-lite-260428"),
        AiModel("seed-1-6-250915", "Seed 1.6 (Sep 2025)", "Retiring", BYTEDANCE, "256K", replacementId = "seed-2-0-lite-260428"),
        AiModel("seed-1-6-250615", "Seed 1.6 (Jun 2025)", "Retiring", BYTEDANCE, "256K", replacementId = "seed-2-0-lite-260428"),
        AiModel("seed-1-6-flash-250715", "Seed 1.6 Flash (Jul 2025)", "Retiring", BYTEDANCE, "256K", replacementId = "seed-2-0-mini-260428"),
        AiModel("seed-1-6-flash-250615", "Seed 1.6 Flash (Jun 2025)", "Retiring", BYTEDANCE, "256K", replacementId = "seed-2-0-mini-260428"),
        AiModel("deepseek-v3-2-251201", "DeepSeek V3.2", "Retiring", DEEPSEEK, "128K", replacementId = "deepseek-v4-flash-ga-260731"),
        AiModel("glm-4-7-251222", "GLM 4.7", "Retiring", ZAI, "200K", replacementId = DEFAULT_MODEL_ID)
    )

    val allModels: List<AiModel> = currentModels + retiringModels

    /**
     * Ids the app used to offer that were never valid ModelArk ids, or dropped their version
     * suffix, mapped to the model they meant. Applied when settings are read, so nobody is stuck
     * with a model that returns "model not found".
     */
    val LEGACY_IDS: Map<String, String> = mapOf(
        "dola-seed-2-0-pro-260228" to "seed-2-0-pro-260328",
        "dola-seed-2-0-lite-260228" to "seed-2-0-lite-260428",
        "dola-seed-2-0-mini-260228" to "seed-2-0-mini-260428",
        "dola-seed-2-0-lite-260428" to "seed-2-0-lite-260428",
        "dola-seed-2-0-mini-260428" to "seed-2-0-mini-260428",
        "deepseek-v3-2" to "deepseek-v3-2-251201",
        "deepseek-v4-flash" to "deepseek-v4-flash-ga-260731",
        "gpt-oss-120b" to DEFAULT_MODEL_ID
    )

    val regions: List<Region> = listOf(
        Region(
            id = DEFAULT_REGION_ID,
            name = "Asia Pacific (Singapore)",
            baseUrl = "https://ark.ap-southeast.bytepluses.com/api/v3/"
        ),
        Region(
            id = EUROPE_REGION_ID,
            name = "Europe (Ireland)",
            baseUrl = "https://ark.eu-west.bytepluses.com/api/v3/"
        )
    )

    fun find(id: String): AiModel? = allModels.firstOrNull { it.id == id }

    fun region(id: String): Region = regions.firstOrNull { it.id == id } ?: regions.first()

    /** Maps a stored id to the id to use today: legacy spellings are corrected, anything else is kept. */
    fun resolve(storedId: String?): String {
        if (storedId.isNullOrBlank()) return DEFAULT_MODEL_ID
        return LEGACY_IDS[storedId] ?: storedId
    }

    fun isAvailableIn(model: AiModel, regionId: String): Boolean =
        regionId != EUROPE_REGION_ID || model.availableInEurope

    /** Current models offered in [regionId], for the settings picker. */
    fun currentModelsFor(regionId: String): List<AiModel> = currentModels.filter { isAvailableIn(it, regionId) }

    /** The model to switch to when [modelId] is retiring or unknown in [regionId]; null when it is fine. */
    fun suggestedReplacement(modelId: String, regionId: String): AiModel? {
        val model = find(modelId)
        val candidate = when {
            model == null -> null
            model.isRetiring -> find(model.replacementId!!)
            !isAvailableIn(model, regionId) -> null
            else -> return null
        }
        return candidate?.takeIf { isAvailableIn(it, regionId) }
            ?: currentModelsFor(regionId).firstOrNull()
    }
}
