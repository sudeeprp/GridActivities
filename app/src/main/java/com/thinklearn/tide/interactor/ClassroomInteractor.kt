package com.thinklearn.tide.interactor

import android.os.Environment
import com.google.firebase.database.*
import com.google.gson.Gson
import com.thinklearn.tide.dto.AttendanceInput
import com.thinklearn.tide.dto.Student
import com.thinklearn.tide.dto.Teacher
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.File


interface ClassroomLoaded {
    fun onLoadComplete()
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
    var loadedEvent: ClassroomLoaded? = null
    var absentees: MutableMap<String, MutableList<String>> = hashMapOf()

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

            val dob_day = it.child("birth_date").child("dd").value
            val dob_month = it.child("birth_date").child("mm").value
            val dob_year = it.child("birth_date").child("yyyy").value
            if(dob_day != null && dob_month != null && dob_year != null) {
                students[i].birthDate = SimpleDateFormat("dd/MM/yyyy").
                        parse(dob_day.toString() + "/" + dob_month.toString() + "/" + dob_year.toString())
            }
            students[i].gender = it.child("gender").value.toString()
            students[i].grade = it.child("grade").value.toString()
        }
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
    fun fill_assets_into_absentees(attendance_snapshot: DataSnapshot?) {
        absentees.clear()
        val t = object : GenericTypeIndicator<ArrayList<String>>() {}
        attendance_snapshot?.children?.forEach {
            val date = it.key.toString()
            val absentee_ids = it.child("absent").getValue(t)
            if(absentee_ids != null) {
                absentees[date] = absentee_ids
            }
        }
    }
    @JvmStatic
    fun set_day_absents(date: String, absentees: ArrayList<String>) {
        val attendance_date_ref = FirebaseDatabase.getInstance().getReference(loadedLearningProject).child("classroom_assets").
                child(loadedClassroomID).child("attendance").child(date)
        if(absentees.size == 0) {
            attendance_date_ref.child("full_class").setValue(":)")
        } else {
            attendance_date_ref.child("full_class").removeValue()
        }
        FirebaseDatabase.getInstance().getReference(loadedLearningProject).child("classroom_assets").
                child(loadedClassroomID).child("attendance").child(date).child("absent").setValue(absentees)
    }
    @JvmStatic
    fun set_day_presents(date: String, presentees: List<String>) {
        //If we get an attendance-set-request before students, ignore
        //presentees is coming from java, where it can be null.
        if(students.size == 0 || presentees == null) {
            return
        }
        val absentees = ArrayList<String>()
        for(student in students) {
            if(!(student.id in presentees)) {
                absentees.add(student.id)
            }
        }
        set_day_absents(date, absentees)
    }
    fun removeLoadedEvent() {
        loadedEvent = null
    }
    fun uploadClassroom(classroomAndAssetsJSON: JSONObject) {
        uploadClassroomPart(classroomAndAssetsJSON)
        uploadAssetPart(classroomAndAssetsJSON)
    }
    fun jsonToMap(json: JSONObject): Map<String, Any> {
        val jsonContent = json.toString()
        return Gson().fromJson<Map<String, Any>>(jsonContent, object : TypeToken<HashMap<String, Any>>() {}.type)
    }
    private fun uploadClassroomPart(classroomAndAssetsJSON: JSONObject): String {
        val classroomPart = classroomAndAssetsJSON.getJSONObject("classroom")
        val classroom = jsonToMap(classroomPart)
        db_classrooms_reference().updateChildren(classroom)

        //return classroom id. Required that there's only one classroom being described here
        lateinit var classroomId: String
        classroom.keys.forEach { classroomId = it }
        return classroomId
    }
    private fun uploadAssetPart(classroomAndAssetsJSON: JSONObject) {
        val assetPart = classroomAndAssetsJSON.getJSONObject("asset")
        val asset = jsonToMap(assetPart)
        db_classroom_assets_reference().updateChildren(asset)
    }
    fun load(learningProject: String, classroom_id: String, loaded_event: ClassroomLoaded) {
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
                        fill_assets_into_absentees(p0.child("attendance"))
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
                        loadedEvent?.onLoadComplete()
                    }
                    override fun onCancelled(p0: DatabaseError) {
                        println("thumbnails fetch: onCancelled ${p0.toException()}")
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
    fun get_active_chapter(grade: String, subject: String): String? {
        return subject_current_chapter.get(grade_subject(grade, subject))
    }
    fun set_active_chapter(grade: String, subject: String, chapter: String) {
        val gradesub = grade_subject(grade, subject)
        FirebaseDatabase.getInstance().getReference(loadedLearningProject).
                child("classroom_assets").child(loadedClassroomID).
                child("class_subject_current").child(gradesub).setValue(chapter)
        subject_current_chapter[gradesub] = chapter
    }
    fun get_students_in_chapter(grade: String, subject: String, chapter: String): ArrayList<Student> {
        val students_in_chapter = ArrayList<Student>()
        students.forEach {
            if(it.grade == grade && get_active_chapter(grade, subject) == chapter) {
                students_in_chapter.add(it)
            }
        }
        return students_in_chapter
    }
    fun chapter_of_student(student: Student, subject: String, chapters: Chapters): String {
        //TODO: Search thru chapters up to class-current and find the one where the student has pending activities
        var currentChapter = get_active_chapter(student.grade, subject)
        if(currentChapter == null) {
            currentChapter = ContentInteractor().first_chapter(student.grade, subject)
        }
        return currentChapter!!
    }
    fun students_and_chapters(grade: String, subject: String): HashMap<String, ArrayList<Student>> {
        val student_chapters_map = HashMap<String, ArrayList<Student>>()
        val chapters = ContentInteractor().chapters_and_activities(grade, subject)
        for(chapter in chapters.chapter_list) {
            student_chapters_map[chapter.name] = ArrayList<Student>()
        }
        for(student in students.filter { it.grade == grade }) {
            val current_chapter = chapter_of_student(student, subject, chapters)
            student_chapters_map[current_chapter]?.add(student)
        }
        return student_chapters_map
    }
    fun current_chapter_page(student: Student, subject: String): String {
        var chapterName = get_active_chapter(student.grade, subject)
        if(chapterName == null) {
            chapterName = ContentInteractor().first_chapter(student.grade, subject)
        }
        return ContentInteractor().chapters_directory(student.grade, subject) + "/" + chapterName + "/index.html"
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
    @JvmStatic
    fun get_current_week_attendance(): AttendanceInput {
        val week_attendance = AttendanceInput()
        week_attendance.studentList = students

        val date_format = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        week_attendance.holidayList = listOf(calendar.time)
        week_attendance.weekStartDate = calendar.time
        week_attendance.absentees = hashMapOf()
        for (i in 1..6) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val date: Date = calendar.time
            val hyphenated_date: String = date_format.format(date)
            if(hyphenated_date in absentees) {
                week_attendance.absentees[hyphenated_date] = absentees[hyphenated_date]
            }
        }
        val today: String = date_format.format(Calendar.getInstance().time)
        if(today in absentees) {
            week_attendance.presentStudents = ArrayList<String>()
            for(student in students) {
                if(!(student.id in absentees[today]!!)) {
                    week_attendance.presentStudents.add(student.id)
                }
            }
        }
        return week_attendance
    }
    @JvmStatic
    fun filterStudents(selectedGrade: String, selectedGender: String): List<Student> {
        val filteredStudentList: List<Student> = students.filter { it.grade == selectedGrade && it.gender == selectedGender}
        return filteredStudentList
    }
}
