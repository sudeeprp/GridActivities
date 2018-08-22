package com.thinklearn.tide.interactor

import android.os.Environment
import android.util.Log
import com.thinklearn.tide.dto.Student
import org.json.JSONArray
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
    companion object {
        val base_path = Environment.getExternalStorageDirectory().getPath() + "/LearningGrid/"
        val default_config =
                """
              {"content_dir_name": """ + base_path +
                        """
              }
a        """.trimIndent()
        var content_path = base_path
        var content_start_page = content_path + "index.html"
    }

    constructor() {
        create_default_if_not_exist()
        read_config()
    }

    fun chapters_page(grade: String, subject: String): String {
        return chapters_directory(grade, subject) + "chapters.html"
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
        //TODO: Read from json in config file
    }
    fun chapters_directory(grade: String, subject: String): String {
        return content_path + "/" + grade + "_" + subject + "/"
    }
    fun first_chapter(grade: String, subject: String): String? {
        var firstChapter: String? = null
        val chaptersDir = chapters_directory(grade, subject)
        val subject_chapters_file = File(chaptersDir + "/chapter_activities.json")
        if(subject_chapters_file.exists()) {
            val subject_chapters_json = subject_chapters_file.readText()
            val subject_chapters_array = JSONArray(subject_chapters_json)
            if (subject_chapters_array.length() > 0) {
                val first_chapter_desc = subject_chapters_array.getJSONObject(0)
                firstChapter = first_chapter_desc.getString("chapter_name")
            }
        }
        return firstChapter
    }

    fun current_chapter_page(student: Student, subject: String): String {
        var chapterName = student.getCurrentChapter(subject)
        if(chapterName == null) {
            chapterName = first_chapter(student.grade, subject)
        }
        return chapters_directory(student.grade, subject) + "/" + chapterName + "/index.html"
    }
}
