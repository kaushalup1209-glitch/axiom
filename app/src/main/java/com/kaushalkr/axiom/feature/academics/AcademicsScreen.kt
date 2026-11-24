package com.kaushalkr.axiom.feature.academics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomScreenColumn
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.AcademicLimits
import com.kaushalkr.axiom.feature.academics.components.ExamCountdownRow
import com.kaushalkr.axiom.feature.academics.components.SubjectProgressRow
import com.kaushalkr.axiom.feature.academics.components.SubjectSheet
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ===== SECTION: ACADEMICS_ROUTE =====
/** Academic planner: exams and deadlines with countdowns, and subjects with syllabus progress. */
@Composable
fun AcademicsRoute(
    onOpenSubject: (subjectId: Long) -> Unit,
    onOpenExam: (examId: Long) -> Unit,
    onNewExam: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AcademicsViewModel = viewModel(factory = AcademicsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AxiomTheme.colors
    val scope = rememberCoroutineScope()
    var showSubjectSheet by rememberSaveable { mutableStateOf(false) }
    var showPastExams by rememberSaveable { mutableStateOf(false) }
    var newSubjectColor by rememberSaveable { mutableIntStateOf(0) }
    var subjectError by rememberSaveable { mutableStateOf<String?>(null) }
    val nameState = rememberTextFieldState()
    val blankError = stringResource(R.string.subject_error_blank)
    val duplicateError = stringResource(R.string.subject_error_duplicate)

    AxiomScreenColumn(modifier = modifier) {
        val loaded = state
        // ===== SECTION: ACADEMICS_HEADER =====
        Text(
            text = when {
                loaded == null || loaded.subjects.isEmpty() -> stringResource(R.string.academics_summary_empty)
                loaded.chaptersTotal == 0 -> pluralStringResource(R.plurals.academics_summary_subjects, loaded.subjects.size, loaded.subjects.size)
                else -> stringResource(
                    R.string.tasks_summary_joined,
                    pluralStringResource(R.plurals.academics_summary_subjects, loaded.subjects.size, loaded.subjects.size),
                    stringResource(R.string.academics_summary_syllabus, (loaded.syllabusProgress * 100).roundToInt()),
                )
            },
            style = AxiomTheme.typography.titleMedium,
            color = colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
            GlassButton(
                onClick = {
                    nameState.clearText()
                    subjectError = null
                    newSubjectColor = (loaded?.subjects?.size ?: 0) % AcademicLimits.SUBJECT_COLOR_COUNT
                    showSubjectSheet = true
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.academics_add_subject))
            }
            GlassButton(onClick = onNewExam, modifier = Modifier.weight(1f), tint = colors.accent) {
                Icon(painter = painterResource(R.drawable.ic_exam), contentDescription = null, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.academics_add_exam))
            }
        }
        // ===== END SECTION: ACADEMICS_HEADER =====

        if (loaded != null) {
            // ===== SECTION: ACADEMICS_EXAMS =====
            GlassCard(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xxs)) {
                Text(
                    text = stringResource(R.string.academics_exams_title),
                    style = AxiomTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .padding(bottom = AxiomSpacing.xs)
                        .semantics { heading() },
                )
                if (loaded.upcomingExams.isEmpty()) {
                    Text(text = stringResource(R.string.academics_exams_empty), style = AxiomTheme.typography.bodyMedium, color = colors.textSecondary)
                } else {
                    loaded.upcomingExams.forEach { exam ->
                        ExamCountdownRow(exam = exam, today = loaded.today, onClick = { onOpenExam(exam.id) })
                    }
                }
                if (loaded.pastExams.isNotEmpty()) {
                    Text(
                        text = if (showPastExams) {
                            stringResource(R.string.academics_hide_past)
                        } else {
                            pluralStringResource(R.plurals.academics_show_past, loaded.pastExams.size, loaded.pastExams.size)
                        },
                        style = AxiomTheme.typography.labelLarge,
                        color = colors.accent,
                        modifier = Modifier
                            .padding(top = AxiomSpacing.xs)
                            .clip(GlassShapes.Capsule)
                            .clickable(role = Role.Button) { showPastExams = !showPastExams }
                            .padding(horizontal = AxiomSpacing.xs, vertical = AxiomSpacing.xxs),
                    )
                    if (showPastExams) {
                        loaded.pastExams.forEach { exam ->
                            ExamCountdownRow(exam = exam, today = loaded.today, onClick = { onOpenExam(exam.id) })
                        }
                    }
                }
            }
            // ===== END SECTION: ACADEMICS_EXAMS =====

            // ===== SECTION: ACADEMICS_SUBJECTS =====
            GlassCard(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xxs)) {
                Text(
                    text = stringResource(R.string.academics_subjects_title),
                    style = AxiomTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .padding(bottom = AxiomSpacing.xs)
                        .semantics { heading() },
                )
                if (loaded.subjects.isEmpty()) {
                    Text(text = stringResource(R.string.academics_subjects_empty), style = AxiomTheme.typography.bodyMedium, color = colors.textSecondary)
                } else {
                    loaded.subjects.forEach { subject ->
                        SubjectProgressRow(subject = subject, onClick = { onOpenSubject(subject.id) })
                    }
                }
            }
            // ===== END SECTION: ACADEMICS_SUBJECTS =====
        }
    }

    SubjectSheet(
        visible = showSubjectSheet,
        title = stringResource(R.string.academics_add_subject),
        nameState = nameState,
        colorIndex = newSubjectColor,
        onSelectColor = { newSubjectColor = it },
        error = subjectError,
        onSave = {
            scope.launch {
                when (viewModel.createSubject(nameState.text.toString(), newSubjectColor)) {
                    SubjectSaveResult.SAVED -> showSubjectSheet = false
                    SubjectSaveResult.BLANK -> subjectError = blankError
                    SubjectSaveResult.DUPLICATE -> subjectError = duplicateError
                }
            }
        },
        onDismiss = { showSubjectSheet = false },
    )
}
// ===== END SECTION: ACADEMICS_ROUTE =====
