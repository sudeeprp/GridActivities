package com.thinklearn.tide.activitydriver

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.thinklearn.tide.interactor.*
import java.io.IOException


class SettingsUtilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_util)

        val refresh_status = findViewById<ImageButton>(R.id.refresh_status)
        refresh_status.setOnClickListener { refreshStatus() }

        EnvironmentalContext.listenToOnlining(this, { dataConnectionUpdate() })
        findViewById<Button>(R.id.export_data).setOnClickListener {
            exportData()
        }
        refreshStatus()
    }
    fun refreshStatus() {
        contentVersionUpdate()
        internetConnectionUpdate()
        dataConnectionUpdate()
    }
    fun contentVersionUpdate() {
        val content_version = findViewById<TextView>(R.id.content_version)
        if(content_version != null) {
            content_version.text = ContentInteractor().get_content_version()
        }
    }
    fun dataConnectionUpdate() {
        val dataConnectionStatus = findViewById<TextView>(R.id.data_connection_status)
        if(dataConnectionStatus != null) {
            dataConnectionStatus.text = EnvironmentalContext.dbOnlineStatus
        }
    }
    fun internetConnectionUpdate() {
        val internetConnectionStatus = findViewById<TextView>(R.id.internet_connection_status)
        if(internetConnectionStatus != null) {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            if(activeNetwork?.isConnected == true) {
                internetConnectionStatus.setText(R.string.is_online)
            }
            else {
                internetConnectionStatus.setText(R.string.offline)
            }
        }
    }
    fun exportData() {
        try {
            val exportedPath = ClassroomDataExchange.exportClassroomData()
            Toast.makeText(this, resources.getString(R.string.export_success) + exportedPath, Toast.LENGTH_LONG).show()
        } catch(e: IOException) {
            Toast.makeText(this, resources.getString(R.string.export_failed) + e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
}
