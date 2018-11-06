package com.thinklearn.tide.interactor

import android.os.Environment
import com.google.firebase.database.*
import com.thinklearn.tide.dto.AttendanceInput
import com.thinklearn.tide.dto.Student
import com.thinklearn.tide.dto.Teacher
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import org.json.JSONObject
import java.io.File


interface DBOpDone {
    fun onSuccess()
    fun onFailure(msg: String?)
}
object ConfigKeys {
    val learning_project_file = "learning_project.json"
    val project_name_key = "db_project_name"

    val selected_class_file = "selected_class.json"
    val selected_class_key = "selected_class_id"

    val selected_mode_file = "selected_mode.json"
    val selected_mode_key = "selected_mode"
    @JvmField
    val teacher_mode_value = "teacher"
    @JvmField
    val student_mode_value = "student"
}
object ClassroomInteractor {
    var loadedLearningProject: String = ""
    @JvmField
    var loadedClassroomID: String = ""
    @JvmField
    var class_name: String = ""
    @JvmField
    var school_name: String = ""
    @JvmField
    var teachers = ArrayList<Teacher>()
    @JvmField
    var students = ArrayList<Student>()
    var subject_current_chapter = HashMap<String, String>()
    var loadedEvent: DBOpDone? = null
    var presentAM: MutableMap<String, MutableList<String>> = hashMapOf()
    var presentPM: MutableMap<String, MutableList<String>> = hashMapOf()

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
            val configJSON = JSONObject(configJsonStr)
            if(configJSON.has(key)) {
                configValue = configJSON.get(key).toString()
            }
        }
        if(configValue == "") {
            writeConfig(filename, key, default)
            configValue = default
        }
        return configValue
    }
    fun learningProject(): String {
        if(loadedLearningProject == "") {
            return getConfig(ConfigKeys.learning_project_file, ConfigKeys.project_name_key, "ICDev")
        } else {
            return loadedLearningProject
       }
    }
    fun selectedClass(): String {
        return getConfig(ConfigKeys.selected_class_file, ConfigKeys.selected_class_key, "")
    }
    fun selectedMode(): String {
        return getConfig(ConfigKeys.selected_mode_file, ConfigKeys.selected_mode_key, "")
    }
    @JvmStatic
    fun setTabMode(mode: String) {
        writeConfig(ConfigKeys.selected_mode_file, ConfigKeys.selected_mode_key, mode)
    }
    fun db_classrooms_reference(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(learningProject()).child("classrooms")
    }
    fun db_classroom_assets_reference(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(learningProject()).child("classroom_assets")
    }
    fun db_teachers_reference(classroomId: String): DatabaseReference {
        return db_classroom_assets_reference().child(classroomId).child("teachers")
    }
    fun db_students_reference(classroomId: String): DatabaseReference {
        return db_classroom_assets_reference().child(classroomId).child("students")
    }
    fun db_onestudent_reference(studentId: String): DatabaseReference {
        return db_students_reference(loadedClassroomID).child(studentId)
    }
    fun db_student_activity_reference(studentRef: DatabaseReference, subject: String, chapter: String,
                                      activity_identifier: String): DatabaseReference {
        return studentRef.child("finished_activities").child(subject).
                child(chapter).child(activity_id_to_firebase_id(activity_identifier))
    }
    fun db_activity_log_reference(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(learningProject()).child("activity_log")
    }
    fun activity_id_to_firebase_id(activity_id: String): String {
        return activity_id.replace('.', '^')
    }
    fun firebase_id_to_activity_id(firebase_id: String): String {
        return firebase_id.replace('^', '.')
    }
    fun reset_lists() {
        teachers.clear()
        students.clear()
        subject_current_chapter.clear()
    }
    fun get_teacher_index(id: String): Int {
        for(i in teachers.indices) {
            if(teachers[i].id == id) {
                return i
            }
        }
        //Teacher not found. Add at end and return the last index
        val teacher = Teacher()
        teacher.id = id
        teachers.add(teacher)
        return teachers.count() - 1
    }
    fun get_student_index(id: String): Int {
        for(i in students.indices) {
            if(students[i].id == id) {
                return i
            }
        }
        //Student not found. Add at end and return the last index
        val student = Student()
        student.id = id
        students.add(student)
        return students.count() - 1
    }
    fun fill_assets_into_teachers(teachers_snapshot: DataSnapshot?) {
        teachers_snapshot?.children?.forEach {
            val i = get_teacher_index(it.key!!)
            teachers[i].teacherName = it.child("name").value.toString()
        }
    }
    fun fill_thumbnails_into_teachers(teachers_thumbnails: DataSnapshot?) {
        teachers_thumbnails?.children?.forEach {
            val i = get_teacher_index(it.key!!)
            teachers[i].thumbnail = it.child("thumbnail").value.toString()
        }
    }
    fun fill_assets_into_students(students_snapshot: DataSnapshot?) {
        students_snapshot?.children?.forEach {
            val i = get_student_index(it.key!!)
            students[i].firstName = it.child("first_name").value.toString()
            students[i].surname = it.child("surname").value.toString()

            var dob_day = it.child("birth_date").child("dd").value
            var dob_month = it.child("birth_date").child("mm").value
            val dob_year = it.child("birth_date").child("yyyy").value
            if(dob_year != null && dob_year != "") {
                if(dob_day == null || dob_day == "") dob_day = "1"
                if(dob_month == null || dob_month == "") dob_month = "1"
                students[i].birthDate = SimpleDateFormat("dd/MM/yyyy").parse(dob_day.toString() + "/" + dob_month.toString() + "/" + dob_year.toString())
            }
            students[i].gender = it.child("gender").value.toString()
            students[i].grade = it.child("grade").value.toString()
            students[i].qualifier = it.child("qualifier").value.toString()
            if(students[i].qualifier == null) students[i].qualifier = ""
        }
        students.sortWith(compareBy { it.surname })
    }
    fun fill_thumbnails_into_students(students_thumbnails: DataSnapshot?) {
        students_thumbnails?.children?.forEach {
            val i = get_student_index(it.key!!)
            students[i].thumbnail = it.child("thumbnail").value.toString()
        }
    }
    @JvmStatic
    fun set_student_thumbnail(studentId: String, thumbnail: String) {
        FirebaseDatabase.getInstance().getReference(loadedLearningProject).child("thumbnails").
                child("classrooms").child(loadedClassroomID).child("students").child(studentId).
                child("thumbnail").setValue(thumbnail)
    }
    fun fill_assets_into_current_chapters(subject_chapter: DataSnapshot?) {
        subject_current_chapter.clear()
        subject_chapter?.children?.forEach {
            subject_current_chapter.put(it.key!!, it.value.toString())
        }
    }
    fun fill_assets_into_present(attendance_snapshot: DataSnapshot?) {
        presentAM.clear()
        presentPM.clear()
        val t = object : GenericTypeIndicator<ArrayList<String>>() {}
        attendance_snapshot?.children?.forEach {
            val date = it.key.toString()
            val presentAM_ids = it.child("presentAM").getValue(t)
            if(presentAM_ids != null) presentAM[date] = presentAM_ids
            val presentPM_ids = it.child("presentPM").getValue(t)
            if(presentPM_ids != null) presentPM[date] = presentPM_ids
        }
    }
    @JvmStatic
    fun set_day_presents(date: String, dayPresentAM: List<String>?, dayPresentPM: List<String>?) {
        //If we get an attendance-set-request before students, ignore
        //presentees is coming from java, where it can be null.
        if(students.size == 0) {
            return
        }
        val attendance_date_ref = FirebaseDatabase.getInstance().getReference(loadedLearningProject).child("classroom_assets").
                child(loadedClassroomID).child("attendance").child(date)
        attendance_date_ref.child("presentAM").setValue(dayPresentAM)
        attendance_date_ref.child("presentPM").setValue(dayPresentPM)
    }
    fun removeLoadedEvent() {
        loadedEvent = null
    }
    fun uploadClassroom(classroomAndAssetsJSON: JSONObject, uploaded: DBOpDone) {
        val class_id_key = "class_id"
        if(classroomAndAssetsJSON.has(class_id_key)) {
            val class_id: String = classroomAndAssetsJSON.getString(class_id_key)
            val uploadMap = mutableMapOf<String, Any>()
            uploadMap.plusAssign(mapClassDescriptionToDBKeys(class_id, classroomAndAssetsJSON))
            uploadMap.plusAssign(mapAssetsToDBKeys(class_id, classroomAndAssetsJSON))
            uploadMap.plusAssign(mapThumbnailsToDBKeys(class_id, classroomAndAssetsJSON))

            val proj_ref = FirebaseDatabase.getInstance().getReference(learningProject())
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
        val classroom_key = "classroom_details"
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

        val teachers_key = "teachers"
        if(classroomAndAssetsJSON.has(teachers_key)) {
            val teachersPart = classroomAndAssetsJSON.getJSONObject(teachers_key)
            val teachers_prefix = class_asset_prefix + "/teachers/"
            assetMap.plusAssign(jsonToMap(teachers_prefix, teachersPart))
        }

        val students_key = "students"
        if(classroomAndAssetsJSON.has(students_key)) {
            val studentsPart = classroomAndAssetsJSON.getJSONObject(students_key)
            val students_prefix = class_asset_prefix + "/students/"
            assetMap.plusAssign(jsonToMap(students_prefix, studentsPart))
        }
        return assetMap
    }
    fun mapThumbnailsToDBKeys(class_id: String, classroomAndAssetsJSON: JSONObject): MutableMap<String, Any> {
        var thumbnailMap = mutableMapOf<String, Any>()
        val thumbnail_key = "thumbnails"
        if(classroomAndAssetsJSON.has(thumbnail_key)) {
            val thumbnailPart = classroomAndAssetsJSON.getJSONObject(thumbnail_key)
            val thumbnail_prefix = "/thumbnails/classrooms/" + class_id + "/"
            thumbnailMap = jsonToMap(thumbnail_prefix, thumbnailPart)
        }
        return thumbnailMap
    }

    fun load(learningProject: String, classroom_id: String, loaded_event: DBOpDone) {
        writeConfig(ConfigKeys.learning_project_file, ConfigKeys.project_name_key, learningProject)
        writeConfig(ConfigKeys.selected_class_file, ConfigKeys.selected_class_key, classroom_id)
        loadedLearningProject = learningProject
        loadedClassroomID = classroom_id
        loadedEvent = loaded_event
        reset_lists()
        FirebaseDatabase.getInstance().getReference(learningProject).child("classroom_assets").
                child(classroom_id).addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        fill_assets_into_teachers(p0.child("teachers"))
                        fill_assets_into_students(p0.child("students"))
                        fill_assets_into_current_chapters(p0.child("class_subject_current"))
                        fill_assets_into_present(p0.child("attendance"))
                    }
                    override fun onCancelled(p0: DatabaseError) {
                        println("classroom_assets fetch: onCancelled ${p0.toException()}")
                    }
                })
        FirebaseDatabase.getInstance().getReference(learningProject).child("thumbnails").child("classrooms").
                child(classroom_id).addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        fill_thumbnails_into_teachers(p0.child("teachers"))
                        fill_thumbnails_into_students(p0.child("students"))
                        //TODO: This will not work if thumbnails come first
                        loadedEvent?.onSuccess()
                    }
                    override fun onCancelled(p0: DatabaseError) {
                        loadedEvent?.onFailure(p0.message)
                    }
                })
        FirebaseDatabase.getInstance().getReference(learningProject).child("classrooms")
                .child(classroom_id).addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        class_name = p0.child("class_name").value.toString()
                        school_name = p0.child("school_name").value.toString()
                    }
                    override fun onCancelled(p0: DatabaseError) {
                        println("classrooms fetch: onCancelled ${p0.toException()}")
                    }
                })
    }

    fun get_teacher(id: String): Teacher? {
        for(teacher in teachers) {
            if(teacher.id == id) {
                return teacher
            }
        }
        return null
    }
    fun get_student(id: String): Student? {
        for(student in students) {
            if(student.id == id) {
                return student
            }
        }
        return null
    }
    fun grade_subject(grade: String, subject: String): String {
        return grade + "_" + subject.toLowerCase()
    }
    fun get_active_chapter_id(grade: String, subject: String): String? {
        return subject_current_chapter.get(grade_subject(grade, subject))
    }
    fun set_active_chapter(grade: String, subject: String, chapter: String) {
        val gradesub = grade_subject(grade, subject)
        FirebaseDatabase.getInstance().getReference(loadedLearningProject).child("classroom_assets").child(loadedClassroomID).child("class_subject_current").child(gradesub).setValue(chapter)
        subject_current_chapter[gradesub] = chapter
        write_activity_log("{\"activity\": \"Chapter activation\", " +
                "\"grade\": \"$grade\", " +
                "\"subject\": \"$subject\", " +
                "\"chapter\": \"$chapter\"}")
    }
    fun get_students_in_chapter(grade: String, subject: String, chapter_id: String): ArrayList<Student> {
        val students_in_chapter = ArrayList<Student>()
        students.forEach {
            if(it.grade == grade && get_active_chapter_id(grade, subject) == chapter_id) {
                students_in_chapter.add(it)
            }
        }
        return students_in_chapter
    }
    fun chapter_id_of_student(student: Student, subject: String, chapters: Chapters): String {
        //TODO: Search thru chapters up to class-current and find the one where the student has pending activities
        var current_chapter_id = get_active_chapter_id(student.grade, subject)
        if(current_chapter_id == null) {
            current_chapter_id = ContentInteractor().first_chapter_id(student.grade, subject)
        }
        return current_chapter_id?:"null"
    }
    fun students_and_chapters(grade: String, subject: String): HashMap<String, ArrayList<Student>> {
        val student_chapters_map = HashMap<String, ArrayList<Student>>()
        val chapters = ContentInteractor().chapters_and_activities(grade, subject)
        for(chapter in chapters.chapter_list) {
            student_chapters_map[chapter.name] = ArrayList<Student>()
        }
        for(student in students.filter { it.grade == grade && !it.qualifier.contains("guest") }) {
            val current_chapter = chapter_id_of_student(student, subject, chapters)
            student_chapters_map[current_chapter]?.add(student)
        }
        return student_chapters_map
    }
    fun current_chapter_page(student: Student, subject: String): String {
        var chapter_id = get_active_chapter_id(student.grade, subject)
        if(chapter_id == null) {
            chapter_id = ContentInteractor().first_chapter_id(student.grade, subject)
        }
        return ContentInteractor().chapters_directory(student.grade, subject) + "/" + chapter_id + "/index.html"
    }
    fun set_student_activity_status(studentId: String, activity_subject: String, activity_chapter: String,
                               activity_identifier: String, activity_datapoint: String) {
        val studentRef = db_onestudent_reference(studentId)
        val studentActivityRef =
                db_student_activity_reference(studentRef, activity_subject, activity_chapter, activity_identifier)

        studentActivityRef.child("data_point").setValue(activity_datapoint)
        val currentTime = Calendar.getInstance().getTime()
        val currentTimestamp = SimpleDateFormat("yyyy-MM-dd").format(currentTime) + "T" +
                SimpleDateFormat("HH:mm:ssz").format(currentTime)
        studentActivityRef.child("time_stamp").setValue(currentTimestamp)
    }
    fun write_activity_log(activity: String) {
        val activityLogRef = db_activity_log_reference()
        class ActivityLogEntry
            (val activity_log: String, val class_id: String, val timestamp: String)
        val activityLogEntry = ActivityLogEntry(
                activity, loadedClassroomID,
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date()))
        activityLogRef.push().setValue(activityLogEntry)
    }
    @JvmStatic
    fun get_current_week_attendance(): AttendanceInput {
        val week_attendance = AttendanceInput()
        week_attendance.studentList = students.filter { !it.qualifier.contains("guest") }

        val date_format = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        week_attendance.holidayList = listOf(calendar.time)
        week_attendance.weekStartDate = calendar.time
        week_attendance.presentAM = hashMapOf()
        week_attendance.presentPM = hashMapOf()
        for (i in 1..6) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val date: Date = calendar.time
            val hyphenated_date: String = date_format.format(date)
            if(hyphenated_date in presentAM) {
                week_attendance.presentAM[hyphenated_date] = presentAM[hyphenated_date]
            }
            if(hyphenated_date in presentPM) {
                week_attendance.presentPM[hyphenated_date] = presentPM[hyphenated_date]
            }
        }
        val today: String = date_format.format(Calendar.getInstance().time)
        if(today in presentAM) {
            week_attendance.presentStudentsAM = presentAM[today]
        }
        if(today in presentPM) {
            week_attendance.presentStudentsPM = presentPM[today]
        }
        return week_attendance
    }
    @JvmStatic
    fun filterStudents(selectedGrade: String, selectedGender: String, includeGuest: Boolean = true): List<Student> {
        var filteredStudentList: List<Student> = students.filter { it.grade == selectedGrade && it.gender == selectedGender}
        if(!includeGuest) {
            filteredStudentList = filteredStudentList.filter { !it.qualifier.contains("guest") }
        }
        return filteredStudentList
    }
}
