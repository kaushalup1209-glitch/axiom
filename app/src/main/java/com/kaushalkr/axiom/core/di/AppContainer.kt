package com.kaushalkr.axiom.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.kaushalkr.axiom.core.data.academic.ExamRepository
import com.kaushalkr.axiom.core.data.academic.SubjectRepository
import com.kaushalkr.axiom.core.data.appearance.AppearanceRepository
import com.kaushalkr.axiom.core.data.database.AxiomDatabase
import com.kaushalkr.axiom.core.data.focus.FocusSessionRepository
import com.kaushalkr.axiom.core.data.routine.RoutineRepository
import com.kaushalkr.axiom.core.domain.academic.AcademicActions
import com.kaushalkr.axiom.core.domain.routine.RoutineActions
import com.kaushalkr.axiom.core.domain.routine.RoutineCommandExecutor
import com.kaushalkr.axiom.core.data.study.StudyPreferencesRepository
import com.kaushalkr.axiom.core.data.task.TaskRepository
import com.kaushalkr.axiom.core.domain.UndoController
import com.kaushalkr.axiom.core.domain.task.TaskActions
import com.kaushalkr.axiom.core.domain.task.TaskCommandExecutor
import com.kaushalkr.axiom.core.platform.GlassDeviceCapability
import com.kaushalkr.axiom.core.platform.PowerSaveModeMonitor
import com.kaushalkr.axiom.core.platform.SystemTimeProvider
import com.kaushalkr.axiom.core.platform.TimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// ===== SECTION: APP_CONTAINER_DATASTORE =====
private val Context.appearanceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppearanceRepository.DATASTORE_NAME,
)
private val Context.studyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = StudyPreferencesRepository.DATASTORE_NAME,
)
private val Context.focusStateDataStore: DataStore<Preferences> by preferencesDataStore(
    name = FocusSessionRepository.STATE_DATASTORE_NAME,
)
// ===== END SECTION: APP_CONTAINER_DATASTORE =====

// ===== SECTION: APP_CONTAINER =====
/**
 * Manual dependency container created once by [com.kaushalkr.axiom.AxiomApplication].
 * Everything is lazy, so nothing is opened until a screen needs it.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    /** App-lifetime scope for writes that must finish even if the screen that started them closes. */
    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val timeProvider: TimeProvider by lazy { SystemTimeProvider() }

    val appearanceRepository: AppearanceRepository by lazy {
        AppearanceRepository(appContext.appearanceDataStore)
    }

    val studyPreferencesRepository: StudyPreferencesRepository by lazy {
        StudyPreferencesRepository(appContext.studyDataStore)
    }

    private val database: AxiomDatabase by lazy {
        Room.databaseBuilder(appContext, AxiomDatabase::class.java, AxiomDatabase.NAME).build()
    }

    val focusSessionRepository: FocusSessionRepository by lazy {
        FocusSessionRepository(database.focusSessionDao(), appContext.focusStateDataStore, timeProvider)
    }

    val taskRepository: TaskRepository by lazy { TaskRepository(database, timeProvider) }

    val undoController: UndoController by lazy { UndoController(applicationScope) }

    val taskActions: TaskActions by lazy { TaskActions(taskRepository, undoController, applicationScope) }

    /** Entry point for the AI assistant (Phase 11) to run structured task commands. */
    val taskCommandExecutor: TaskCommandExecutor by lazy { TaskCommandExecutor(taskRepository, taskActions) }

    val subjectRepository: SubjectRepository by lazy { SubjectRepository(database, timeProvider) }

    val examRepository: ExamRepository by lazy { ExamRepository(database, timeProvider) }

    val routineRepository: RoutineRepository by lazy { RoutineRepository(database, timeProvider) }

    val routineActions: RoutineActions by lazy { RoutineActions(routineRepository, undoController, applicationScope) }

    val academicActions: AcademicActions by lazy {
        AcademicActions(subjectRepository, examRepository, undoController, applicationScope)
    }

    /** Entry point for the AI assistant (Phase 11) to create and reschedule routines, subjects and exams. */
    val routineCommandExecutor: RoutineCommandExecutor by lazy {
        RoutineCommandExecutor(routineRepository, subjectRepository, examRepository, routineActions)
    }

    val powerSaveModeMonitor: PowerSaveModeMonitor by lazy {
        PowerSaveModeMonitor(appContext)
    }

    val glassDeviceCapability: GlassDeviceCapability by lazy {
        GlassDeviceCapability.from(appContext)
    }
}
// ===== END SECTION: APP_CONTAINER =====
