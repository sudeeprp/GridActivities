package com.thinklearn.tide.interactor

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException

val default_curriculum = """
<!DOCTYPE html>
<html>
<head><meta charset="utf-8"/><title>Need Content</title></head>
<body><h1>Feed me some content!</h1></body>
</html>
""".trimIndent()


class ContentInteractor {
    val base_path = Environment.getExternalStorageDirectory().getPath() + "/LearningGrid/"
    val default_config =
            """
            {"config":
              {"content_dir_name": """ + base_path +
                    """
              }
            }
        """.trimIndent()
    var content_path = base_path
    var content_start_page = content_path + "index.html"

    constructor() {
        create_default_if_not_exist()
        read_config()
    }

    fun create_default_if_not_exist() {
        try {
            val baseDirFile = File(base_path)
            if (!baseDirFile.exists()) {
                baseDirFile.mkdirs()
            }
            if (!File(content_start_page).exists()) {
                File(content_start_page).writeText(default_curriculum)
            }
            val configFile = File(base_path + "content_location.json")
            if (!configFile.exists()) {
                configFile.writeText(default_config)
            }
        } catch (e: IOException) {
            Log.e("Write", "Create defaults failed: " + e.message)
        }
    }
    fun read_config() {
        create_default_if_not_exist()
        //TODO: Read from json in config file
    }
}