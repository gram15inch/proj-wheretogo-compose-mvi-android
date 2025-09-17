package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.ReportRemoteDatasource
import com.wheretogo.data.feature.mapDataError
import com.wheretogo.data.model.report.RemoteReport
import com.wheretogo.data.name
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ReportRemoteDatasourceImpl @Inject constructor() : ReportRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val reportRootCollection = FireStoreCollections.REPORT.name()
    override suspend fun addReport(report: RemoteReport): Result<Unit> {
        return runCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(reportRootCollection).document(report.reportId)
                    .set(report).addOnSuccessListener {
                        continuation.resume(Unit)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
        }.mapDataError()
    }

    override suspend fun getReport(reportId: String): Result<RemoteReport> {
        return runCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(reportRootCollection).document(reportId).get()
                    .addOnSuccessListener {
                        continuation.resume(it)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
            snapshot.toObject(RemoteReport::class.java)
        }.mapDataError()
    }

    override suspend fun removeReport(reportId: String): Result<Unit> {
        return runCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(reportRootCollection).document(reportId).delete()
                    .addOnSuccessListener {
                        continuation.resume(Unit)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
        }.mapDataError()
    }

    override suspend fun getReportByType(reportType: String): Result<List<RemoteReport>> {
        return runCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(reportRootCollection)
                    .whereEqualTo(RemoteReport::type.name, reportType)
                    .limit(10).get()
                    .addOnSuccessListener {
                        continuation.resume(it)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
            snapshot.map { it.toObject(RemoteReport::class.java) }
        }.mapDataError()
    }

    override suspend fun getReportByStatus(reportStatus: String): Result<List<RemoteReport>> {
        return runCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(reportRootCollection)
                    .whereEqualTo(RemoteReport::status.name, reportStatus)
                    .limit(10).get()
                    .addOnSuccessListener {
                        continuation.resume(it)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
            snapshot.map { it.toObject(RemoteReport::class.java) }
        }.mapDataError()
    }

    override suspend fun getReportByUid(userId: String): Result<List<RemoteReport>> {
        return runCatching {
            val snapshot = suspendCancellableCoroutine { continuation ->
                firestore.collection(reportRootCollection)
                    .whereEqualTo(RemoteReport::userId.name, userId)
                    .limit(10).get()
                    .addOnSuccessListener {
                        continuation.resume(it)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
            snapshot.map { it.toObject(RemoteReport::class.java) }
        }.mapDataError()
    }
}