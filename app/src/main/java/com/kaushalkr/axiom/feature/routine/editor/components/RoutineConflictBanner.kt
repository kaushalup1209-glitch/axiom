package com.kaushalkr.axiom.feature.routine.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.domain.routine.RoutineConflict
import com.kaushalkr.axiom.feature.routine.weekdayShortName

// ===== SECTION: ROUTINE_CONFLICT_BANNER =====
/** Live warning while editing: which blocks this one overlaps, with a way to fix it. */
@Composable
fun RoutineConflictBanner(
    conflicts: List<RoutineConflict>,
    onFix: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (conflicts.isEmpty()) return
    val colors = AxiomTheme.colors
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = GlassShapes.Tile,
        spec = GlassTokens.ListItem,
        contentPadding = PaddingValues(horizontal = AxiomSpacing.md, vertical = AxiomSpacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
            Icon(painter = painterResource(R.drawable.ic_warning), contentDescription = null, tint = colors.danger, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pluralStringResource(R.plurals.routine_conflict_title, conflicts.size, conflicts.size),
                    style = AxiomTheme.typography.titleSmall,
                    color = colors.danger,
                )
                Text(
                    text = conflictList(conflicts),
                    style = AxiomTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = stringResource(R.string.routine_conflict_fix),
                style = AxiomTheme.typography.labelLarge,
                color = colors.accent,
                modifier = Modifier
                    .clip(GlassShapes.Capsule)
                    .clickable(role = Role.Button, onClick = onFix)
                    .padding(horizontal = AxiomSpacing.sm, vertical = AxiomSpacing.xs),
            )
        }
    }
}

/** "Lunch (Mon), Gym (Wed)". */
@Composable
fun conflictList(conflicts: List<RoutineConflict>): String =
    conflicts.take(MAX_NAMED).map { conflict ->
        stringResource(R.string.routine_conflict_item, conflict.block.title, weekdayShortName(conflict.date.dayOfWeek))
    }.joinToString(", ")

private const val MAX_NAMED = 3
// ===== END SECTION: ROUTINE_CONFLICT_BANNER =====
