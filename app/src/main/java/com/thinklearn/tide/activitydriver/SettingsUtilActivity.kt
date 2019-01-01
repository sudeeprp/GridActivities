package com.thinklearn.tide.activitydriver

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import com.thinklearn.tide.interactor.*
import java.io.File
import java.io.IOException
import android.bluetooth.BluetoothAdapter
import android.os.Environment
import android.widget.*


class SettingsUtilActivity : AppCompatActivity() {
    var exportFilePath: String = ""
    var exportChoices = arrayOf ("To sdcard",            "via Bluetooth")
    var exportRoutines = arrayOf(fun() {sendToSDCard()}, fun() {sendViaBluetooth()})
    var selectedExportChoiceIndex = -1
    val exchange_folder = "LearningGridExchange/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_util)

        val refresh_status = findViewById<ImageButton>(R.id.refresh_status)
        refresh_status.setOnClickListener { refreshStatus() }

        EnvironmentalContext.listenToOnlining(this, { dataConnectionUpdate() })
        setupExportControls()
        refreshStatus()
    }
    fun setupExportControls() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, exportChoices)
        val exportChoicesView = findViewById<ListView>(R.id.export_destination_choice)
        exportChoicesView.adapter = adapter
        exportChoicesView.setOnItemClickListener { _, _, position, _ ->
            selectedExportChoiceIndex = position
        }
        findViewById<Button>(R.id.export_data).setOnClickListener {
            exportData()
        }
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
            if(selectedExportChoiceIndex != -1) {
                exportFilePath = ClassroomDataExchange.exportClassroomData()
                Toast.makeText(this, resources.getString(R.string.export_success) + exportFilePath, Toast.LENGTH_LONG).show()
                exportRoutines[selectedExportChoiceIndex]()
            } else {
                Toast.makeText(this, R.string.select_export_dest, Toast.LENGTH_LONG).show()
            }
        } catch(e: IOException) {
            Toast.makeText(this, resources.getString(R.string.export_failed) + e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
    fun sendToSDCard() {
        val exchangePath = "/storage/sdcard1/"
        if (File(exchangePath).canWrite()) {
            val targetFolder = File(exchangePath + exchange_folder)
            if(targetFolder.exists()) {
                targetFolder.mkdirs()
            }
            File(exportFilePath).copyTo(targetFolder, true)
        } else {
            Toast.makeText(this, R.string.no_sdcard, Toast.LENGTH_LONG).show()
        }
    }
    fun sendViaBluetooth() {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        val DISCOVER_DURATION = 300
        if (btAdapter != null) {
            val discoveryIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION)
            startActivityForResult(discoveryIntent, 3)
        } else {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show()
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
                    .filter{ it.activityInfo.packageName == "com.android.bluetooth" }

            if (appsList.size > 0) {
                intent.setClassName(appsList[0].activityInfo.packageName, appsList[0].activityInfo.name)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Bluetooth app isn't found",
                        Toast.LENGTH_LONG).show()
            }
        }
    }
}
