package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.DataError
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.NaverMapApiService
import com.wheretogo.data.feature.dataErrorCatching
import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.data.toDataError
import com.wheretogo.domain.model.app.AppBuildConfig

import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RouteRemoteDatasourceImpl @Inject constructor(
    private val naverApiService: NaverMapApiService,
    private val appBuildConfig: AppBuildConfig
) : RouteRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val courseRootCollection = appBuildConfig.dbPrefix + FireStoreCollections.COURSE.name
    private val routeCollection = FireStoreCollections.ROUTE.name
    private val routeDocument = FireStoreCollections.ROUTE.name

    override suspend fun getRouteInCourse(courseId: String): Result<RemoteRoute> {
        return dataErrorCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(courseRootCollection).document(courseId)
                    .collection(routeCollection).document(routeDocument)
                    .get()
                    .addOnSuccessListener {
                        continuation.resume(it)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
            snapshot.toObject(RemoteRoute::class.java) ?: RemoteRoute(courseId = courseId)
        }
    }

    override suspend fun setRouteInCourse(remoteRoute: RemoteRoute): Result<Unit> {
        return dataErrorCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(courseRootCollection).document(remoteRoute.courseId)
                    .collection(routeCollection).document(routeDocument)
                    .set(remoteRoute)
                    .addOnSuccessListener {
                        continuation.resume(Unit)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
        }
    }


    override suspend fun removeRouteInCourse(courseId: String): Result<Unit> {
        return dataErrorCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(courseRootCollection).document(courseId)
                    .collection(routeCollection).document(routeDocument)
                    .delete()
                    .addOnSuccessListener {
                        continuation.resume(Unit)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
        }
    }

    private fun convertLatLng(latlng: DataLatLng): String =
        "${latlng.longitude}, ${latlng.latitude}"

    private fun convertWaypoints(waypoints: List<DataLatLng>): String {
        var str = ""
        waypoints.forEach {
            str += convertLatLng(it) + "|"
        }
        return str
    }

    override suspend fun getRouteByNaver(waypoints: List<DataLatLng>): Result<RemoteRoute> {
        return dataErrorCatching {
            if (waypoints.size < 2)
                return Result.failure(DataError.ArgumentInvalid())


            val response = naverApiService.getRouteWayPoint(
                appBuildConfig.naverMapsApigwClientIdKey,
                appBuildConfig.naverMapsApigwClientSecretkey,
                start = convertLatLng(waypoints.first()),
                goal = convertLatLng(waypoints.last()),
                waypoints = convertWaypoints(waypoints.drop(1).dropLast(1))
            )


            if (!response.isSuccessful)
                return Result.failure(response.toDataError())

            val points = response.body()!!.route.traoptimal.map { it.path }.first()
                .map { DataLatLng(it[1], it[0]) }
            val duration = response.body()!!.route.traoptimal.first().summary.duration
            val distance = response.body()!!.route.traoptimal.first().summary.distance
            RemoteRoute(
                duration = duration,
                distance = distance,
                points = points
            )
        }
    }
}