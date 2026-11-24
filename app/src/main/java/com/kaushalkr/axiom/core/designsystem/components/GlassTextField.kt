package com.kaushalkr.axiom.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLengthTrim
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassBackdrop
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassStyle
import com.kaushalkr.axiom.core.designsystem.glass.glassSurface
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: GLASS_TEXT_FIELD =====
/**
 * Text input on its own Liquid Glass surface ([GlassTokens.Input]). Place it directly on a
 * screen; inside a glass card use [AxiomTextField] instead to avoid stacking glass layers.
 *
 * @param minLines more than 1 makes the field multi-line.
 */
@Composable
fun GlassTextField(
    state: TextFieldState,
    placeholder: String,
    label: String,
    modifier: Modifier = Modifier,
    @DrawableRes leadingIconRes: Int? = null,
    trailing: (@Composable () -> Unit)? = null,
    minLines: Int = 1,
    maxLength: Int = Int.MAX_VALUE,
    textStyle: TextStyle = AxiomTheme.typography.bodyLarge,
    shape: Shape = GlassShapes.Input,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    imeAction: ImeAction = if (minLines > 1) ImeAction.Default else ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
) {
    val colors = AxiomTheme.colors
    val style = LocalGlassStyle.current
    val backdrop = LocalGlassBackdrop.current
    val focusManager = LocalFocusManager.current
    val surface = remember(backdrop, style, shape) {
        Modifier.glassSurface(backdrop = backdrop, style = style, spec = GlassTokens.Input, shape = shape)
    }
    val isMultiLine = minLines > 1

    BasicTextField(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = label },
        inputTransformation = InputTransformation.maxLengthTrim(maxLength),
        textStyle = textStyle.copy(color = colors.textPrimary),
        keyboardOptions = KeyboardOptions(capitalization = capitalization, imeAction = imeAction),
        onKeyboardAction = { onImeAction?.invoke() ?: focusManager.clearFocus() },
        lineLimits = if (isMultiLine) {
            TextFieldLineLimits.MultiLine(minHeightInLines = minLines, maxHeightInLines = MAX_VISIBLE_LINES)
        } else {
            TextFieldLineLimits.SingleLine
        },
        cursorBrush = SolidColor(colors.accent),
        decorator = TextFieldDecorator { innerTextField ->
            Row(
                modifier = Modifier
                    .then(surface)
                    .defaultMinSize(minHeight = AxiomDimens.minTouchTarget + 4.dp)
                    .padding(horizontal = AxiomSpacing.md, vertical = AxiomSpacing.sm),
                verticalAlignment = if (isMultiLine) Alignment.Top else Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
            ) {
                if (leadingIconRes != null) {
                    Icon(
                        painter = painterResource(leadingIconRes),
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Box(modifier = Modifier.weight(1f).padding(vertical = AxiomSpacing.xxs)) {
                    if (state.text.isEmpty()) {
                        Text(text = placeholder, style = textStyle, color = colors.textTertiary)
                    }
                    innerTextField()
                }
                trailing?.invoke()
            }
        },
    )
}

private const val MAX_VISIBLE_LINES = 8
// ===== END SECTION: GLASS_TEXT_FIELD =====
