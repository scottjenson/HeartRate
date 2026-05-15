---
name: tile-learnings
description: Pitfalls and API quirks encountered building the Wear OS launch tile
metadata:
  type: project
---

`DpProp` is in `DimensionBuilders`, not `LayoutElementBuilders`. Using `LayoutElementBuilders.DpProp` fails at compile time.

`PrimaryLayout` requires `.setResponsiveContentInsetEnabled(true)` — without it, lint flags a warning and content clips on round screens with long locale strings.

`DeviceParameters` type is inferred from `requestParams.deviceConfiguration`; importing it explicitly is unused and triggers a Kotlin unused-import warning.

`android.content.Intent` is not needed in `TileService` — remove it.

**Why:** All of these caused compile errors or lint failures during initial implementation.

**How to apply:** When writing or editing `HeartRateTileService.kt`, use `DimensionBuilders.DpProp`, always call `setResponsiveContentInsetEnabled(true)`, and don't import `Intent` or `DeviceParameters` explicitly.
