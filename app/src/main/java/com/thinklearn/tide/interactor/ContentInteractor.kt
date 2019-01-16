package com.thinklearn.tide.interactor

import android.content.Context
import android.os.Environment
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.lang.Integer.parseInt

val default_curriculum = """
<!DOCTYPE html>
<html>
<head><meta charset="utf-8"/><title>Need Content</title></head>
<body><h1>Feed me some content!</h1></body>
</html>
""".trimIndent()

class Chapters {
    class ActivityInChapter(val activity_identifier: String, val mandatory: Boolean)
    class Chapter(val name: String, val activities: ArrayList<ActivityInChapter>)
    var chapter_list = ArrayList<Chapter>()

    fun getChapter(chapterID: String): Chapter? {
        for(chapter in chapter_list) {
            if(chapter.name == chapterID) {
                return chapter
            }
        }
        return null
    }
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
    constructor(chapters: ArrayList<Chapter>) {
        chapter_list = chapters
    }
}

class ContentInteractor {
    val grades_file = "display names of grades.json"
    val subjects_file = "display names of subjects.json"
    val subject_background_pic = "subject_logo.png"
    val content_desc_filename = "content_descriptor.json"

    object content{
        var content_path = ""
        var content_version = ""
    }

    constructor() {
        if(content.content_path.isEmpty()) {
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
    fun getConfig(filename: String, key: String): String {
        val configFile = File(content.content_path + filename)
        var configValue = ""
        if(configFile.exists()) {
            val configJsonStr = configFile.readText()
            val configJSON = JSONObject(configJsonStr)
            if(configJSON.has(key)) {
                configValue = configJSON.get(key).toString()
            }
        }
        return configValue
    }
    fun getTokenFromDirlist(path: String, dirPrefix: String, tokenNumber: Int): ArrayList<String> {
        var contentDirEntries = File(path).list().filter { File(path + "/" + it).isDirectory }
        if(dirPrefix.isNotEmpty()) {
            contentDirEntries = contentDirEntries.filter { it.startsWith(dirPrefix) }
        }
        val tokens = arrayListOf<String>()
        contentDirEntries.forEach {
            val dirTokens = it.split("_")
            if (dirTokens.size > 1) {
                try {
                    //The first token must be a grade-number for us to parse the grade/subject out
                    parseInt(dirTokens[0])
                    if (!tokens.contains(dirTokens[tokenNumber])) {
                        tokens.plusAssign(dirTokens[tokenNumber])
                    }
                } catch (e: NumberFormatException) {
                    //do nothing
                }
            }
        }
        return tokens
    }
    fun getSetBefore_(path: String): ArrayList<String> {
        return getTokenFromDirlist(path, "", 0)
    }
    fun getSetAfter_(path: String, dirPrefix: String): ArrayList<String> {
        return getTokenFromDirlist(path, dirPrefix, 1)
    }

    fun get_content_version(): String {
        return content.content_version
    }

    fun chapters_page(grade: String, subject: String): String {
        return chapters_directory(grade, subject) + "chapters.html"
    }

    fun get_subjects(grade: String): ArrayList<String> {
        var subjects = getSetAfter_(content.content_path, grade + "_")
        if(subjects.size == 0) {
            subjects = arrayListOf("french", "math")
        }
        return subjects
    }
    fun get_grade_background_path(grade: String): String {
        return content.content_path + "/grade" + grade + "_logo.png"
    }
    fun get_grade_display_name(grade: String, context: Context, packageName: String): String {
        var gradeDisplayName = getConfig(grades_file, grade)
        if(gradeDisplayName.isEmpty()) {
            gradeDisplayName = context.resources.getString(context.resources.getIdentifier
                                    ("grade" + grade, "string", packageName))
        }
        return gradeDisplayName
    }
    fun get_subject_background_path(grade: String, subject: String): String {
        return chapters_directory(grade, subject) + subject_background_pic
    }
    fun get_subject_display_name(subject: String, context: Context, packageName: String): String {
        var subjectDisplayName = getConfig(subjects_file, subject)
        if(subjectDisplayName.isEmpty()) {
            subjectDisplayName = context.resources.getString(context.resources.getIdentifier
                                    (subject, "string", packageName))
        }
        return subjectDisplayName
    }
    fun get_grades(): ArrayList<String> {
        var grades = getSetBefore_(content.content_path)
        if(grades.size == 0) {
            grades = arrayListOf("1", "2")
        }
        return grades
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
