package com.kresna.aitopicapp.domain.model

/** One chat model offered by BytePlus ModelArk, as listed in [AiModelCatalog]. */
data class AiModel(
    /** Exact string sent in the `model` field of chat completions, e.g. `seed-2-0-lite-260428`. */
    val id: String,
    val name: String,
    val description: String,
    val provider: String,
    /** Context window as shown on the model list, e.g. "256K". */
    val contextWindow: String,
    /** Only the Seed 2.0 family is served in the Europe region; everything is served in Singapore. */
    val availableInEurope: Boolean = false,
    /** Set when ModelArk has scheduled the model for retirement: the id to move to. */
    val replacementId: String? = null
) {
    val isRetiring: Boolean get() = replacementId != null
}
