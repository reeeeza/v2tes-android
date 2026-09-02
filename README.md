# v2TeS Android

Android Studio project for **v2TeS** (`app.v2tes.client`).

This is a WebView client that packages the v2TeS UI. Connect is a lab handshake — it does not attach Android `VpnService` or run an Xray core.

Publisher: IRIS BUDDY INC. · Director: Mohamad Reza Eskandari

## Open in Android Studio

1. Clone this repository.
2. **File → Open** and select the `v2tes-android` folder (the one that contains `settings.gradle.kts`).
3. Trust the Gradle project and wait for sync (Android SDK 34, JDK 17).
4. Select the `app` run configuration → Run on a device or emulator (API 23+). Galaxy S24 Ultra is supported (portrait, punch-hole insets).

## Build a debug APK

In Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Layout

```
app/src/main/java/app/v2tes/client/MainActivity.java   WebView shell
app/src/main/assets/www/                              packaged UI
```

To swap in a real tunnel later, replace `MainActivity` with a `VpnService` + Xray/sing-box core. Play Console still requires the VPN declaration and organization website `https://irisbuddy.ca`.
