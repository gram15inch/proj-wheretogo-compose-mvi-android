package com.wheretogo.presentation.composable.content


import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.wheretogo.domain.RecentCardSituation
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.headlineArrayRes
import com.wheretogo.presentation.feature.kaomojiArrayRes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@Composable
fun rememberSituationHeadline(situation: RecentCardSituation): String {
    val items = stringArrayResource(situation.headlineArrayRes())
    return remember(situation) { items.randomOrNull().orEmpty() }
}


@Composable
fun rememberSituationKaomoji(situation: RecentCardSituation): String {
    val arrayRes = situation.kaomojiArrayRes()
    val items = stringArrayResource(arrayRes)
    return remember(situation) { items.randomOrNull().orEmpty() }
}


@Composable
fun rememberLastStampLabel(stampAt: Long?): String? {
    val context = LocalContext.current
    LocalConfiguration.current

    val todayFmt = stringResource(R.string.recent_card_last_today)          // "오늘 %1$s"
    val yesterday = stringResource(R.string.recent_card_last_yesterday)     // "어제"
    val daysAgoFmt = stringResource(R.string.recent_card_last_days_ago)     // "%1$d일 전"
    val thisYearPattern = stringResource(R.string.recent_card_date_this_year) // "M월 d일"
    val fullPattern = stringResource(R.string.recent_card_date_full)          // "yyyy년 M월 d일"

    return remember(stampAt, context) {
        if (stampAt == null) return@remember null
        formatLastStamp(
            stampAt = stampAt,
            context = context,
            todayFmt = todayFmt,
            yesterday = yesterday,
            daysAgoFmt = daysAgoFmt,
            thisYearPattern = thisYearPattern,
            fullPattern = fullPattern,
        )
    }
}

internal fun formatLastStamp(
    stampAt: Long,
    context: Context,
    todayFmt: String,
    yesterday: String,
    daysAgoFmt: String,
    thisYearPattern: String,
    fullPattern: String,
    now: Long = System.currentTimeMillis(),
): String {
    val daysSince = (now.toLocalEpochDay() - stampAt.toLocalEpochDay())

    return when {
        daysSince <= 0L -> {
            val time = android.text.format.DateFormat
                .getTimeFormat(context)
                .format(Date(stampAt))
            String.format(todayFmt, time)
        }
        daysSince == 1L -> yesterday
        daysSince <= 6L -> String.format(daysAgoFmt, daysSince)
        else -> {
            val pattern = if (isSameYear(stampAt, now)) thisYearPattern else fullPattern
            SimpleDateFormat(pattern, context.resources.configuration.locales[0])
                .format(Date(stampAt))
        }
    }
}

private fun isSameYear(a: Long, b: Long): Boolean {
    val cal = Calendar.getInstance()
    cal.timeInMillis = a
    val yearA = cal.get(Calendar.YEAR)
    cal.timeInMillis = b
    return yearA == cal.get(Calendar.YEAR)
}

private fun Long.toLocalEpochDay(): Long {
    val offset = TimeZone.getDefault().getOffset(this)
    return Math.floorDiv(this + offset, DAY_MILLIS)
}

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L