package com.kaushalkr.axiom.feature.tasks.editor.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: EDITOR_FIELD_LABEL =====
/** Small label above a group of options inside an editor card. */
@Composable
fun EditorFieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = AxiomTheme.typography.labelLarge,
        color = AxiomTheme.colors.textSecondary,
        modifier = modifier,
    )
}
// ===== END SECTION: EDITOR_FIELD_LABEL =====
