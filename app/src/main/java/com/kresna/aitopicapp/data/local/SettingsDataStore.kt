package com.kresna.aitopicapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kresna.aitopicapp.domain.model.AiModelCatalog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val API_KEY = stringPreferencesKey("api_key")
        val MODEL_ID = stringPreferencesKey("model_id")
        val REGION = stringPreferencesKey("region")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_KEY] ?: ""
    }

    /** Stored ids are passed through [AiModelCatalog.resolve], so an outdated id is corrected on read. */
    val modelId: Flow<String> = context.dataStore.data.map { preferences ->
        AiModelCatalog.resolve(preferences[MODEL_ID])
    }

    val region: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[REGION] ?: AiModelCatalog.DEFAULT_REGION_ID
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    suspend fun saveModelId(modelId: String) {
        context.dataStore.edit { preferences ->
            preferences[MODEL_ID] = modelId
        }
    }

    suspend fun saveRegion(region: String) {
        context.dataStore.edit { preferences ->
            preferences[REGION] = region
        }
    }

    suspend fun saveSettings(apiKey: String, modelId: String, region: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
            preferences[MODEL_ID] = modelId
            preferences[REGION] = region
        }
    }

    suspend fun getSettings(): AppSettings {
        val data = context.dataStore.data.first()
        return AppSettings(
            apiKey = data[API_KEY] ?: "",
            modelId = AiModelCatalog.resolve(data[MODEL_ID]),
            region = data[REGION] ?: AiModelCatalog.DEFAULT_REGION_ID
        )
    }
}

data class AppSettings(
    val apiKey: String = "",
    val modelId: String = AiModelCatalog.DEFAULT_MODEL_ID,
    val region: String = AiModelCatalog.DEFAULT_REGION_ID
) {
    val baseUrl: String get() = AiModelCatalog.region(region).baseUrl
}
