# AI Topic Learning App

An Android application that uses BytePlus ModelArk AI to generate educational content (MCQs, Q&A, Fun Facts) about any topic you want to learn.

## Features

- **Multiple Topics**: Create and manage multiple learning topics
- **AI-Generated Content**: Generate MCQs, Q&A, Fun Facts or a Mix using BytePlus AI
- **Endless Learning Feed**: Content is ordered oldest to newest; new batches are appended to the end and generated automatically as you approach it
- **Resume Where You Left Off**: Opening a topic jumps to the next unseen item
- **Difficulty Levels**: Beginner, Intermediate, Advanced or Mixed per topic
- **Quiz Tracking**: MCQ answers are saved; see accuracy per topic and retry questions
- **Review Modes**: Practise missed questions or go through bookmarked cards
- **Progress Dashboard**: Seen / answered / accuracy per topic, on the home screen and topic screen
- **Reliable Generation**: Tolerant parsing of AI output, duplicate filtering, automatic retries and clear error messages
- **Offline Support**: Access previously generated content without internet
- **Share & Copy**: Share or copy questions and facts
- **Edit & Delete**: Long-press to edit topics, swipe to delete (with confirmation)
- **Model Selection**: Choose from 9+ AI models in settings
- **Region Selection**: Select API region (Asia Pacific or Europe)

## Architecture

This app follows **Clean Architecture** with **MVVM** presentation pattern:

```
Domain Layer (Entities, Repository Interfaces, Use Cases)
    ↓
Data Layer (Room DB, API, Repository Implementations)
    ↓
Presentation Layer (ViewModels, Composables)
```

### Key Design Decisions

- **Repository Pattern**: Interfaces in domain, implementations in data layer
- **Use Cases**: Encapsulate business logic, called by ViewModels
- **Hilt DI**: dependency injection throughout
- **TypeConverters**: Type-safe database queries
- **Shared UiState**: Consistent error/loading/success states

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- BytePlus API key (free tier available)

### Installation

1. Clone the repository
2. Open in Android Studio
3. Wait for Gradle sync
4. Run on device or emulator

### Configuration

1. Open the app
2. Tap the **Settings** icon (gear) in the top right
3. Enter your BytePlus API key
4. Select your preferred AI model
5. Select your region
6. Tap **Save Settings**

## Available AI Models

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

## Project Structure

```
app/src/main/java/com/kresna/aitopicapp/
├── data/                          # Data Layer
│   ├── local/                     # Room DB, DataStore, TypeConverters
│   ├── remote/                    # BytePlus API
│   └── repository/                # Repository Implementations
├── di/                            # Dependency Injection
│   ├── IoDispatcher.kt, ...       # Dispatcher qualifiers
│   ├── DatabaseModule.kt          # Room, DAOs, Repository bindings
│   ├── DispatcherModule.kt        # Coroutine dispatchers
│   └── NetworkModule.kt           # Retrofit, OkHttp, API
├── domain/                        # Domain Layer
│   ├── model/                     # Entities (Topic, GeneratedContent, etc.)
│   ├── repository/                # Repository Interfaces
│   └── usecase/                   # Business Logic
├── ui/                            # Presentation Layer
│   ├── common/                    # Shared UiState
│   ├── components/                # Reusable UI Components
│   ├── navigation/                # NavGraph
│   ├── screens/                   # Screen composables + ViewModels
│   └── theme/                     # Material3 Theme
└── util/                          # PromptBuilder, ContentParser, ContentDeduplicator, LearningPosition
```

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Kotlin | Programming language |
| Jetpack Compose | UI framework |
| Material3 | Design system |
| Room | Local database with TypeConverters |
| DataStore | Preferences storage |
| Hilt | Dependency injection |
| Retrofit | HTTP client |
| Gson | JSON serialization |
| BytePlus ModelArk | AI content generation |

## Testing

Pure-Kotlin logic (response parsing, duplicate filtering, resume position, prompts) is covered by JVM unit tests:

```bash
./gradlew testDebugUnitTest
```

## License

This project is licensed under the MIT License.
