package com.thinklearn.tide.interactor

import android.os.Environment
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*


object ClassroomDataExchange {
    val class_id_key = "class_id"
    val classroom_key = "classroom_details"
    val teachers_key = "teachers"
    val students_key = "students"
    val thumbnails_key = "thumbnails"
    val preferred_path = "/storage/sdcard1/"
    val exchange_folder = "LearningGridExchange/"

    fun uploadClassroom(classroomAndAssetsJSON: JSONObject, uploaded: DBOpDone) {
        if(classroomAndAssetsJSON.has(class_id_key)) {
            val class_id: String = classroomAndAssetsJSON.getString(class_id_key)
            val uploadMap = mutableMapOf<String, Any>()
            uploadMap.plusAssign(mapClassDescriptionToDBKeys(class_id, classroomAndAssetsJSON))
            uploadMap.plusAssign(mapAssetsToDBKeys(class_id, classroomAndAssetsJSON))
            uploadMap.plusAssign(mapThumbnailsToDBKeys(class_id, classroomAndAssetsJSON))

            val proj_ref = FirebaseDatabase.getInstance().getReference(ClassroomInteractor.learningProject())
            proj_ref.updateChildren(uploadMap)
                    .addOnSuccessListener{ uploaded.onSuccess() }
                    .addOnFailureListener{ uploaded.onFailure(it.message) }
        } else {
            throw JSONException("No class found")
        }
    }
    fun jsonToMap(prefix: String, json: JSONObject): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        json.keys().forEach {
            map[prefix + it] = json.getString(it)
        }
        return map
    }
    private fun mapClassDescriptionToDBKeys
            (class_id: String, classroomAndAssetsJSON: JSONObject): MutableMap<String, Any> {
        var classroomMap = mutableMapOf<String, Any>()
        if (classroomAndAssetsJSON.has(classroom_key)) {
            val classroomPart = classroomAndAssetsJSON.getJSONObject(classroom_key)
            val classroom_prefix =  "/classrooms/" + class_id + "/"
            classroomMap = jsonToMap(classroom_prefix, classroomPart)
        }
        return classroomMap
    }
    private fun mapAssetsToDBKeys(class_id: String, classroomAndAssetsJSON: JSONObject): MutableMap<String, Any> {
        val assetMap = mutableMapOf<String, Any>()
        val class_asset_prefix = "/classroom_assets/" + class_id

        if(classroomAndAssetsJSON.has(teachers_key)) {
            val teachersPart = classroomAndAssetsJSON.getJSONObject(teachers_key)
            val teachers_prefix = class_asset_prefix + "/teachers/"
            assetMap.plusAssign(jsonToMap(teachers_prefix, teachersPart))
        }

        if(classroomAndAssetsJSON.has(students_key)) {
            val studentsPart = classroomAndAssetsJSON.getJSONObject(students_key)
            val students_prefix = class_asset_prefix + "/students/"
            assetMap.plusAssign(jsonToMap(students_prefix, studentsPart))
        }
        return assetMap
    }
    fun mapThumbnailsToDBKeys(class_id: String, classroomAndAssetsJSON: JSONObject): MutableMap<String, Any> {
        var thumbnailMap = mutableMapOf<String, Any>()
        if(classroomAndAssetsJSON.has(thumbnails_key)) {
            val thumbnailPart = classroomAndAssetsJSON.getJSONObject(thumbnails_key)
            val thumbnail_prefix = "/thumbnails/classrooms/" + class_id + "/"
            thumbnailMap = jsonToMap(thumbnail_prefix, thumbnailPart)
        }
        return thumbnailMap
    }
    fun exportClassroomData(): String {
        val exportJson = JSONObject()
        exportJson.put(class_id_key, ClassroomInteractor.loadedClassroomID)
        exportJson.put(teachers_key, exportTeachersJson())
        exportJson.put(students_key, exportStudentsJson())
        exportJson.put(thumbnails_key, exportThumbnailsJson())

        var exchangePath = preferred_path
        if (!File(exchangePath).exists()) {
            exchangePath = Environment.getExternalStorageDirectory().getPath() + "/"
        }
        exchangePath += exchange_folder
        val exchangeDirFile = File(exchangePath)
        if (!exchangeDirFile.exists()) {
            exchangeDirFile.mkdirs()
        }
        val exportFilePath = exchangePath + "classroomdata-" + ClassroomInteractor.loadedClassroomID +
                "-" + EnvironmentalContext.getDeviceIdentity() + ".json"
        val exportFile = File(exportFilePath)
        exportFile.writeText(exportJson.toString(2))

        return exportFilePath
    }
    fun exportTeachersJson(): JSONObject {
        val teachersJson = JSONObject()
        ClassroomInteractor.teachers.forEach {
            teachersJson.put(it.id + "/name", it.teacherName)
        }
        return teachersJson
    }
    fun exportStudentsJson(): JSONObject {
        val studentsJson = JSONObject()
        ClassroomInteractor.students.forEach {
            studentsJson.put(it.id + "/first_name", it.firstName)
            studentsJson.put(it.id + "/surname", it.surname)
            var birthday = ""
            var birthmonth = ""
            var birthyear = ""
            if(it.birthDate != null) {
                val cal = Calendar.getInstance()
                cal.time = it.birthDate
                birthday = cal.get(Calendar.DAY_OF_MONTH).toString()
                birthmonth = cal.get(Calendar.MONTH).toString()
                birthyear = cal.get(Calendar.YEAR).toString()
            }
            studentsJson.put(it.id + "/birth_date/dd", birthday)
            studentsJson.put(it.id + "/birth_date/mm", birthmonth)
            studentsJson.put(it.id + "/birth_date/yyyy", birthyear)
            studentsJson.put(it.id + "/gender", it.gender)
            studentsJson.put(it.id + "/grade", it.grade)
        }
        return studentsJson
    }
    fun exportThumbnailsJson(): JSONObject {
        val thumbnailsJson = JSONObject()
        ClassroomInteractor.teachers.forEach {
            thumbnailsJson.put("teachers/" + it.id + "thumbnail", it.thumbnail)
        }
        ClassroomInteractor.students.forEach {
            thumbnailsJson.put("students/" + it.id + "thumbnail", it.thumbnail)
        }
        return thumbnailsJson
    }
}