# Liquid Launcher

Android home-screen launcher (Kotlin + Jetpack Compose) with an iOS-style **liquid glass / water** look.
Every app icon, the clock, the dock, the search pill, the app drawer search bar and the long-press menu
are glass surfaces that refract an animated water wallpaper. Touching anywhere drops a ripple into the water.

## Build and run
1. Open this folder in **Android Studio** (Ladybug or newer) and let Gradle sync.
2. Run on a phone or emulator with **Android 13 (API 33) or newer**. The glass effect uses AGSL shaders, which need API 33.
3. Press the Home button and choose **Liquid Launcher** as the default home app.

To get an APK: Build > Build Bundle(s) / APK(s) > Build APK(s).

## Features
- Animated water wallpaper (gradient, moving caustics, touch ripples)
- Liquid-glass icons: lens refraction at the edges, colour dispersion, rim light, specular highlight
- Squishy spring animation and stronger lens effect when an icon is pressed
- Large glass clock and date (follows the phone language, Bengali digits included)
- Paged home grid with page dots, glass dock (4 apps), glass search pill
- Swipe up (or tap the search pill) for the app drawer with live search; drag the search bar down to close
- Long-press an app: add to / remove from dock, app info, uninstall
- Updates automatically when apps are installed or removed; Home press returns to the first page

## Project layout
- `glass/Shaders.kt` – AGSL water wallpaper and glass shaders
- `glass/LiquidGlass.kt` – `Modifier.waterWallpaper()` and `Modifier.liquidGlass()`
- `glass/WaterState.kt` – shared time and ripple state
- `ui/` – clock, icons, dock, drawer, menu, home screen
- `AppRepository.kt`, `DockStore.kt` – app list and dock persistence

## Notes
- Many animated glass surfaces cost GPU time. On low-end phones, lower the three loop iterations in `waterBg` (Shaders.kt).
- Wallpaper colours are set at the top of `waterBg` in Shaders.kt.
