# Mouse Trap v1.0.0

This is the first consolidated stable release of Mouse Trap for the rooted TCL Flip 2.

## Highlights

- Adds custom DumbMouse targets without modifying or re-signing DumbDown Launcher.
- Uses an LSPosed hook scoped to DumbDown Launcher and safely refuses ambiguous hook targets.
- Lists launchable third-party apps while excluding system/internal pages and apps already handled by the launcher's built-in mouse list.
- Uses a title-cased **Mouse Trap** heading, 20sp/18sp typography, 36dp icons, wrapped labels, high-contrast checkboxes, and reverse white focus.
- Fully occludes the wallpaper behind the app list while exposing it around the heading and bottom action.
- D-pad moves through apps and **OK** toggles the focused checkbox.
- Updates the title-cased **Custom Targets** count immediately without persisting staged changes.
- Places the single **Save Mouse Targets** action in the absolute-bottom TCL menu-bar area with no unused bar beneath it.
- D-pad Down from the final app focuses Save, OK persists the staged targets, and D-pad Up returns to the final app.
- Preserves the launcher's built-in handling for Spotify, Uber Lite, Chrome, Maps Lite, Apple Music, AntennaPod, and OpenBubbles.

## Verification

Built and signed with the established Mouse Trap signing identity and installed on the rooted TCL Flip 2 (`4058L`, Android 11).

Verified on-device:

- target filtering and built-in exclusions
- wallpaper occlusion and exposure regions
- title, status, and action capitalization
- icons, wrapped labels, and checkbox contrast
- D-pad and OK checkbox interaction
- live Custom Targets count
- unsaved toggles leaving `mousetrap_packages` unchanged
- absolute-bottom Save focus, activation, and return navigation
- no separate bar beneath **Save Mouse Targets**

The visible release version is `1.0.0`. Android version code remains `5` so this consolidated build upgrades all development builds already installed on the phone.
