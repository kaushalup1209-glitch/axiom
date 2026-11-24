# Third-party notices

Axiom includes or depends on the following open-source software.

## Backdrop (Liquid Glass) and Shapes — Kyant

- Source: https://github.com/Kyant0/AndroidLiquidGlass
- Documentation: https://kyant.gitbook.io/backdrop
- Artifacts: `io.github.kyant0:backdrop:2.0.1`, `io.github.kyant0:shapes:1.2.1`
- License: Apache License 2.0. Full text: [`third_party/kyant-backdrop/LICENSE`](third_party/kyant-backdrop/LICENSE)
- Copyright 2025 Kyant

The rendering itself comes unmodified from the published Backdrop library. The following Axiom
files are adapted from the Backdrop catalog sample app (`app/src/commonMain/kotlin/com/kyant/backdrop/catalog/`).
Each file has a header that names its source file and lists the changes made:

| Axiom file | Adapted from |
|---|---|
| `core/designsystem/glass/interaction/DampedDragAnimation.kt` | `utils/DampedDragAnimation.kt` |
| `core/designsystem/glass/interaction/InteractiveHighlight.kt` | `utils/InteractiveHighlight.kt` |
| `core/designsystem/glass/interaction/DragGestureInspector.kt` | `utils/DragGestureInspector.kt` |
| `core/designsystem/components/GlassButton.kt` | `components/LiquidButton.kt` |
| `core/designsystem/components/GlassToggle.kt` | `components/LiquidToggle.kt` |
| `core/designsystem/components/GlassSlider.kt` | `components/LiquidSlider.kt` |
| `core/designsystem/components/GlassTabRow.kt` | `components/LiquidBottomTabs.kt` |
| `core/designsystem/components/GlassTab.kt` | `components/LiquidBottomTab.kt` |

## Material Symbols — Google

- Source: https://github.com/google/material-design-icons
- License: Apache License 2.0
- Used as vector drawables in `app/src/main/res/drawable/ic_*.xml` (except the launcher icon files).

## Android Jetpack, Kotlin and kotlinx libraries

AndroidX (Compose, Activity, Lifecycle, Navigation, DataStore), Kotlin, kotlinx.coroutines and
kotlinx.serialization are licensed under the Apache License 2.0.
