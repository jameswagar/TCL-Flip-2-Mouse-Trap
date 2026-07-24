# Mouse Trap v1.2.0

Initial verified release for the rooted TCL Flip 2.

## Features

- Select additional launchable apps that should automatically activate DumbMouse.
- Defaults: Beeper, Telegram, and Lime.
- Full-screen keypad-friendly chooser with wrapped long app names.
- Live custom-target count.
- Filters system/internal entries and DumbDown Launcher's existing mouse targets.
- Mouse-pointer icon.
- Update-resilient LSPosed hook that discovers the launcher's target predicate by signature and fails safely if it is no longer unique.

## Requirements

Automatic mouse activation requires **root/Magisk, DumbMouse, LSPosed, and DumbDown Launcher**. The app UI opens without LSPosed, but selections do not affect mouse behavior until the module is enabled and scoped to `com.offlineinc.dumbdownlauncher`.

## Verified artifact

- Package: `com.dumbphone.mousetrap`
- Version: `1.2.0` (`versionCode 4`)
- SHA-256: `8c9f1d269b36903e8518cd850f8dc8ff8824b2a7075b56c221ed67565296100d`
- Verified installed APK on device serial `DEVICE_SERIAL` matched this digest exactly.
