# Mouse Trap v1.0.4

Verified compatibility release for Dumb Launcher `v6.37.0-beta.7`.

## Changes

- Adds verified launcher helper profiles for `bc.w0`, `bc.x0`, and `bc.z0`.
- Requires exactly one static `boolean(Context, String)` collaborator across discovered and profiled classes.
- Fails closed when no candidate or multiple candidates exist.
- Preserves legacy `boolean(String)` support and existing configured package storage.

## Verification

- Host resolver regression tests passed.
- Exact signed APK and signer continuity verified.
- Runtime LSPosed binding to `bc.z0.f` verified on a 4058G.
- Physical DumbMouse activation verified with Aurora Store and Beeper.
