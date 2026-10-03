# Mouse Trap v1.0.4-beta.1

Development candidate for Dumb Launcher `v6.37.0-beta.7` compatibility.

## Changes

- Adds verified launcher helper profiles for `bc.w0`, `bc.x0`, and `bc.z0`.
- Requires exactly one static `boolean(Context, String)` collaborator across discovered and profiled classes.
- Fails closed when no candidate or multiple candidates exist.
- Preserves legacy `boolean(String)` support and existing configured package storage.

## Verification status

- Host resolver regression tests: required before packaging.
- Exact signed APK identity and signer continuity: required before installation.
- Runtime LSPosed binding to `bc.z0.f` and physical mouse behavior on the 4058G: required before release.
