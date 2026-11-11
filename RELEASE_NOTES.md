# Mouse Trap v1.0.6

Compatibility update for newer Dumb Launcher releases.

## Changes

- Discovers the mouse-target predicate in both the `bc.*` and `ec.*` launcher namespaces, while requiring exactly one matching method across both.
- Preserves support for older launchers and fails closed if discovery is missing or ambiguous.
- Keeps existing Mouse Trap selections and the launcher's built-in targets unchanged.

## Verification

- Host resolver tests cover both namespaces, older and newer targets, ambiguity, and incomplete discovery.
- A signed 1.0.6-dev candidate was installed on a TCL 4058G running Dumb Launcher v6.55.0-beta.13. LSPosed logged the expected hook and the user confirmed that the mouse worked.
- The final release's packaged code was compared with that tested development candidate; the release APK itself was not installed on the phone.

## Updating

Install the signed APK over the existing Mouse Trap installation and restart Dumb Launcher. Existing selections and LSPosed scope are retained by an in-place update.
