package com.dhkim139.feature.navigationoption.ui

import com.dhkim139.feature.navigationoption.EndpointRole
import com.dhkim139.feature.navigationoption.FocusTarget
import com.dhkim139.feature.navigationoption.MapApp
import com.dhkim139.feature.navigationoption.NavOption
import com.dhkim139.feature.navigationoption.NavOptionResult

internal sealed interface NavOptionScreenState {

    data object Loading : NavOptionScreenState

    data class Error(val message: UiText) : NavOptionScreenState

    data class Ready(val content: SheetContent) : NavOptionScreenState
}

internal sealed interface NavOptionIntent {

    data class Load(val option: NavOption) : NavOptionIntent

    data object Retry : NavOptionIntent

    data object ToggleMyLocation : NavOptionIntent

    data class EndpointClicked(val role: EndpointRole) : NavOptionIntent

    data object ClosePicker : NavOptionIntent

    data class SelectLot(val lotId: String) : NavOptionIntent

    data object ClearParking : NavOptionIntent

    data object OpenMapAppPicker : NavOptionIntent

    data object CloseMapAppPicker : NavOptionIntent

    data class SelectMapApp(val app: MapApp) : NavOptionIntent

    data object Confirm : NavOptionIntent
}

internal sealed interface NavOptionEffect {

    data class Focus(val focusTarget: FocusTarget) : NavOptionEffect

    data class StartNavigation(val result: NavOptionResult) : NavOptionEffect
}

