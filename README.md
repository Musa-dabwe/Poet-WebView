# My Web View (Android WebView Client)

A free, open-source Android application that provides a simple, fullscreen WebView client. You can use this project as a template to wrap any website or web application in a native Android shell.

---

## Features

- **Fullscreen WebView:** Loads your specified URL in a fullscreen, immersive WebView.
- **File Uploads:** Supports `<input type="file">` for both single and multiple file selection.
- **File Downloads:** Handles file downloads gracefully using Android's native `DownloadManager`.
- **JavaScript Enabled:** Comes with JavaScript, DOM storage, and other essential features enabled by default.
- **Media Playback:** Allows media to be played programmatically without requiring an explicit user gesture.
- **Edge-to-Edge UI:** The WebView content extends behind the system bars for a modern, seamless look.

---

## How to Customize

This project is designed to be easily customized. Follow these instructions to point it to your own website and rebrand it.

### 1. Change the Website URL

To load your own website, edit the `loadUrl(...)` call in `app/src/main/kotlin/com/musa/poetmusic/MainActivity.kt`:

```kotlin
// in MainActivity.kt
webView.loadUrl("https://your-website.com")
```

### 2. Change the App Name

The application's name is defined as a string resource. To change it, edit `app/src/main/res/values/strings.xml`:

```xml
<!-- in strings.xml -->
<resources>
    <string name="app_name">Your App Name</string>
    ...
</resources>
```

### 3. Change the App Icons

The app icons are located in the `app/src/main/res/` directory, under various `mipmap-*` folders (e.g., `mipmap-hdpi`, `mipmap-xhdpi`, etc.). To use your own icons, replace the `ic_launcher.png` and `ic_launcher_round.png` files in each of these directories with your own assets.

A common set of directories to update is:
- `app/src/main/res/mipmap-hdpi/`
- `app/src/main/res/mipmap-mdpi/`
- `app/src/main/res/mipmap-xhdpi/`
- `app/src/main/res/mipmap-xxhdpi/`
- `app/src/main/res/mipmap-xxxhdpi/`

---

## Build and Run

### Requirements
- Android Studio (latest recommended)
- Android SDK 34

### Steps
1.  Open the project in Android Studio.
2.  Allow Gradle to sync.
3.  Click the "Run" button to build and install the app on your connected device or emulator.

Alternatively, you can build from the command line:
```bash
./gradlew installDebug
```

---

## Project Structure

```
app/
  src/main/
    kotlin/com/musa/poetmusic/
      MainActivity.kt
      ObservableWebView.kt
    res/
      layout/activity_main.xml
      values/strings.xml
      mipmap-*/ic_launcher.png
    AndroidManifest.xml
build.gradle.kts
```

---

## Permissions

Declared in `AndroidManifest.xml`:

- `INTERNET`: Required to load the website.
- `WRITE_EXTERNAL_STORAGE`: Required by `DownloadManager` to save files.
- `FOREGROUND_SERVICE`: Recommended when using `DownloadManager`.
- `DOWNLOAD_WITHOUT_NOTIFICATION`: Allows downloads to occur without a system notification.