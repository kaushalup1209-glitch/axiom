package com.kaushalkr.axiom.feature.academics.exam

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomChip
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.components.GlassDialog
import com.kaushalkr.axiom.core.designsystem.components.GlassTextField
import com.kaushalkr.axiom.core.designsystem.layout.LocalScreenContentPadding
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.SubjectPalette
import com.kaushalkr.axiom.core.model.AcademicLimits
import com.kaushalkr.axiom.core.model.ExamKind
import com.kaushalkr.axiom.feature.home.formatTime
import com.kaushalkr.axiom.feature.home.rememberTimeFormatter
import com.kaushalkr.axiom.feature.routine.countdownText
import com.kaushalkr.axiom.feature.routine.labelRes
import com.kaushalkr.axiom.feature.tasks.editor.components.EditorFieldLabel
import com.kaushalkr.axiom.feature.tasks.editor.components.EditorSectionCard
import com.kaushalkr.axiom.feature.tasks.editor.components.TaskDateSheet
import com.kaushalkr.axiom.feature.tasks.editor.components.TaskTimeSheet
import com.kaushalkr.axiom.feature.tasks.rememberShortDateFormatter
import java.time.temporal.ChronoUnit

// ===== SECTION: EXAM_EDITOR_ROUTE =====
/** Creates or edits an exam or academic deadline, with a live countdown preview. */
@Composable
fun ExamEditorRoute(
    examId: Long,
    presetSubjectId: Long,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExamEditorViewModel = viewModel(factory = ExamEditorViewModel.factory(examId, presetSubjectId)),
) {
    val colors = AxiomTheme.colors
    val chromePadding = LocalScreenContentPadding.current
    val layoutDirection = LocalLayoutDirection.current
    val latestOnClose by rememberUpdatedState(onClose)
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val canSave by remember(viewModel) { derivedStateOf { viewModel.canSave } }
    val hasChanges by remember(viewModel) { derivedStateOf { viewModel.hasChanges } }
    var showDateSheet by rememberSaveable { mutableStateOf(false) }
    var showTimeSheet by rememberSaveable { mutableStateOf(false) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    val dateFormatter = rememberShortDateFormatter()
    val timeFormatter = rememberTimeFormatter()

    LaunchedEffect(viewModel) { viewModel.closeEvents.collect { latestOnClose() } }
    BackHandler(enabled = hasChanges) { showDiscardDialog = true }

    Box(modifier = modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.TopCenter) {
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
            // ===== SECTION: EXAM_EDITOR_HEADER =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AxiomDimens.screenHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
            ) {
                Text(
                    text = stringResource(if (viewModel.isNew) R.string.exam_editor_title_new else R.string.exam_editor_title_edit),
                    style = AxiomTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                )
                GlassButton(onClick = viewModel::save, enabled = canSave, tint = colors.accent) {
                    Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.editor_save))
                }
            }
            // ===== END SECTION: EXAM_EDITOR_HEADER =====

            if (viewModel.isLoaded) {
                // ===== SECTION: EXAM_EDITOR_FORM =====
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
                        placeholder = stringResource(R.string.exam_title_placeholder),
                        label = stringResource(R.string.exam_title_label),
                        maxLength = AcademicLimits.EXAM_TITLE_MAX_LENGTH,
                        textStyle = AxiomTheme.typography.titleMedium,
                    )
                    EditorSectionCard(iconRes = R.drawable.ic_exam, title = stringResource(R.string.routine_editor_type)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                            ExamKind.entries.forEach { option ->
                                AxiomChip(
                                    label = stringResource(option.labelRes()),
                                    selected = option == viewModel.kind,
                                    onClick = { viewModel.kind = option },
                                    role = Role.RadioButton,
                                )
                            }
                        }
                        if (subjects.isNotEmpty()) {
                            EditorFieldLabel(stringResource(R.string.filter_subject))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                                AxiomChip(
                                    label = stringResource(R.string.editor_estimate_none),
                                    selected = viewModel.subjectId == null,
                                    onClick = { viewModel.subjectId = null },
                                    role = Role.RadioButton,
                                )
                                subjects.forEach { subject ->
                                    AxiomChip(
                                        label = subject.name,
                                        selected = subject.id == viewModel.subjectId,
                                        onClick = { viewModel.subjectId = subject.id },
                                        dotColor = SubjectPalette.color(subject.colorIndex, colors.isDark),
                                        role = Role.RadioButton,
                                    )
                                }
                            }
                        }
                    }
                    EditorSectionCard(iconRes = R.drawable.ic_calendar, title = stringResource(R.string.exam_editor_when)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                            AxiomChip(
                                label = dateFormatter.format(viewModel.date),
                                selected = true,
                                onClick = { showDateSheet = true },
                                iconRes = R.drawable.ic_calendar,
                                role = Role.Button,
                            )
                            AxiomChip(
                                label = stringResource(R.string.editor_time_any),
                                selected = viewModel.time == null,
                                onClick = { viewModel.time = null },
                                role = Role.RadioButton,
                            )
                            AxiomChip(
                                label = viewModel.time?.let { timeFormatter.formatTime(it) } ?: stringResource(R.string.editor_time_pick),
                                selected = viewModel.time != null,
                                onClick = { showTimeSheet = true },
                                iconRes = R.drawable.ic_study_time,
                                role = Role.Button,
                            )
                        }
                        Text(
                            text = countdownText(ChronoUnit.DAYS.between(viewModel.today, viewModel.date)),
                            style = AxiomTheme.typography.titleSmall,
                            color = colors.accent,
                        )
                    }
                    GlassTextField(
                        state = viewModel.notesState,
                        placeholder = stringResource(R.string.exam_notes_placeholder),
                        label = stringResource(R.string.editor_description_label),
                        minLines = 3,
                        maxLength = AcademicLimits.EXAM_NOTES_MAX_LENGTH,
                    )
                    if (!viewModel.isNew) {
                        GlassButton(
                            onClick = viewModel::delete,
                            modifier = Modifier.fillMaxWidth(),
                            tint = colors.danger,
                            contentColor = Color.White,
                        ) {
                            Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = null, modifier = Modifier.size(20.dp))
                            Text(stringResource(R.string.exam_delete))
                        }
                    }
                }
                // ===== END SECTION: EXAM_EDITOR_FORM =====
            }
        }
    }

    // ===== SECTION: EXAM_EDITOR_OVERLAYS =====
    TaskDateSheet(
        visible = showDateSheet,
        selectedDate = viewModel.date,
        today = viewModel.today,
        onSelect = { viewModel.date = it },
        onDismiss = { showDateSheet = false },
    )
    TaskTimeSheet(
        visible = showTimeSheet,
        initialTime = viewModel.time,
        onConfirm = { viewModel.time = it },
        onDismiss = { showTimeSheet = false },
    )
    GlassDialog(
        visible = showDiscardDialog,
        onDismissRequest = { showDiscardDialog = false },
        title = stringResource(R.string.dialog_discard_title),
        message = stringResource(R.string.exam_discard_message),
        confirmLabel = stringResource(R.string.dialog_discard_confirm),
        onConfirm = onClose,
        dismissLabel = stringResource(R.string.dialog_discard_keep),
        isDestructive = true,
    )
    // ===== END SECTION: EXAM_EDITOR_OVERLAYS =====
}
// ===== END SECTION: EXAM_EDITOR_ROUTE =====
