# Poet Music (Android)

An Android application that hosts the Poet Music web app inside a full‑screen WebView. The app launches directly into `https://68e798dba018581a602a8f18--stellular-panda-0301bb.netlify.app/` and provides a native container for media playback, file selection, and downloads.

---

## Overview

- Loads the Poet Music site in a `WebView` (no native screens beyond the container)
- Keeps all navigation within the app
- Enables JavaScript, DOM storage, and file access
- Allows media playback without an extra user gesture
- Supports `<input type="file">` (single and multiple selection) via the system picker
- Handles file downloads using Android's `DownloadManager`
- Displays a floating action button to open the download manager, which auto-hides on scroll

---

## How it works

- UI: `app/src/main/res/layout/activity_main.xml` hosts a full‑screen `ObservableWebView` inside a `CoordinatorLayout` with a `FloatingActionButton`.
- Activity: `app/src/main/kotlin/com/musa/poetmusic/MainActivity.kt`
  - Uses ViewBinding to access the `WebView` and `FloatingActionButton`
  - Configures WebView settings:
    - `javaScriptEnabled = true`
    - `domStorageEnabled = true`
    - `allowFileAccess = true`
    - `setMediaPlaybackRequiresUserGesture(false)` to allow programmatic playback
  - Sets `WebViewClient()` to keep navigation inside the app
  - Sets a `WebChromeClient` that implements `onShowFileChooser(...)` to open the system file picker and return selected URIs (single or multiple) back to the page
  - Sets a `DownloadListener` that uses `DownloadManager` to handle file downloads
  - Implements a scroll listener on the `ObservableWebView` to show/hide the `FloatingActionButton`
  - Lifecycle:
    - `onResume()` resumes the WebView and timers
    - `onPause()` does not pause timers so playlist logic can continue

The current start URL is loaded here:

```kotlin
// app/src/main/kotlin/com/musa/poetmusic/MainActivity.kt
webView.loadUrl("https://68e798dba018581a602a8f18--stellular-panda-0301bb.netlify.app/")
```

---

## Change the site URL

If you want the app to host a different site, edit the `loadUrl(...)` call in `MainActivity.kt`:

```kotlin
webView.loadUrl("https://your-site.example.com/")
```

Optionally, harden navigation by overriding `shouldOverrideUrlLoading` in a custom `WebViewClient` to limit external origins.

---

## Build and run

### Requirements
- Android Studio (latest)
- Android SDK 34 (compile/target)
- Gradle Wrapper (included)

### Steps
- Open the project in Android Studio, let it sync, then click Run
- Or from the command line:

```bash
./gradlew installDebug
```

Then launch the app on your connected device/emulator.

---

## Project structure (high level)

```
app/
  src/main/
    kotlin/com/musa/poetmusic/
      MainActivity.kt
      ObservableWebView.kt
    res/layout/activity_main.xml
    AndroidManifest.xml
build.gradle.kts
settings.gradle.kts
```

---

## Permissions

Declared in `AndroidManifest.xml`:

- `INTERNET` — required to load the hosted site
- `WRITE_EXTERNAL_STORAGE` — required for the `DownloadManager` to save files to external storage.
- `FOREGROUND_SERVICE` — good practice when using `DownloadManager`.
- `DOWNLOAD_WITHOUT_NOTIFICATION` — allows the app to download files without showing a notification.

---

## Notes and limitations

- Media autoplay: Enabled via `setMediaPlaybackRequiresUserGesture(false)`, but actual behavior may still depend on site logic and OS policies.
- File uploads: `<input type="file">` is supported (single and multiple). The app returns selected URIs to the page via `onActivityResult`.
- Offline content: The current app loads a hosted site. If you need to bundle local content, switch to loading from `file:///android_asset/...` and serve your files from `app/src/main/assets/` (not implemented in the current code).

---

## Troubleshooting

- Nothing loads: Verify network connectivity and that the URL is reachable over HTTPS.
- File picker not appearing: Ensure the action originates from a user gesture and that your emulator/device has a documents provider.
- External links opening outside the app: Provide a custom `WebViewClient` and handle navigation as desired.
- Downloads failing: Ensure the `WRITE_EXTERNAL_STORAGE` permission is granted on the device.