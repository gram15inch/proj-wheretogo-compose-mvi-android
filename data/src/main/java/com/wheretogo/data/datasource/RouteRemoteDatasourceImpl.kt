package com.wheretogo.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.FireStoreTableName
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.data.name
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RouteRemoteDatasourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RouteRemoteDatasource {
    private val courseTableName = FireStoreTableName.CHECKPOINT_TABLE.name()
    private val routeTable = FireStoreTableName.ROUTE_TABLE.name()

    override suspend fun getRouteInCourse(courseId: String): RemoteRoute {
        return suspendCancellableCoroutine { continuation ->
            val routeId = getRouteId(courseId)
            firestore.collection(courseTableName).document(courseId)
                .collection(routeTable).document(routeId)
                .get()
                .addOnSuccessListener {
                    val route =
                        it.toObject(RemoteRoute::class.java) ?: RemoteRoute(getRouteId(courseId))
                    continuation.resume(route)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun setRouteInCourse(courseId: String, remoteRoute: RemoteRoute): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val routeId = getRouteId(courseId)
            firestore.collection(courseTableName).document(courseId)
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
            firestore.collection(courseTableName).document(courseId)
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
}