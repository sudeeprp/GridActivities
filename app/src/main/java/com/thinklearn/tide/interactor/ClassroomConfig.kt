package com.thinklearn.tide.interactor

import android.os.Environment
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.File

object ClassroomConfig {
    val project_name_key = "db_project_name"

    val selected_class_file = "selected_class.json"
    val selected_class_key = "selected_class_id"

    val selected_mode_file = "selected_mode.json"
    val selected_mode_key = "selected_mode"
    @JvmField
    val teacher_mode_value = "teacher"
    @JvmField
    val student_mode_value = "student"

    fun baseDir(): String {
        val base_dir = Environment.getExternalStorageDirectory().getPath() + "/LearningGrid/"
        val baseDirFile = File(base_dir)
        if (!baseDirFile.exists()) {
            baseDirFile.mkdirs()
        }
        return base_dir
    }
    fun writeConfig(filename: String, key: String, value: String) {
        val configFile = File(baseDir() + filename)
        var configJsonStr = "{}"
        if(configFile.exists()) {
            configJsonStr = configFile.readText()
        }
        val configJSON = JSONObject(configJsonStr)
        configJSON.put(key, value)
        configFile.writeText(configJSON.toString(2))
    }
    fun getConfig(filename: String, key: String, default: String): String {
        val configFile = File(baseDir() + filename)
        var configValue = ""
        if(configFile.exists()) {
            val configJsonStr = configFile.readText()
            try {
                val configJSON = JSONObject(configJsonStr)
                if (configJSON.has(key)) {
                    configValue = configJSON.get(key).toString()
                }
            } catch(j: JSONException) {
                Log.d("Config parse", "Unexpected config in " + filename)
            }
        }
        if(configValue == "") {
            writeConfig(filename, key, default)
            configValue = default
        }
        return configValue
    }
}

