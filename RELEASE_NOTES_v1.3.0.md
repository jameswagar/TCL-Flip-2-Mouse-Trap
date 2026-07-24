# Mouse Trap v1.3.0

## Changes

- Redesigns Mouse Trap to visually align with Force Stop and TCL's Recent Apps interface.
- Retains the title-cased **Mouse Trap** heading and adds 20sp/18sp typography with 36dp app icons.
- Adds a fully black app-list background so the launcher wallpaper cannot interfere with app names or checkboxes.
- Keeps the launcher wallpaper visible around the heading and bottom action.
- Adds high-contrast checkbox treatment:
  - D-pad moves the focused row.
  - **OK** toggles the focused checkbox.
  - Focused rows reverse to white with black text and controls.
- Keeps long app names wrapped instead of forcing a marquee.
- Capitalizes **Custom Targets** and **Save Mouse Targets**.
- Moves the single **Save Mouse Targets** action into the absolute-bottom TCL menu-bar area, eliminating the unused gray bar beneath it.
- D-pad Down from the final app focuses **Save Mouse Targets**; OK then saves all staged checkbox changes.
- Checkbox toggles continue to update the live count immediately but do not persist until Save is activated.

## Verification

Built and signed with the existing Mouse Trap signing identity and installed over v1.2.0 on the rooted TCL Flip 2 (`4058L`, Android 11).

Verified on-device:

- wallpaper occlusion and exposure regions
- title, status, and action capitalization
- 36dp icons and wrapped labels
- selected and unselected checkbox contrast
- OK toggling and live count updates
- unsaved toggles leave `mousetrap_packages` unchanged
- D-pad navigation from the final app to the bottom Save action
- OK activation of Save
- D-pad Up returning from Save to the final app
- no separate bar beneath **Save Mouse Targets**
