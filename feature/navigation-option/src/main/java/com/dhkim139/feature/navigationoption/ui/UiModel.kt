package com.dhkim139.feature.navigationoption.ui

import androidx.annotation.StringRes
import com.dhkim139.feature.navigationoption.EndpointRole
import com.dhkim139.feature.navigationoption.MapApp
import com.dhkim139.feature.navigationoption.NavLatLng
import com.dhkim139.feature.navigationoption.R


internal data class EndpointChoice(
    val role: EndpointRole,
    val lotId: String?,
    val lotName: String? = null,
    val linkSec: Int = 0,
    val isEstimated: Boolean = false,
    val lotPosition: NavLatLng? = null,
) {
    val parked: Boolean get() = lotId != null

    companion object {
        fun none(role: EndpointRole) = EndpointChoice(role, null)
    }
}

internal sealed interface OverviewRow {

    val delivered: Boolean

    data object MyLocation : OverviewRow {
        override val delivered: Boolean get() = true
    }

    data class Endpoint(
        val endpoint: CourseEndpoint,
        val choice: EndpointChoice,
        override val delivered: Boolean,
        val canPark: Boolean,
    ) : OverviewRow
}

internal sealed interface SheetContent {

    data class Overview(
        val rows: List<OverviewRow>,
        val useMyLocation: Boolean,
        val mapApp: MapApp,
        val notice: DeliveryNotice?,
    ) : SheetContent

    data class ParkingPicker(
        val role: EndpointRole,
        val candidates: List<ParkingCandidate>,
        val selectedLotId: String?,
        val otherRole: EndpointRole,
        val otherParked: Boolean,
        val showsMyLocation: Boolean,
        val loading: Boolean,
    ) : SheetContent

    data class MapAppPicker(
        val full: List<MapApp>,
        val limited: List<MapApp>,
        val selected: MapApp,
        val splitGroups: Boolean,
    ) : SheetContent
}


internal enum class DeliveryNotice {
    ONE_OF_TWO,
    TWO_STOPS_ONLY,
}

internal data class NavCourse(
    val id: String,
    val name: String,
    val path: List<NavLatLng>,
    val distanceM: Int,
    val durationSec: Int,
    val start: CourseEndpoint,
    val end: CourseEndpoint,
) {
    fun endpoint(role: EndpointRole): CourseEndpoint =
        if (role == EndpointRole.START) start else end
}

internal data class CourseEndpoint(
    val role: EndpointRole,
    val position: NavLatLng,
    val addressText: String,
)

internal enum class CapacityLevelUi(@StringRes val labelRes: Int) {
    FEW(R.string.capacity_few),
    SOME(R.string.capacity_some),
    MANY(R.string.capacity_many),
}

internal enum class ParkingTagUi(@StringRes val labelRes: Int) {
    FREE(R.string.tag_free),
    NIGHT_OK(R.string.tag_night_ok),
    RESTROOM(R.string.tag_restroom),
    SCENIC(R.string.tag_scenic),
    EV_CHARGER(R.string.tag_ev_charger),
    LARGE_VEHICLE(R.string.tag_large_vehicle),
    CROWDED(R.string.tag_crowded),
    SEASONAL(R.string.tag_seasonal),
    NEEDS_PERMISSION(R.string.tag_needs_permission),
    ACCESSIBLE(R.string.tag_accessible),
}

internal data class ParkingLot(
    val id: String,
    val name: String,
    val position: NavLatLng,
    val feeNote: String?,
    val tags: List<ParkingTagUi>,
    val capacity: CapacityLevelUi?,
)

internal data class ParkingCandidate(
    val lot: ParkingLot,
    val linkSec: Int,
    val isEstimated: Boolean, // 약
    val linkM: Int,
    val straightM: Int,
) {
    fun toEndpoint(role: EndpointRole) = EndpointChoice(
        role = role,
        lotId = lot.id,
        lotName = lot.name,
        linkSec = linkSec,
        isEstimated = isEstimated,
        lotPosition = lot.position,
    )
}

internal data class CourseParking(
    val start: List<ParkingCandidate>,
    val end: List<ParkingCandidate>,
) {
    fun candidateOf(role: EndpointRole): List<ParkingCandidate> =
        if (role == EndpointRole.START) start else end

    companion object {
        val EMPTY = CourseParking(emptyList(), emptyList())
    }
}

sealed interface UiText {

    data class Res(
        @StringRes val id: Int,
        val args: List<Any> = emptyList(),
    ) : UiText

    data class Raw(val value: String) : UiText

    companion object {
        fun res(@StringRes id: Int, vararg args: Any): Res = Res(id, args.toList())
    }
}

internal sealed interface SheetTarget {
    data object Overview : SheetTarget
    data class ParkingPicker(val role: EndpointRole) : SheetTarget
    data object MapAppPicker : SheetTarget
}
