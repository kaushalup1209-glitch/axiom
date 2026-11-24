package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: GLASS_SEARCH_FIELD =====
/** Capsule glass search input with a clear button. */
@Composable
fun GlassSearchField(
    state: TextFieldState,
    placeholder: String,
    clearLabel: String,
    modifier: Modifier = Modifier,
    maxLength: Int = Int.MAX_VALUE,
) {
    val colors = AxiomTheme.colors
    GlassTextField(
        state = state,
        placeholder = placeholder,
        label = placeholder,
        modifier = modifier,
        maxLength = maxLength,
        leadingIconRes = R.drawable.ic_search,
        shape = GlassShapes.Capsule,
        imeAction = ImeAction.Search,
        trailing = if (state.text.isEmpty()) {
            null
        } else {
            {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(GlassShapes.Capsule)
                        .clickable(role = Role.Button, onClickLabel = clearLabel) { state.clearText() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = clearLabel,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
    )
}
// ===== END SECTION: GLASS_SEARCH_FIELD =====
