package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kaushalkr.axiom.core.designsystem.layout.LocalScreenContentPadding
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing

// ===== SECTION: AXIOM_SCREEN_COLUMN =====
/**
 * Standard scrollable screen body. Applies the chrome padding from the app root, phone
 * margins, and a centred maximum width on tablets. Content scrolls under the floating
 * glass chrome, which refracts it.
 */
@Composable
fun AxiomScreenColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(AxiomSpacing.lg),
    content: @Composable ColumnScope.() -> Unit,
) {
    val chromePadding = LocalScreenContentPadding.current
    Box(
        modifier = modifier
            .fillMaxSize()
            // Shrinks the viewport above the keyboard so focused fields scroll into view.
            .imePadding()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = AxiomDimens.contentMaxWidth)
                .fillMaxWidth()
                .padding(chromePadding)
                .padding(horizontal = AxiomDimens.screenHorizontalPadding),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}
// ===== END SECTION: AXIOM_SCREEN_COLUMN =====
