package com.wheretogo.data.datasourceimpl

import com.google.firebase.firestore.FirebaseFirestore
import com.wheretogo.data.FireStoreCollections
import com.wheretogo.data.datasource.ReportRemoteDatasource
import com.wheretogo.data.model.report.RemoteReport
import com.wheretogo.data.name
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ReportRemoteDatasourceImpl @Inject constructor() : ReportRemoteDatasource {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val reportRootCollection = FireStoreCollections.REPORT.name()
    override suspend fun addReport(report: RemoteReport): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(reportRootCollection).document(report.reportId)
                .set(report).addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun getReport(reportId: String): RemoteReport? {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(reportRootCollection).document(reportId).get()
                .addOnSuccessListener {
                    continuation.resume(it.toObject(RemoteReport::class.java))
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun removeReport(reportId: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(reportRootCollection).document(reportId).delete()
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun getReportByType(reportType: String): List<RemoteReport> {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(reportRootCollection).whereEqualTo(RemoteReport::type.name, reportType)
                .limit(10).get()
                .addOnSuccessListener {
                    continuation.resume(it.map { it.toObject(RemoteReport::class.java) })
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun getReportByStatus(reportStatus: String): List<RemoteReport> {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(reportRootCollection).whereEqualTo(RemoteReport::status.name, reportStatus)
                .limit(10).get()
                .addOnSuccessListener {
                    continuation.resume(it.map { it.toObject(RemoteReport::class.java) })
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun getReportByUid(userId: String): List<RemoteReport> {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(reportRootCollection).whereEqualTo(RemoteReport::userId.name, userId)
                .limit(10).get()
                .addOnSuccessListener {
                    continuation.resume(it.map { it.toObject(RemoteReport::class.java) })
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }
}