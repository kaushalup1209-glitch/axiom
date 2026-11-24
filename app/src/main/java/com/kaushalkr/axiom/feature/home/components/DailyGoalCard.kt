package com.kaushalkr.axiom.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomPill
import com.kaushalkr.axiom.core.designsystem.components.AxiomProgressRing
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.tabularNumbers
import com.kaushalkr.axiom.feature.home.formatStudyMinutes
import kotlin.math.roundToInt

// ===== SECTION: HOME_DAILY_GOAL_CARD =====
/** Today's focused time against the daily goal, with a circular progress ring. */
@Composable
fun DailyGoalCard(
    studiedMinutes: Int,
    goalMinutes: Int,
    progress: Float,
    isGoalReached: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.lg),
        ) {
            AxiomProgressRing(
                progress = progress,
                modifier = Modifier.size(116.dp),
                strokeWidth = 11.dp,
            ) {
                Text(
                    text = stringResource(R.string.home_goal_percent, (progress * 100).roundToInt()),
                    style = AxiomTheme.typography.titleLarge.tabularNumbers(),
                    color = colors.textPrimary,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.home_goal_title),
                    style = AxiomTheme.typography.titleSmall,
                    color = colors.textSecondary,
                )
                Text(
                    text = formatStudyMinutes(studiedMinutes),
                    style = AxiomTheme.typography.headlineSmall.tabularNumbers(),
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.home_goal_of, formatStudyMinutes(goalMinutes)),
                    style = AxiomTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(AxiomSpacing.xs))
                AxiomPill(
                    text = if (isGoalReached) {
                        stringResource(R.string.home_goal_reached)
                    } else {
                        stringResource(R.string.home_goal_remaining, formatStudyMinutes(goalMinutes - studiedMinutes))
                    },
                )
            }
        }
    }
}
// ===== END SECTION: HOME_DAILY_GOAL_CARD =====
