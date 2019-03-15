package com.thinklearn.tide.activitydriver

import android.app.Application
import android.os.Build
import com.google.firebase.database.FirebaseDatabase
import android.os.StrictMode



class ActivityDriverApp: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        //Nougat onwards: A file:// url will not be handed over to the pdf viewer.
        //We set the default policy over here to enable this
        //https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
    }
}