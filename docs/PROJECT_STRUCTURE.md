# Axiom project structure

Package root: `app/src/main/java/com/kaushalkr/axiom/`. Every logical block is wrapped in
`// ===== SECTION: NAME =====` / `// ===== END SECTION: NAME =====`. The full list with file
and line numbers is in [`SECTION_INDEX.md`](SECTION_INDEX.md); regenerate it with
`python3 tools/section_index.py > docs/SECTION_INDEX.md` after changing sections.

## Source tree

```
com.kaushalkr.axiom
├── AxiomApplication.kt              Creates the AppContainer
├── MainActivity.kt                  Edge-to-edge, first-frame gate, theme + system bars
├── app/
│   ├── AxiomApp.kt                  Root layout: captured screen, floating chrome, overlay host
│   └── MainActivityViewModel.kt     Settings + Battery Saver + device caps → GlassResolution
├── core/
│   ├── di/AppContainer.kt           Manual DI (DataStore, monitors, capability)
│   ├── model/AppearanceSettings.kt  ThemeMode, GlassMode, AppearanceSettings
│   ├── data/appearance/AppearanceRepository.kt   DataStore-backed preferences
│   ├── platform/
│   │   ├── GlassDeviceCapability.kt  API 31 blur / API 33 refraction / low-RAM detection
│   │   └── PowerSaveModeMonitor.kt   Battery Saver as a Flow
│   ├── designsystem/
│   │   ├── theme/                    AxiomColors, AxiomTypography, AxiomDimens, AxiomTheme
│   │   ├── layout/AxiomWindowLayout.kt   Width classes, screen content padding
│   │   ├── motion/
│   │   │   ├── AxiomMotion.kt            Motion tokens + scheme scaled by the animation scale setting
│   │   │   ├── ContinuousSpring.kt       Retargetable frame-driven spring (no restart stalls)
│   │   │   └── AxiomPressIndication.kt   Soft press overlay that replaces the Material ripple
│   │   ├── glass/
│   │   │   ├── GlassTokens.kt            All glass parameters: surfaces, indicator, thumbs, tint, background
│   │   │   ├── GlassModeResolver.kt      Caps requested mode by device and power state
│   │   │   ├── GlassStyle.kt             Per-mode rendering parameters + CompositionLocals
│   │   │   ├── GlassBackdrops.kt         LocalGlassBackdrop, capture helpers
│   │   │   ├── GlassSurfaceModifier.kt   Modifier.glassSurface (Full / Lite / Off)
│   │   │   ├── shader/                   Shared compiled lens and highlight shaders (GlassShaderCache)
│   │   │   └── interaction/              DampedDragAnimation, InteractiveHighlight, gestures
│   │   └── components/               One reusable component per file:
│   │       GlassCard, GlassButton, GlassIconButton, GlassToggle, GlassSlider,
│   │       GlassTabRow, GlassTab, GlassSegmentedControl, GlassDialog, GlassOverlayHost,
│   │       AxiomBackground, AxiomScreenColumn, ScreenHeader, AxiomIconBadge, AxiomPill,
│   │       StatusBarScrim
│   └── ui/                           App-specific shared UI
│       ├── FeaturePlaceholderScreen.kt   Phase 01 placeholder layout
│       └── AxiomAppMark.kt
├── navigation/
│   ├── AxiomRoutes.kt               Type-safe @Serializable routes
│   ├── TopLevelDestination.kt       Five tabs: label, icon, route
│   ├── AxiomNavHost.kt              Graph, transitions, navigate helpers
│   ├── AxiomNavigationBar.kt        Floating Liquid Glass tab bar
│   └── AxiomTopActions.kt           Floating Shield / Settings / Back buttons
└── feature/
    ├── home/HomeScreen.kt
    ├── planner/PlannerScreen.kt
    ├── focus/FocusScreen.kt
    ├── insights/InsightsScreen.kt
    ├── assistant/AssistantScreen.kt          (AI tab)
    ├── shield/DistractionShieldScreen.kt
    └── settings/
        ├── SettingsScreen.kt                 SettingsRoute (stateful) + SettingsScreen (stateless)
        ├── SettingsViewModel.kt
        └── components/                       SettingsSection, SettingsToggleRow,
                                              SettingsActionRow, SettingsInfoNote
```

Tests live in `app/src/test/java/com/kaushalkr/axiom/` (JVM + Robolectric).

## Liquid Glass rendering model

The app root (`AxiomApp`) stacks three layers:

1. **Captured screen**: `AxiomBackground` (captured into `backgroundBackdrop`) and the
   `NavHost`. The whole layer is captured into `screenBackdrop`.
2. **Status bar scrim.**
3. **Floating chrome and overlays**: top actions, navigation bar and dialogs draw
   `screenBackdrop`, so they refract the content scrolling beneath them.

Rules that keep this correct:

- Glass inside a screen reads `LocalGlassBackdrop`. At screen level that is the ambient
  background; inside a `GlassCard` it is the card's exported backdrop (glass-on-glass).
- Never draw a backdrop from inside the layer that captures it. That loop crashes the
  RenderThread (see the Backdrop FAQ). Chrome and dialogs therefore live outside the captured
  screen, and dialogs are shown through `GlassOverlayHost`.
- Components never check the glass mode themselves beyond reading `LocalGlassStyle`.

| Mode | Captures backdrops | Blur | Lens refraction | Depth shadow | Rim light + sheen | Interactive light | Surfaces |
|---|---|---|---|---|---|---|---|
| Full | yes | yes | yes (wide rim, depth; dispersion on chrome) | yes | yes | RuntimeShader | tinted glass |
| Lite | yes | yes (1.5× radius) | no | no | yes | flat glow | stronger tint |
| Off | no | no | no | no | no | flat glow | opaque + hairline border |

### Where to tune

- **Glass look:** `GlassTokens.kt`. One `GlassSurfaceSpec` per surface kind (`Card`, `Dialog`,
  `Button`, `FloatingButton`, `NavigationBar`, `SegmentedControl`), plus indicator, thumb, press,
  background-texture and tint tokens. Components never hard-code glass values.
- **Motion:** `AxiomMotionTokens` in `AxiomMotion.kt`. Every spring and tween is defined there at
  1x. `AxiomMotion` scales them by the Settings animation scale (tween durations × scale, spring
  stiffness ÷ scale², Off = snap). The Liquid Glass drag springs keep the Backdrop catalog values.
- **Press, release and drag springs** use `ContinuousSpring`: a new target only redirects the running
  spring, so velocity carries through and there is no idle frame. Do not drive per-event animation
  with `Animatable.animateTo`, which restarts and holds for one frame each time.
- **Shaders:** always use `cachedLens` and `CachedHighlights` instead of the library's `lens` and
  `Highlight.Default/Ambient`; they share one compiled shader app-wide (prewarmed at start-up).
- Stationary refraction is visible because the ambient background carries orbs and a tiled dot
  texture (`GlassBackgroundTokens`); a featureless background would hide the lens.

`GlassModeResolver` lowers the requested mode when the device cannot render it:
Android 11 and lower → Off, Android 12 → Lite, low-RAM devices → Lite, Battery Saver (when
"Adapt to Battery Saver" is on) → Lite. Settings explains any downgrade.

## Where to add things in later phases

- New feature: `feature/<name>/` with `<Name>Screen.kt`, `<Name>ViewModel.kt`, and
  `components/` for feature-only UI. Register its route in `AxiomRoutes.kt` and `AxiomNavHost.kt`.
- New app-wide dependency (Room database, repositories): `core/di/AppContainer.kt`.
- New reusable visual component: `core/designsystem/components/`, one per file.
- Permissions: add them to `AndroidManifest.xml` only in the phase that needs them.
