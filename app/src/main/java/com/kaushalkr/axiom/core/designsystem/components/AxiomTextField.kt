package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLengthTrim
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kyant.shapes.RoundedRectangle

// ===== SECTION: AXIOM_TEXT_FIELD =====
/**
 * Single-line inset text field for use inside glass cards. Deliberately not glass itself, to
 * avoid stacking another glass layer on the card. Observe [state] for changes.
 */
@Composable
fun AxiomTextField(
    state: TextFieldState,
    placeholder: String,
    label: String,
    modifier: Modifier = Modifier,
    maxLength: Int = Int.MAX_VALUE,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words,
) {
    val colors = AxiomTheme.colors
    val focusManager = LocalFocusManager.current
    BasicTextField(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = label },
        inputTransformation = InputTransformation.maxLengthTrim(maxLength),
        textStyle = AxiomTheme.typography.bodyLarge.copy(color = colors.textPrimary),
        keyboardOptions = KeyboardOptions(capitalization = capitalization, imeAction = ImeAction.Done),
        onKeyboardAction = { focusManager.clearFocus() },
        lineLimits = TextFieldLineLimits.SingleLine,
        cursorBrush = SolidColor(colors.accent),
        decorator = TextFieldDecorator { innerTextField ->
            Box(
                modifier = Modifier
                    .clip(RoundedRectangle(14.dp))
                    .background(colors.controlTrack)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (state.text.isEmpty()) {
                    Text(text = placeholder, style = AxiomTheme.typography.bodyLarge, color = colors.textTertiary)
                }
                innerTextField()
            }
        },
    )
}
// ===== END SECTION: AXIOM_TEXT_FIELD =====
