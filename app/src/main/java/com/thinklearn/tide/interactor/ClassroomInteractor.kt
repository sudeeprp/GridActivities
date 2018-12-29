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
    var presentAM: MutableMap<String, MutableList<String>> = hashMapOf()
    var presentPM: MutableMap<String, MutableList<String>> = hashMapOf()

    fun learningProject(): String {
        if(loadedLearningProject == "") {
            return ClassroomConfig.getConfig(ClassroomConfig.learning_project_file, ClassroomConfig.project_name_key, "ICDev")
        } else {
            return loadedLearningProject
       }
    }
    fun selectedClass(): String {
        return ClassroomConfig.getConfig(ClassroomConfig.selected_class_file, ClassroomConfig.selected_class_key, "")
    }
    fun selectedMode(): String {
        return ClassroomConfig.getConfig(ClassroomConfig.selected_mode_file, ClassroomConfig.selected_mode_key, "")
    }
    @JvmStatic
    fun setTabMode(mode: String) {
        ClassroomConfig.writeConfig(ClassroomConfig.selected_mode_file, ClassroomConfig.selected_mode_key, mode)
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
    @JvmStatic
    fun get_current_week_attendance(): AttendanceInput {
        val week_attendance = AttendanceInput()
        week_attendance.studentList = students.filter { !isGuest(it) }

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
    fun isGuest(student: Student): Boolean {
        return student.qualifier.contains("guest")
    }
    @JvmStatic
    fun filterStudents(selectedGrade: String, includeGuest: Boolean = true): List<Student> {
        return filterStudents(selectedGrade, "", includeGuest)
    }
    @JvmStatic
    fun filterStudents(selectedGrade: String, selectedGender: String, includeGuest: Boolean = true): List<Student> {
        var filteredStudentList: List<Student> = students.filter { it.grade == selectedGrade }
        if(selectedGender.isNotEmpty()) {
            filteredStudentList = filteredStudentList.filter { it.gender == selectedGender }
        }
        if(!includeGuest) {
            filteredStudentList = filteredStudentList.filter { !isGuest(it) }
        }
        return filteredStudentList
    }
}
