package com.thinklearn.tide.interactor

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thinklearn.tide.activitydriver.R
import java.net.NetworkInterface
import java.util.*


object EnvironmentalContext {
    var dbConnectionStatusMsg: String = ""
    var dataStatusMsg: String = ""

    var dbOnlineStatus: String = ""
    fun listenToOnlining(resourcer: Context, refresher: ()->Unit) {
        val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectedRef.addValueEventListener( object: ValueEventListener {
            override fun onDataChange(onlineSnapshot: DataSnapshot) {
                val isOnline = onlineSnapshot.getValue(Boolean::class.java)
                if(isOnline == true) {
                    dbOnlineStatus = resourcer.resources.getString(R.string.is_online)
                } else {
                    dbOnlineStatus = resourcer.resources.getString(R.string.offline)
                }
                refresher()
            }
            override fun onCancelled(p0: DatabaseError) {
                dbOnlineStatus = "(Unknown)"
            }
        })
    }
    fun getDeviceIdentity(): String {
        val allConnections = Collections.list(NetworkInterface.getNetworkInterfaces())
        var macID = StringBuilder()
        for (nif in allConnections) {
            if (nif.getName().toLowerCase() == "wlan0") {
                val macBytes = nif.getHardwareAddress() ?: return ""
                for (b in macBytes) {
                    macID.append(String.format("%02X", b))
                }
            }
        }
        return macID.toString()
    }
}
