# Xem Phim Launcher

Minimal Android launcher used by FreeKiosk.

When the user taps Xem Phim, it opens https://m.youtube.com directly in Brave (com.brave.browser) and finishes.

The AccessibilityService detects the Brave YouTube address-bar region and places a non-touchable accessibility overlay over that region. This hides the Brave address bar visually while keeping Brave and Brave Shields active.

## Build

GitHub Actions builds `app-debug.apk` automatically. The workflow is in `.github/workflows/build.yml`.

## One-time Accessibility setup

Enable the Xem Phim AccessibilityService on the tablet. ADB can be used:

`adb shell settings put secure enabled_accessibility_services com.tam.xemphim/.BraveToolbarService`

`adb shell settings put secure accessibility_enabled 1`
