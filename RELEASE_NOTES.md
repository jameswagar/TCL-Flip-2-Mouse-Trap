# Mouse Trap v1.0.7

Compatibility update for newer Dumb Launcher releases.

## Changes

- Discovers the mouse-target predicate in the `fc.*` namespace as well as `bc.*` and `ec.*`, while requiring exactly one matching method across them.
- Preserves older launcher compatibility and fails closed when discovery is missing, ambiguous, or incomplete.
- Keeps Mouse Trap selections and the launcher's built-in targets unchanged.

## Verification

- Host resolver tests cover all three namespaces and cross-namespace ambiguity.
- A signed 1.0.7-dev candidate was installed on a TCL 4058G running Dumb Launcher v6.64.0-beta.3. LSPosed logged the expected hook and the user confirmed the mouse worked.
- The release APK's packaged code was compared with the tested development candidate; the release APK itself was not installed on the phone.

## Updating

Install the signed APK over the existing Mouse Trap installation and restart Dumb Launcher. An in-place update preserves existing selections and LSPosed scope.
