package com.kaushalkr.axiom.feature.routine.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.components.GlassDialog
import com.kaushalkr.axiom.core.designsystem.components.GlassTextField
import com.kaushalkr.axiom.core.designsystem.layout.LocalScreenContentPadding
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.domain.routine.RoutineConflict
import com.kaushalkr.axiom.core.model.MINUTES_PER_DAY
import com.kaushalkr.axiom.core.model.RoutineKind
import com.kaushalkr.axiom.core.model.RoutineLimits
import com.kaushalkr.axiom.core.model.SubjectRef
import com.kaushalkr.axiom.feature.routine.defaultTitleRes
import com.kaushalkr.axiom.feature.routine.editor.components.RoutineAutomationCard
import com.kaushalkr.axiom.feature.routine.editor.components.RoutineConflictBanner
import com.kaushalkr.axiom.feature.routine.editor.components.RoutineConflictSheet
import com.kaushalkr.axiom.feature.routine.editor.components.RoutineDaysCard
import com.kaushalkr.axiom.feature.routine.editor.components.RoutineKindCard
import com.kaushalkr.axiom.feature.routine.editor.components.RoutineLinkedTasksCard
import com.kaushalkr.axiom.feature.routine.editor.components.RoutineTimeCard
import com.kaushalkr.axiom.feature.routine.editor.components.TaskLinkSheet
import com.kaushalkr.axiom.feature.tasks.editor.components.TaskDateSheet
import com.kaushalkr.axiom.feature.tasks.editor.components.TaskTimeSheet
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: ROUTINE_EDITOR_ROUTE =====
/**
 * Stateful entry point for creating, editing or duplicating a timetable block. Saving a block
 * that overlaps others opens the conflict sheet; leaving with unsaved changes asks first.
 */
@Composable
fun RoutineBlockEditorRoute(
    blockId: Long,
    duplicateOf: Long,
    initialDate: LocalDate?,
    initialStartMinute: Int?,
    onClose: () -> Unit,
    onOpenDuplicate: (blockId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoutineBlockEditorViewModel = viewModel(
        factory = RoutineBlockEditorViewModel.factory(blockId, duplicateOf, initialDate, initialStartMinute),
    ),
) {
    val latestOnClose by rememberUpdatedState(onClose)
    val latestOnOpenDuplicate by rememberUpdatedState(onOpenDuplicate)
    var showConflictSheet by rememberSaveable { mutableStateOf(false) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var showStartSheet by rememberSaveable { mutableStateOf(false) }
    var showEndSheet by rememberSaveable { mutableStateOf(false) }
    var showDateSheet by rememberSaveable { mutableStateOf(false) }
    var showTaskSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RoutineEditorEvent.Closed -> latestOnClose()
                RoutineEditorEvent.ShowConflicts -> showConflictSheet = true
                is RoutineEditorEvent.OpenDuplicate -> latestOnOpenDuplicate(event.blockId)
            }
        }
    }

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val openTasks by viewModel.openTasks.collectAsStateWithLifecycle()
    val conflicts by viewModel.conflicts.collectAsStateWithLifecycle()
    val hasChanges by remember(viewModel) { derivedStateOf { viewModel.hasChanges } }
    val subjectName = subjects.firstOrNull { it.id == viewModel.subjectId }?.name
    val fallbackTitle = subjectName.takeIf { viewModel.kind == RoutineKind.STUDY } ?: stringResource(viewModel.kind.defaultTitleRes())

    BackHandler(enabled = hasChanges) { showDiscardDialog = true }

    RoutineBlockEditorScreen(
        viewModel = viewModel,
        subjects = subjects,
        conflicts = conflicts,
        fallbackTitle = fallbackTitle,
        onPickStart = { showStartSheet = true },
        onPickEnd = { showEndSheet = true },
        onPickDate = { showDateSheet = true },
        onLinkTasks = { showTaskSheet = true },
        modifier = modifier,
    )

    // ===== SECTION: ROUTINE_EDITOR_OVERLAYS =====
    TaskTimeSheet(
        visible = showStartSheet,
        initialTime = viewModel.startMinute.toLocalTime(),
        onConfirm = { time -> viewModel.selectStart(time.hour * 60 + time.minute) },
        onDismiss = { showStartSheet = false },
    )
    TaskTimeSheet(
        visible = showEndSheet,
        initialTime = (viewModel.startMinute + viewModel.durationMinutes).toLocalTime(),
        onConfirm = { time -> viewModel.selectEnd(time.hour * 60 + time.minute) },
        onDismiss = { showEndSheet = false },
    )
    TaskDateSheet(
        visible = showDateSheet,
        selectedDate = viewModel.oneOffDate,
        today = viewModel.today,
        onSelect = viewModel::selectOneDay,
        onDismiss = { showDateSheet = false },
    )
    TaskLinkSheet(
        visible = showTaskSheet,
        openTasks = openTasks,
        linkedIds = viewModel.linkedTasks.map { it.id }.toSet(),
        onToggle = viewModel::toggleTask,
        onDismiss = { showTaskSheet = false },
    )
    RoutineConflictSheet(
        visible = showConflictSheet,
        conflicts = conflicts,
        resolutions = viewModel.resolutions,
        onMove = { viewModel.resolveByMoving(fallbackTitle) },
        onShorten = { viewModel.resolveByShortening(fallbackTitle) },
        onSaveAnyway = { viewModel.save(fallbackTitle, force = true) },
        onDismiss = { showConflictSheet = false },
    )
    GlassDialog(
        visible = showDiscardDialog,
        onDismissRequest = { showDiscardDialog = false },
        title = stringResource(R.string.dialog_discard_title),
        message = stringResource(R.string.routine_discard_message),
        confirmLabel = stringResource(R.string.dialog_discard_confirm),
        onConfirm = onClose,
        dismissLabel = stringResource(R.string.dialog_discard_keep),
        isDestructive = true,
    )
    // ===== END SECTION: ROUTINE_EDITOR_OVERLAYS =====
}

private fun Int.toLocalTime(): LocalTime {
    val minute = mod(MINUTES_PER_DAY)
    return LocalTime.of(minute / 60, minute % 60)
}
// ===== END SECTION: ROUTINE_EDITOR_ROUTE =====

// ===== SECTION: ROUTINE_EDITOR_SCREEN =====
@Composable
private fun RoutineBlockEditorScreen(
    viewModel: RoutineBlockEditorViewModel,
    subjects: List<SubjectRef>,
    conflicts: List<RoutineConflict>,
    fallbackTitle: String,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit,
    onPickDate: () -> Unit,
    onLinkTasks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val chromePadding = LocalScreenContentPadding.current
    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = AxiomDimens.contentMaxWidth)
                .fillMaxSize()
                .padding(
                    start = chromePadding.calculateStartPadding(layoutDirection),
                    end = chromePadding.calculateEndPadding(layoutDirection),
                    top = chromePadding.calculateTopPadding(),
                ),
        ) {
            // ===== SECTION: ROUTINE_EDITOR_HEADER =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AxiomDimens.screenHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
            ) {
                Text(
                    text = stringResource(if (viewModel.isNew) R.string.routine_editor_title_new else R.string.routine_editor_title_edit),
                    style = AxiomTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                )
                GlassButton(onClick = { viewModel.save(fallbackTitle) }, enabled = viewModel.isLoaded, tint = colors.accent) {
                    Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.editor_save))
                }
            }
            // ===== END SECTION: ROUTINE_EDITOR_HEADER =====

            if (viewModel.isLoaded) {
                // ===== SECTION: ROUTINE_EDITOR_FORM =====
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            PaddingValues(
                                start = AxiomDimens.screenHorizontalPadding,
                                end = AxiomDimens.screenHorizontalPadding,
                                top = AxiomSpacing.lg,
                                bottom = chromePadding.calculateBottomPadding(),
                            ),
                        ),
                    verticalArrangement = Arrangement.spacedBy(AxiomSpacing.md),
                ) {
                    GlassTextField(
                        state = viewModel.titleState,
                        placeholder = fallbackTitle,
                        label = stringResource(R.string.routine_editor_title_label),
                        maxLength = RoutineLimits.TITLE_MAX_LENGTH,
                        textStyle = AxiomTheme.typography.titleMedium,
                    )
                    RoutineConflictBanner(conflicts = conflicts, onFix = { viewModel.save(fallbackTitle) })
                    RoutineKindCard(
                        kind = viewModel.kind,
                        subjectId = viewModel.subjectId,
                        subjects = subjects,
                        onSelectKind = { viewModel.kind = it },
                        onSelectSubject = { viewModel.subjectId = it },
                    )
                    RoutineTimeCard(
                        startMinute = viewModel.startMinute,
                        durationMinutes = viewModel.durationMinutes,
                        onPickStart = onPickStart,
                        onPickEnd = onPickEnd,
                        onSelectDuration = viewModel::selectDuration,
                    )
                    RoutineDaysCard(
                        repeatMode = viewModel.repeatMode,
                        daysMask = viewModel.daysMask,
                        oneOffDate = viewModel.oneOffDate,
                        onSetDays = viewModel::setDays,
                        onToggleDay = viewModel::toggleDay,
                        onUseOneDay = viewModel::useOneDay,
                        onPickDate = onPickDate,
                    )
                    RoutineLinkedTasksCard(
                        linkedTasks = viewModel.linkedTasks,
                        onLinkTasks = onLinkTasks,
                        onUnlink = viewModel::unlinkTask,
                    )
                    GlassTextField(
                        state = viewModel.notesState,
                        placeholder = stringResource(R.string.editor_description_placeholder),
                        label = stringResource(R.string.editor_description_label),
                        minLines = 2,
                        maxLength = RoutineLimits.NOTES_MAX_LENGTH,
                    )
                    RoutineAutomationCard(automation = viewModel.automation, onChange = { viewModel.automation = it })
                    if (!viewModel.isNew) {
                        Row(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
                            GlassButton(onClick = viewModel::duplicate, modifier = Modifier.weight(1f)) {
                                Icon(painter = painterResource(R.drawable.ic_duplicate), contentDescription = null, modifier = Modifier.size(20.dp))
                                Text(stringResource(R.string.routine_editor_duplicate))
                            }
                            GlassButton(
                                onClick = viewModel::delete,
                                modifier = Modifier.weight(1f),
                                tint = colors.danger,
                                contentColor = Color.White,
                            ) {
                                Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = null, modifier = Modifier.size(20.dp))
                                Text(stringResource(R.string.task_action_delete))
                            }
                        }
                    }
                }
                // ===== END SECTION: ROUTINE_EDITOR_FORM =====
            }
        }
    }
}
// ===== END SECTION: ROUTINE_EDITOR_SCREEN =====
