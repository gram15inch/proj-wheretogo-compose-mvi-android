package com.dhkim139.admin.wheretogo.core

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AdminApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        firebaseInit()
    }

    private fun firebaseInit() {
        FirebaseApp.initializeApp(this@AdminApplication)
        FirebaseFirestore.getInstance()
        FirebaseMessaging.getInstance().subscribeToTopic("admin")
    }
}


