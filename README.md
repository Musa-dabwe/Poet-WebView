# Poet Music (Android)

An Android application that hosts the Poet Music web app inside a full‑screen WebView. The app launches directly into `https://poetmusic.netlify.app/` and provides a native container for media playback and file selection.

---

## Overview

- Loads the Poet Music site in a `WebView` (no native screens beyond the container)
- Keeps all navigation within the app
- Enables JavaScript, DOM storage, and file access
- Allows media playback without an extra user gesture
- Supports `<input type="file">` (single and multiple selection) via the system picker

---

## How it works

- UI: `app/src/main/res/layout/activity_main.xml` hosts a full‑screen `WebView` with id `webview`.
- Activity: `app/src/main/kotlin/com/musa/poetmusic/MainActivity.kt`
  - Uses ViewBinding to access the `WebView`
  - Configures WebView settings:
    - `javaScriptEnabled = true`
    - `domStorageEnabled = true`
    - `allowFileAccess = true`
    - `setMediaPlaybackRequiresUserGesture(false)` to allow programmatic playback
  - Sets `WebViewClient()` to keep navigation inside the app
  - Sets a `WebChromeClient` that implements `onShowFileChooser(...)` to open the system file picker and return selected URIs (single or multiple) back to the page
  - Lifecycle:
    - `onResume()` resumes the WebView and timers
    - `onPause()` does not pause timers so playlist logic can continue

The current start URL is loaded here:

```kotlin
// app/src/main/kotlin/com/musa/poetmusic/MainActivity.kt
webView.loadUrl("https://poetmusic.netlify.app/")
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
    kotlin/com/musa/poetmusic/MainActivity.kt
    res/layout/activity_main.xml
    AndroidManifest.xml
build.gradle.kts
settings.gradle.kts
```

---

## Permissions

Declared in `AndroidManifest.xml`:

- `INTERNET` — required to load the hosted site
- `READ_EXTERNAL_STORAGE` — present for broad compatibility with older Android versions when picking files. Modern Android typically uses the system picker (SAF) and may not require this permission; you can remove it if not needed for your use case and policy.

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
