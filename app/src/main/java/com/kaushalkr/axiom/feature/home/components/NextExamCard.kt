package com.kaushalkr.axiom.feature.home.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.model.Exam
import com.kaushalkr.axiom.feature.academics.components.ExamCountdownRow
import java.time.LocalDate

// ===== SECTION: HOME_NEXT_EXAM_CARD =====
/** Real data: the nearest exam or deadline with its countdown. Home shows it only when one exists. */
@Composable
fun NextExamCard(
    exam: Exam,
    today: LocalDate,
    onOpenExam: (examId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier) {
        HomeCardHeader(
            iconRes = R.drawable.ic_exam,
            title = stringResource(R.string.home_exam_title),
        )
        Spacer(Modifier.height(AxiomSpacing.sm))
        ExamCountdownRow(exam = exam, today = today, onClick = { onOpenExam(exam.id) })
    }
}
// ===== END SECTION: HOME_NEXT_EXAM_CARD =====
