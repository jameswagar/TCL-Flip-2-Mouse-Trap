# Mouse Trap

Mouse Trap is a keypad-friendly app and LSPosed module for the rooted TCL Flip 2. It lets the user choose additional apps that should automatically activate DumbMouse, without modifying or re-signing DumbDown Launcher.

## What it does

- Presents a full-screen, keypad-friendly list of launchable third-party apps.
- Excludes system/internal pages and apps already covered by DumbDown Launcher's built-in mouse list.
- Wraps long app names and updates the custom-target count live.
- Starts with Beeper, Telegram, and Lime selected.
- Preserves the launcher's existing behavior for Spotify, Uber Lite, Chrome, Maps Lite, Apple Music, AntennaPod, and OpenBubbles.
- Uses a mouse-pointer app icon.

## Requirements and LSPosed dependency

Mouse Trap **requires LSPosed for the automatic mouse behavior**. The configuration screen itself can open without LSPosed, but selecting apps will have no effect unless the module is enabled and scoped to DumbDown Launcher.

Required phone components:

- Root/Magisk
- DumbMouse (the phone's existing Magisk module/native mouse service)
- [LSPosed 1.9.2](https://github.com/LSPosed/LSPosed/releases/tag/v1.9.2) — the official release currently installed on the phone. The upstream repository is archived, but this is the exact compatible version used and verified by Mouse Trap.
- DumbDown Launcher (`com.offlineinc.dumbdownlauncher`)

Mouse Trap does not implement a second mouse or accessibility service. Its LSPosed hook augments the launcher's existing mouse-target decision. This avoids competing with the launcher's accessibility service.

## Installation

1. Install the signed release APK.
2. Grant the one-time development permission:
   ```sh
   adb shell pm grant com.dumbphone.mousetrap android.permission.WRITE_SECURE_SETTINGS
   ```
3. In LSPosed, enable **Mouse Trap** and scope it only to **Dumb Launcher** (`com.offlineinc.dumbdownlauncher`).
4. Restart DumbDown Launcher once (or reboot the phone).
5. Open Mouse Trap, select apps, and choose **Save mouse targets**.

The selected package names are stored in the per-user Android secure setting `mousetrap_packages`.

## Hook safety

The current DumbDown Launcher obfuscates method names on each release. Mouse Trap therefore locates the mouse-target predicate by its distinctive `boolean(String)` signature instead of a fixed obfuscated name. If that signature is no longer unique after a future launcher update, Mouse Trap refuses to hook and logs a safe failure rather than guessing.

## Reverting

Disable Mouse Trap in LSPosed and restart DumbDown Launcher. The launcher's original built-in mouse behavior immediately returns unchanged. The app can then be uninstalled normally.

## Building

`build.sh` uses the locally installed Android SDK build tools and OpenJDK. It creates a local signing keystore if one does not exist. Signing keys and APK/build outputs are ignored by Git and must never be committed.
