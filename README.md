# MoodFood 🍽️😊

An Android application that provides personalized nutrition suggestions based on your mood and wellness goals.

## Features

- 🎭 **Mood Tracking**: Log your daily moods and symptoms
- 🍽️ **AI-Powered Suggestions**: Get personalized meal recommendations using OpenRouter AI
- 📊 **Progress Tracking**: Monitor your mood trends and wellness journey
- 📈 **Analytics**: View detailed trends and mood distribution over time
- 🔐 **Secure Authentication**: Firebase-based user authentication
- 💾 **Local Data**: Room database for offline-first experience

## Setup Instructions

### Prerequisites

- Android Studio (latest version)
- JDK 11 or higher
- An OpenRouter API key ([Get one here](https://openrouter.ai/))

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/bhusallaxman22/moodfood.git
   cd moodfood
   ```

2. **Configure API Keys**
   ```bash
   # Copy the example env file
   cp .env.example .env
   
   # Edit .env and add your OpenRouter API key
   # Replace 'your-openrouter-api-key-here' with your actual key
   ```

3. **Configure Firebase** (if using Firebase features)
   - Download `google-services.json` from Firebase Console
   - Place it in `app/google-services.json`

4. **Build and Run**
   - Open the project in Android Studio
   - Sync Gradle files
   - Run on an emulator or physical device

## Environment Variables

The app uses a `.env` file for sensitive configuration. Never commit this file to version control!

```env
# Required
OPENROUTER_API_KEY=your-openrouter-api-key-here

# Optional (defaults provided)
OPENROUTER_MODEL=deepseek/deepseek-chat-v3.1
OPENROUTER_REFERER=https://moodfood.app
```

### How it works:

1. The `.env` file is read by `build.gradle.kts` during build time
2. Values are injected into `BuildConfig` constants
3. The app accesses them via `BuildConfig.OPENROUTER_API_KEY`
4. `.env` is in `.gitignore` to prevent accidental commits

## Project Structure

```
app/
├── src/main/java/com/example/moodfood/
│   ├── data/
│   │   ├── ai/              # OpenRouter API integration
│   │   ├── auth/            # Authentication logic
│   │   ├── db/              # Room database
│   │   └── progress/        # Progress tracking
│   └── ui/
│       ├── auth/            # Login/Signup screens
│       ├── home/            # Home screen with mood input
│       ├── progress/        # Progress tracking screen
│       ├── trends/          # Analytics and trends
│       └── screens/         # Other UI screens
└── build.gradle.kts         # Build configuration with .env support
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **Database**: Room
- **Authentication**: Firebase Auth
- **Networking**: Retrofit + OkHttp
- **AI Integration**: OpenRouter API
- **Async**: Kotlin Coroutines + Flow

## Security Notes

⚠️ **Never commit sensitive data!**

- `.env` file is gitignored
- Use `.env.example` as a template
- API keys should only exist in your local `.env` file
- For CI/CD, use environment secrets

## Contributing

1. Create a feature branch from `main`
2. Make your changes
3. Ensure `.env` is not committed
4. Submit a pull request

## License

[Add your license here]

## Contact

For questions or issues, please open a GitHub issue.
