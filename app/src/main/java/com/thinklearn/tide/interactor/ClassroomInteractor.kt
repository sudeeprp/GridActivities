package com.thinklearn.tide.interactor

import com.thinklearn.tide.dto.AttendanceInput
import com.thinklearn.tide.dto.Student
import com.thinklearn.tide.dto.Teacher
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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
    @JvmStatic
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
