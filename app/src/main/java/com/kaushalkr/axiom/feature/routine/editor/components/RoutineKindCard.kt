package com.kaushalkr.axiom.feature.routine.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomChip
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.RoutinePalette
import com.kaushalkr.axiom.core.designsystem.theme.SubjectPalette
import com.kaushalkr.axiom.core.model.RoutineKind
import com.kaushalkr.axiom.core.model.SubjectRef
import com.kaushalkr.axiom.feature.routine.iconRes
import com.kaushalkr.axiom.feature.routine.labelRes
import com.kaushalkr.axiom.feature.tasks.editor.components.EditorFieldLabel
import com.kaushalkr.axiom.feature.tasks.editor.components.EditorSectionCard

// ===== SECTION: ROUTINE_KIND_CARD =====
/** Block type (study, break, sleep, personal) and, for study blocks, the subject. */
@Composable
fun RoutineKindCard(
    kind: RoutineKind,
    subjectId: Long?,
    subjects: List<SubjectRef>,
    onSelectKind: (RoutineKind) -> Unit,
    onSelectSubject: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    EditorSectionCard(iconRes = R.drawable.ic_routine, title = stringResource(R.string.routine_editor_type), modifier = modifier) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
            RoutineKind.entries.forEach { option ->
                AxiomChip(
                    label = stringResource(option.labelRes()),
                    selected = option == kind,
                    onClick = { onSelectKind(option) },
                    iconRes = option.iconRes(),
                    dotColor = if (option == kind) null else RoutinePalette.color(option, colors),
                    role = Role.RadioButton,
                )
            }
        }
        if (kind == RoutineKind.STUDY) {
            EditorFieldLabel(stringResource(R.string.filter_subject))
            if (subjects.isEmpty()) {
                EditorFieldLabel(stringResource(R.string.routine_editor_no_subjects))
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                    AxiomChip(
                        label = stringResource(R.string.editor_estimate_none),
                        selected = subjectId == null,
                        onClick = { onSelectSubject(null) },
                        role = Role.RadioButton,
                    )
                    subjects.forEach { subject ->
                        AxiomChip(
                            label = subject.name,
                            selected = subject.id == subjectId,
                            onClick = { onSelectSubject(subject.id) },
                            dotColor = SubjectPalette.color(subject.colorIndex, colors.isDark),
                            role = Role.RadioButton,
                        )
                    }
                }
            }
        }
    }
}
// ===== END SECTION: ROUTINE_KIND_CARD =====
