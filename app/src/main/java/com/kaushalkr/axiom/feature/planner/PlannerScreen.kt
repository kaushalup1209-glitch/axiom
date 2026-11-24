package com.kaushalkr.axiom.feature.planner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.components.GlassSegmentedControl
import com.kaushalkr.axiom.core.designsystem.layout.LocalScreenContentPadding
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.feature.academics.AcademicsRoute
import com.kaushalkr.axiom.feature.routine.RoutineRoute
import com.kaushalkr.axiom.feature.tasks.TaskListRoute
import java.time.LocalDate

// ===== SECTION: PLANNER_SCREEN =====
/**
 * Planner tab: a fixed section switcher (Tasks, Routine, Academics) above the chosen section.
 * Sections keep their own view models, so switching back restores their state.
 *
 * @param requestedSection a section another screen asked to open (e.g. Home's routine card);
 *   applied once, then [onSectionRequestHandled] clears it.
 */
@Composable
fun PlannerScreen(
    onOpenTask: (taskId: Long) -> Unit,
    onNewTask: (dueToday: Boolean) -> Unit,
    onOpenBlock: (blockId: Long) -> Unit,
    onNewBlock: (date: LocalDate, startMinute: Int?) -> Unit,
    onOpenSubject: (subjectId: Long) -> Unit,
    onOpenExam: (examId: Long) -> Unit,
    onNewExam: () -> Unit,
    modifier: Modifier = Modifier,
    requestedSection: PlannerSection? = null,
    onSectionRequestHandled: () -> Unit = {},
) {
    val motion = AxiomTheme.motion
    // Start on a requested section so a first visit from Home does not flash the Tasks section.
    var section by rememberSaveable { mutableStateOf(requestedSection ?: PlannerSection.TASKS) }
    LaunchedEffect(requestedSection) {
        if (requestedSection != null) {
            section = requestedSection
            onSectionRequestHandled()
        }
    }
    val chromePadding = LocalScreenContentPadding.current
    val layoutDirection = LocalLayoutDirection.current
    // Sections sit below the switcher, so they only need the side and bottom chrome padding.
    val sectionPadding = PaddingValues(
        start = chromePadding.calculateStartPadding(layoutDirection),
        end = chromePadding.calculateEndPadding(layoutDirection),
        top = 0.dp,
        bottom = chromePadding.calculateBottomPadding(),
    )

    Column(modifier = modifier.fillMaxSize()) {
        // ===== SECTION: PLANNER_SECTION_SWITCHER =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = chromePadding.calculateStartPadding(layoutDirection),
                    end = chromePadding.calculateEndPadding(layoutDirection),
                    top = chromePadding.calculateTopPadding(),
                ),
            contentAlignment = Alignment.TopCenter,
        ) {
            GlassSegmentedControl(
                options = PlannerSection.entries.map { stringResource(it.labelRes) },
                selectedIndex = section.ordinal,
                onSelect = { index -> section = PlannerSection.entries[index] },
                modifier = Modifier
                    .widthIn(max = AxiomDimens.contentMaxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = AxiomDimens.screenHorizontalPadding),
            )
        }
        Spacer(Modifier.height(AxiomSpacing.sm))
        // ===== END SECTION: PLANNER_SECTION_SWITCHER =====

        // ===== SECTION: PLANNER_SECTION_CONTENT =====
        CompositionLocalProvider(LocalScreenContentPadding provides sectionPadding) {
            AnimatedContent(
                targetState = section,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                transitionSpec = {
                    fadeIn(motion.tween(AxiomMotionTokens.ContentSwapIn)) togetherWith fadeOut(motion.tween(AxiomMotionTokens.ContentSwapOut))
                },
                label = "PlannerSection",
            ) { shown ->
                when (shown) {
                    PlannerSection.TASKS -> TaskListRoute(onOpenTask = onOpenTask, onNewTask = onNewTask)
                    PlannerSection.ROUTINE -> RoutineRoute(onOpenBlock = onOpenBlock, onNewBlock = onNewBlock, onOpenExam = onOpenExam)
                    PlannerSection.ACADEMICS -> AcademicsRoute(onOpenSubject = onOpenSubject, onOpenExam = onOpenExam, onNewExam = onNewExam)
                }
            }
        }
        // ===== END SECTION: PLANNER_SECTION_CONTENT =====
    }
}
// ===== END SECTION: PLANNER_SCREEN =====
