package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.BuildConfig
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.NaverMapApiService
import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.data.name

import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RouteRemoteDatasourceImpl @Inject constructor(
    private val naverApiService: NaverMapApiService,
) : RouteRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val courseRootCollection = FireStoreCollections.COURSE.name()
    private val routeCollection = FireStoreCollections.ROUTE.name
    private val routeDocument = FireStoreCollections.ROUTE.name

    override suspend fun getRouteInCourse(courseId: String): RemoteRoute {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(courseRootCollection).document(courseId)
                .collection(routeCollection).document(routeDocument)
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
            firestore.collection(courseRootCollection).document(remoteRoute.courseId)
                .collection(routeCollection).document(routeDocument)
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
            firestore.collection(courseRootCollection).document(courseId)
                .collection(routeCollection).document(routeDocument)
                .delete()
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    private fun convertLatLng(latlng: DataLatLng): String = "${latlng.longitude}, ${latlng.latitude}"
    private fun convertWaypoints(waypoints: List<DataLatLng>): String {
        var str = ""
        waypoints.forEach {
            str += convertLatLng(it) + "|"
        }
        return str
    }

    override suspend fun getRouteByNaver(waypoints: List<DataLatLng>): RemoteRoute {
        return if (waypoints.size >= 2) {
            val msg = naverApiService.getRouteWayPoint(
                BuildConfig.NAVER_MAPS_APIGW_CLIENT_ID_KEY,
                BuildConfig.NAVER_MAPS_APIGW_CLIENT_SECRET_KEY,
                start = convertLatLng(waypoints.first()),
                goal = convertLatLng(waypoints.last()),
                waypoints = convertWaypoints(waypoints.drop(1).dropLast(1))
            )

            if (msg.body()?.currentDateTime != null) {
                val points = msg.body()!!.route.traoptimal.map { it.path }.first()
                    .map { DataLatLng(it[1], it[0]) }
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
}