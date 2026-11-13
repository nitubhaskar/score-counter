# 🎴 Score Counter — Card Game Score Tracker for Android

A sleek, lightweight, and modern Android application designed for tracking scores during card games, board games, and multi-player tabletop games. Built natively with **Kotlin**, **Jetpack Compose**, and **Material 3**.

---

## ✨ Features

- 🎨 **Concept Table Layout**: Clean grid layout with vibrant, theme-adaptive player colors for maximum contrast and legibility in both Light and Dark modes.
- 📐 **Adaptive Screen Auto-Fitting**: Automatically stretches player columns to fill 100% of the screen width for 1–4 players, with smooth horizontal scrolling for 5+ players.
- ↔️ **Intuitive Column Reordering**: Tap any player header to reveal 1-tap `◀` and `▶` arrows to instantly swap column order.
- 🏆 **Smart Score Highlighting & Winner Stars**:
  - Toggle between **highest total** or **lowest total** green target score highlighting.
  - Mark round winners with a green star (`★`) badge.
- 🔢 **Custom Number Pad Dialog**: Dedicated, clutter-free numeric keypad for rapid score entries.
- 🔒 **Keep Screen Awake Toggle**: Built-in top-bar lock (`🔒`) to keep your screen awake continuously during game night.
- 💾 **100% Local Data Persistence**: Saves active games, player lists, round score history, column orders, and theme preferences locally across app restarts.
- 🌓 **Dark & Light Material 3 Themes**: One-tap toggle between sleek charcoal dark mode and clean light mode.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Min SDK**: `26` (Android 8.0 Oreo)
- **Target SDK**: `35` (Android 15)
- **Persistence**: SharedPreferences + JSON Serialization
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio Ladybug (or newer)](https://developer.android.com/studio)
- JDK 17 or higher
- Android SDK 35

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/nitubhaskar/Score-counter.git
   cd Score-counter
   ```

2. **Open in Android Studio**:
   Open Android Studio and select **Open an Existing Project**, then choose the `Score-counter` directory.

3. **Build & Run**:
   - Run via Gradle task:
     ```bash
     ./gradlew :app:assembleDebug
     ```
   - Or press **Run (Shift + F10)** in Android Studio to launch on your connected Android device or emulator.

---

## 📱 License

Distributed under the **GNU General Public License v3.0 (GPLv3)**. See `LICENSE` for details.
