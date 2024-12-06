package com.wheretogo.data.datasourceimpl

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.BuildConfig
import com.wheretogo.data.FireStoreTableName
import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.NaverMapApiService
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.model.map.DataLatLng
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
    private val courseTable = FireStoreTableName.COURSE_TABLE.name()
    private val routeTable = FireStoreTableName.ROUTE_TABLE.name()

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

    override fun getRouteId(courseId: String): String {
        return "${courseId}_route"
    }

    override suspend fun getRouteByNaver(waypoints: List<LatLng>): List<LatLng> {
        return if (waypoints.size >= 2) {
            val msg = naverApiService.getRouteWayPoint(
                BuildConfig.NAVER_CLIENT_ID_KEY,
                BuildConfig.NAVER_CLIENT_SECRET_KEY,
                start = convertLatLng(waypoints.first()),
                goal = convertLatLng(waypoints.last()),
                waypoints = convertWaypoints(waypoints.drop(1).dropLast(1))
            )

            if (msg.body()?.currentDateTime != null) {
                val r = msg.body()!!.route.traoptimal.map { it.path }.first()
                    .map { LatLng(it[1], it[0]) }
                Log.i("tst5", "${msg}")
                Log.i("tst5", "${msg.body()}")
                return r
            } else {
                Log.d("tst", "${msg}")
                Log.d("tst", "${msg.body()}")
                return emptyList()
            }
        } else
            emptyList()

    }

    private fun convertLatLng(latlng: LatLng): String = "${latlng.longitude}, ${latlng.latitude}"
    private fun convertWaypoints(waypoints: List<LatLng>): String {
        var str = ""
        waypoints.forEach {
            str += convertLatLng(it) + "|"
        }
        return str
    }

    data class LatLngGeo(val latitude: Double, val longitude: Double, val geohash: String)

    suspend fun setGeoTest(): Boolean {
        val courseId = "cs1"
        /*   val points = getRouteInCourse(courseId).points.map {
               LatLngGeo(
                   it.latitude, it.longitude,
                   GeoFireUtils.getGeoHashForLocation(GeoLocation(it.latitude, it.longitude))
               )
           }*/

        fun getGeoHase(num: Int): String {
            val random = (0..9).random()
            return when (num) {
                0 -> {
                    "wyd7u$random"
                }

                1 -> {
                    "wydfu$random"
                }

                else -> {
                    "wyg7u$random"
                }
            }
        }

        val list = mutableListOf<RemoteCourse>()
        (1..100).forEach {
            list.add(
                RemoteCourse(
                    courseId = "cs$it",
                    geoHash = getGeoHase(it % 3)
                )
            )
        }


        val points = list

        val isNotEmpty = points.isNotEmpty()
        val batch = firestore.batch()
        points.forEach {
            val doc = firestore.collection("TEST_POINT_GEO2_TABLE").document()
            batch.set(doc, it)
        }
        return isNotEmpty && suspendCancellableCoroutine { continuation ->
            batch.commit().addOnSuccessListener {
                continuation.resume(true)
            }.addOnFailureListener {
                continuation.resume(false)
            }
        }
    }

    suspend fun getGeoTest(meter: Double): Int {
        val lat = 37.2560577
        val lng = 127.0940143

        val start = "wyd7um"
        val end = start + "\uf8ff"
        /* val query = GeoFireUtils.getGeoHashQueryBounds(GeoLocation(lat,lng),meter)
         Log.d("tst5","${query[0].startHash}/${query[0].endHash}")*/
        return suspendCancellableCoroutine { continuation ->
            firestore.collection("TEST_POINT_GEO_TABLE")
                .whereGreaterThanOrEqualTo("geohash", start)
                .whereLessThanOrEqualTo("geohash", end)
                .get().addOnSuccessListener {
                    val data = it.toObjects(DataLatLng::class.java)
                    Log.d("tst5", "size: ${data.size} $data")
                    continuation.resume(data.size)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    suspend fun getTest(): Int {
        val lat = 37.2560577
        val lng = 127.0940143

        return suspendCancellableCoroutine { continuation ->
            firestore.collection("TEST_POINT_TABLE")
                .whereGreaterThanOrEqualTo("latitude", lat)
                .whereLessThanOrEqualTo("latitude", lat + 0.001)
                .whereGreaterThanOrEqualTo("longitude", lng - 0.001)
                .whereLessThanOrEqualTo("longitude", lng)
                .get().addOnSuccessListener {
                    val data = it.toObjects(DataLatLng::class.java)
                    Log.d("tst5", "size: ${data.size} $data")
                    continuation.resume(data.size)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }


}