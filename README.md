# 📱 Reminder - Android Task Management App

A modern, premium Android reminder application built with Java, featuring a beautiful dark UI. Stay organized with smart notifications, home screen widgets, cloud backup, and an intuitive calendar view.

## ✨ Features

### 🔔 Smart Notifications
- **Exact Alarm Scheduling** - Alarms fire precisely at the scheduled time
- **Advanced Repeating Tasks** - Customize repeats by specific days (Mon, Wed, Fri) or intervals (every X hours/minutes)
- **Snooze Functionality** - Configurable snooze duration in settings
- **All-Day Reminders** - Sticky notifications that persist throughout the day
- **Rich Actions** - Complete or snooze directly from notifications
- **Custom Sound** - Choose your preferred notification ringtone

### 👆 Intuitive Gestures
- **Swipe Right** - Instantly mark a task as **Complete** (Green tick animation)
- **Swipe Left** - **Delete** a task (with Undo option)
- **Quick Templates** - Tap the Glass icon to access pre-set reminders like "Drink Water"

### 🏠 Home Screen Widget
- **Sticky Note Design** - Beautiful glassmorphic widget with your upcoming tasks
- **Smart Date Display** - Shows time for today's tasks, date + time for future tasks
- **Overdue Indicators** - Visual highlighting for past-due tasks
- **Interactive** - Tap '+' to add tasks or check completion directly from the home screen

### 💾 Cloud Backup & Restore
- **Google Drive Integration** - Securely backup your reminders to your personal Google Drive
- **One-Tap Restore** - Easily recover your data on a new device
- **Android Auto Backup** - Seamless system-level backup support

### 🎨 Premium UI/UX
- **Dark Mode Only** - Stunning CRED-inspired dark theme
- **Glassmorphism** - Modern glass-effect cards and panels
- **Smooth Animations** - Polished transitions and micro-interactions
- **Help Section** - Integrated "How to Use" guide with visual tutorials

### ⚙️ Settings & Customization
- **Default Snooze Duration** - Configure your preferred snooze time (5/10/15/30 minutes)
- **Notification Sound Picker** - Choose from system ringtones
- **Widget Pinning** - Quick access to add widget to home screen

## 🛠️ Tech Stack

### Architecture & Design
- **MVVM Pattern** - Clean separation of concerns
- **Repository Pattern** - Abstraction layer for data operations
- **Dependency Injection** - Hilt for dependency management

### Core Technologies
- **Language**: Java
- **UI**: XML Layouts with Material Design 3
- **Database**: Room (SQLite)
- **Async**: LiveData + Executors
- **Navigation**: Jetpack Navigation Component
- **Cloud**: Google Sign-In & Drive API

### Key Libraries
```gradle
// Dependency Injection
implementation 'com.google.dagger:hilt-android:2.48'

// Database
implementation 'androidx.room:room-runtime:2.6.0'

// UI & Navigation
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.navigation:navigation-fragment:2.7.5'

// Cloud Services
implementation 'com.google.android.gms:play-services-auth:20.7.0'
implementation 'com.google.api-client:google-api-client-android:2.2.0'
```

## 🚀 Getting Started

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/YOUR_USERNAME/Reminder.git
cd Reminder
```

### 2️⃣ Open in Android Studio
1. Launch Android Studio
2. Click **File → Open**
3. Navigate to the cloned `Reminder` directory
4. Click **OK**

### 3️⃣ Sync Gradle
Android Studio should automatically sync Gradle dependencies. If not:
1. Click **File → Sync Project with Gradle Files**
2. Wait for the sync to complete

### 4️⃣ Run the App
1. Connect an Android device or start an emulator (API 26+)
2. Click the **Run** button (▶️) or press `Shift + F10`
3. Select your device and click **OK**

## 📂 Project Structure

```
app/src/main/
├── java/com/example/reminder/
│   ├── data/                  # Database, DAOs, Repositories
│   │   ├── AppDatabase.java
│   │   ├── Reminder.java      # Entity
│   │   ├── ReminderDao.java
│   │   └── BackupRepository.java # Google Drive Logic
│   ├── di/                    # Dependency Injection (Hilt Modules)
│   │   └── AppModule.java
│   ├── domain/                # Use Cases & Clean Architecture
│   │   └── usecase/
│   ├── receiver/              # Broadcast Receivers
│   │   ├── AlarmReceiver.java
│   │   └── BootReceiver.java
│   ├── service/               # Background Services
│   │   ├── AlarmScheduler.java
│   │   └── WidgetRefreshService.java
│   ├── templates/             # Pre-defined Task Templates
│   │   ├── ReminderTemplate.java
│   │   └── Templateadapter.java
│   ├── ui/                    # Fragments & ViewModels
│   │   ├── HomeFragment.java
│   │   ├── AddEditFragment.java
│   │   ├── SettingsFragment.java
│   │   ├── HelpFragment.java  # Documentation screen
│   │   └── SwipeCallback.java # Gesture logic
│   ├── utils/                 # Helpers & Extensions
│   │   ├── NotificationHelper.java
│   │   └── DateFormatter.java
│   ├── widget/                # Widget Implementation
│   │   ├── StickyNoteWidgetProvider.java
│   │   └── WidgetRemoteViewsService.java
│   ├── worker/                # WorkManager Tasks
│   │   └── BackupWorker.java
│   └── ReminderApp.java       # Application Class
├── res/
│   ├── drawable/              # Custom backgrounds & icons
│   ├── layout/                # XML layouts
│   ├── navigation/            # Navigation graph
│   └── values/                # Colors, strings, themes
└── AndroidManifest.xml
```

## 🔮 Future Enhancements

- [ ] Task Categories with color coding
- [ ] Subtasks and checklists
- [ ] Location-based reminders
- [ ] Dark/Light theme toggle
- [ ] Voice input for reminders

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
