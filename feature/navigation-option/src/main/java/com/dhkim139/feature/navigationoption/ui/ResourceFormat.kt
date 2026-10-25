package com.dhkim139.feature.navigationoption.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dhkim139.feature.navigationoption.MapApp
import com.dhkim139.feature.navigationoption.R
import com.dhkim139.feature.navigationoption.domain.ParkingUnavailableException


@Composable
internal fun UiText.asString(): String = when (this) {
    is UiText.Raw -> value
    is UiText.Res -> if (args.isEmpty()) {
        stringResource(id)
    } else {
        stringResource(id, *args.toTypedArray())
    }
}

@Composable
internal fun linkLabel(sec: Int, isEstimated: Boolean = false): String {
    val base = if (sec < 60) {
        stringResource(R.string.link_close)
    } else {
        stringResource(R.string.link_minutes, sec / 60)
    }
    return if (isEstimated) stringResource(R.string.link_estimated, base) else base
}

@Composable
internal fun candidateMeta(candidate: ParkingCandidate): String {
    val separator = stringResource(R.string.separator)
    val lot = candidate.lot

    return buildList {
        add(linkLabel(candidate.linkSec, candidate.isEstimated))
        lot.feeNote?.let(::add)
        lot.tags.forEach { add(stringResource(it.labelRes)) }
        lot.capacity?.let { add(stringResource(it.labelRes)) }
    }.joinToString(separator)
}

@Composable
internal fun choiceSubtitle(choice: EndpointChoice, roleLabel: String): String =
    if (choice.linkSec < 60) {
        stringResource(R.string.overview_subtitle_parked_close, roleLabel)
    } else {
        stringResource(
            R.string.overview_subtitle_parked,
            roleLabel,
            linkLabel(choice.linkSec, choice.isEstimated),
        )
    }

@Composable
internal fun noticeLabel(notice: DeliveryNotice, mapApp: MapApp): String = stringResource(
    when (notice) {
        DeliveryNotice.ONE_OF_TWO -> R.string.overview_notice_one_of_two
        DeliveryNotice.TWO_STOPS_ONLY -> R.string.overview_notice_two_stops
    },
    stringResource(mapApp.labelRes),
)

internal fun errorTextFor(cause: Throwable): UiText = when {
    cause !is ParkingUnavailableException ->
        UiText.res(R.string.error_date_load)

    cause.reason == ParkingUnavailableException.Reason.PENDING ->
        UiText.res(R.string.error_parking_pending)

    else -> UiText.res(R.string.error_parking_failed)
}