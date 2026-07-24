package com.wheretogo.presentation.feature

import androidx.annotation.ArrayRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wheretogo.domain.RecentCardSituation
import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.presentation.R

@Composable
fun ReportReason.toDisplayName(): String = when (this) {
    ReportReason.SPAM -> stringResource(R.string.report_spam)
    ReportReason.HARASSMENT -> stringResource(R.string.report_harassment)
    ReportReason.INAPPROPRIATE -> stringResource(R.string.report_inappropriate)
    ReportReason.VIOLENCE -> stringResource(R.string.report_violence)
    ReportReason.HATE_SPEECH -> stringResource(R.string.report_hate_speech)
    ReportReason.MISINFORMATION -> stringResource(R.string.report_misinformation)
    ReportReason.COPYRIGHT -> stringResource(R.string.report_copyright)
    ReportReason.OTHER -> stringResource(R.string.report_other)
}

@ArrayRes
fun RecentCardSituation.headlineArrayRes(): Int = when (this) {
    RecentCardSituation.EMPTY -> R.array.recent_card_headline_empty
    RecentCardSituation.TODAY -> R.array.recent_card_headline_today
    RecentCardSituation.RECENT -> R.array.recent_card_headline_recent
    RecentCardSituation.PAUSED -> R.array.recent_card_headline_paused
    RecentCardSituation.DORMANT -> R.array.recent_card_headline_dormant
}

@ArrayRes
fun RecentCardSituation.kaomojiArrayRes(): Int = when (this) {
    RecentCardSituation.EMPTY -> R.array.recent_card_kaomoji_empty
    RecentCardSituation.TODAY -> R.array.recent_card_kaomoji_today
    RecentCardSituation.RECENT -> R.array.recent_card_kaomoji_recent
    RecentCardSituation.PAUSED -> R.array.recent_card_kaomoji_paused
    RecentCardSituation.DORMANT -> R.array.recent_card_kaomoji_dormant
}