package com.dhkim139.feature.navigationoption.ui

import com.dhkim139.feature.navigationoption.EndpointRole
import com.dhkim139.feature.navigationoption.MapApp
import com.dhkim139.feature.navigationoption.NavLatLng


internal object PreviewData {

    private val lotNearStart = ParkingLot(
        id = "lot-101",
        name = "양재 시민의숲 공영",
        position = NavLatLng(37.4700, 127.0380),
        feeNote = "30분 1,200원",
        tags = listOf(ParkingTagUi.RESTROOM, ParkingTagUi.NIGHT_OK),
        capacity = CapacityLevelUi.MANY,
    )

    private val lotNearStart2 = ParkingLot(
        id = "lot-102",
        name = "화물터미널 노상",
        position = NavLatLng(37.4665, 127.0421),
        feeNote = null,
        tags = listOf(ParkingTagUi.FREE),
        capacity = null,
    )

    val course = NavCourse(
        id = "course-1",
        name = "양재 – 청계산 드라이브",
        path = listOf(NavLatLng(37.4712, 127.0400), NavLatLng(37.4500, 127.0600)),
        distanceM = 22_000,
        durationSec = 35 * 60,
        start = CourseEndpoint(
            role = EndpointRole.START,
            position = NavLatLng(37.4712, 127.0400),
            addressText = "양재대로 132 부근",
        ),
        end = CourseEndpoint(
            role = EndpointRole.END,
            position = NavLatLng(37.4500, 127.0600),
            addressText = "청계산로 8 부근",
        ),
    )

    private val startCandidates = listOf(
        ParkingCandidate(
            lot = lotNearStart,
            linkSec = 100,
            isEstimated = false,
            linkM = 700,
            straightM = 550,
        ),
        ParkingCandidate(
            lot = lotNearStart2,
            linkSec = 240,
            isEstimated = true,
            linkM = 1_900,
            straightM = 910,
        ),
    )

    val parking = CourseParking(start = startCandidates, end = emptyList())

    private fun parkedAtStart() = mapOf(
        EndpointRole.START to startCandidates.first().toEndpoint(EndpointRole.START),
        EndpointRole.END to EndpointChoice.none(EndpointRole.END),
    )

    val emptySelection = SheetSelection()
    val parkedSelection = SheetSelection(choices = parkedAtStart())
    val kakaoSelection = parkedSelection.copy(mapApp = MapApp.KAKAO_MAP)

    val startParkingPicker = SheetTarget.ParkingPicker(role = EndpointRole.START)
    val emptyParkingPicker = SheetTarget.ParkingPicker(role = EndpointRole.END)

    val overviewContent = emptySelection.fold(SheetTarget.Overview)
    val parkedContent = parkedSelection.fold(SheetTarget.Overview)
    val pickerContent = parkedSelection.fold(startParkingPicker)
    val emptyPickerContent = emptySelection.fold(emptyParkingPicker)
    val loadingPickerContent = (emptySelection.fold(emptyParkingPicker) as SheetContent.ParkingPicker).copy(loading = true)
    val mapAppPickerContent = parkedSelection.fold(SheetTarget.MapAppPicker)
    val noMyLocationContent = emptySelection
        .copy(useMyLocation = false)
        .fold(SheetTarget.Overview)

    val kakaoParkedContent = kakaoSelection.fold(SheetTarget.Overview)

    val kakaoNoLocationContent = kakaoSelection
        .copy(useMyLocation = false)
        .fold(SheetTarget.Overview)

    private fun SheetSelection.fold(target: SheetTarget) =
        createSheetContent(course, parking, target)
}
