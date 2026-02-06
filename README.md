# 📱 Reminder - Android Task Management App

A modern, premium Android reminder application built with Java, featuring a beautiful dark UI. Stay organized with smart notifications, home screen widgets, and an intuitive calendar view.

## ✨ Features

### 🔔 Smart Notifications
- **Exact Alarm Scheduling** - Alarms fire precisely at the scheduled time
- **Custom Repeat Intervals** - Daily, Weekly, Monthly, Hourly, or custom minute-based repeats
- **Snooze Functionality** - Configurable snooze duration in settings
- **All-Day Reminders** - Sticky notifications that persist throughout the day
- **Rich Actions** - Complete or snooze directly from notifications
- **Custom Sound & Vibration** - Choose your preferred notification sound

### 🏠 Home Screen Widget
- **Sticky Note Design** - Beautiful glassmorphic widget with your upcoming tasks
- **Smart Date Display** - Shows time for today's tasks, date + time for future tasks
- **Overdue Indicators** - Visual highlighting for past-due tasks
- **One-Tap Actions** - Click to open app or add new reminders
- **Real-time Sync** - Widget updates automatically when tasks change

### 📅 Calendar Overview
- **Monthly View** - Beautiful grid layout with dot indicators for days with reminders
- **Task Preview** - View all reminders for a selected day
- **Smooth Animations** - Collapsible calendar with elegant transitions
- **Quick Navigation** - Easily browse through months

### 🎨 Premium UI/UX
- **Dark Mode Only** - Stunning CRED-inspired dark theme
- **Glassmorphism** - Modern glass-effect cards and panels
- **Smooth Animations** - Polished transitions and micro-interactions
- **Date Separators** - Visual grouping of tasks by date in the main list
- **Multi-Select** - Bulk delete multiple reminders at once
- **Material Design 3** - Latest Material components throughout

### ⚙️ Settings & Customization
- **Default Snooze Duration** - Configure your preferred snooze time (5/10/15/30 minutes)
- **Notification Sound Picker** - Choose from system ringtones
- **Widget Pinning** - Quick access to add widget to home screen

### 💾 Backup & Persistence
- **Android Auto Backup** - Automatic cloud backup via Android system
- **Boot Receiver** - Alarms persist across device restarts
- **Local Database** - All data stored securely with Room Database

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

### Key Libraries
```gradle
// Dependency Injection
implementation 'com.google.dagger:hilt-android:2.48'

// Database
implementation 'androidx.room:room-runtime:2.6.0'

// UI & Navigation
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.navigation:navigation-fragment:2.7.5'

// Widgets
implementation 'androidx.glance:glance-appwidget:1.0.0' (planned)
```

## 📋 Prerequisites

- **Android Studio**: Arctic Fox or newer
- **JDK**: 11 or higher
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)

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

## 📱 First Launch Setup

### Required Permissions
On first launch, the app will request:
- **Notifications** - To show reminder alerts
- **Exact Alarms** - For precise scheduling (Android 12+)

### Add Your First Reminder
1. Tap the **+** button on the home screen
2. Enter a title and optional description
3. Set the date and time
4. Choose a repeat interval (optional)
5. Tap **Save**

### Pin the Widget
1. Open the menu (☰) on the home screen
2. Tap **"Add Widget to Home Screen"**
3. Or add manually from your launcher's widget picker

## 📂 Project Structure

```
app/src/main/
├── java/com/example/reminder/
│   ├── data/                  # Database, DAOs, Repositories
│   │   ├── AppDatabase.java
│   │   ├── Reminder.java      # Entity
│   │   ├── ReminderDao.java
│   │   └── ReminderRepository.java
│   ├── di/                    # Dependency Injection
│   │   └── AppModule.java
│   ├── receiver/              # Broadcast Receivers
│   │   ├── AlarmReceiver.java
│   │   └── BootReceiver.java
│   ├── ui/                    # Fragments & ViewModels
│   │   ├── HomeFragment.java
│   │   ├── AddEditFragment.java
│   │   ├── SettingsFragment.java
│   │   └── calendar/
│   ├── utils/                 # Helpers
│   │   └── NotificationHelper.java
│   ├── widget/                # Widget Implementation
│   │   ├── StickyNoteWidgetProvider.java
│   │   └── WidgetRemoteViewsService.java
│   └── ReminderApp.java       # Application Class
├── res/
│   ├── drawable/              # Custom backgrounds & icons
│   ├── layout/                # XML layouts
│   ├── navigation/            # Navigation graph
│   └── values/                # Colors, strings, themes
└── AndroidManifest.xml
```

## 🎨 Customization

### Change Theme Colors
Edit `app/src/main/res/values/colors.xml`:
```xml
<color name="brand_accent">#0A84FF</color>  <!-- Primary accent -->
<color name="background_page">#0F0F10</color>  <!-- Page background -->
```

### Modify Widget Layout
Edit `app/src/main/res/layout/widget_sticky_note.xml` for the widget appearance.

## 🐛 Known Issues

- **Widget Loading**: If the widget shows "Loading", try removing and re-adding it
- **Notification Sound**: Custom sounds may require storage permission on some devices

## 🔮 Future Enhancements

- [ ] Google Drive Backup/Restore
- [ ] Task Categories with color coding
- [ ] Subtasks and checklists
- [ ] Location-based reminders
- [ ] Dark/Light theme toggle

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.


**⭐ If you find this project helpful, please consider giving it a star!**
