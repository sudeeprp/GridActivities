package com.thinklearn.tide.interactor

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object EnvironmentalContext {
    var dbConnectionStatusMsg: String = ""
    var dataStatusMsg: String = ""

    var dbOnlineStatus: String = ""
    fun listenToOnlining(refresher: ()->Unit) {
        val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectedRef.addValueEventListener( object: ValueEventListener {
            override fun onDataChange(onlineSnapshot: DataSnapshot) {
                val isOnline = onlineSnapshot.getValue(Boolean::class.java)
                if(isOnline == true) {
                    dbOnlineStatus = "(Online)"
                } else {
                    dbOnlineStatus = "(Offline)"
                }
                refresher()
            }
            override fun onCancelled(p0: DatabaseError) {
                dbOnlineStatus = "(Unknown)"
            }
        })
    }
}
