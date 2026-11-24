package com.kaushalkr.axiom.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import com.kaushalkr.axiom.core.model.AcademicLimits
import com.kaushalkr.axiom.core.model.RoutineKind

// ===== SECTION: PLANNER_SUBJECT_PALETTE =====
/**
 * Subject colours, tuned per theme for contrast on glass. Subjects store an index, so a
 * theme change recolours them consistently.
 */
object SubjectPalette {
    private val light = listOf(
        Color(0xFF4F46E5),
        Color(0xFF0891B2),
        Color(0xFF16A34A),
        Color(0xFFD97706),
        Color(0xFFDB2777),
        Color(0xFF7C3AED),
        Color(0xFFDC2626),
        Color(0xFF475569),
    )
    private val dark = listOf(
        Color(0xFF908EFF),
        Color(0xFF38BDF8),
        Color(0xFF4ADE80),
        Color(0xFFFBBF24),
        Color(0xFFF472B6),
        Color(0xFFC4B5FD),
        Color(0xFFF87171),
        Color(0xFFA3B1C6),
    )

    val size: Int get() = AcademicLimits.SUBJECT_COLOR_COUNT

    fun color(index: Int, isDark: Boolean): Color = (if (isDark) dark else light)[index.mod(size)]
}
// ===== END SECTION: PLANNER_SUBJECT_PALETTE =====

// ===== SECTION: PLANNER_ROUTINE_PALETTE =====
/** Colours of routine block kinds. Study blocks with a subject use the subject colour instead. */
object RoutinePalette {
    fun color(kind: RoutineKind, colors: AxiomColors): Color = when (kind) {
        RoutineKind.STUDY -> colors.accent
        RoutineKind.BREAK -> colors.success
        RoutineKind.SLEEP -> if (colors.isDark) Color(0xFF8FA3FF) else Color(0xFF5468C9)
        RoutineKind.PERSONAL -> if (colors.isDark) Color(0xFFFFB86B) else Color(0xFFD9822B)
    }
}
// ===== END SECTION: PLANNER_ROUTINE_PALETTE =====
