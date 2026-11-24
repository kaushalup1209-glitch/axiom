package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kyant.shapes.Capsule

// ===== SECTION: AXIOM_PILL =====
/** Compact accent label, e.g. a phase or status tag. */
@Composable
fun AxiomPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    Text(
        text = text,
        style = AxiomTheme.typography.labelSmall,
        color = colors.accent,
        modifier = modifier
            .clip(Capsule())
            .background(colors.accentSoft)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}
// ===== END SECTION: AXIOM_PILL =====
