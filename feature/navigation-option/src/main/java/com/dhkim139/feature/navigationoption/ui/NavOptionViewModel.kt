package com.dhkim139.feature.navigationoption.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim139.feature.navigationoption.CourseDirection
import com.dhkim139.feature.navigationoption.EndpointRole
import com.dhkim139.feature.navigationoption.FocusTarget
import com.dhkim139.feature.navigationoption.MapApp
import com.dhkim139.feature.navigationoption.NavOption
import com.dhkim139.feature.navigationoption.domain.GetCourseParkingUseCase
import com.dhkim139.feature.navigationoption.domain.GetMapAppIdUseCase
import com.dhkim139.feature.navigationoption.domain.SaveMapAppIdUseCase
import com.wheretogo.domain.model.course.StartDirection
import com.wheretogo.domain.usecase.course.GetCourseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
internal class NavOptionViewModel @Inject constructor(
    private val getCourse: GetCourseUseCase,
    private val getCourseParking: GetCourseParkingUseCase,
    private val getMapAppId: GetMapAppIdUseCase,
    private val saveMapAppId: SaveMapAppIdUseCase,
) : ViewModel() {
    private var option: NavOption? = null
    private var course: NavCourse? = null

    private var parking: CourseParking? = null

    private var selection: SheetSelection? = null

    private val _state = MutableStateFlow<NavOptionScreenState>(NavOptionScreenState.Loading)
    val state: StateFlow<NavOptionScreenState> = _state.asStateFlow()

    private val _effect = Channel<NavOptionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: NavOptionIntent) {
        when (intent) {
            is NavOptionIntent.Load -> load(intent.option)
            NavOptionIntent.Retry -> loadCourse()
            NavOptionIntent.ToggleMyLocation -> toggleMyLocation()
            is NavOptionIntent.EndpointClicked -> openParkingPicker(intent.role)
            NavOptionIntent.ClosePicker -> moveTo(SheetTarget.Overview)
            is NavOptionIntent.SelectLot -> selectLot(intent.lotId)
            NavOptionIntent.ClearParking -> clearParking()
            NavOptionIntent.OpenMapAppPicker -> moveTo(SheetTarget.MapAppPicker)
            NavOptionIntent.CloseMapAppPicker -> moveTo(SheetTarget.Overview)
            is NavOptionIntent.SelectMapApp -> selectMapApp(intent.app)
            NavOptionIntent.Confirm -> confirm()
        }
    }

    private fun load(navOption: NavOption) {
        if (navOption == this.option) {
            return
        }
        initState()
        this.option = navOption
        loadCourse()

    }

    private fun loadCourse() {
        data class Loaded(
            val course: NavCourse,
            val mapApp: MapApp
        )

        val id = option?.courseId ?: return
        course = null
        selection = null
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val direction =
                        if (option?.direction == CourseDirection.FORWARD) StartDirection.FORWARD else StartDirection.REVERSE
                    val course = async { getCourse(id).getOrThrow().toNav(direction)!! }
                    val mapApp = async { MapApp.fromIdOrDefault(getMapAppId()) }
                    Loaded(
                        course = course.await(),
                        mapApp = mapApp.await(),
                    )
                }
            }.onSuccess {
                course = it.course
                select(SheetSelection(mapApp = it.mapApp))
            }.onFailure {
                _state.value = NavOptionScreenState.Error(errorTextFor(it))
            }
        }
    }


    private fun loadParking() {
        selection ?: return
        val content = _state.asReadyContent() as? SheetContent.ParkingPicker ?: return
        _state.updateReadyContent<SheetContent.ParkingPicker> { it.copy(loading = true) }
        viewModelScope.launch {
            runCatching {
                val minDuration = async { delay(500.milliseconds) }
                val parking = withContext(Dispatchers.IO) {
                    val o = option ?: throw IllegalStateException("option not found")
                    val p = parking?: getCourseParking(o.courseId, o.direction).getOrThrow()
                    p.also { parking = it }
                }
                minDuration.await()
                selection?.runAutoSelection(content.role, parking)?:return@launch
            }.onSuccess {
                select(it)
            }.onFailure { e ->
                _state.update { NavOptionScreenState.Error(errorTextFor(e)) }
            }
        }
    }

    private fun openParkingPicker(role: EndpointRole) {
        moveTo(SheetTarget.ParkingPicker(role = role))
        loadParking()
    }

    private fun SheetSelection.runAutoSelection(
        role: EndpointRole,
        parking: CourseParking,
    ): SheetSelection {
        // 보여줄 주차장 필터링
        val visible = UiRules.visibleParking(parking.candidateOf(role))

        // 기존의 선택지에 주차장이 있으면 사용
        val currentLotId = selectEndpoint(role).lotId
        val autoSelected = currentLotId?.let { id -> visible.firstOrNull { it.lot.id == id } }
            ?: visible.firstOrNull()
            ?: return updateSelection(EndpointChoice.none(role))

        emit(NavOptionEffect.Focus(FocusTarget(role = role, latLng = autoSelected.lot.position)))
        return updateSelection(autoSelected.toEndpoint(role))
    }

    private fun selectLot(lotId: String) {
        parking ?: return
        val state = _state.value
        if (state !is NavOptionScreenState.Ready) return
        val picker = state.content
        if (state.content !is SheetContent.ParkingPicker) return

        val current = selection ?: return
        val candidate = parking!!.candidateOf(picker.role).find { it.lot.id == lotId } ?: return
        val next = candidate.toEndpoint(picker.role)
        emit(NavOptionEffect.Focus(FocusTarget(role = picker.role, latLng = candidate.lot.position)))
        select(current.updateSelection(next))

    }

    private fun clearParking() {
        val mode = _state.asReadyContent() as? SheetContent.ParkingPicker ?: return
        val current = selection ?: return
        selection = current.updateSelection(EndpointChoice.none(mode.role))
        moveTo(SheetTarget.Overview)
    }

    private fun selectMapApp(app: MapApp) {
        val current = selection ?: return
        selection = current.copy(mapApp = app)
        moveTo(SheetTarget.Overview)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { saveMapAppId(app.id) }
        }
    }

    private fun toggleMyLocation() {
        val current = selection ?: return
        select(current.copy(useMyLocation = !current.useMyLocation))
    }

    private fun confirm() {
        val current = selection ?: return
        val loaded = course ?: return
        emit(NavOptionEffect.StartNavigation(current.toResult(loaded)))
    }

    private fun select(next: SheetSelection) {
        selection = next
        val content = createContent(next)
        render(content)
    }

    private fun moveTo(next: SheetTarget) {
        val content = createContent(next) ?: return
        render(content)
    }

    private fun render(content: SheetContent) {
        _state.value = NavOptionScreenState.Ready(content = content)
    }

    private fun createContent(target: SheetTarget): SheetContent? {
        val s = selection ?: SheetSelection().also { selection = it }
        val c = course ?: return null
        val p = parking ?: CourseParking.EMPTY
        return s.createSheetContent(course = c, parking = p, target = target)
    }

    private fun createContent(s: SheetSelection): SheetContent {
        val c = course ?: throw IllegalStateException("course not found")
        val p = parking ?: CourseParking.EMPTY
        val content = _state.asReadyContent()
        return s.createSheetContent(course = c, parking = p, content = content)
    }

    private fun emit(e: NavOptionEffect) {
        viewModelScope.launch { _effect.send(e) }
    }

    private fun MutableStateFlow<NavOptionScreenState>.asReadyContent(): SheetContent? {
        return (_state.value as? NavOptionScreenState.Ready)?.content
    }

    private inline fun <reified T : SheetContent> MutableStateFlow<NavOptionScreenState>.updateReadyContent(
        callback: (T) -> T
    ) {
        update { state ->
            if (state is NavOptionScreenState.Ready && state.content is T) {
                state.copy(content = callback(state.content))
            } else {
                state
            }
        }
    }

    private fun initState() {
        option = null
        course = null
        parking = null
        selection = null
        _state.value = NavOptionScreenState.Loading
    }
}