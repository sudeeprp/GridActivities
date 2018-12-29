package com.thinklearn.tide.activitydriver

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.thinklearn.tide.interactor.*
import java.io.File
import java.io.IOException
import android.bluetooth.BluetoothAdapter


class SettingsUtilActivity : AppCompatActivity() {
    var exportFilePath: String = ""

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
            exportFilePath = ClassroomDataExchange.exportClassroomData()
            Toast.makeText(this, resources.getString(R.string.export_success) + exportFilePath, Toast.LENGTH_LONG).show()
            sendViaBluetooth()
        } catch(e: IOException) {
            Toast.makeText(this, resources.getString(R.string.export_failed) + e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
    fun sendViaBluetooth() {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        val DISCOVER_DURATION = 300
        if (btAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show()
        } else {
            val discoveryIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION)
            startActivityForResult(discoveryIntent, 3)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 3) {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(exportFilePath)))

            val appsList = packageManager.queryIntentActivities(intent, 0)

            if (appsList.size > 0) {
                var packageName: String = ""
                var className: String = ""
                var found = false

                for (info in appsList) {
                    packageName = info.activityInfo.packageName
                    if (packageName == "com.android.bluetooth") {
                        className = info.activityInfo.name
                        found = true
                        break
                    }
                }
                if (!found) {
                    Toast.makeText(this, "Bluetooth app isn't found",
                            Toast.LENGTH_LONG).show()
                } else {
                    intent.setClassName(packageName, className)
                    startActivity(intent)
                }
            }
        }
    }
}
