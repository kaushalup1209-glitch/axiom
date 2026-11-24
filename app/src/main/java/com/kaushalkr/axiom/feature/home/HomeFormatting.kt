package com.kaushalkr.axiom.feature.home

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.kaushalkr.axiom.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// ===== SECTION: HOME_FORMAT_DURATION =====
/** "2h 36m", "45m" or "4h". */
@Composable
@ReadOnlyComposable
fun formatStudyMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0 -> stringResource(R.string.duration_minutes, minutes)
        minutes == 0 -> stringResource(R.string.duration_hours, hours)
        else -> stringResource(R.string.duration_hours_minutes, hours, minutes)
    }
}

/** Countdown readout: "24:59", or "1:04:59" for an hour or more. Rounds up to whole seconds. */
fun formatCountdown(remainingMillis: Long): String {
    val totalSeconds = (remainingMillis + 999L) / 1000L
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    }
}
// ===== END SECTION: HOME_FORMAT_DURATION =====

// ===== SECTION: HOME_FORMAT_DATE_TIME =====
/** The UI locale, read from the configuration so a language change recomposes. */
@Composable
fun currentUiLocale(): Locale = LocalConfiguration.current.locales[0] ?: Locale.ROOT

/** Localized "Tuesday, 24 November" (order and punctuation follow the locale). */
@Composable
fun rememberFormattedDate(date: LocalDate): String {
    val locale = currentUiLocale()
    val formatter = remember(locale) {
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEEdMMMM"), locale)
    }
    return remember(date, formatter) { formatter.format(date) }
}

/** Localized short time, e.g. "16:30" or "4:30 PM". */
@Composable
fun rememberTimeFormatter(): DateTimeFormatter {
    val locale = currentUiLocale()
    return remember(locale) { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale) }
}

fun DateTimeFormatter.formatTime(time: LocalTime): String = format(time)
// ===== END SECTION: HOME_FORMAT_DATE_TIME =====
