---
name: font-rendering
description: Why the heart rate number uses native android.graphics.Paint instead of Compose Text
metadata:
  type: project
---

`fontVariationSettings` (`'ROND' 100, 'opsz' 96, 'wght' 650`) is not available on `TextStyle` in the Compose version pinned by `wear.compose:compose-material3:1.5.0`. Attempting `TextStyle.copy(fontVariationSettings = ...)` fails to compile.

The workaround: draw the number with `android.graphics.Paint.setFontVariationSettings()` inside a `Modifier.drawBehind { drawIntoCanvas { canvas.nativeCanvas.drawText(...) } }` on a sized `Spacer`.

Font is `google-sans-flex` with `=== Typeface.DEFAULT` reference-equality check to detect fallback to Roboto, then falls back to `google-sans`.

`animateColorAsState` color is bridged to the native Paint via `.toArgb()`.

**Why:** Variable font axes are the only way to get the signature Pixel rounded look (ROND axis). Compose's TextStyle doesn't expose this at the pinned library version.

**How to apply:** Do not replace the Paint-based rendering with Compose `Text` unless the wear.compose library version is bumped and `fontVariationSettings` is confirmed available in `TextStyle`.
