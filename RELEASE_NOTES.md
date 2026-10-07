# Mouse Trap v1.0.5

Durable compatibility release for obfuscated Dumb Launcher builds.

## Changes

- Replaces R8-generated helper-name profiles with bounded DEX discovery under `bc.*`.
- Loads candidate classes without initialization and requires exactly one static `boolean(Context, String)` collaborator.
- Fails closed when discovery is absent, ambiguous, or incomplete.
- Preserves legacy `boolean(String)` support and existing configured package storage.

## Verification

- Host resolver regression tests passed, including incomplete class-loading and method-inspection cases.
- Exact signed APK, installed-byte identity, and signer continuity were verified.
- Runtime LSPosed binding to `bc.a1.f` was verified on Dumb Launcher `v6.37.0-beta.10` on a 4058G.
- Physical DumbMouse behavior was confirmed after installation.
