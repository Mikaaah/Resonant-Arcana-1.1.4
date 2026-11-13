# Development guidance

Target Minecraft 1.21.1 / NeoForge / Java 21. Preserve mod IDs, balance, optional integrations, and current 1.1.4 visibility behavior.

This checkout is a source overlay over the exact owned 1.1.3 binary in baseline/. It is not the complete recovered source. Read docs/DEVELOPMENT.md before changing baseline-only classes; never overwrite current behavior with old source.

Run ./gradlew clean visibilityChecks jar for build validation. Runtime checks require the dependencies listed in verification/1.1.4/. Do not claim in-game checks were run unless they actually ran.

Keep tool responses bounded: inspect selected files or line ranges, summarize tree/diff metadata locally, and never print binary/base64 content or full build logs into the conversation. Upload binaries as files. Keep only the baseline, Gradle wrapper, and current verified release JAR in this repository.
