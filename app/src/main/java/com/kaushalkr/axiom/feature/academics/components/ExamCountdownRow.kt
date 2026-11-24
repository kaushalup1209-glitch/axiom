package com.kaushalkr.axiom.feature.academics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.SubjectPalette
import com.kaushalkr.axiom.core.domain.academic.ExamCountdown
import com.kaushalkr.axiom.core.model.Exam
import com.kaushalkr.axiom.feature.home.formatTime
import com.kaushalkr.axiom.feature.home.rememberTimeFormatter
import com.kaushalkr.axiom.feature.routine.countdownText
import com.kaushalkr.axiom.feature.routine.labelRes
import com.kaushalkr.axiom.feature.tasks.rememberShortDateFormatter
import com.kyant.shapes.RoundedRectangle
import java.time.LocalDate

// ===== SECTION: EXAM_COUNTDOWN_ROW =====
/**
 * An exam or deadline with its subject, date and a countdown pill. The pill turns to the
 * danger colour in the last [URGENT_DAYS] days.
 */
@Composable
fun ExamCountdownRow(
    exam: Exam,
    today: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val dateFormatter = rememberShortDateFormatter()
    val timeFormatter = rememberTimeFormatter()
    val daysUntil = ExamCountdown.daysUntil(exam, today)
    val isPast = daysUntil < 0
    val isUrgent = daysUntil in 0..URGENT_DAYS
    val subjectColor = exam.subject?.let { SubjectPalette.color(it.colorIndex, colors.isDark) } ?: colors.accent
    val detail = listOfNotNull(
        exam.subject?.name,
        stringResource(exam.kind.labelRes()),
        listOfNotNull(dateFormatter.format(exam.date), exam.time?.let { timeFormatter.formatTime(it) }).joinToString(", "),
    ).joinToString(" · ")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RowShape)
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = AxiomDimens.minTouchTarget)
            .padding(vertical = AxiomSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RowShape)
                .background(subjectColor.copy(alpha = BADGE_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(R.drawable.ic_exam), contentDescription = null, tint = subjectColor, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exam.title,
                style = AxiomTheme.typography.titleSmall,
                color = if (isPast) colors.textSecondary else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = detail, style = AxiomTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        val pillColor = when {
            isPast -> colors.textTertiary
            isUrgent -> colors.danger
            else -> colors.accent
        }
        Text(
            text = countdownText(daysUntil),
            style = AxiomTheme.typography.labelMedium,
            color = pillColor,
            maxLines = 1,
            modifier = Modifier
                .clip(GlassShapes.Capsule)
                .background(pillColor.copy(alpha = BADGE_ALPHA))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

private val RowShape = RoundedRectangle(12.dp)
private const val URGENT_DAYS = 3L
private const val BADGE_ALPHA = 0.14f
// ===== END SECTION: EXAM_COUNTDOWN_ROW =====
