package com.dhkim139.feature.navigationoption.ui

import com.dhkim139.feature.navigationoption.EndpointRole
import com.dhkim139.feature.navigationoption.MapApp
import com.dhkim139.feature.navigationoption.NavLatLng
import com.dhkim139.feature.navigationoption.NavOptionResult

data class NavPoint(
    val role: EndpointRole,
    val position: NavLatLng,
    val parking: ParkingRef?,
) {
    data class ParkingRef(val id: String, val name: String)
}

internal data class SheetSelection(
    val useMyLocation: Boolean = true,
    val choices: Map<EndpointRole, EndpointChoice> = mapOf(
        EndpointRole.START to EndpointChoice.none(EndpointRole.START),
        EndpointRole.END to EndpointChoice.none(EndpointRole.END),
    ),
    val mapApp: MapApp = MapApp.DEFAULT,
) {
    fun selectEndpoint(role: EndpointRole): EndpointChoice =
        choices[role] ?: EndpointChoice.none(role)

    fun updateSelection(choice: EndpointChoice): SheetSelection =
        copy(choices = choices + (choice.role to choice))

    val orderedMapApps: List<MapApp>
        get() = UiRules.DISPLAY_ORDER

    fun createSheetContent(
        course: NavCourse,
        parking: CourseParking,
        target: SheetTarget,
    ): SheetContent = when (target) {
        SheetTarget.Overview -> overviewContent(course)
        is SheetTarget.ParkingPicker -> pickerContent(parking, target.role)
        SheetTarget.MapAppPicker -> mapAppPickerContent()
    }

    fun createSheetContent(
        course: NavCourse,
        parking: CourseParking,
        content: SheetContent?,
    ): SheetContent = when (content) {
        null,
        is SheetContent.Overview -> overviewContent(course)

        is SheetContent.ParkingPicker -> pickerContent(parking, content.role)
        is SheetContent.MapAppPicker -> mapAppPickerContent()
    }

    fun overviewContent(course: NavCourse): SheetContent.Overview {
        val rows = buildList {
            add(OverviewRow.MyLocation)
            for (role in listOf(EndpointRole.START, EndpointRole.END)) {
                add(
                    OverviewRow.Endpoint(
                        endpoint = course.endpoint(role),
                        choice = effectiveChoiceOf(role),
                        delivered = role in deliveredRoles(),
                        canPark = canPark(role),
                    )
                )
            }
        }

        return SheetContent.Overview(
            rows = rows,
            useMyLocation = useMyLocation,
            mapApp = mapApp,
            notice = when {
                mapApp.supportsWaypoints -> null
                useMyLocation -> DeliveryNotice.ONE_OF_TWO
                else -> DeliveryNotice.TWO_STOPS_ONLY
            },
        )
    }

    private fun deliveredRoles(): List<EndpointRole> =
        if (!mapApp.supportsWaypoints && useMyLocation) {
            listOf(EndpointRole.START)
        } else {
            listOf(EndpointRole.START, EndpointRole.END)
        }

    private fun canPark(role: EndpointRole): Boolean =
        role in deliveredRoles() && (mapApp.supportsWaypoints || useMyLocation)

    private fun effectiveChoiceOf(role: EndpointRole): EndpointChoice =
        if (canPark(role)) selectEndpoint(role) else EndpointChoice.none(role)

    fun pickerContent(
        parking: CourseParking,
        role: EndpointRole,
    ): SheetContent.ParkingPicker {
        val other = selectEndpoint(role.opposite())
        return SheetContent.ParkingPicker(
            role = role,
            candidates = UiRules.visibleParking(parking.candidateOf(role)),
            selectedLotId = selectEndpoint(role).lotId,
            otherRole = other.role,
            otherParked = other.parked,
            showsMyLocation = useMyLocation,
            loading = false
        )
    }

    fun mapAppPickerContent(): SheetContent.MapAppPicker {
        val (full, limited) = orderedMapApps.partition { it.supportsWaypoints }
        val fullRouteStops = (if (useMyLocation) 1 else 0) +
                EndpointRole.entries.sumOf { role -> if (selectEndpoint(role).parked) 2 else 1 }

        return SheetContent.MapAppPicker(
            full = full,
            limited = limited,
            selected = mapApp,
            splitGroups = fullRouteStops > UiRules.DELIVERABLE_WITHOUT_WAYPOINTS,
        )
    }


    fun toResult(course: NavCourse): NavOptionResult = NavOptionResult(
        courseId = course.id,
        useMyLocation = useMyLocation,
        mapApp = mapApp,
        courseName = course.name,
        points = deliveredRoles().flatMap { pointsAt(course, it) },
    )

    private fun pointsAt(course: NavCourse, role: EndpointRole): List<NavPoint> {
        val choice = effectiveChoiceOf(role)
        val endpoint = course.endpoint(role)

        val coursePoint = NavPoint(
            role = role,
            position = endpoint.position,
            parking = null,
        )
        val parkingPoint = choice.lotId?.let { lotId ->
            NavPoint(
                role = role,
                position = choice.lotPosition ?: endpoint.position,
                parking = NavPoint.ParkingRef(lotId, choice.lotName.orEmpty()),
            )
        }

        return when {
            parkingPoint == null -> listOf(coursePoint)
            !mapApp.supportsWaypoints -> listOf(parkingPoint)
            else -> listOf(parkingPoint, coursePoint)
        }
    }

}