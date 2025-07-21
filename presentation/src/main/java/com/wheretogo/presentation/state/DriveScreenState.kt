package com.wheretogo.presentation.state


data class DriveScreenState(
    val searchBarState: SearchBarState = SearchBarState(),
    val naverMapState: NaverMapState = NaverMapState(),
    val listState: ListState = ListState(),
    val popUpState: PopUpState = PopUpState(),
    val bottomSheetState: BottomSheetState = BottomSheetState(),
    val floatingButtonState: FloatingButtonState = FloatingButtonState(),
    val isLoading: Boolean = false,
    val error: String? = null
)