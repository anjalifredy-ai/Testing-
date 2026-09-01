package com.rikky.blankct

import android.app.Application
import com.google.firebase.FirebaseApp

class BlankCTApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
