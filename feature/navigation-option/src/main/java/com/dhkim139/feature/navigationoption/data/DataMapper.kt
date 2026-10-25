package com.dhkim139.feature.navigationoption.data

import com.dhkim139.feature.navigationoption.NavLatLng
import com.dhkim139.feature.navigationoption.domain.ParkingUnavailableException
import com.dhkim139.feature.navigationoption.ui.CapacityLevelUi
import com.dhkim139.feature.navigationoption.ui.CourseParking
import com.dhkim139.feature.navigationoption.ui.ParkingCandidate
import com.dhkim139.feature.navigationoption.ui.ParkingLot
import com.dhkim139.feature.navigationoption.ui.ParkingTagUi


internal fun CourseParkingOneWayDto.toDomain(): CourseParking {
    when (status) {
        CourseParkingStatus.PENDING ->
            throw ParkingUnavailableException(ParkingUnavailableException.Reason.PENDING)

        CourseParkingStatus.FAILED ->
            throw ParkingUnavailableException(ParkingUnavailableException.Reason.FAILED)

        CourseParkingStatus.READY, CourseParkingStatus.PARTIAL -> Unit
    }

    return CourseParking(
        start = start.lots.mapNotNull { it.toDomain() },
        end = goal.lots.mapNotNull { it.toDomain() },
    )
}

private fun ParkingOneWayEntry.toDomain(): ParkingCandidate? {
    return ParkingCandidate(
        lot = ParkingLot(
            id = lotId,
            name = name,
            position = NavLatLng(
                location.lat,
                location.lng
            ),
            feeNote = feeNote,
            tags = knownTags.mapNotNull { it.toDomain() },
            capacity = capacityLevel.toDomain(),
        ),
        linkSec = travelSec,
        isEstimated = isEstimated,
        linkM = travelDistanceM,
        straightM = straightDistanceM,
    )
}

private fun ParkingTag.toDomain(): ParkingTagUi? = when (this) {
    ParkingTag.FREE -> ParkingTagUi.FREE
    ParkingTag.NIGHT_OK -> ParkingTagUi.NIGHT_OK
    ParkingTag.RESTROOM -> ParkingTagUi.RESTROOM
    ParkingTag.SCENIC -> ParkingTagUi.SCENIC
    ParkingTag.EV_CHARGER -> ParkingTagUi.EV_CHARGER
    ParkingTag.LARGE_VEHICLE -> ParkingTagUi.LARGE_VEHICLE
    ParkingTag.CROWDED -> ParkingTagUi.CROWDED
    ParkingTag.SEASONAL -> ParkingTagUi.SEASONAL
    ParkingTag.NEEDS_PERMISSION -> ParkingTagUi.NEEDS_PERMISSION
    ParkingTag.ACCESSIBLE -> ParkingTagUi.ACCESSIBLE
    else -> null
}

private fun CapacityLevel.toDomain(): CapacityLevelUi? = when (this) {
    CapacityLevel.FEW -> CapacityLevelUi.FEW
    CapacityLevel.SOME -> CapacityLevelUi.SOME
    CapacityLevel.MANY -> CapacityLevelUi.MANY
    else -> null
}
