package com.kaushalkr.axiom.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.tabularNumbers
import com.kaushalkr.axiom.core.domain.StudyDay
import com.kaushalkr.axiom.core.domain.StudyStreak
import com.kaushalkr.axiom.core.model.StudyDefaults
import com.kaushalkr.axiom.feature.home.currentUiLocale
import com.kaushalkr.axiom.feature.home.formatStudyMinutes
import java.time.format.TextStyle

// ===== SECTION: HOME_STREAK_CARD =====
/** Current consistency streak and the last seven days. */
@Composable
fun StreakCard(
    streak: StudyStreak,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val daysText = pluralStringResource(R.plurals.home_streak_days, streak.currentDays, streak.currentDays)
    val caption = if (streak.todayMet) {
        stringResource(R.string.home_streak_caption_met)
    } else {
        stringResource(R.string.home_streak_caption_pending, formatStudyMinutes(StudyDefaults.STREAK_MINIMUM_MINUTES))
    }
    GlassCard(modifier = modifier) {
        HomeCardHeader(
            iconRes = R.drawable.ic_streak,
            title = stringResource(R.string.home_streak_title),
            subtitle = caption,
            trailing = {
                Text(text = daysText, style = AxiomTheme.typography.titleLarge.tabularNumbers(), color = colors.accent)
            },
        )
        Spacer(Modifier.height(AxiomSpacing.lg))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            streak.lastSevenDays.forEach { day -> StreakDay(day) }
        }
    }
}
// ===== END SECTION: HOME_STREAK_CARD =====

// ===== SECTION: HOME_STREAK_DAY =====
@Composable
private fun StreakDay(day: StudyDay) {
    val colors = AxiomTheme.colors
    val locale = currentUiLocale()
    val letter = day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale)
    val fullName = day.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val description = stringResource(if (day.met) R.string.home_streak_day_met else R.string.home_streak_day_missed, fullName)
    Column(
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs),
    ) {
        Text(
            text = letter,
            style = AxiomTheme.typography.labelMedium,
            color = if (day.isToday) colors.accent else colors.textTertiary,
        )
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (day.met) colors.accent else colors.controlTrack)
                .then(if (day.isToday && !day.met) Modifier.border(2.dp, colors.accent, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (day.met) {
                Icon(
                    painter = painterResource(R.drawable.ic_streak),
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
// ===== END SECTION: HOME_STREAK_DAY =====
