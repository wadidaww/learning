package com.kresna.aitopicapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kresna.aitopicapp.data.local.SettingsDataStore
import com.kresna.aitopicapp.domain.model.AiModel
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

    private val _selectedModelId = MutableStateFlow("dola-seed-2-1-turbo-260628")
    val selectedModelId: StateFlow<String> = _selectedModelId.asStateFlow()

    private val _selectedRegion = MutableStateFlow("ap-southeast-1")
    val selectedRegion: StateFlow<String> = _selectedRegion.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    val availableModels = listOf(
        AiModel(
            id = "dola-seed-2-1-turbo-260628",
            name = "Dola Seed 2.1 Turbo",
            description = "Fastest, newest model"
        ),
        AiModel(
            id = "dola-seed-2-0-lite-260228",
            name = "Dola Seed 2.0 Lite",
            description = "Lightweight, fast"
        ),
        AiModel(
            id = "dola-seed-2-0-mini-260228",
            name = "Dola Seed 2.0 Mini",
            description = "Small & efficient"
        ),
        AiModel(
            id = "dola-seed-2-0-pro-260228",
            name = "Dola Seed 2.0 Pro",
            description = "Balanced performance"
        ),
        AiModel(
            id = "seed-1-6-flash-250615",
            name = "Seed 1.6 Flash",
            description = "Flash model, fast"
        ),
        AiModel(
            id = "seed-1-6-250615",
            name = "Seed 1.6",
            description = "Standard model"
        ),
        AiModel(
            id = "deepseek-v3-2",
            name = "DeepSeek V3.2",
            description = "DeepSeek model"
        ),
        AiModel(
            id = "deepseek-v4-flash",
            name = "DeepSeek V4 Flash",
            description = "DeepSeek v4, fast"
        ),
        AiModel(
            id = "gpt-oss-120b",
            name = "GPT-OSS 120B",
            description = "Open source model"
        )
    )

    val availableRegions = listOf(
        Region(
            id = "ap-southeast-1",
            name = "Asia Pacific (Singapore)",
            baseUrl = "https://ark.ap-southeast.bytepluses.com/api/v3/"
        ),
        Region(
            id = "eu-west-1",
            name = "Europe (Ireland)",
            baseUrl = "https://ark.eu-west.bytepluses.com/api/v3/"
        )
    )

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
