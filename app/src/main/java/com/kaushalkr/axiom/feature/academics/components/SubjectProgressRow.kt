package com.kaushalkr.axiom.feature.academics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomLinearProgress
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.SubjectPalette
import com.kaushalkr.axiom.core.model.Subject
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt

// ===== SECTION: SUBJECT_PROGRESS_ROW =====
/** A subject with its syllabus progress. Tap to open its chapters. */
@Composable
fun SubjectProgressRow(
    subject: Subject,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val subjectColor = SubjectPalette.color(subject.colorIndex, colors.isDark)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RowShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = AxiomSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RowShape)
                .background(subjectColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = subject.name.take(1).uppercase(),
                style = AxiomTheme.typography.titleMedium,
                color = subjectColor,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = subject.name,
                    style = AxiomTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (subject.chaptersTotal > 0) {
                    Text(
                        text = stringResource(R.string.home_goal_percent, (subject.progress * 100).roundToInt()),
                        style = AxiomTheme.typography.labelLarge,
                        color = subjectColor,
                    )
                }
            }
            Text(
                text = if (subject.chaptersTotal == 0) {
                    stringResource(R.string.subject_no_chapters)
                } else {
                    pluralStringResource(R.plurals.subject_chapters_progress, subject.chaptersTotal, subject.chaptersDone, subject.chaptersTotal)
                },
                style = AxiomTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            if (subject.chaptersTotal > 0) AxiomLinearProgress(progress = subject.progress, height = 6.dp)
        }
        Icon(painter = painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = colors.textTertiary, modifier = Modifier.size(20.dp))
    }
}

private val RowShape = RoundedRectangle(12.dp)
// ===== END SECTION: SUBJECT_PROGRESS_ROW =====
