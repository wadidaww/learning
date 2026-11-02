package com.kresna.aitopicapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kresna.aitopicapp.data.local.SettingsDataStore
import com.kresna.aitopicapp.domain.model.AiModel
import com.kresna.aitopicapp.domain.model.AiModelCatalog
import com.kresna.aitopicapp.domain.model.Region
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _selectedModelId = MutableStateFlow(AiModelCatalog.DEFAULT_MODEL_ID)
    val selectedModelId: StateFlow<String> = _selectedModelId.asStateFlow()

    private val _selectedRegion = MutableStateFlow(AiModelCatalog.DEFAULT_REGION_ID)
    val selectedRegion: StateFlow<String> = _selectedRegion.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    val availableRegions: List<Region> = AiModelCatalog.regions

    /** Current models served in the selected region. */
    fun modelsForRegion(regionId: String): List<AiModel> = AiModelCatalog.currentModelsFor(regionId)

    /** Retiring models served in the selected region; still selectable until the retirement date. */
    fun retiringModelsForRegion(regionId: String): List<AiModel> =
        AiModelCatalog.retiringModels.filter { AiModelCatalog.isAvailableIn(it, regionId) }

    fun findModel(id: String): AiModel? = AiModelCatalog.find(id)

    /** Non-null when the selected model is retiring, unknown, or not served in the selected region. */
    fun suggestedReplacement(modelId: String, regionId: String): AiModel? =
        AiModelCatalog.suggestedReplacement(modelId, regionId)

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = settingsDataStore.getSettings()
            _apiKey.value = settings.apiKey
            _selectedModelId.value = settings.modelId
            _selectedRegion.value = settings.region
        }
    }

    fun updateApiKey(apiKey: String) {
        _apiKey.value = apiKey
        _isSaved.value = false
    }

    fun updateModel(modelId: String) {
        _selectedModelId.value = modelId
        _isSaved.value = false
    }

    fun updateRegion(regionId: String) {
        _selectedRegion.value = regionId
        _isSaved.value = false
    }

    fun saveSettings() {
        viewModelScope.launch {
            settingsDataStore.saveSettings(
                apiKey = _apiKey.value,
                modelId = _selectedModelId.value,
                region = _selectedRegion.value
            )
            _isSaved.value = true
        }
    }
}
