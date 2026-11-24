package com.kaushalkr.axiom.feature.tasks.editor

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomPill
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.components.GlassDialog
import com.kaushalkr.axiom.core.designsystem.components.GlassTextField
import com.kaushalkr.axiom.core.designsystem.layout.LocalScreenContentPadding
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.TaskLimits
import com.kaushalkr.axiom.feature.tasks.editor.components.SubjectSuggestions
import com.kaushalkr.axiom.feature.tasks.editor.components.TaskDateSheet
import com.kaushalkr.axiom.feature.tasks.editor.components.TaskDeadlineCard
import com.kaushalkr.axiom.feature.tasks.editor.components.TaskPriorityEffortCard
import com.kaushalkr.axiom.feature.tasks.editor.components.TaskSubtasksCard
import com.kaushalkr.axiom.feature.tasks.editor.components.TaskTimeSheet

// ===== SECTION: TASK_EDITOR_ROUTE =====
/**
 * Stateful entry point for creating or editing a task. Leaving with unsaved changes asks
 * first; deleting closes the editor and offers undo on the screen underneath.
 */
@Composable
fun TaskEditorScreenRoute(
    taskId: Long,
    dueToday: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskEditorViewModel = viewModel(factory = TaskEditorViewModel.factory(taskId, dueToday)),
) {
    val latestOnClose by rememberUpdatedState(onClose)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { latestOnClose() }
    }
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var showDateSheet by rememberSaveable { mutableStateOf(false) }
    var showTimeSheet by rememberSaveable { mutableStateOf(false) }
    val hasChanges by remember(viewModel) { derivedStateOf { viewModel.hasChanges } }

    BackHandler(enabled = hasChanges) { showDiscardDialog = true }

    TaskEditorScreen(
        viewModel = viewModel,
        subjects = subjects,
        onPickDate = { showDateSheet = true },
        onPickTime = { showTimeSheet = true },
        modifier = modifier,
    )

    TaskDateSheet(
        visible = showDateSheet,
        selectedDate = viewModel.dueDate,
        today = viewModel.today,
        onSelect = viewModel::selectDueDate,
        onDismiss = { showDateSheet = false },
    )
    TaskTimeSheet(
        visible = showTimeSheet,
        initialTime = viewModel.dueTime,
        onConfirm = { time -> viewModel.dueTime = time },
        onDismiss = { showTimeSheet = false },
    )
    GlassDialog(
        visible = showDiscardDialog,
        onDismissRequest = { showDiscardDialog = false },
        title = stringResource(R.string.dialog_discard_title),
        message = stringResource(R.string.dialog_discard_message),
        confirmLabel = stringResource(R.string.dialog_discard_confirm),
        onConfirm = onClose,
        dismissLabel = stringResource(R.string.dialog_discard_keep),
        isDestructive = true,
    )
}
// ===== END SECTION: TASK_EDITOR_ROUTE =====

// ===== SECTION: TASK_EDITOR_SCREEN =====
/** Fixed header with Save above a scrolling form of glass fields and cards. */
@Composable
private fun TaskEditorScreen(
    viewModel: TaskEditorViewModel,
    subjects: List<String>,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chromePadding = LocalScreenContentPadding.current
    val layoutDirection = LocalLayoutDirection.current
    val canSave by remember(viewModel) { derivedStateOf { viewModel.canSave } }

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
            TaskEditorHeader(
                isNew = viewModel.isNew,
                isCompleted = viewModel.isCompleted,
                canSave = canSave,
                onSave = viewModel::save,
                modifier = Modifier.padding(horizontal = AxiomDimens.screenHorizontalPadding),
            )
            if (viewModel.isLoaded) {
                TaskEditorForm(
                    viewModel = viewModel,
                    subjects = subjects,
                    onPickDate = onPickDate,
                    onPickTime = onPickTime,
                    contentPadding = PaddingValues(
                        start = AxiomDimens.screenHorizontalPadding,
                        end = AxiomDimens.screenHorizontalPadding,
                        top = AxiomSpacing.lg,
                        bottom = chromePadding.calculateBottomPadding(),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}
// ===== END SECTION: TASK_EDITOR_SCREEN =====

// ===== SECTION: TASK_EDITOR_HEADER =====
@Composable
private fun TaskEditorHeader(
    isNew: Boolean,
    isCompleted: Boolean,
    canSave: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        Text(
            text = stringResource(if (isNew) R.string.editor_title_new else R.string.editor_title_edit),
            style = AxiomTheme.typography.headlineMedium,
            color = colors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (isCompleted) AxiomPill(text = stringResource(R.string.view_completed))
        GlassButton(onClick = onSave, enabled = canSave, tint = colors.accent) {
            Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(20.dp))
            Text(stringResource(R.string.editor_save))
        }
    }
}
// ===== END SECTION: TASK_EDITOR_HEADER =====

// ===== SECTION: TASK_EDITOR_FORM =====
@Composable
private fun TaskEditorForm(
    viewModel: TaskEditorViewModel,
    subjects: List<String>,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val titleFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (viewModel.isNew) titleFocus.requestFocus()
    }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(AxiomSpacing.md),
    ) {
        GlassTextField(
            state = viewModel.titleState,
            placeholder = stringResource(R.string.editor_title_placeholder),
            label = stringResource(R.string.editor_title_label),
            maxLength = TaskLimits.TITLE_MAX_LENGTH,
            textStyle = AxiomTheme.typography.titleMedium,
            modifier = Modifier.focusRequester(titleFocus),
        )
        GlassTextField(
            state = viewModel.descriptionState,
            placeholder = stringResource(R.string.editor_description_placeholder),
            label = stringResource(R.string.editor_description_label),
            minLines = 3,
            maxLength = TaskLimits.DESCRIPTION_MAX_LENGTH,
        )
        GlassTextField(
            state = viewModel.subjectState,
            placeholder = stringResource(R.string.editor_subject_placeholder),
            label = stringResource(R.string.editor_subject_label),
            leadingIconRes = R.drawable.ic_subject,
            maxLength = TaskLimits.SUBJECT_MAX_LENGTH,
            capitalization = KeyboardCapitalization.Words,
        )
        SubjectSuggestions(subjects = subjects, subjectState = viewModel.subjectState)
        TaskDeadlineCard(
            dueDate = viewModel.dueDate,
            dueTime = viewModel.dueTime,
            recurrence = viewModel.recurrence,
            today = viewModel.today,
            onSetDate = viewModel::selectDueDate,
            onPickDate = onPickDate,
            onPickTime = onPickTime,
            onClearTime = { viewModel.dueTime = null },
            onSelectRecurrence = viewModel::selectRecurrence,
        )
        TaskPriorityEffortCard(
            priority = viewModel.priority,
            estimatedMinutes = viewModel.estimatedMinutes,
            onSelectPriority = { viewModel.priority = it },
            onSelectEstimate = { viewModel.estimatedMinutes = it },
        )
        TaskSubtasksCard(
            subtasks = viewModel.subtasks,
            newSubtaskState = viewModel.newSubtaskState,
            canAdd = viewModel.canAddSubtask,
            onAdd = viewModel::addSubtask,
            onToggle = viewModel::toggleSubtask,
            onRemove = viewModel::removeSubtask,
        )
        if (!viewModel.isNew) {
            GlassButton(
                onClick = viewModel::delete,
                modifier = Modifier.fillMaxWidth(),
                tint = colors.danger,
                contentColor = Color.White,
            ) {
                Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = null, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.editor_delete))
            }
        }
    }
}
// ===== END SECTION: TASK_EDITOR_FORM =====
