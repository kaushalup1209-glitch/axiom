package com.kaushalkr.axiom.feature.academics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomTextField
import com.kaushalkr.axiom.core.designsystem.components.GlassBottomSheet
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.SubjectPalette
import com.kaushalkr.axiom.core.model.AcademicLimits

// ===== SECTION: SUBJECT_SHEET =====
/**
 * Creates or edits a subject: name and colour. [onDelete] adds a destructive button (edit only).
 *
 * @param error message under the name, e.g. when the name is taken.
 */
@Composable
fun SubjectSheet(
    visible: Boolean,
    title: String,
    nameState: TextFieldState,
    colorIndex: Int,
    onSelectColor: (Int) -> Unit,
    error: String?,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val colors = AxiomTheme.colors
    GlassBottomSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        title = title,
        closeLabel = stringResource(R.string.sheet_close),
    ) {
        AxiomTextField(
            state = nameState,
            placeholder = stringResource(R.string.subject_name_placeholder),
            label = stringResource(R.string.subject_name_label),
            maxLength = AcademicLimits.SUBJECT_NAME_MAX_LENGTH,
            capitalization = KeyboardCapitalization.Words,
            onImeAction = onSave,
        )
        if (error != null) {
            Text(text = error, style = AxiomTheme.typography.bodySmall, color = colors.danger)
        }
        Text(text = stringResource(R.string.subject_color), style = AxiomTheme.typography.labelLarge, color = colors.textSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
            (0 until SubjectPalette.size).forEach { index ->
                ColorSwatch(
                    color = SubjectPalette.color(index, colors.isDark),
                    selected = index == colorIndex,
                    description = stringResource(R.string.subject_color_option, index + 1),
                    onClick = { onSelectColor(index) },
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
            if (onDelete != null) {
                GlassButton(
                    onClick = {
                        onDismiss()
                        onDelete()
                    },
                    modifier = Modifier.weight(1f),
                    tint = colors.danger,
                    contentColor = Color.White,
                ) {
                    Text(stringResource(R.string.subject_delete))
                }
            }
            GlassButton(onClick = onSave, modifier = Modifier.weight(1f), tint = colors.accent) {
                Text(stringResource(R.string.editor_save))
            }
        }
    }
}
// ===== END SECTION: SUBJECT_SHEET =====

// ===== SECTION: SUBJECT_COLOR_SWATCH =====
@Composable
private fun ColorSwatch(color: Color, selected: Boolean, description: String, onClick: () -> Unit) {
    val colors = AxiomTheme.colors
    Box(
        modifier = Modifier
            .size(AxiomDimens.minTouchTarget)
            .clip(CircleShape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color)
                .then(if (selected) Modifier.border(2.dp, colors.textPrimary, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
// ===== END SECTION: SUBJECT_COLOR_SWATCH =====
