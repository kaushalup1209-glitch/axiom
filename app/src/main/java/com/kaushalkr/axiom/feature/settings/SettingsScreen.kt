package com.kaushalkr.axiom.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaushalkr.axiom.BuildConfig
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomScreenColumn
import com.kaushalkr.axiom.core.designsystem.components.AxiomTextField
import com.kaushalkr.axiom.core.designsystem.components.GlassDialog
import com.kaushalkr.axiom.core.designsystem.components.GlassSegmentedControl
import com.kaushalkr.axiom.core.designsystem.components.GlassSlider
import com.kaushalkr.axiom.core.designsystem.components.ScreenHeader
import com.kaushalkr.axiom.core.designsystem.glass.GlassLimitation
import com.kaushalkr.axiom.core.designsystem.glass.GlassResolution
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassResolution
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotion
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.AnimationScale
import com.kaushalkr.axiom.core.model.AppearanceSettings
import com.kaushalkr.axiom.core.model.StudyDefaults
import com.kaushalkr.axiom.core.model.StudyPreferences
import com.kaushalkr.axiom.core.model.GlassMode
import com.kaushalkr.axiom.core.model.ThemeMode
import com.kaushalkr.axiom.core.ui.AxiomAppMark
import com.kaushalkr.axiom.feature.home.formatStudyMinutes
import com.kaushalkr.axiom.feature.settings.components.SettingsActionRow
import com.kaushalkr.axiom.feature.settings.components.SettingsDivider
import com.kaushalkr.axiom.feature.settings.components.SettingsInfoNote
import com.kaushalkr.axiom.feature.settings.components.SettingsSection
import com.kaushalkr.axiom.feature.settings.components.SettingsToggleRow
import kotlinx.coroutines.flow.drop
import kotlin.math.roundToInt

// ===== SECTION: SETTINGS_ROUTE =====
/** Stateful entry point: connects [SettingsViewModel] to the stateless [SettingsScreen]. */
@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val study by viewModel.study.collectAsStateWithLifecycle()
    val loadedSettings = settings
    val loadedStudy = study
    if (loadedSettings != null && loadedStudy != null) {
        SettingsScreen(
            settings = loadedSettings,
            study = loadedStudy,
            glassResolution = LocalGlassResolution.current,
            onThemeModeChange = viewModel::setThemeMode,
            onGlassModeChange = viewModel::setGlassMode,
            onUseRecommendedGlass = viewModel::useRecommendedGlassMode,
            onUserNameChange = viewModel::setUserName,
            onDailyGoalChange = viewModel::setDailyGoalMinutes,
            onGlassTintChange = viewModel::setGlassTintStrength,
            onAdaptToBatterySaverChange = viewModel::setAdaptGlassToBatterySaver,
            onAnimationScaleChange = viewModel::setAnimationScale,
            onResetAppearance = viewModel::resetAppearance,
            modifier = modifier,
        )
    }
}
// ===== END SECTION: SETTINGS_ROUTE =====

// ===== SECTION: SETTINGS_SCREEN =====
@Composable
fun SettingsScreen(
    settings: AppearanceSettings,
    study: StudyPreferences,
    glassResolution: GlassResolution,
    onThemeModeChange: (ThemeMode) -> Unit,
    onGlassModeChange: (GlassMode) -> Unit,
    onUseRecommendedGlass: () -> Unit,
    onUserNameChange: (String) -> Unit,
    onDailyGoalChange: (Int) -> Unit,
    onGlassTintChange: (Float) -> Unit,
    onAdaptToBatterySaverChange: (Boolean) -> Unit,
    onAnimationScaleChange: (AnimationScale) -> Unit,
    onResetAppearance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var showLicensesDialog by rememberSaveable { mutableStateOf(false) }
    val motion = AxiomTheme.motion

    AxiomScreenColumn(modifier = modifier) {
        ScreenHeader(
            eyebrow = stringResource(R.string.settings_eyebrow),
            title = stringResource(R.string.settings_title),
            subtitle = stringResource(R.string.settings_subtitle),
            modifier = Modifier.padding(bottom = AxiomSpacing.xs),
        )

        SettingsSection(title = stringResource(R.string.settings_section_study)) {
            UserNameSetting(study.userName, onUserNameChange)
            SettingsDivider()
            DailyGoalSetting(study.dailyGoalMinutes, onDailyGoalChange)
        }

        SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
            ThemeModeSetting(settings.themeMode, onThemeModeChange)
            SettingsDivider()
            GlassModeSetting(settings.glassMode ?: glassResolution.recommended, glassResolution, onGlassModeChange, onUseRecommendedGlass)
            AnimatedVisibility(
                visible = glassResolution.effective != GlassMode.OFF,
                enter = motion.expandEnter(),
                exit = motion.collapseExit(),
            ) {
                Column {
                    SettingsDivider()
                    GlassTintSetting(settings.glassTintStrength, onGlassTintChange)
                }
            }
            SettingsDivider()
            SettingsToggleRow(
                iconRes = R.drawable.ic_battery_saver,
                title = stringResource(R.string.settings_adapt_battery),
                description = stringResource(R.string.settings_adapt_battery_desc),
                checked = settings.adaptGlassToBatterySaver,
                onCheckedChange = onAdaptToBatterySaverChange,
            )
        }

        SettingsSection(title = stringResource(R.string.settings_section_motion)) {
            AnimationScaleSetting(settings.animationScale, onAnimationScaleChange)
        }

        SettingsSection(title = stringResource(R.string.settings_section_about), hostsGlass = false) {
            AboutAppHeader()
            SettingsDivider()
            SettingsActionRow(
                iconRes = R.drawable.ic_licenses,
                title = stringResource(R.string.settings_licenses),
                description = stringResource(R.string.settings_licenses_desc),
                onClick = { showLicensesDialog = true },
            )
            SettingsDivider()
            SettingsActionRow(
                iconRes = R.drawable.ic_reset,
                title = stringResource(R.string.settings_reset_appearance),
                description = stringResource(R.string.settings_reset_appearance_desc),
                onClick = { showResetDialog = true },
                isDestructive = true,
            )
        }
    }

    // ===== SECTION: SETTINGS_DIALOGS =====
    GlassDialog(
        visible = showResetDialog,
        onDismissRequest = { showResetDialog = false },
        title = stringResource(R.string.dialog_reset_title),
        message = stringResource(R.string.dialog_reset_message),
        confirmLabel = stringResource(R.string.dialog_reset_confirm),
        onConfirm = onResetAppearance,
        dismissLabel = stringResource(R.string.dialog_cancel),
        isDestructive = true,
    )
    GlassDialog(
        visible = showLicensesDialog,
        onDismissRequest = { showLicensesDialog = false },
        title = stringResource(R.string.dialog_licenses_title),
        message = stringResource(R.string.dialog_licenses_message),
        confirmLabel = stringResource(R.string.dialog_close),
        onConfirm = {},
    )
    // ===== END SECTION: SETTINGS_DIALOGS =====
}
// ===== END SECTION: SETTINGS_SCREEN =====

// ===== SECTION: SETTINGS_THEME_MODE =====
@Composable
private fun ThemeModeSetting(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val options = listOf(
        stringResource(R.string.theme_system),
        stringResource(R.string.theme_light),
        stringResource(R.string.theme_dark),
    )
    SettingFieldLabel(stringResource(R.string.settings_theme))
    Spacer(Modifier.height(10.dp))
    GlassSegmentedControl(
        options = options,
        selectedIndex = themeMode.ordinal,
        onSelect = { index -> onThemeModeChange(ThemeMode.entries[index]) },
        modifier = Modifier.fillMaxWidth(),
    )
}
// ===== END SECTION: SETTINGS_THEME_MODE =====

// ===== SECTION: SETTINGS_GLASS_MODE =====
@Composable
private fun GlassModeSetting(
    glassMode: GlassMode,
    glassResolution: GlassResolution,
    onGlassModeChange: (GlassMode) -> Unit,
    onUseRecommendedGlass: () -> Unit,
) {
    val colors = AxiomTheme.colors
    val options = listOf(
        stringResource(R.string.glass_full),
        stringResource(R.string.glass_lite),
        stringResource(R.string.glass_off),
    )
    val description = when (glassMode) {
        GlassMode.FULL -> stringResource(R.string.glass_desc_full)
        GlassMode.LITE -> stringResource(R.string.glass_desc_lite)
        GlassMode.OFF -> stringResource(R.string.glass_desc_off)
    }
    val limitationText = when (glassResolution.limitation) {
        GlassLimitation.NONE -> null
        GlassLimitation.BATTERY_SAVER -> stringResource(R.string.glass_limit_battery_saver)
        GlassLimitation.NO_REFRACTION_SUPPORT -> stringResource(R.string.glass_limit_no_refraction)
        GlassLimitation.NO_BLUR_SUPPORT -> stringResource(R.string.glass_limit_no_blur)
        GlassLimitation.LOW_RAM_DEVICE -> stringResource(R.string.glass_limit_low_ram)
    }

    SettingFieldLabel(stringResource(R.string.settings_glass))
    Spacer(Modifier.height(10.dp))
    GlassSegmentedControl(
        options = options,
        selectedIndex = glassMode.ordinal,
        onSelect = { index -> onGlassModeChange(GlassMode.entries[index]) },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = description,
        style = AxiomTheme.typography.bodySmall,
        color = colors.textSecondary,
    )
    GlassRecommendationRow(glassResolution, onUseRecommendedGlass)
    // Keep the last note visible while it collapses away.
    val lastLimitationText = remember { arrayOfNulls<String>(1) }
    if (limitationText != null) lastLimitationText[0] = limitationText
    val motion = AxiomTheme.motion
    AnimatedVisibility(
        visible = limitationText != null,
        enter = motion.expandEnter(),
        exit = motion.collapseExit(),
    ) {
        Column {
            Spacer(Modifier.height(AxiomSpacing.sm))
            SettingsInfoNote(text = limitationText ?: lastLimitationText[0].orEmpty())
        }
    }
}
// ===== END SECTION: SETTINGS_GLASS_MODE =====

// ===== SECTION: SETTINGS_GLASS_RECOMMENDATION =====
@Composable
private fun GlassRecommendationRow(
    glassResolution: GlassResolution,
    onUseRecommendedGlass: () -> Unit,
) {
    val colors = AxiomTheme.colors
    val recommendedName = stringResource(
        when (glassResolution.recommended) {
            GlassMode.FULL -> R.string.glass_full
            GlassMode.LITE -> R.string.glass_lite
            GlassMode.OFF -> R.string.glass_off
        },
    )
    Spacer(Modifier.height(AxiomSpacing.xs))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        Text(
            text = stringResource(
                if (glassResolution.followsRecommendation) R.string.glass_recommended_auto else R.string.glass_recommended_manual,
                recommendedName,
            ),
            style = AxiomTheme.typography.bodySmall,
            color = colors.textTertiary,
            modifier = Modifier.weight(1f),
        )
        if (!glassResolution.followsRecommendation) {
            Text(
                text = stringResource(R.string.glass_use_recommended),
                style = AxiomTheme.typography.labelLarge,
                color = colors.accent,
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onUseRecommendedGlass)
                    .defaultMinSize(minHeight = AxiomDimens.minTouchTarget)
                    .padding(horizontal = AxiomSpacing.xs, vertical = AxiomSpacing.sm),
            )
        }
    }
}
// ===== END SECTION: SETTINGS_GLASS_RECOMMENDATION =====

// ===== SECTION: SETTINGS_USER_NAME =====
@Composable
private fun UserNameSetting(
    savedName: String,
    onUserNameChange: (String) -> Unit,
) {
    // Seeded once from storage; not re-keyed, so trimming on save never fights the cursor.
    val nameState = rememberTextFieldState(savedName)
    val latestOnUserNameChange by rememberUpdatedState(onUserNameChange)
    LaunchedEffect(nameState) {
        snapshotFlow { nameState.text.toString() }
            .drop(1)
            .collect { latestOnUserNameChange(it) }
    }
    val label = stringResource(R.string.settings_name)
    SettingFieldLabel(label)
    Spacer(Modifier.height(10.dp))
    AxiomTextField(
        state = nameState,
        placeholder = stringResource(R.string.settings_name_placeholder),
        label = label,
        maxLength = StudyDefaults.USER_NAME_MAX_LENGTH,
    )
}
// ===== END SECTION: SETTINGS_USER_NAME =====

// ===== SECTION: SETTINGS_DAILY_GOAL =====
@Composable
private fun DailyGoalSetting(
    goalMinutes: Int,
    onDailyGoalChange: (Int) -> Unit,
) {
    val colors = AxiomTheme.colors
    var draftMinutes by remember(goalMinutes) { mutableFloatStateOf(goalMinutes.toFloat()) }
    val stepped = snapGoalMinutes(draftMinutes)
    val label = stringResource(R.string.settings_daily_goal)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingFieldLabel(label)
        Text(text = formatStudyMinutes(stepped), style = AxiomTheme.typography.labelLarge, color = colors.accent)
    }
    GlassSlider(
        value = draftMinutes,
        onValueChange = { draftMinutes = it },
        valueRange = StudyDefaults.DAILY_GOAL_MIN_MINUTES.toFloat()..StudyDefaults.DAILY_GOAL_MAX_MINUTES.toFloat(),
        onValueChangeFinished = { onDailyGoalChange(snapGoalMinutes(draftMinutes)) },
        contentDescription = label,
    )
    Text(
        text = stringResource(R.string.settings_daily_goal_desc),
        style = AxiomTheme.typography.bodySmall,
        color = colors.textSecondary,
    )
}

private fun snapGoalMinutes(minutes: Float): Int {
    val step = StudyDefaults.DAILY_GOAL_STEP_MINUTES
    return ((minutes / step).roundToInt() * step)
        .coerceIn(StudyDefaults.DAILY_GOAL_MIN_MINUTES, StudyDefaults.DAILY_GOAL_MAX_MINUTES)
}
// ===== END SECTION: SETTINGS_DAILY_GOAL =====

// ===== SECTION: SETTINGS_ANIMATION_SCALE =====
@Composable
private fun AnimationScaleSetting(
    animationScale: AnimationScale,
    onAnimationScaleChange: (AnimationScale) -> Unit,
) {
    val options = listOf(
        stringResource(R.string.animation_scale_off),
        stringResource(R.string.animation_scale_half),
        stringResource(R.string.animation_scale_normal),
        stringResource(R.string.animation_scale_one_and_half),
        stringResource(R.string.animation_scale_double),
    )
    SettingFieldLabel(stringResource(R.string.settings_animation_scale))
    Spacer(Modifier.height(10.dp))
    GlassSegmentedControl(
        options = options,
        selectedIndex = animationScale.ordinal,
        onSelect = { index -> onAnimationScaleChange(AnimationScale.entries[index]) },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = stringResource(R.string.settings_animation_scale_desc),
        style = AxiomTheme.typography.bodySmall,
        color = AxiomTheme.colors.textSecondary,
    )
}
// ===== END SECTION: SETTINGS_ANIMATION_SCALE =====

// ===== SECTION: SETTINGS_EXPAND_TRANSITIONS =====
private fun AxiomMotion.expandEnter(): EnterTransition =
    fadeIn(tween(AxiomMotionTokens.ExpandFade)) +
        expandVertically(spring(AxiomMotionTokens.Expand, IntSize.VisibilityThreshold))

private fun AxiomMotion.collapseExit(): ExitTransition =
    fadeOut(tween(AxiomMotionTokens.ExpandFade)) +
        shrinkVertically(spring(AxiomMotionTokens.Expand, IntSize.VisibilityThreshold))
// ===== END SECTION: SETTINGS_EXPAND_TRANSITIONS =====

// ===== SECTION: SETTINGS_GLASS_TINT =====
@Composable
private fun GlassTintSetting(
    tintStrength: Float,
    onGlassTintChange: (Float) -> Unit,
) {
    val colors = AxiomTheme.colors
    // Local draft keeps the slider smooth; the value is persisted once per gesture.
    var draftStrength by remember(tintStrength) { mutableFloatStateOf(tintStrength) }
    val label = stringResource(R.string.settings_glass_tint)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingFieldLabel(label)
        Text(
            text = stringResource(R.string.settings_glass_tint_value, (draftStrength * 100).roundToInt()),
            style = AxiomTheme.typography.labelLarge,
            color = colors.accent,
        )
    }
    GlassSlider(
        value = draftStrength,
        onValueChange = { draftStrength = it },
        onValueChangeFinished = { onGlassTintChange(draftStrength) },
        contentDescription = label,
    )
    Text(
        text = stringResource(R.string.settings_glass_tint_desc),
        style = AxiomTheme.typography.bodySmall,
        color = colors.textSecondary,
    )
}
// ===== END SECTION: SETTINGS_GLASS_TINT =====

// ===== SECTION: SETTINGS_ABOUT_HEADER =====
@Composable
private fun AboutAppHeader() {
    val colors = AxiomTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.md),
    ) {
        AxiomAppMark()
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                style = AxiomTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(
                    R.string.settings_version_and_developer,
                    BuildConfig.VERSION_NAME,
                    stringResource(R.string.app_developer),
                ),
                style = AxiomTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
    }
    Spacer(Modifier.height(AxiomSpacing.md))
    Text(
        text = stringResource(R.string.app_tagline),
        style = AxiomTheme.typography.bodyMedium,
        color = colors.textSecondary,
    )
}
// ===== END SECTION: SETTINGS_ABOUT_HEADER =====

// ===== SECTION: SETTINGS_FIELD_LABEL =====
@Composable
private fun SettingFieldLabel(text: String) {
    Text(
        text = text,
        style = AxiomTheme.typography.titleSmall,
        color = AxiomTheme.colors.textPrimary,
    )
}
// ===== END SECTION: SETTINGS_FIELD_LABEL =====
