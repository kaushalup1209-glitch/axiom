package com.kaushalkr.axiom.feature.academics.subject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.VisibilityThreshold
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomProgressRing
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.components.GlassIconButton
import com.kaushalkr.axiom.core.designsystem.components.GlassTextField
import com.kaushalkr.axiom.core.designsystem.layout.LocalScreenContentPadding
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.SubjectPalette
import com.kaushalkr.axiom.core.model.AcademicLimits
import com.kaushalkr.axiom.feature.academics.SubjectSaveResult
import com.kaushalkr.axiom.feature.academics.components.ExamCountdownRow
import com.kaushalkr.axiom.feature.academics.components.SubjectSheet
import com.kaushalkr.axiom.feature.academics.subject.components.ChapterRow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ===== SECTION: SUBJECT_DETAIL_ROUTE =====
/** A subject's syllabus. Chapters are a lazy list, so long syllabi scroll smoothly. */
@Composable
fun SubjectDetailRoute(
    subjectId: Long,
    onClose: () -> Unit,
    onOpenExam: (examId: Long) -> Unit,
    onNewExam: (subjectId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubjectDetailViewModel = viewModel(factory = SubjectDetailViewModel.factory(subjectId)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AxiomTheme.colors
    val motion = AxiomTheme.motion
    val chromePadding = LocalScreenContentPadding.current
    val layoutDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()
    val latestOnClose by rememberUpdatedState(onClose)
    var showEditSheet by rememberSaveable { mutableStateOf(false) }
    var editColor by rememberSaveable { mutableIntStateOf(0) }
    var editError by rememberSaveable { mutableStateOf<String?>(null) }
    val editName = rememberTextFieldState()
    val blankError = stringResource(R.string.subject_error_blank)
    val duplicateError = stringResource(R.string.subject_error_duplicate)

    LaunchedEffect(viewModel) { viewModel.closeEvents.collect { latestOnClose() } }
    LaunchedEffect(viewModel) { viewModel.isMissing.collect { missing -> if (missing) latestOnClose() } }

    Box(modifier = modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.TopCenter) {
        val loaded = state ?: return@Box
        val subject = loaded.detail.subject
        val subjectColor = SubjectPalette.color(subject.colorIndex, colors.isDark)
        val chapters = loaded.detail.chapters
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
            // ===== SECTION: SUBJECT_DETAIL_HEADER =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AxiomDimens.screenHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
            ) {
                Box(Modifier.size(14.dp).clip(CircleShape).background(subjectColor))
                Text(
                    text = subject.name,
                    style = AxiomTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                )
                GlassIconButton(
                    onClick = {
                        editName.setTextAndPlaceCursorAtEnd(subject.name)
                        editColor = subject.colorIndex
                        editError = null
                        showEditSheet = true
                    },
                    iconRes = R.drawable.ic_edit,
                    contentDescription = stringResource(R.string.subject_edit),
                    floating = false,
                )
            }
            // ===== END SECTION: SUBJECT_DETAIL_HEADER =====

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = AxiomDimens.screenHorizontalPadding,
                    end = AxiomDimens.screenHorizontalPadding,
                    top = AxiomSpacing.lg,
                    bottom = chromePadding.calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
            ) {
                // ===== SECTION: SUBJECT_DETAIL_SUMMARY =====
                item(key = "summary", contentType = "summary") {
                    GlassCard(modifier = Modifier.fillMaxWidth(), hostsGlass = true) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.lg)) {
                            AxiomProgressRing(progress = subject.progress, modifier = Modifier.size(84.dp), strokeWidth = 8.dp) {
                                Text(
                                    text = stringResource(R.string.home_goal_percent, (subject.progress * 100).roundToInt()),
                                    style = AxiomTheme.typography.titleMedium,
                                    color = colors.textPrimary,
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (subject.chaptersTotal == 0) {
                                        stringResource(R.string.subject_no_chapters)
                                    } else {
                                        pluralStringResource(R.plurals.subject_chapters_progress, subject.chaptersTotal, subject.chaptersDone, subject.chaptersTotal)
                                    },
                                    style = AxiomTheme.typography.titleSmall,
                                    color = colors.textPrimary,
                                )
                                Text(
                                    text = pluralStringResource(R.plurals.subject_open_tasks, loaded.openTaskCount, loaded.openTaskCount),
                                    style = AxiomTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                )
                            }
                        }
                        if (loaded.upcomingExams.isNotEmpty()) {
                            Spacer(Modifier.height(AxiomSpacing.sm))
                            loaded.upcomingExams.forEach { exam ->
                                ExamCountdownRow(exam = exam, today = loaded.today, onClick = { onOpenExam(exam.id) })
                            }
                        }
                        Spacer(Modifier.height(AxiomSpacing.sm))
                        GlassButton(onClick = { onNewExam(subject.id) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(painter = painterResource(R.drawable.ic_exam), contentDescription = null, modifier = Modifier.size(20.dp))
                            Text(stringResource(R.string.academics_add_exam))
                        }
                    }
                }
                // ===== END SECTION: SUBJECT_DETAIL_SUMMARY =====

                // ===== SECTION: SUBJECT_DETAIL_ADD_CHAPTERS =====
                item(key = "add", contentType = "add") {
                    Column(verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                        GlassTextField(
                            state = viewModel.newChaptersState,
                            placeholder = stringResource(R.string.subject_chapters_placeholder),
                            label = stringResource(R.string.subject_chapters_add),
                            minLines = 2,
                            maxLength = AcademicLimits.CHAPTER_TITLE_MAX_LENGTH * 20,
                        )
                        GlassButton(
                            onClick = viewModel::addChapters,
                            modifier = Modifier.fillMaxWidth(),
                            tint = colors.accent,
                            enabled = chapters.size < AcademicLimits.MAX_CHAPTERS_PER_SUBJECT,
                        ) {
                            Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(20.dp))
                            Text(stringResource(R.string.subject_chapters_add))
                        }
                    }
                }
                // ===== END SECTION: SUBJECT_DETAIL_ADD_CHAPTERS =====

                // ===== SECTION: SUBJECT_DETAIL_CHAPTERS =====
                itemsIndexed(items = chapters, key = { _, chapter -> chapter.id }, contentType = { _, _ -> "chapter" }) { index, chapter ->
                    ChapterRow(
                        chapter = chapter,
                        ringColor = subjectColor,
                        canMoveUp = index > 0,
                        canMoveDown = index < chapters.lastIndex,
                        onToggle = { viewModel.toggleChapter(chapter) },
                        onMove = { offset -> viewModel.moveChapter(chapter, offset) },
                        onDelete = { viewModel.deleteChapter(chapter) },
                        modifier = if (motion.isEnabled) {
                            Modifier.animateItem(
                                fadeInSpec = motion.tween(AxiomMotionTokens.ListFadeIn),
                                placementSpec = motion.spring(AxiomMotionTokens.ListPlacement, IntOffset.VisibilityThreshold),
                                fadeOutSpec = motion.tween(AxiomMotionTokens.ListFadeOut),
                            )
                        } else {
                            Modifier
                        },
                    )
                }
                // ===== END SECTION: SUBJECT_DETAIL_CHAPTERS =====
            }
        }
    }

    SubjectSheet(
        visible = showEditSheet,
        title = stringResource(R.string.subject_edit),
        nameState = editName,
        colorIndex = editColor,
        onSelectColor = { editColor = it },
        error = editError,
        onSave = {
            scope.launch {
                when (viewModel.updateSubject(editName.text.toString(), editColor)) {
                    SubjectSaveResult.SAVED -> showEditSheet = false
                    SubjectSaveResult.BLANK -> editError = blankError
                    SubjectSaveResult.DUPLICATE -> editError = duplicateError
                }
            }
        },
        onDismiss = { showEditSheet = false },
        onDelete = viewModel::deleteSubject,
    )
}
// ===== END SECTION: SUBJECT_DETAIL_ROUTE =====
