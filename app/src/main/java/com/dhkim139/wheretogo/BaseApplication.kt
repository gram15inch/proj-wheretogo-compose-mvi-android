package com.dhkim139.wheretogo

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dhkim139.wheretogo.woker.JourneyUpdateWorker
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication:Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() =  Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

        //setWorkManager()
    }


    private fun setWorkManager(){
        val workRequest = OneTimeWorkRequestBuilder<JourneyUpdateWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}