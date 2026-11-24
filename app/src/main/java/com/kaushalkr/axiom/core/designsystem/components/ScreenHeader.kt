package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: SCREEN_HEADER =====
/** Large screen title with an accent eyebrow and a supporting line. */
@Composable
fun ScreenHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = eyebrow.uppercase(),
            style = AxiomTheme.typography.labelMedium,
            color = colors.accent,
        )
        Text(
            text = title,
            style = AxiomTheme.typography.displaySmall,
            color = colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = subtitle,
            style = AxiomTheme.typography.bodyLarge,
            color = colors.textSecondary,
        )
    }
}
// ===== END SECTION: SCREEN_HEADER =====
