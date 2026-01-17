package com.wheretogo.presentation.viewmodel.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.map.clustering.ClusteringKey
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.LIST_ITEM_ZOOM
import com.wheretogo.domain.MarkerType
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.dummy.guideCheckPoint
import com.wheretogo.domain.model.dummy.guideCourse
import com.wheretogo.domain.usecase.app.GuideMoveStepUseCase
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.MoveAnimation
import com.wheretogo.presentation.OverlayType
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.test.TestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val guideMoveStepUseCase: GuideMoveStepUseCase,
) : ViewModel() {
    val state = MutableStateFlow(TestState())
    val checkMarkerPointGroup = mutableListOf<Marker>()
    val searchBarItemGroup = listOf(
        SearchBarItem(
            guideCourse.courseName,
            "addr1",
            isHighlight = true,
            latlng = guideCourse.cameraLatLng
        ),
        SearchBarItem(
            "ccpcpcpcp",
            "addr2",
            latlng = guideCheckPoint.latLng
        )
    )
   val infos = listOf(
       MarkerInfo(
           contentId = "1",
           position = LatLng(
               latitude = 37.56443627807609, longitude = 126.97401841609955
           ),
           caption = "기본 화질",
           type = MarkerType.CHECKPOINT,
           iconPath = "/data/data/com.dhkim139.wheretogo.debug/image/small/IM01K9JH9E02GMTGQ7P5PNWPX2EQ.webp"

       ),
        MarkerInfo(
            contentId = "2",
            position = LatLng(
                latitude=37.56395549514234, longitude=126.9795471639889
            ),
            caption = "더 큰 화질",
            type= MarkerType.CHECKPOINT,
            iconPath = "/data/data/com.dhkim139.wheretogo.debug/image/small/IM01K9JH9V13S53BBDKY8XG4KY54.webp"
        )
    )
    val overlays = infos.map{
        AppMarker(
            it.contentId,
            OverlayType.LEAF_MARKER,
            it
        )
    }

    init {
        viewModelScope.launch {
            state.update {
                it.copy(
                    overlays =  overlays,
                    //clustererGroup = createComplex(overlays),

                    searchBarState = it.searchBarState.copy(
                        searchBarItemGroup = searchBarItemGroup
                    )
                )

            }
            //guideMoveStepUseCase(true)
            launch {
                observeSettingsUseCase().collect {
                    it.onSuccess { setting ->
                        state.update { old ->
                            old.copy(
                                step = setting.tutorialStep
                            ).run {
                                when (setting.tutorialStep) {
                                    DriveTutorialStep.SKIP -> {
                                        val camera = CameraState(
                                            latLng = guideCourse.cameraLatLng,
                                            zoom = LIST_ITEM_ZOOM,
                                            updateSource = CameraUpdateSource.GUIDE
                                        )
                                        this.copy(
                                            naverState = naverState.copy(
                                                requestCameraState = camera
                                            )
                                        )
                                    }

                                    else -> {
                                        this
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun cameraUpdated(cameraState: CameraState){
        state.update {
            it.copy(
                naverState = it.naverState.copy(
                    latestCameraState = cameraState.copy(
                        updateSource = CameraUpdateSource.USER,
                        zoom = cameraState.zoom
                    )
                )
            )
        }
    }

    fun markerClick(){
        viewModelScope.launch {
            guideMoveStepUseCase(true)
        }
    }

    fun searchBarItemClick(item: SearchBarItem){
        item.latlng?.let { latlng->
            state.update {
                it.run {
                    copy(
                        naverState = naverState.copy(
                            latestCameraState = it.naverState.latestCameraState.copy(
                                latLng = latlng,
                                zoom = LIST_ITEM_ZOOM,
                                updateSource = CameraUpdateSource.SEARCH_BAR,
                                moveAnimation = MoveAnimation.APP_EASING
                            )
                        )
                    )
                }
            }
        }

    }
}



class ItemKey(val id: String, private val position: com.naver.maps.geometry.LatLng,val oi: OverlayImage) : ClusteringKey {
    override fun getPosition(): com.naver.maps.geometry.LatLng {
        return position
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val itemKey = other as ItemKey
        return id == itemKey.id
    }

    override fun hashCode() = id.hashCode()

}

