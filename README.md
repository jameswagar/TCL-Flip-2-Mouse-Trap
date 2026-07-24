# Mouse Trap

Mouse Trap is a keypad-friendly app and LSPosed module for the rooted TCL Flip 2. It lets the user choose additional apps that should automatically activate DumbMouse, without modifying or re-signing DumbDown Launcher.

## What it does

- Presents a keypad-friendly list of launchable third-party apps with 36dp icons, 18sp wrapped labels, high-contrast checkboxes, and a live **Custom Targets** count.
- Uses the launcher wallpaper around the title and bottom save action while fully occluding it behind the app list.
- Uses **OK** to toggle the focused checkbox without saving immediately.
- Places the single **Save Mouse Targets** action at the absolute bottom of the screen; D-pad Down from the final app focuses it and OK saves.
- Excludes system/internal pages and apps already covered by DumbDown Launcher's built-in mouse list.
- Starts with Beeper, Telegram, and Lime selected.
- Preserves the launcher's existing behavior for Spotify, Uber Lite, Chrome, Maps Lite, Apple Music, AntennaPod, and OpenBubbles.
- Uses a mouse-pointer app icon.

## Requirements and LSPosed dependency

Mouse Trap **requires LSPosed for the automatic mouse behavior**. LSPosed is a separate dependency: it is not bundled with Mouse Trap and is not preinstalled on a stock Dumb Co phone. Install LSPosed before configuring Mouse Trap. The configuration screen itself can open without LSPosed, but selecting apps will have no effect unless the module is enabled and scoped to DumbDown Launcher.

Required phone components:

- Root/Magisk
- DumbMouse (the phone's existing Magisk module/native mouse service)
- [LSPosed 1.9.2](https://github.com/LSPosed/LSPosed/releases/tag/v1.9.2) — a separate dependency that must be installed on a stock Dumb Co phone. This archived official release is the exact version tested and verified for compatibility with Mouse Trap.
- DumbDown Launcher (`com.offlineinc.dumbdownlauncher`)

Mouse Trap does not implement a second mouse or accessibility service. Its LSPosed hook augments the launcher's existing mouse-target decision. This avoids competing with the launcher's accessibility service.

## Installation

1. On a stock Dumb Co phone, install [LSPosed 1.9.2](https://github.com/LSPosed/LSPosed/releases/tag/v1.9.2). This dependency is not supplied by Mouse Trap or preinstalled by Dumb Co.
2. Install the signed Mouse Trap release APK.
3. Grant the one-time development permission:
   ```sh
   adb shell pm grant com.dumbphone.mousetrap android.permission.WRITE_SECURE_SETTINGS
   ```
4. In LSPosed, enable **Mouse Trap** and scope it only to **Dumb Launcher** (`com.offlineinc.dumbdownlauncher`).
5. Restart DumbDown Launcher once (or reboot the phone).
6. Open Mouse Trap, select apps with **OK**, move down to **Save Mouse Targets**, and press **OK** to save.

The selected package names are stored in the per-user Android secure setting `mousetrap_packages`.

## Hook safety

The current DumbDown Launcher obfuscates method names on each release. Mouse Trap therefore locates the mouse-target predicate by its distinctive `boolean(String)` signature instead of a fixed obfuscated name. If that signature is no longer unique after a future launcher update, Mouse Trap refuses to hook and logs a safe failure rather than guessing.

## Reverting

Disable Mouse Trap in LSPosed and restart DumbDown Launcher. The launcher's original built-in mouse behavior immediately returns unchanged. The app can then be uninstalled normally.

## Building

`build.sh` uses the locally installed Android SDK build tools and OpenJDK. It deliberately refuses to create a signing identity: supply an existing release or development keystore and read the password without placing it in shell history:

```sh
export MOUSE_TRAP_KEYSTORE=/secure/path/mousetrap-release.jks
read -s MOUSE_TRAP_STOREPASS && export MOUSE_TRAP_STOREPASS
./build.sh
```

The official release keystore is required to produce update-compatible APKs. A newly generated development key can build test APKs but cannot update the official release. Preserve signing keys and passwords privately; the keystore, APK, and build outputs are ignored by Git and must never be committed.

Optional overrides:

```sh
export MOUSE_TRAP_KEY_ALIAS=mousetrap
read -s MOUSE_TRAP_KEYPASS && export MOUSE_TRAP_KEYPASS
```
