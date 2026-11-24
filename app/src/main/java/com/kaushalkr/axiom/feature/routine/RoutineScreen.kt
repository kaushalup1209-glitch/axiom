package com.kaushalkr.axiom.feature.routine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaushalkr.axiom.core.designsystem.layout.LocalScreenContentPadding
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.feature.routine.components.DayAgendaHeader
import com.kaushalkr.axiom.feature.routine.components.DayTimeline
import com.kaushalkr.axiom.feature.routine.components.MonthOverview
import com.kaushalkr.axiom.feature.routine.components.RoutineHeader
import com.kaushalkr.axiom.feature.routine.components.WeekOverview
import java.time.LocalDate
import kotlin.math.min

// ===== SECTION: ROUTINE_ROUTE =====
/**
 * Stateful entry point for the timetable.
 *
 * @param onNewBlock opens the block editor for [LocalDate] at a start minute (or null for "next
 *   half hour").
 */
@Composable
fun RoutineRoute(
    onOpenBlock: (blockId: Long) -> Unit,
    onNewBlock: (date: LocalDate, startMinute: Int?) -> Unit,
    onOpenExam: (examId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoutineViewModel = viewModel(factory = RoutineViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val chromePadding = LocalScreenContentPadding.current
    val layoutDirection = LocalLayoutDirection.current
    val listPadding = PaddingValues(
        start = AxiomDimens.screenHorizontalPadding,
        end = AxiomDimens.screenHorizontalPadding,
        top = AxiomSpacing.md,
        bottom = chromePadding.calculateBottomPadding(),
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = AxiomDimens.contentMaxWidth)
                .fillMaxSize()
                .padding(
                    start = chromePadding.calculateStartPadding(layoutDirection),
                    end = chromePadding.calculateEndPadding(layoutDirection),
                    top = chromePadding.calculateTopPadding(),
                ),
        ) {
            RoutineHeader(
                state = state,
                onSelectView = viewModel::selectView,
                onStep = viewModel::step,
                onToday = viewModel::goToToday,
                onNewBlock = { onNewBlock(state?.selectedDate ?: LocalDate.now(), null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AxiomDimens.screenHorizontalPadding),
            )
            val loaded = state ?: return@Column
            // ===== SECTION: ROUTINE_ROUTE_VIEWS =====
            val contentModifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            when (loaded.view) {
                RoutineView.DAY -> DayTimeline(
                    day = loaded.day,
                    nowMinute = loaded.nowMinute.takeIf { loaded.selectedDate == loaded.today },
                    onOpenBlock = onOpenBlock,
                    onMoveBlock = viewModel::moveBlock,
                    onDeleteBlock = viewModel::deleteBlock,
                    onCreateAt = { minute -> onNewBlock(loaded.selectedDate, minute) },
                    contentPadding = listPadding,
                    modifier = contentModifier,
                    header = {
                        DayAgendaHeader(
                            day = loaded.day,
                            today = loaded.today,
                            hasAnyBlocks = loaded.hasAnyBlocks,
                            onOpenExam = onOpenExam,
                        )
                    },
                )
                RoutineView.WEEK -> WeekOverview(
                    week = loaded.week,
                    today = loaded.today,
                    onOpenDay = { date -> viewModel.selectDate(date, showDay = true) },
                    onOpenBlock = onOpenBlock,
                    contentPadding = listPadding,
                    modifier = contentModifier,
                )
                RoutineView.MONTH -> MonthOverview(
                    month = loaded.month,
                    selectedDate = loaded.selectedDate,
                    today = loaded.today,
                    onMonthChange = { month ->
                        viewModel.selectDate(month.atDay(min(loaded.selectedDate.dayOfMonth, month.lengthOfMonth())))
                    },
                    onOpenDay = { date -> viewModel.selectDate(date, showDay = true) },
                    onOpenExam = onOpenExam,
                    contentPadding = listPadding,
                    modifier = contentModifier,
                )
            }
            // ===== END SECTION: ROUTINE_ROUTE_VIEWS =====
        }
    }
}
// ===== END SECTION: ROUTINE_ROUTE =====
