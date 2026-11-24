package com.kaushalkr.axiom.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kyant.shapes.Capsule

// ===== SECTION: GLASS_ICON_BUTTON =====
/** Circular Liquid Glass button holding a single icon. Size equals the minimum touch target. */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    @DrawableRes iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    floating: Boolean = true,
    iconTint: Color = Color.Unspecified,
) {
    GlassButton(
        onClick = onClick,
        modifier = modifier.size(AxiomDimens.iconButtonSize),
        shape = Capsule(),
        minHeight = AxiomDimens.iconButtonSize,
        contentPadding = PaddingValues(0.dp),
        floating = floating,
        contentColor = iconTint,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp),
        )
    }
}
// ===== END SECTION: GLASS_ICON_BUTTON =====
