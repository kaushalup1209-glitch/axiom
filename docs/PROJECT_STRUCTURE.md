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
│   ├── di/AppContainer.kt           Manual DI (DataStore, Room, repositories, task/routine/academic actions,
│   │                                AI command executors, undo, time)
│   ├── model/                       AppearanceSettings, StudyPreferences + StudyDefaults, FocusSession,
│   │                                Task (+ TaskPriority, TaskRecurrence, Subtask, TaskDraft, TaskLimits),
│   │                                Routine (RoutineBlock, RoutineKind, Weekdays, RoutineAutomation,
│   │                                RoutineBlockDraft, RoutineLimits), Academic (Subject, Chapter,
│   │                                Exam, ExamKind, ExamDraft, AcademicLimits)
│   ├── domain/
│   │   ├── StudyStreakCalculator.kt   Pure streak rules
│   │   ├── UndoController.kt          App-wide undo events shown by GlassUndoSnackbarHost
│   │   ├── task/
│   │   │   ├── TaskActions.kt             Complete / reopen / delete / move / save, with undo, in the app scope
│   │   │   ├── TaskRecurrenceCalculator.kt   Next due date for repeating tasks (missed ones skipped)
│   │   │   ├── TaskCommand.kt             AI-ready commands + TaskCommandExecutor + TaskSummary
│   │   │   └── TaskDraftMapping.kt        Task → editable TaskDraft
│   │   ├── routine/
│   │   │   ├── RoutineSchedule.kt         Pure timetable rules: occurrences per day (incl. past midnight),
│   │   │   │                              side-by-side lanes, now/next, free slots, planned minutes
│   │   │   ├── RoutineConflictDetector.kt Overlaps over the next 8 days + one-off dates; move/shorten fixes
│   │   │   ├── RoutineActions.kt          Save / move / delete blocks with undo, in the app scope
│   │   │   └── RoutineCommand.kt          AI-ready timetable + academic commands and RoutineCommandExecutor
│   │   └── academic/
│   │       ├── ExamCountdown.kt           Days until, upcoming, past, next
│   │       └── AcademicActions.kt         Delete subject / chapter / exam with undo; tick and reorder chapters
│   ├── data/
│   │   ├── appearance/AppearanceRepository.kt   Theme, glass, tint, motion preferences
│   │   ├── study/StudyPreferencesRepository.kt  Name, daily goal, quick-start length
│   │   ├── focus/                   FocusSessionRepository (active session in DataStore, history in Room), entity, DAO
│   │   ├── task/                    TaskRepository, TaskDao, TaskEntity / SubtaskEntity
│   │   ├── routine/                 RoutineRepository, RoutineDao, RoutineBlockEntity / RoutineTaskLinkEntity
│   │   ├── academic/                SubjectRepository, ExamRepository, DAOs, Subject / Chapter / Exam entities
│   │   └── database/AxiomDatabase.kt   Room database v3 (schemas exported to app/schemas)
│   ├── platform/
│   │   ├── GlassDeviceCapability.kt  API level, low-RAM flag and total memory
│   │   ├── PowerSaveModeMonitor.kt   Battery Saver as a Flow
│   │   └── TimeProvider.kt           Wall clock + aligned tickers (replaceable in tests)
│   ├── designsystem/
│   │   ├── theme/                    AxiomColors, AxiomTypography, AxiomDimens, AxiomTheme,
│   │   │                             PlannerPalette (subject colours, routine kind colours)
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
│   │   │   ├── GlassSurfaceModifier.kt   Modifier.glassSurface (Full / Lite / Off dispatch)
│   │   │   ├── GlassLiteSurface.kt       Lite lightweight glass: gradient, sheen, rim, one layer
│   │   │   ├── shader/                   Shared compiled lens and highlight shaders (GlassShaderCache)
│   │   │   └── interaction/              DampedDragAnimation, InteractiveHighlight, gestures
│   │   └── components/               One reusable component per file:
│   │       GlassCard, GlassButton, GlassIconButton, GlassToggle, GlassSlider,
│   │       GlassTabRow (+ GlassTabRowLight), GlassTab, LightControlThumb,
│   │       GlassSegmentedControl, GlassDialog, GlassOverlayHost,
│   │       GlassBottomSheet, GlassTextField, GlassSearchField, GlassUndoSnackbarHost,
│   │       AxiomBackground, AxiomScreenColumn, ScreenHeader, AxiomIconBadge, AxiomPill,
│   │       AxiomChip, AxiomTextField, AxiomMonthCalendar (stateful) + AxiomMonthGrid
│   │       (stateless, with per-day marks), AxiomLinearProgress, AxiomProgressRing, StatusBarScrim
│   └── ui/                           App-specific shared UI
│       ├── FeaturePlaceholderScreen.kt   Phase 01 placeholder layout
│       ├── reorder/ReorderableListState.kt   Long-press drag-to-reorder for LazyColumn
│       └── AxiomAppMark.kt
├── navigation/
│   ├── AxiomRoutes.kt               Type-safe @Serializable routes (incl. TaskEditorRoute,
│   │                                RoutineBlockRoute, SubjectRoute, ExamRoute)
│   ├── TopLevelDestination.kt       Five tabs: label, icon, route
│   ├── AxiomNavHost.kt              Graph, transitions, navigate helpers
│   ├── AxiomNavigationBar.kt        Floating Liquid Glass tab bar
│   └── AxiomTopActions.kt           Floating Shield / Settings / Back buttons
└── feature/
    ├── home/
    │   ├── HomeScreen.kt             HomeDashboardRoute (stateful) + HomeScreen (stateless)
    │   ├── HomeViewModel.kt          Minute-tick dashboard state, second-tick focus clock
    │   ├── HomeUiState.kt, HomeFormatting.kt, HomeTaskSummary.kt (real task stats)
    │   └── components/               DailyGoalCard, QuickFocusCard, StudyStatTile, StreakCard,
    │                                 TodayRoutineCard, NextExamCard, TodayTasksCard, HomeCardHeader
    ├── planner/
    │   ├── PlannerScreen.kt          Planner tab: Tasks | Routine | Academics switcher over the section
    │   └── PlannerSection.kt
    ├── tasks/
    │   ├── TasksScreen.kt            TaskListRoute (stateful) + TasksScreen: fixed header over a LazyColumn
    │   ├── TasksViewModel.kt         View, search, filter, sort; list built off the main thread
    │   ├── TaskListModels.kt, TaskListBuilder.kt, TaskListReorder.kt, TaskFormatting.kt
    │   ├── components/               TasksHeader, TaskList, TaskCard, SwipeableTaskItem,
    │   │                             TaskCompletionCheck, TaskSectionHeader, TasksEmptyState,
    │   │                             TaskFilterSheet, TaskSortSheet
    │   └── editor/
    │       ├── TaskEditorScreen.kt   TaskEditorScreenRoute: form, sheets, discard dialog
    │       ├── TaskEditorViewModel.kt
    │       └── components/           EditorSectionCard, EditorFieldLabel, TaskDeadlineCard,
    │                                 TaskPriorityEffortCard, TaskSubtasksCard, SubjectSuggestions,
    │                                 TaskDateSheet, TaskTimeSheet
    ├── routine/
    │   ├── RoutineScreen.kt          RoutineRoute: header + day timeline / week overview / month calendar
    │   ├── RoutineViewModel.kt       View, selected date, drag moves; UI built off the main thread
    │   ├── RoutineModels.kt, RoutineFormatting.kt
    │   ├── components/               RoutineHeader, DayTimeline, RoutineTimelineBlock, DayAgendaHeader,
    │   │                             WeekOverview, WeekDayCard, MonthOverview
    │   └── editor/
    │       ├── RoutineBlockEditorScreen.kt   RoutineBlockEditorRoute: form, sheets, conflict sheet, discard
    │       ├── RoutineBlockEditorViewModel.kt   Create / edit / duplicate, live conflict check
    │       └── components/           RoutineKindCard, RoutineTimeCard, RoutineDaysCard,
    │                                 RoutineLinkedTasksCard, TaskLinkSheet, RoutineAutomationCard,
    │                                 RoutineConflictBanner, RoutineConflictSheet
    ├── academics/
    │   ├── AcademicsScreen.kt        AcademicsRoute: summary, exams with countdowns, subjects
    │   ├── AcademicsViewModel.kt
    │   ├── components/               ExamCountdownRow, SubjectProgressRow, SubjectSheet
    │   ├── subject/                  SubjectDetailRoute + view model; components/ChapterRow
    │   └── exam/                     ExamEditorRoute + view model
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
3. **Floating chrome and overlays**: top actions, navigation bar, the undo snackbar, dialogs and
   bottom sheets draw `screenBackdrop`, so they refract the content scrolling beneath them.

Rules that keep this correct:

- Glass inside a screen reads `LocalGlassBackdrop`. At screen level that is the ambient
  background; inside a `GlassCard` it is the card's exported backdrop (glass-on-glass).
- Never draw a backdrop from inside the layer that captures it. That loop crashes the
  RenderThread (see the Backdrop FAQ). Chrome and dialogs therefore live outside the captured
  screen, and dialogs are shown through `GlassOverlayHost`.
- Components never check the glass mode themselves beyond reading `LocalGlassStyle`.

| Mode | Captured layers | Backdrop blur | Lens refraction | Shaders | Standard surfaces (cards, rows, timer, buttons, inputs, controls) | Prominent surfaces (navigation bar, dialogs, sheets) |
|---|---|---|---|---|---|---|
| Full (opt-in) | background + screen + per-card | every surface | yes | lens, rim light, press light | original Kyant0 Liquid Glass | original Kyant0 Liquid Glass |
| Lite (default) | screen only | prominent only (1.5× radius) | no | none | lightweight glass: gradient, sheen, drawn rim, no layer capture | blur + tint + drawn rim + hardware shadow |
| Off | none | none | no | none | opaque + hairline border | opaque + hairline border |

Lite drops blur entirely (lightweight glass everywhere, nothing captured) when the device has no
RenderEffect (Android 11 and lower) or while "Adapt to Battery Saver" is active. A screen in Lite
therefore blurs at most two surfaces: the navigation bar and one open dialog or sheet. Mark a
surface `GlassEmphasis.PROMINENT` in `GlassTokens` only if it is that kind of surface.

### Where to tune

- **Lite look:** `GlassLiteTokens` (fill gradient, rim colours, thumb and indicator details) and the
  Lite rows of `GlassTintTokens`.
- **Glass look:** `GlassTokens.kt`. One `GlassSurfaceSpec` per surface kind (`Card`, `Dialog`,
  `Button`, `FloatingButton`, `NavigationBar`, `ListItem`, `Input`, `Sheet`, `Snackbar`,
  `SegmentedControl`), plus indicator, thumb, press,
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

## Data rules

- **Real vs placeholder data:** placeholder content, when a phase needs it, lives only in
  `core/data/sample/` and is labelled "Preview" wherever it is shown. Since Phase 04 no sample
  data remains: the Home routine card shows real timetable blocks. `HomeDashboardUiState`
  documents which fields are real (distraction time stays null until Phase 06).
- **Focus timing** is derived from stored timestamps (`ActiveFocusSession`), so it stays exact in
  the background or after the process is killed, without a service. A session that passed its
  planned end is completed when Home next opens. Sessions under one minute are discarded.
- **Tasks** are stored in Room (`tasks`, `subtasks`; added by the additive 1→2 auto-migration,
  covered by `AxiomDatabaseMigrationTest`). Deleting is a hard delete whose snapshot is restored
  with the same ids on undo; completing a repeating task also creates the next occurrence, and
  undo removes it again. Manual order is a fractional `sort_order`, so a drag writes one row.
  Views: Today = due today, Upcoming = due later or no date, Overdue = due before today,
  Done = the latest 300 completed, grouped by completion day.
- **Routines** are stored in Room v3 (`routine_blocks`, `routine_task_links`; additive 2→3
  auto-migration together with the academic tables). A routine is a repeating block: `days_mask`
  holds the weekdays (bit 0 = Monday); 0 means a one-off block on `date` (an epoch day). Times are minutes
  after midnight; a block may end after midnight (`start + duration > 1440`) and then also shows
  as a continuation the next morning. Dragging moves every occurrence of a block.
  `routine_task_links` has a foreign key to the block only (cascade); reads join `tasks`, so a
  deleted task drops out and comes back with its undo.
- **Conflicts:** `RoutineConflictDetector` checks the next 8 days (one full week of weekdays)
  plus upcoming one-off dates. The editor shows overlaps live and on save offers: move to the next
  free start, shorten to fit, or keep both.
- **Academics:** `subjects`, `chapters` (cascade on subject) and `exams` (subject set to null when
  the subject is deleted). Tasks keep their subject as text; renaming a subject also renames it on
  its tasks. Deleting a subject, chapter, exam or block is undoable.
- **Later-phase hooks:** each block stores `auto_start_focus`, `track_distractions` and
  `restrict_apps`. `RoutineSchedule.now` returns the ongoing and next blocks, and
  `RoutineOccurrence.elapsedMinutes` / `remainingMinutes` give their timing, for automatic focus
  sessions (Phase 05) and Distraction Shield (Phases 06–08).
- **Undo** runs through `UndoController` in the application scope, so an undo offered by a screen
  that has closed (the editor) still works from the snackbar.
- **AI preparation:** the assistant should call `AppContainer.taskCommandExecutor` with
  `TaskCommand`s (`create_task`, `update_task`, `reschedule_task`, `complete_task`, `delete_task`,
  `list_open_tasks`). `needsConfirmation` marks the ones to confirm with the user first.
  Timetables and academics go through `AppContainer.routineCommandExecutor` with
  `RoutineCommand`s (`create_routine_block`, `update_routine_block`, `reschedule_routine_block`,
  `delete_routine_block`, `list_routine`, `find_free_time`, `create_subject`, `add_chapters`,
  `create_exam`). Overlapping saves are reported back unless the command sets `allowOverlap`.
- **Glass recommendation:** with no manual choice (new installs, or after Reset appearance),
  `GlassModeResolver.recommend` picks Lite, or Off on low-RAM devices, phones under ~3 GB memory
  and Android 9 or older. Full is never chosen automatically. Settings shows the recommendation
  and can restore it.
- **Shaders** are compiled (off the main thread) only once Full glass is in effect.

## Performance rules

- Remember glass modifiers (`GlassCard`, `GlassButton` do this) and use shared shapes from
  `GlassShapes`, so recomposition never rebuilds the glass pipeline.
- Set `hostsGlass = true` on a `GlassCard` only when it contains glass controls; in Full, exporting
  a backdrop records an extra layer on every redraw (Lite and Off never export).
- Lite reuses one captured layer (`screenBackdrop`) for every blurred surface; do not add new
  backdrop captures or `drawBackdrop` calls for standard surfaces.
- Keep ticking values out of card parameters: pass a `State` and read it in the smallest
  composable (see `QuickFocusCard`). Use `animate = false` on progress that ticks.
- Tickers come from `TimeProvider.ticks` inside `WhileSubscribed` flows, so they stop when the
  screen is not visible.
- Lists: key rows by a stable id, set `contentType`, pass immutable row models (`TaskListItem`)
  and build them on `Dispatchers.Default`. Task rows use `GlassTokens.ListItem` (no drop or depth
  shadow) and never host glass. Keep glass controls in fixed headers rather than floating over a
  scrolling list.
- Timetable: blocks, week-day cards and chapter rows use `GlassTokens.ListItem` and never host
  glass. Glass controls sit in the fixed Routine header (view switcher, previous/next, New block)
  and the month calendar card. The day timeline draws its hour grid in one draw call and places
  blocks by time; a drag offset is applied in a graphics layer, so dragging never relayouts the
  timeline. Short blocks get a minimum height and overlapping blocks share the width in lanes, so
  text never stacks. The week view shows up to 5 blocks per day plus "+N more"; the month view
  shows dots per day instead of blocks.
- In lazy lists, do not keep gesture state such as a swipe-to-dismiss state in `rememberSaveable`:
  the list restores it by key, so a re-added row would come back already dismissed.

## Where to add things in later phases

- New feature: `feature/<name>/` with `<Name>Screen.kt`, `<Name>ViewModel.kt`, and
  `components/` for feature-only UI. Register its route in `AxiomRoutes.kt` and `AxiomNavHost.kt`.
- New app-wide dependency (Room database, repositories): `core/di/AppContainer.kt`.
- New reusable visual component: `core/designsystem/components/`, one per file.
- Permissions: add them to `AndroidManifest.xml` only in the phase that needs them.
