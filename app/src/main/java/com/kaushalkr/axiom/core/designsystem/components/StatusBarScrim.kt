package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: STATUS_BAR_SCRIM =====
/** Soft fade under the status bar so system icons stay legible over scrolled content. */
@Composable
fun StatusBarScrim(
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val background = AxiomTheme.colors.background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    0f to background.copy(alpha = 0.88f),
                    0.6f to background.copy(alpha = 0.55f),
                    1f to background.copy(alpha = 0f),
                ),
            ),
    )
}
// ===== END SECTION: STATUS_BAR_SCRIM =====
