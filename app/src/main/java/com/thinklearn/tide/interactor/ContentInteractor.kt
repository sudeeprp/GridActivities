package com.thinklearn.tide.interactor

import android.os.Environment
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

val default_curriculum = """
<!DOCTYPE html>
<html>
<head><meta charset="utf-8"/><title>Need Content</title></head>
<body><h1>Feed me some content!</h1></body>
</html>
""".trimIndent()

class Chapters {
    constructor(chapters_file: File) {
        if(chapters_file.exists()) {
            val chapters_json = chapters_file.readText()
            val chapters_array = JSONArray(chapters_json)
            try {
                for(i in 0 until chapters_array.length()) {
                    var chapterId = ""
                    if(chapters_array.getJSONObject(i).has("chapter_id")) {
                        chapterId = chapters_array.getJSONObject(i).getString("chapter_id")
                    } else {
                        chapterId = chapters_array.getJSONObject(i).getString("chapter_name")
                    }
                    val chapterActivities = ArrayList<ActivityInChapter>()
                    val activities_json = chapters_array.getJSONObject(i).getJSONObject("activities")
                    val activityIdentifiers = activities_json.names()
                    if(activityIdentifiers != null) {
                        for (activity_index in 0 until activityIdentifiers.length()) {
                            val activityIdentifier = activityIdentifiers[activity_index].toString()
                            val activity_json = activities_json.getJSONObject(activityIdentifier)
                            val mandatory = activity_json.getBoolean("mandatory")
                            chapterActivities.add(ActivityInChapter(activityIdentifier, mandatory))
                        }
                    }
                    chapter_list.add(Chapter(chapterId, chapterActivities))
                }
            } catch(j: JSONException) {
                Log.e("chapters file", chapters_file.toString() + ": " + j.message)
            }
        }
    }
    class ActivityInChapter(val activity_identifier: String, val mandatory: Boolean)
    class Chapter(val name: String, val activities: ArrayList<ActivityInChapter>)
    var chapter_list = ArrayList<Chapter>()
}

class ContentInteractor {
    object content{
        var content_path = ""
        var content_version = ""
    }
    val content_desc_filename = "content_descriptor.json"

    constructor() {
        if(content.content_path == "") {
            val trial_path = "/storage/sdcard1/LearningGrid/"
            val trial_content_desc_file = File(trial_path + content_desc_filename)
            if(trial_content_desc_file.exists() && trial_content_desc_file.canRead()) {
                content.content_path = trial_path
            } else {
                content.content_path = Environment.getExternalStorageDirectory().getPath() + "/LearningGrid/"
            }
            val content_desc_file = File(content.content_path + content_desc_filename)
            if(content_desc_file.exists()) {
                try {
                    content.content_version = JSONObject(content_desc_file.readText()).get("content_version").toString()
                } catch(j: JSONException) {
                    Log.e("no content version", "JSON parse error: " + j.message)
                }
            }
        }
    }

    fun get_content_version(): String {
        return content.content_version
    }

    fun chapters_page(grade: String, subject: String): String {
        return chapters_directory(grade, subject) + "chapters.html"
    }

    fun get_subjects(grade: String): ArrayList<String> {
        //TODO: Get subjects from the curriculum directories
        return arrayListOf("french", "math")
    }
    fun chapters_directory(grade: String, subject: String): String {
        return content.content_path + "/" + grade + "_" + subject + "/"
    }
    fun chapters_and_activities(grade: String, subject: String): Chapters {
        val subject_chapters_file = File(chapters_directory(grade, subject) + "/chapter_activities.json")
        return Chapters(subject_chapters_file)
    }
    fun first_chapter_id(grade: String, subject: String): String? {
        var firstChapterId: String? = null
        val chaptersDir = chapters_directory(grade, subject)
        val subject_chapters_file = File(chaptersDir + "/chapter_activities.json")
        if(subject_chapters_file.exists()) {
            val subject_chapters_json = subject_chapters_file.readText()
            val subject_chapters_array = JSONArray(subject_chapters_json)
            if (subject_chapters_array.length() > 0) {
                val first_chapter_desc = subject_chapters_array.getJSONObject(0)
                firstChapterId = first_chapter_desc.getString("chapter_id")
            }
        }
        return firstChapterId
    }
    fun activity_directory(grade: String, subject: String, chapterName: String, activity_identifier: String): String {
        return chapters_directory(grade, subject) + "/" + chapterName + "/" + activity_identifier
    }
    fun activity_page(grade: String, subject: String, chapterName: String, activity_identifier: String): String {
        val page_dir = activity_directory(grade, subject, chapterName, activity_identifier)
        var page_filename = ""
        if(File(page_dir).exists()) {
            page_filename = page_dir + "/index.html"
            if (!File(page_filename).exists()) {
                File(page_dir).walk().forEach { page_filename = it.absolutePath }
            }
        }
        return page_filename
    }
}
