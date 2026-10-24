# Developer Documentation - AI Topic Learning App

## Architecture Overview

This app follows **Clean Architecture** with **MVVM** presentation pattern.

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────────┐  │
│  │  Screens │  │Components│  │      ViewModels      │  │
│  └──────────┘  └──────────┘  └──────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│                    Domain Layer                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │                   Models                         │  │
│  └──────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│                     Data Layer                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────────┐  │
│  │  Room DB │  │BytePlus  │  │    Repositories      │  │
│  │DataStore │  │ModelArk  │  │                      │  │
│  └──────────┘  └──────────┘  └──────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## Directory Structure

```
app/src/main/java/com/kresna/aitopicapp/
│
├── AiTopicApp.kt                 # Hilt Application class
├── MainActivity.kt               # Single Activity entry point
│
├── data/                         # Data Layer
│   ├── local/                    # Local data sources
│   │   ├── AppDatabase.kt        # Room database
│   │   ├── SettingsDataStore.kt  # Settings preferences (DataStore)
│   │   ├── dao/                  # Data Access Objects
│   │   │   ├── TopicDao.kt       # Topic database operations
│   │   │   └── ContentDao.kt     # Content database operations
│   │   └── entity/               # Database entities
│   │       ├── TopicEntity.kt    # Topic table schema
│   │       └── ContentEntity.kt  # Content table schema
│   │
│   ├── remote/                   # Remote data sources
│   │   ├── BytePlusApi.kt        # BytePlus API interface
│   │   ├── BytePlusRequest.kt    # API request models
│   │   └── BytePlusResponse.kt   # API response models
│   │
│   └── repository/               # Data repositories
│       ├── TopicRepository.kt    # Topic data operations
│       └── ContentRepository.kt  # Content data + API operations
│
├── di/                           # Dependency Injection
│   ├── AppModule.kt              # Retrofit, OkHttp, DataStore providers
│   └── DatabaseModule.kt         # Room DB, DAO providers
│
├── domain/                       # Domain Layer
│   └── model/                    # Domain models
│       ├── Topic.kt              # Topic data class
│       ├── GeneratedContent.kt   # Content data class
│       └── ContentType.kt        # MCQ, QA, FUN_FACT enum
│
├── ui/                           # UI Layer
│   ├── navigation/
│   │   └── NavGraph.kt           # Navigation routes
│   │
│   ├── theme/
│   │   ├── Color.kt              # App colors
│   │   ├── Theme.kt              # Material3 theme
│   │   └── Type.kt               # Typography
│   │
│   ├── screens/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt     # Topic list UI
│   │   │   └── HomeViewModel.kt  # Home state management
│   │   │
│   │   ├── topic/
│   │   │   ├── TopicDetailScreen.kt    # Generate content UI
│   │   │   └── TopicDetailViewModel.kt # Generation state
│   │   │
│   │   ├── content/
│   │   │   ├── ContentPagerScreen.kt    # Swipeable content
│   │   │   └── ContentPagerViewModel.kt # Pager state
│   │   │
│   │   └── settings/
│   │       ├── SettingsScreen.kt        # App settings UI
│   │       └── SettingsViewModel.kt     # Settings state
│   │
│   └── components/               # Reusable UI components
│       ├── TopicCard.kt          # Topic list item
│       ├── AddTopicDialog.kt     # Create topic dialog
│       ├── EditTopicDialog.kt    # Edit topic dialog
│       ├── MCQCard.kt            # MCQ content card
│       ├── QACard.kt             # Q&A content card
│       ├── FunFactCard.kt        # Fun fact card
│       ├── PageIndicator.kt      # Pager dots indicator
│       ├── ShareButton.kt        # Share content button
│       ├── ErrorDialog.kt        # Error retry dialog
│       └── ShimmerEffect.kt      # Loading shimmer
│
└── util/                         # Utilities
    ├── PromptBuilder.kt          # AI prompt templates
    └── ContentParser.kt          # JSON response parser
```

## Key Classes

### Data Layer

#### `AppDatabase.kt`
Room database with two tables: `topics` and `generated_content`. Uses singleton pattern via Hilt.

#### `SettingsDataStore.kt`
DataStore-based preferences for storing API key, selected model, and region.

```kotlin
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val apiKey: Flow<String>      // BytePlus API key
    val modelId: Flow<String>     // Selected AI model
    val region: Flow<String>      // API region
    
    suspend fun saveSettings(apiKey: String, modelId: String, region: String)
}
```

#### `TopicDao.kt`
```kotlin
// Key operations
fun observeTopicsWithProgress(): Flow<List<TopicWithProgressRow>>  // Topics + progress in one query
suspend fun insertTopic(topic: TopicEntity): Long
suspend fun deleteTopic(topic: TopicEntity)     // Repository also deletes the topic's content
```

#### `ContentDao.kt`
```kotlin
// All lists are ordered oldest first (createdAt ASC, id ASC)
fun observeByTopicAndType(topicId: Long, contentType: ContentType): Flow<List<ContentEntity>>
fun observeAllForTopic(topicId: Long): Flow<List<ContentEntity>>
fun observeBookmarked(topicId: Long) / observeMissed(topicId: Long)
fun observeProgress(topicId: Long): Flow<ProgressRow>
suspend fun insertAll(content: List<ContentEntity>): List<Long>
suspend fun markAsViewed(contentId: Long)
suspend fun setUserAnswer(contentId: Long, answerIndex: Int?)
suspend fun setBookmarked(contentId: Long, bookmarked: Boolean)
```

Database version 5 adds `userAnswerIndex`, `isBookmarked`, `topics.difficulty` and an index on
`generated_content.topicId` (`MIGRATION_4_5`).

#### `BytePlusApi.kt`
```kotlin
interface BytePlusApi {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: BytePlusRequest
    ): Response<BytePlusResponse>
}
```

#### `ContentRepository.kt`
Main repository handling both local DB and remote API calls:
```kotlin
suspend fun generateContent(
    topic: Topic,
    contentType: ContentType,
    count: Int
): Result<List<GeneratedContent>>   // never success(emptyList()); fails with GenerationException
```

### Domain Layer

#### `ContentType.kt`
```kotlin
enum class ContentType(val displayName: String) {
    MCQ("Multiple Choice"),
    QA("Question & Answer"),
    FUN_FACT("Fun Fact")
}
```

### UI Layer

#### `NavGraph.kt`
Four routes:
- `home` - Topic list
- `settings` - App settings (API key, model, region)
- `topic/{topicId}` - Topic detail
- `content/{topicId}/{contentType}` - Content pager

#### Screen Flow
```
HomeScreen ──┬──> TopicDetailScreen ──> ContentPagerScreen
             │
             └──> SettingsScreen
```

## State Management

Each ViewModel uses `MutableStateFlow` for UI state:

### HomeViewModel
```kotlin
val topics: StateFlow<List<Topic>>
val isLoading: StateFlow<Boolean>
```

### TopicDetailViewModel
```kotlin
val uiState: StateFlow<TopicDetailUiState>
val selectedContentType: StateFlow<ContentType>
val selectedCount: StateFlow<Int>
val isGenerating: StateFlow<Boolean>
val generationResult: StateFlow<GenerationResult?>
```

### ContentPagerViewModel
```kotlin
val uiState: StateFlow<ContentPagerUiState>
val currentPage: StateFlow<Int>
```

### SettingsViewModel
```kotlin
val apiKey: StateFlow<String>
val selectedModelId: StateFlow<String>
val selectedRegion: StateFlow<String>
val isSaved: StateFlow<Boolean>
val availableModels: List<AiModel>
val availableRegions: List<Region>
```

## Data Flow

### Content Generation Flow
```
User taps "Generate" (topic screen) or nears the end of the pager
    │
    ▼
GenerateContentUseCase(topic, contentType)
    │
    ▼
ContentRepositoryImpl.generateContent()   (serialised by a Mutex)
    │
    ├──▶ ContentDao.getDedupeTexts()          existing questions / facts, oldest first
    ├──▶ PromptBuilder.build()                topic, difficulty, avoid-list
    ├──▶ BytePlusApi.chatCompletions()        system + user message
    ├──▶ ContentParser.parse()                tolerant JSON parsing, salvages truncated output
    ├──▶ ContentDeduplicator.filterNew()      drops repeats (fun facts compared by fact text)
    └──▶ ContentDao.insertAll()               increasing createdAt → appended at the end
    │
    ▼
Room Flow emits the longer list → pager shows the new items after the existing ones
```

Up to 3 attempts are made for retryable failures (network errors, timeouts, HTTP 408/429/5xx,
unparseable output, only-duplicate output). A generation never "succeeds" with zero items: it fails
with a `GenerationException` whose message is shown to the user with a Retry action.

### Content Viewing Flow
```
ContentPagerScreen opens (topicId, contentType, filter)
    │
    ▼
ContentRepository.observeContent()       ORDER BY createdAt ASC, id ASC
    │
    ▼
LearningPosition.startIndex()            first unviewed item after the furthest viewed one
    │
    ▼
pager scrolls there once, then marks each settled page as viewed
    │
    ▼
within PREFETCH_THRESHOLD of the end → next batch is generated in the background
    │
    ▼
trailing status page shows "generating", the error + Try again, or "Generate more"
```

Filters: `ALL` (learning feed, generates more), `BOOKMARKED` and `MISSED` (review modes, fixed
snapshot of matching items, no generation).

### Settings Flow
```
SettingsScreen opens
    │
    ▼
SettingsViewModel.loadSettings()
    │
    ▼
SettingsDataStore reads preferences
    │
    ▼
User modifies settings
    │
    ▼
SettingsViewModel.saveSettings()
    │
    ▼
SettingsDataStore saves to DataStore
```

## Dependency Injection

### Hilt Modules

#### `AppModule.kt`
Provides:
- `SettingsDataStore` - Settings preferences
- `OkHttpClient` - HTTP client
- `Retrofit` - REST client
- `BytePlusApi` - API interface

#### `DatabaseModule.kt`
Provides:
- `AppDatabase` - Room database instance
- `TopicDao` - Topic data access
- `ContentDao` - Content data access

## Data Models

### API Models (BytePlus)

```kotlin
// BytePlusRequest
data class BytePlusRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 4096
)

data class ChatMessage(
    val role: String,  // "user" or "assistant"
    val content: String
)

// BytePlusResponse
data class BytePlusResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage?
)

data class Choice(
    val index: Int?,
    val message: ResponseMessage?,
    val finish_reason: String?
)
```

### Settings Models

```kotlin
data class AppSettings(
    val apiKey: String = "",
    val modelId: String = "dola-seed-2-1-turbo-260628",
    val region: String = "ap-southeast-1"
)

data class AiModel(
    val id: String,
    val name: String,
    val description: String
)

data class Region(
    val id: String,
    val name: String,
    val baseUrl: String
)
```

### Entity → Domain Mapping

```kotlin
// TopicEntity
data class TopicEntity(
    val id: Long,
    val name: String,
    val description: String,
    val createdAt: Long
) {
    fun toDomain() = Topic(id, name, description, createdAt)
    
    companion object {
        fun fromDomain(topic: Topic) = TopicEntity(...)
    }
}

// ContentEntity
data class ContentEntity(
    val id: Long,
    val topicId: Long,
    val contentType: String,
    val question: String,
    val answer: String?,
    val options: String?,    // Stored as "opt1|||opt2|||opt3"
    val correctIndex: Int?,
    val explanation: String?,
    val isViewed: Boolean,
    val createdAt: Long
) {
    fun toDomain() = GeneratedContent(...)
}
```

## API Integration

### BytePlus ModelArk API

**Base URL**: `https://ark.ap-southeast.bytepluses.com/api/v3/`

**Authentication**: `Authorization: Bearer {API_KEY}`

**Available Regions**:
- `ap-southeast-1` - Asia Pacific (Singapore)
- `eu-west-1` - Europe (Ireland)

### Request Format (OpenAI-compatible)
```json
{
  "model": "dola-seed-2-1-turbo-260628",
  "messages": [
    {"role": "user", "content": "Generate 5 MCQ questions about..."}
  ],
  "temperature": 0.7,
  "max_tokens": 4096
}
```

### Response Format
```json
{
  "id": "chatcmpl-xxx",
  "choices": [{
    "index": 0,
    "message": {"role": "assistant", "content": "Generated text..."},
    "finish_reason": "stop"
  }],
  "usage": {
    "prompt_tokens": 100,
    "completion_tokens": 500,
    "total_tokens": 600
  }
}
```

### Response Handling
```kotlin
val text = response.choices?.firstOrNull()?.message?.content
val contentList = ContentParser.parseResponse(text, contentType, topicId)
```

### Available Models

| Model ID | Name | Description |
|----------|------|-------------|
| dola-seed-2-1-turbo-260628 | Dola Seed 2.1 Turbo | Fastest, newest |
| dola-seed-2-0-lite-260228 | Dola Seed 2.0 Lite | Lightweight |
| dola-seed-2-0-mini-260228 | Dola Seed 2.0 Mini | Small & fast |
| dola-seed-2-0-pro-260228 | Dola Seed 2.0 Pro | Balanced |
| seed-1-6-flash-250615 | Seed 1.6 Flash | Flash model |
| seed-1-6-250615 | Seed 1.6 | Standard |
| deepseek-v3-2 | DeepSeek V3.2 | DeepSeek |
| deepseek-v4-flash | DeepSeek V4 Flash | DeepSeek v4 |
| gpt-oss-120b | GPT-OSS 120B | Open source |

## Prompt Engineering

### MCQ Prompt Template
```
Generate {count} unique multiple choice questions about: {topic}
Additional context: {context}
Return JSON array with question, options, correctIndex, explanation
```

### QA Prompt Template
```
Generate {count} unique question and answer pairs about: {topic}
Return JSON array with question and answer
```

### Fun Fact Prompt Template
```
Generate {count} unique fun facts about: {topic}
Return JSON array with fact and source
```

## Key Features Implementation

### Swipe-to-Delete
Uses `SwipeToDismissBox` with `SwipeToDismissBoxValue.EndToStart`:
```kotlin
SwipeToDismissBox(
    state = dismissState,
    backgroundContent = { /* Delete icon */ },
    enableDismissFromStartToEnd = false
) { /* Topic content */ }
```

### Share Functionality
Uses `Intent.ACTION_SEND`:
```kotlin
val intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, shareText)
}
context.startActivity(Intent.createChooser(intent, "Share via"))
```

### Shimmer Loading
Uses `animateFloat` with `infiniteRepeatable`:
```kotlin
val brush = Brush.linearGradient(
    colors = shimmerColors,
    start = Offset.Zero,
    end = Offset(x = translateAnim.value, y = translateAnim.value)
)
```

### Horizontal Pager
```kotlin
HorizontalPager(state = pagerState) { page ->
    when (contentList[page].contentType) {
        ContentType.MCQ -> MCQCard(content = contentList[page])
        ContentType.QA -> QACard(content = contentList[page])
        ContentType.FUN_FACT -> FunFactCard(content = contentList[page])
    }
}
```

### Settings with DataStore
```kotlin
// Reading
val apiKey by settingsDataStore.apiKey.collectAsState(initial = "")

// Writing
viewModelScope.launch {
    settingsDataStore.saveApiKey("new-api-key")
}
```

## Adding New Features

### Adding a New Screen

1. Create `YourScreen.kt` in `ui/screens/yourfeature/`
2. Create `YourViewModel.kt` with `@HiltViewModel`
3. Add route in `NavGraph.kt`
4. Add navigation from existing screens

### Adding a New Content Type

1. Add enum value in `ContentType.kt`
2. Add prompt template in `PromptBuilder.kt`
3. Add parser in `ContentParser.kt`
4. Create card component in `ui/components/`
5. Add case in `ContentPagerScreen.kt`

### Adding a New DAO Method

1. Add method in appropriate DAO interface
2. Add repository wrapper method
3. Call from ViewModel

### Adding a New AI Model

1. Add model to `availableModels` list in `SettingsViewModel.kt`
2. The model will automatically appear in settings dropdown

## Testing

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Lint check
./gradlew lint
```

## Performance Considerations

- Room queries return `Flow` for reactive updates
- `StateFlow` used with `WhileSubscribed(5000)` for lifecycle awareness
- `HorizontalPager` lazy-loads pages
- Shimmer effects use `rememberInfiniteTransition`
- API calls use coroutines with proper error handling
- DataStore for efficient preferences storage

## Common Issues

### Issue: API key not working
**Solution**: 
1. Go to Settings and verify the API key is correct
2. Check if you have free tokens remaining
3. Ensure the key has proper permissions in BytePlus console

### Issue: Model not available
**Solution**: 
1. Check if the model is activated in BytePlus console
2. Try a different model from Settings
3. Check region availability

### Issue: Database migration error
**Solution**: Increment version in `AppDatabase.kt` and add migration

### Issue: Compose recomposition issues
**Solution**: Use `remember`, `derivedStateOf`, and stable types

### Issue: Network timeout
**Solution**: Check internet connection and API status

### Issue: Empty response from AI
**Solution**: 
1. Check API key permissions
2. Try a different model
3. Simplify the prompt
