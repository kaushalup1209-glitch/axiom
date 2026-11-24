package com.kaushalkr.axiom.feature.tasks.editor.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.kaushalkr.axiom.core.designsystem.components.AxiomChip
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing

// ===== SECTION: SUBJECT_SUGGESTIONS =====
/** Subjects used on earlier tasks that match what is typed; tap one to fill the field. */
@Composable
fun SubjectSuggestions(
    subjects: List<String>,
    subjectState: TextFieldState,
    modifier: Modifier = Modifier,
) {
    val latestSubjects by rememberUpdatedState(subjects)
    val matches by remember(subjectState) {
        derivedStateOf {
            val typed = subjectState.text.toString().trim()
            latestSubjects
                .filter { !it.equals(typed, ignoreCase = true) && it.contains(typed, ignoreCase = true) }
                .take(MAX_SUGGESTIONS)
        }
    }
    if (matches.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs),
    ) {
        matches.forEach { subject ->
            AxiomChip(
                label = subject,
                selected = false,
                onClick = { subjectState.setTextAndPlaceCursorAtEnd(subject) },
                role = Role.Button,
            )
        }
    }
}

private const val MAX_SUGGESTIONS = 8
// ===== END SECTION: SUBJECT_SUGGESTIONS =====
