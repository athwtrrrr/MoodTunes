# Mood Tunes 🎵

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)
![Room Database](https://img.shields.io/badge/Room-4285F4?style=for-the-badge&logo=google-cloud&logoColor=white)

A Android app that connects your emotions with music through intelligent mood tracking and personalized recommendations.

[Features](#features) • [Screenshots](#screenshots) • [Tech Stack](#tech-stack) • [Architecture](#architecture) • [Installation](#installation) 

</div>

## 🎯 Overview

Mood Tunes is a mobile-first application that bridges emotional awareness with music discovery. By tracking your moods and music preferences, it provides personalized insights and recommendations to enhance your emotional well-being through the power of music.

## ✨ Features

### 🎨 Intuitive Mood Selection
- **Visual Mood Cards**: Swipe through beautifully designed mood cards with color psychology
- **Emotional Connection**: Images and colors that intuitively represent different emotional states
- **One-tap Selection**: Streamlined workflow to quickly capture your current mood

### 🎵 Smart Music Recommendations
- **Deezer Integration**: Real-time song recommendations based on your selected mood
- **Audio Previews**: 30-second song previews with built-in media player
- **Smart Genre Mapping**: Mood-specific music genres (Calm → Lo-fi, Energetic → Dance)

### 📊 Emotional Insights & Analytics
- **Mood History**: Complete log of your emotional patterns with timestamps
- **Advanced Filtering**: Search, sort, and filter by mood, date, or favorites
- **Pattern Recognition**: AI-powered analysis of your mood trends and transitions

### 💾 Robust Data Management
- **Local-First Architecture**: Works offline with Room database
- **Full CRUD Operations**: Create, read, update, and delete mood entries
- **Favorite System**: Bookmark meaningful songs for quick access

## 📸 Screenshots

<div align="center">

| Mood Selection | Song Recommendations | History | Analytics |
|:--------------:|:--------------------:|:--------:|:----------:|
| <img width="502" height="1110" alt="Mood Selection" src="https://github.com/user-attachments/assets/0fc413cb-d02d-4078-beb1-32f1d0d2b467" /> | <img width="502" height="1110" alt="Song Recommendations" src="https://github.com/user-attachments/assets/24597a56-e4b6-4e58-a476-9425cddaf032" /> | <img width="498" height="1110" alt="History" src="https://github.com/user-attachments/assets/a34e06d6-37aa-46bd-921e-7940a450fe56" /> | <img width="498" height="1110" alt="Analytics" src="https://github.com/user-attachments/assets/9ac94875-c4f7-4d8f-a7a1-356d8644b8c6" /> |



</div>

## 🛠 Tech Stack

### Core Technologies
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room 
- **Async Programming**: Coroutines & Flow
- **Dependency Injection**: Manual DI

### Android Components
- **UI**: XML Layouts with Material Design
- **Navigation**: Navigation Component
- **Media**: MediaPlayer for audio playback
- **Networking**: Retrofit + Gson
- **Image Loading**: Custom coroutine-based solution

### External APIs
- **Deezer API**: Music recommendations and previews
- **Genre-based Chart**: Intelligent mood-to-music mapping

## 🏗 Architecture

```bash
app/
├── data/
│   ├── local/         # Room database & entities
│   ├── repository/    # Data repositories
│   └── api/           # Deezer API integration
├── ui/
│   ├── fragments/     # 4 main fragments
│   ├── adapters/      # RecyclerView adapters
│   └── viewmodels/    # SharedViewModel
└── utils/             # Shared utilities
```
### Key Architecture Decisions

1. **Single Activity Architecture**: Uses Navigation Component with fragments
2. **SharedViewModel**: State sharing across all fragments
3. **Reactive Programming**: StateFlow for real-time UI updates
4. **Repository Pattern**: Clean separation between data layers

## 🚀 Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 21+ (Android 5.0 Lollipop)

### Building from Source

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/mood-tunes.git
   cd mood-tunes
2. **Open in Android Studio**

- Open Android Studio
- Select "Open an existing project"
- Navigate to the cloned directory 
3. **Build and Run**

- Connect an Android device or start an emulator
- Click "Run" in Android Studio (Shift+F10)
  🔧 Key Technical Features

## Complex RecyclerView Implementation

- Multiple interactive elements per item
- Selection state management
- Image loading with cancellation
- Playback controls integration 
### Reactive Data Flow
// Real-time filtering with multiple criteria
val filteredMoodLogs = combine(
searchQuery, sortOption, selectedMoodForSort, moodLogs
) { query, sort, moodFilter, allLogs ->
// Complex filtering logic
} 

### Full CRUD Operations

- CREATE: saveMoodLog() - Save new mood entries
- READ: getAllMoodLogs() - Flow-based data streaming
- UPDATE: toggleFavorite() - Favorite management
- DELETE: deleteMoodLog() + swipe gestures
### Media Playback Management

- Lifecycle-aware audio playback
- MediaPlayer state management
- Proper resource cleanup
- Error handling and recovery
