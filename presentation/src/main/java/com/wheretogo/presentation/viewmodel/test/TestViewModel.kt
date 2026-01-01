package com.wheretogo.presentation.viewmodel.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.map.clustering.ClusterMarkerInfo
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.clustering.ClusteringKey
import com.naver.maps.map.clustering.DefaultClusterMarkerUpdater
import com.naver.maps.map.clustering.DefaultLeafMarkerUpdater
import com.naver.maps.map.clustering.DefaultMarkerManager
import com.naver.maps.map.clustering.LeafMarkerInfo
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.LIST_ITEM_ZOOM
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.dummy.guideCheckPoint
import com.wheretogo.domain.model.dummy.guideCourse
import com.wheretogo.domain.usecase.app.GuideMoveStepUseCase
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.presentation.CameraUpdateSource
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.MoveAnimation
import com.wheretogo.presentation.feature.map.DriveMapOverlayService
import com.wheretogo.presentation.feature.naver.NaverMapOverlayModifier
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.CameraState
import com.wheretogo.presentation.state.test.TestState
import com.wheretogo.presentation.toNaver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val overlayService: DriveMapOverlayService,
    private val creater: NaverMapOverlayModifier,
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
        MapOverlay.MarkerContainer(
            it.contentId,
            it.type,
            creater.createMarker(it)
        )
    }

    init {
        viewModelScope.launch {
            state.update {
                it.copy(
 overlays =  overlays,
                    clustererGroup = createComplex(overlays),

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
                                                cameraState = camera
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
                    cameraState = cameraState.copy(
                        updateSource = CameraUpdateSource.USER,
                        zoom = cameraState.zoom
                    )
                )
            )
        }
    }

    fun createClusterer(overlays : List<MapOverlay>):List<Clusterer<ItemKey>>{
        val clusterer = Clusterer.Builder<ItemKey>()
        val markers= overlays
            .mapNotNull {
                if(it is MapOverlay.MarkerContainer && it.type == MarkerType.CHECKPOINT) {
                    val icon = it.marker.coreMarker?.icon?:return@mapNotNull null
                    val position = it.marker.markerInfo.position?.toNaver()?:return@mapNotNull null
                    ItemKey(
                        it.contentId,
                        position,
                        icon
                    ) to null
                }
                else null
            }.toMap()

        clusterer.clusterMarkerUpdater(object : DefaultClusterMarkerUpdater() {
            override fun updateClusterMarker(info: ClusterMarkerInfo, marker: Marker) {
                super.updateClusterMarker(info, marker)
                marker.icon = if (info.size < 3) {
                    MarkerIcons.CLUSTER_LOW_DENSITY
                } else {
                    MarkerIcons.CLUSTER_MEDIUM_DENSITY
                }
            }
        }).leafMarkerUpdater(object : DefaultLeafMarkerUpdater() {
            override fun updateLeafMarker(info: LeafMarkerInfo, marker: Marker) {
                super.updateLeafMarker(info, marker)

                val key = info.key as ItemKey
                marker.icon = key.oi

            }
        })
        val result = clusterer.build()
        result.addAll(markers)
        return listOf(result)
    }


    fun createComplex(overlays : List<MapOverlay>):List<Clusterer<ItemKey>>{
        val builder= Clusterer.ComplexBuilder<ItemKey>()

        val markers= overlays
            .mapNotNull {
                if(it is MapOverlay.MarkerContainer && it.type == MarkerType.CHECKPOINT) {
                    it
                }
                else null
            }
        val keys = markers.mapNotNull{
            val icon = it.marker.coreMarker?.icon?:return@mapNotNull null
            val position = it.marker.markerInfo.position?.toNaver()?:return@mapNotNull null

            ItemKey(
                it.contentId,
                position,
                icon
            ) to null
        }.toMap()
        builder.markerManager(object : DefaultMarkerManager() {
            override fun createMarker() = super.createMarker().apply {
                checkMarkerPointGroup.add(this)
            }
        })
        builder.clusterMarkerUpdater(object : DefaultClusterMarkerUpdater() {
            override fun updateClusterMarker(info: ClusterMarkerInfo, marker: Marker) {
                super.updateClusterMarker(info, marker)
                marker.icon = if (info.size < 3) {
                    MarkerIcons.CLUSTER_LOW_DENSITY
                } else {
                    MarkerIcons.CLUSTER_MEDIUM_DENSITY
                }
            }
        }).leafMarkerUpdater(object : DefaultLeafMarkerUpdater() {
            override fun updateLeafMarker(info: LeafMarkerInfo, marker: Marker) {
                super.updateLeafMarker(info, marker)

                val key = info.key as ItemKey
                marker.icon = key.oi

            }
        })
        val result = builder.build()
        result.addAll(keys)

        return listOf(result)
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
                            cameraState = it.naverState.cameraState.copy(
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

