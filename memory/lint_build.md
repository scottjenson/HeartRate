---
name: lint-build
description: Key lint fixes and build configuration decisions for this project
metadata:
  type: project
---

**compileSdk = 36 is required** — `activity-compose:1.13.0` mandates it. Do not drop below 36.

**`kotlinOptions` deprecation warning** — The replacement (`compilerOptions` block inside `android {}`) requires a newer AGP than this project uses. Leave `kotlinOptions` as-is; the deprecation is a warning only and does not block the build.

**`InvalidFragmentVersionForActivityResult`** — A false-positive lint error for projects using `registerForActivityResult` without any Fragment dependency. Suppressed with `@SuppressLint("InvalidFragmentVersionForActivityResult")` on the property in `MainActivity`. Do not add a fragment library just to satisfy this check.

**`GradleTargetSdkVersion` warning in AS** — AS shows this any time `targetSdk` is edited. It is informational only; the build succeeds. No action needed for this app.

**`WearStandaloneAppFlag`** — Manifest must include `<meta-data android:name="com.google.android.wearable.standalone" android:value="true"/>` inside `<application>` or lint fails.

**Skipped lint issues** (intentional): dependency version bumps, `android:icon`, tile preview assets, `taskAffinity`, `WearRecents`.

**How to apply:** Run `./gradlew lintDebug` to check for regressions. Expect 12 warnings (all version-bump noise); zero errors is the target.
