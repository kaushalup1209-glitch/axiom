package com.kaushalkr.axiom.feature.planner

import androidx.annotation.StringRes
import com.kaushalkr.axiom.R

// ===== SECTION: PLANNER_SECTION =====
/** The Planner tab's sections, in switcher order. */
enum class PlannerSection(@param:StringRes val labelRes: Int) {
    TASKS(R.string.tasks_title),
    ROUTINE(R.string.planner_section_routine),
    ACADEMICS(R.string.planner_section_academics),
}
// ===== END SECTION: PLANNER_SECTION =====
