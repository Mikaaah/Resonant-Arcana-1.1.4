# Development

## Current build

Use Java 21 and `./gradlew clean visibilityChecks jar` from the repository root. The build targets Minecraft 1.21.1 / NeoForge 21.1.219 and Iron’s Spells 1.21.1-3.16.3. Iron’s API is resolved from its author’s Maven repository unless `libs/irons-spellbooks.jar` is supplied locally. Third-party runtime dependencies are not committed or bundled.

## Source recovery status

The active source is the validated 1.1.4 update against the exact 1.1.3 binary baseline. Unchanged classes/resources are copied from `baseline/resonant-arcana-1.1.3.jar`; current Java classes replace only their counterparts. The baseline resource title is corrected during packaging. This is not the complete original 1.1.3 source. The previous 0.3.1 checkout is in the original repository’s Git history and must not be used to overwrite newer behavior.

Before changing a class supplied only by the baseline, recover and reconcile its current source. Keep optional integration boundaries intact. Never label decompiled or historical code as the complete original source. Retain the baseline until a complete reconciled source build has equivalent validation.

The active update covers the client config/screen, spell-specific particle visibility, server particle identity and continuous-effect routing. The debug wand still works through the baseline’s existing `WandItem`, `WandClient` and `WandSelectScreen`; those are not the retired school wands.

## Layout

- `src/main/java`: current update source; the opt-in `VisibilityClientChecks` fixture is excluded from release JARs.
- `src/test/java`: configuration persistence, isolation, validation and scope checks.
- `baseline`: the locked owned build input.
- `examples`: generated default visibility configuration.
- `verification/1.1.4`: successful check summaries and dependency fingerprints.
- `downloads`: the current verified release and checksum.
- `wiki`: current guides and version-labelled reference pages; obsolete school-wand pages are removed.

Retired asset/document generators referenced an older source tree and have been removed from this checkout. Do not regenerate current assets from those historical scripts.

## Runtime checks

Provide Iron’s Spells and its required dependencies in `run-server/mods` and `run-client/mods`; the tested versions/hashes are recorded in `verification/1.1.4/Validation-Dependencies.json`. These are test dependencies, not new hard dependencies of this update.

Run `./gradlew runServer -Dresonant_arcana.integrationTest=true`. The opt-in server fixture stops after writing `build/validation/server-integration.json`. For the client fixture, copy the generated `run-server/world` to `run-client/saves/ArcanaQA`, then run `./gradlew runClient -Dresonant_arcana.visibilityClientChecks=true` with a graphical display. The fixture validates the registered Mods menu config screen and all 228 spells’ live particle controls, then exits. Normal development runs leave these fixtures off.

## Repository cleanup

The 18 old alpha/0.x JARs, 11 retired school-wand wiki pages, stale source snapshot/resources and obsolete generators were removed from the active tree. Their history remains in the original repository. Player guides are stored in wiki/; automatic wiki publication is not enabled in this separate repository.
