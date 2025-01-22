package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.BuildConfig
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.NaverMapApiService
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.data.name
import com.wheretogo.domain.model.map.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RouteRemoteDatasourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val naverApiService: NaverMapApiService,
) : RouteRemoteDatasource {
    private val courseTable = FireStoreCollections.COURSE.name()
    private val routeTable = FireStoreCollections.ROUTE.name()

    override suspend fun getRouteInCourse(courseId: String): RemoteRoute {
        return suspendCancellableCoroutine { continuation ->
            val routeId = getRouteId(courseId)
            firestore.collection(courseTable).document(courseId)
                .collection(routeTable).document(routeId)
                .get()
                .addOnSuccessListener {
                    val route =
                        it.toObject(RemoteRoute::class.java) ?: RemoteRoute(courseId = courseId)
                    continuation.resume(route)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun setRouteInCourse(remoteRoute: RemoteRoute): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val routeId = getRouteId(remoteRoute.courseId)
            firestore.collection(courseTable).document(remoteRoute.courseId)
                .collection(routeTable).document(routeId)
                .set(remoteRoute)
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resume(false)
                }
        }
    }


    override suspend fun removeRouteInCourse(courseId: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val routeId = getRouteId(courseId)
            firestore.collection(courseTable).document(courseId)
                .collection(routeTable).document(routeId)
                .delete()
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resume(false)
                }
        }
    }

    private fun getRouteId(courseId: String): String {
        return "${courseId}_route"
    }

    private fun convertLatLng(latlng: LatLng): String = "${latlng.longitude}, ${latlng.latitude}"
    private fun convertWaypoints(waypoints: List<LatLng>): String {
        var str = ""
        waypoints.forEach {
            str += convertLatLng(it) + "|"
        }
        return str
    }

    override suspend fun getRouteByNaver(waypoints: List<LatLng>): RemoteRoute {
        return if (waypoints.size >= 2) {
            val msg = naverApiService.getRouteWayPoint(
                BuildConfig.NAVER_CLIENT_ID_KEY,
                BuildConfig.NAVER_CLIENT_SECRET_KEY,
                start = convertLatLng(waypoints.first()),
                goal = convertLatLng(waypoints.last()),
                waypoints = convertWaypoints(waypoints.drop(1).dropLast(1))
            )

            if (msg.body()?.currentDateTime != null) {
                val points = msg.body()!!.route.traoptimal.map { it.path }.first()
                    .map { LatLng(it[1], it[0]) }
                val duration = msg.body()!!.route.traoptimal.first().summary.duration
                val distance = msg.body()!!.route.traoptimal.first().summary.distance
                return RemoteRoute(
                    duration = duration,
                    distance = distance,
                    points = points
                )
            } else {
                return RemoteRoute()
            }
        } else
            RemoteRoute()
    }

    override suspend fun getAddress(latlng: LatLng): String {
        val msg = naverApiService.getAddress(
            clientId = BuildConfig.NAVER_CLIENT_ID_KEY,
            clientSecret = BuildConfig.NAVER_CLIENT_SECRET_KEY,
            coords = convertLatLng(latlng),
            output = "json"
        )

        if (msg.code() == 200) {
            val region = msg.body()?.results?.firstOrNull()?.region
            val addr =
                region?.run { "${area1.name} ${area2.name} ${area3.name} ${area4.name}" } ?: ""
            return addr
        } else {
            return ""
        }
    }
}