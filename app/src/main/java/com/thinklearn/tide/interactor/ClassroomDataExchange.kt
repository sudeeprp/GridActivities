package com.thinklearn.tide.interactor

import android.os.Environment
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*


object ClassroomDataExchange {
    val class_id_key = "class_id"
    val type_key = "type"
    val classroom_key = "classroom_details"
    val classrooms_data_key = "classrooms"
    val teachers_key = "teachers"
    val students_key = "students"
    val thumbnails_key = "thumbnails"
    var last_exchange_folder_path = ""
    var last_exchange_filename = ""

    fun get_last_exchange_file_path(): String {
        return last_exchange_folder_path + last_exchange_filename
    }
    fun uploadAssets(assetsJSON: JSONObject, uploaded: DBOpDone) {
        if(assetsJSON.has(class_id_key)) {
            uploadClassroom(assetsJSON, uploaded)
        } else if(assetsJSON.has(type_key) && assetsJSON.getString(type_key) == "consolidated a") {
            uploadMultiClass(assetsJSON, uploaded)
        } else {
            throw JSONException("No class found")
        }
    }
    fun uploadClassroom(classroomAndAssetsJSON: JSONObject, uploaded: DBOpDone) {
        val class_id: String = classroomAndAssetsJSON.getString(class_id_key)
        val uploadMap = mutableMapOf<String, Any>()
        uploadMap.plusAssign(mapClassDescriptionToDBKeys(class_id, classroomAndAssetsJSON))
        uploadMap.plusAssign(mapAssetsToDBKeys(class_id, classroomAndAssetsJSON))
        uploadMap.plusAssign(mapThumbnailsToDBKeys(class_id, classroomAndAssetsJSON))

        val proj_ref = FirebaseDatabase.getInstance().getReference(ClassroomInteractor.learningProjectDB())
        proj_ref.updateChildren(uploadMap)
                .addOnSuccessListener{ uploaded.onSuccess() }
                .addOnFailureListener{ uploaded.onFailure(it.message) }
    }
    fun jsonToDBpaths(prefix: String, json: JSONObject, jsonKey: String): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        if(json.has(jsonKey)) {
            val mapTobePathed = json.getJSONObject(jsonKey)
            mapTobePathed.keys().forEach {
                map[prefix + it] = mapTobePathed.getString(it)
            }
        }
        return map
    }
    fun uploadMultiClass(assetsJSON: JSONObject, uploaded: DBOpDone) {
        val uploadMap = mutableMapOf<String, Any>()
        uploadMap.plusAssign(jsonToDBpaths("/classrooms/", assetsJSON, classrooms_data_key))
        uploadMap.plusAssign(jsonToDBpaths("/classroom_assets/", assetsJSON, teachers_key))
        uploadMap.plusAssign(jsonToDBpaths("/classroom_assets/", assetsJSON, students_key))
        if(uploadMap.isEmpty()) {
            throw(JSONException("No class data"))
        }
        val proj_ref = FirebaseDatabase.getInstance().getReference(ClassroomInteractor.learningProjectDB())
        proj_ref.updateChildren(uploadMap)
                .addOnSuccessListener{ uploaded.onSuccess() }
                .addOnFailureListener{ uploaded.onFailure(it.message) }
    }
    private fun mapClassDescriptionToDBKeys
            (class_id: String, classroomAndAssetsJSON: JSONObject): MutableMap<String, Any> {
        val classroom_prefix =  "/classrooms/" + class_id + "/"
        return jsonToDBpaths(classroom_prefix, classroomAndAssetsJSON, classroom_key)
    }
    private fun mapAssetsToDBKeys(class_id: String, classroomAndAssetsJSON: JSONObject): MutableMap<String, Any> {
        val assetMap = mutableMapOf<String, Any>()
        val class_asset_prefix = "/classroom_assets/" + class_id

        val teachers_prefix = class_asset_prefix + "/teachers/"
        jsonToDBpaths(teachers_prefix, classroomAndAssetsJSON, teachers_key)

        val students_prefix = class_asset_prefix + "/students/"
        jsonToDBpaths(students_prefix, classroomAndAssetsJSON, students_key)

        return assetMap
    }
    fun mapThumbnailsToDBKeys(class_id: String, classroomAndAssetsJSON: JSONObject): MutableMap<String, Any> {
        val thumbnail_prefix = "/thumbnails/classrooms/" + class_id + "/"
        return jsonToDBpaths(thumbnail_prefix, classroomAndAssetsJSON, thumbnails_key)
    }
    fun exportClassroomData() {
        val exportJson = JSONObject()
        exportJson.put(class_id_key, ClassroomInteractor.loadedClassroomID)
        exportJson.put(teachers_key, exportTeachersJson())
        exportJson.put(students_key, exportStudentsJson())
        exportJson.put(thumbnails_key, exportThumbnailsJson())

        val exchange_folder_path = Environment.getExternalStorageDirectory().getPath() + "/LearningGridExchange/"
        val exchangeDirFile = File(exchange_folder_path)
        if (!exchangeDirFile.exists()) {
            exchangeDirFile.mkdirs()
        }
        //Extension is .json.txt to enable good data-transfer
        val exportFileName = "classroomdata-" + ClassroomInteractor.loadedClassroomID +
                "-" + EnvironmentalContext.getDeviceIdentity() + ".json.txt"
        val exportFile = File(exchange_folder_path + exportFileName)
        exportFile.writeText(exportJson.toString(2).replace("\\/", "/"))
        last_exchange_folder_path = exchange_folder_path
        last_exchange_filename = exportFileName
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
            if(it.qualifier != null && it.qualifier.isNotEmpty()) {
                studentsJson.put(it.id + "/qualifier", it.qualifier)
            }
        }
        return studentsJson
    }
    fun exportThumbnailsJson(): JSONObject {
        val thumbnailsJson = JSONObject()
        ClassroomInteractor.teachers.forEach {
            thumbnailsJson.put("teachers/" + it.id + "/thumbnail", it.thumbnail)
        }
        ClassroomInteractor.students.forEach {
            thumbnailsJson.put("students/" + it.id + "/thumbnail", it.thumbnail)
        }
        return thumbnailsJson
    }
}