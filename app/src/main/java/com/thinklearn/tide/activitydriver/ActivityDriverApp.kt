package com.thinklearn.tide.activitydriver

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class ActivityDriverApp: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}