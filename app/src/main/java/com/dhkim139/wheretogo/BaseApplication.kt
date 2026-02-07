package com.dhkim139.wheretogo

import android.app.Application
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.skt.Tmap.TMapTapi
import com.wheretogo.domain.model.app.AppBuildConfig
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var buildConfig: AppBuildConfig

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        timberInit()
        strictModeInit()
        firebaseInit()
        tMapInit()
    }

    private fun firebaseInit() {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseApp.initializeApp(this@BaseApplication)
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = buildConfig.isCrashlytics
            FirebaseStorage.getInstance()
            FirebaseAuth.getInstance()
            FirebaseFirestore.getInstance()
            FirebaseMessaging.getInstance()
        }
    }

    private fun timberInit(){
        if (BuildConfig.DEBUG) {
            Timber.uprootAll()
            Timber.plant(PrefixedDebugTree())
        } else {
            Timber.uprootAll()
        }
    }

    private fun tMapInit() {
        TMapTapi(this@BaseApplication).apply {
            setSKTMapAuthentication(buildConfig.tmapAppKey)
        }
    }

    private fun strictModeInit() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectNetwork()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )

            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects() // 닫히지 않은 리소스 감지
                    .detectLeakedSqlLiteObjects() // DB Connection 누수 감지
                    .penaltyLog()
                    .build()
            )
        }
    }

    class PrefixedDebugTree : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String {
            return "${super.createStackElementTag(element)}"
        }
    }
}