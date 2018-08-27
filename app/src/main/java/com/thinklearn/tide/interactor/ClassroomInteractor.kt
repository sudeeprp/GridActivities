package com.thinklearn.tide.interactor

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thinklearn.tide.dto.AttendanceInput
import com.thinklearn.tide.dto.Student
import com.thinklearn.tide.dto.Teacher
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.google.firebase.database.GenericTypeIndicator
import kotlin.collections.HashSet
//import jdk.nashorn.internal.objects.NativeDate.getTime


interface ClassroomLoaded {
    fun onLoadComplete()
}
object ClassroomInteractor {
    lateinit var loadedLearningProject: String
    lateinit var loadedClassroomID: String
    var teachers = ArrayList<Teacher>()
    @JvmField
    var students = ArrayList<Student>()
    var subject_current_chapter = HashMap<String, String>()
    var loadedEvent: ClassroomLoaded? = null
    var absentees: MutableMap<String, MutableList<String>> = hashMapOf()

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
            val i = get_teacher_index(it.key)
            teachers[i].teacherName = it.child("name").value.toString()
        }
    }
    fun fill_thumbnails_into_teachers(teachers_thumbnails: DataSnapshot?) {
        teachers_thumbnails?.children?.forEach {
            val i = get_teacher_index(it.key)
            teachers[i].thumbnail = it.child("thumbnail").value.toString()
        }
    }
    fun fill_assets_into_students(students_snapshot: DataSnapshot?) {
        students_snapshot?.children?.forEach {
            val i = get_student_index(it.key)
            students[i].firstName = it.child("first_name").value.toString()
            students[i].surname = it.child("surname").value.toString()
            students[i].birthDate = SimpleDateFormat("dd/MM/yyyy").
                    parse(it.child("birth_date").child("dd").value.toString() + "/" +
                            it.child("birth_date").child("mm").value.toString() + "/" +
                            it.child("birth_date").child("yyyy").value.toString())
            students[i].gender = it.child("gender").value.toString()
            students[i].grade = it.child("grade").value.toString()
            it.child("current_chapter").children.forEach {
                students[i].setCurrentChapter(it.key.toString(), it.value.toString())
            }
        }
    }
    fun fill_thumbnails_into_students(students_thumbnails: DataSnapshot?) {
        students_thumbnails?.children?.forEach {
            val i = get_student_index(it.key)
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
        //TODO: In all fill_assets_ functions, remove the old entries before filling new ones.
        subject_chapter?.children?.forEach {
            subject_current_chapter.put(it.key, it.value.toString())
        }
    }
    fun fill_assets_into_absentees(attendance_snapshot: DataSnapshot?) {
        val t = object : GenericTypeIndicator<ArrayList<String>>() {}
        attendance_snapshot?.children?.forEach {
            val date = it.key
            val absentee_ids = it.child("absent").getValue(t)
            if(absentee_ids != null) {
                absentees[date] = absentee_ids
            }
        }
    }
    @JvmStatic
    fun set_day_absents(date: String, absentees: ArrayList<String>) {
        if(absentees.size == 0) {
            FirebaseDatabase.getInstance().getReference(loadedLearningProject).child("classroom_assets").
                    child(loadedClassroomID).child("attendance").child(date).child("full_class").setValue(":)")
        }
        FirebaseDatabase.getInstance().getReference(loadedLearningProject).child("classroom_assets").
                child(loadedClassroomID).child("attendance").child(date).child("absent").setValue(absentees)
    }
    @JvmStatic
    fun set_day_presents(date: String, presentees: List<String>) {
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
    fun load(learningProject: String, classroom_id: String, loaded_event: ClassroomLoaded) {
        loadedLearningProject = learningProject
        loadedClassroomID = classroom_id
        loadedEvent = loaded_event
        reset_lists()
        FirebaseDatabase.getInstance().getReference(learningProject).child("classroom_assets").
                child(classroom_id).addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot?) {
                        fill_assets_into_teachers(p0?.child("teachers"))
                        fill_assets_into_students(p0?.child("students"))
                        fill_assets_into_current_chapters(p0?.child("class_subject_current"))
                        fill_assets_into_absentees(p0?.child("attendance"))
                    }
                    override fun onCancelled(p0: DatabaseError?) {
                        println("classroom_assets fetch: onCancelled ${p0?.toException()}")
                    }
                })
        FirebaseDatabase.getInstance().getReference(learningProject).child("thumbnails").child("classrooms").
                child(classroom_id).addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot?) {
                        fill_thumbnails_into_teachers(p0?.child("teachers"))
                        fill_thumbnails_into_students(p0?.child("students"))
                        //TODO: This will not work if thumbnails come first
                        loadedEvent?.onLoadComplete()
                    }
                    override fun onCancelled(p0: DatabaseError?) {
                        println("thumbnails fetch: onCancelled ${p0?.toException()}")
                    }
                })
        FirebaseDatabase.getInstance().getReference(learningProject).child("classrooms")
                .child(classroom_id).addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot?) {
                        //TODO: add memeber function to fill classroom descriptor and call it here
                    }
                    override fun onCancelled(p0: DatabaseError?) {
                        println("classrooms fetch: onCancelled ${p0?.toException()}")
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
    fun get_current_chapter(grade_subject: String): String? {
        return subject_current_chapter.get(grade_subject)
    }
    fun set_active_chapter(grade_subject: String, chapter: String) {
        FirebaseDatabase.getInstance().getReference(loadedLearningProject).
                child("classroom_assets").child(loadedClassroomID).
                child("class_subject_current").child(grade_subject).setValue(chapter)
        subject_current_chapter[grade_subject] = chapter
    }
    fun get_students_in_chapter(grade: String, subject: String, chapter: String): ArrayList<Student> {
        var students_in_chapter = ArrayList<Student>()
        students.forEach {
            if(it.grade == grade) {
                //TODO: Check if it's cleaner to put this in fill-to-asset logic
                var currentChapter = it.getCurrentChapter(subject)
                if(currentChapter == null) {
                    currentChapter = ContentInteractor().first_chapter(it.grade, subject)
                }
                if(currentChapter == chapter) {
                    students_in_chapter.add(it)
                }
            }
        }
        return students_in_chapter
    }
    fun student_activity_complete(student: Student, subject: String, activityName: String) {
        //TODO: <<student.addActivityCompletion(subject, activityName)
    }
    @JvmStatic
    fun get_current_week_attendance(): AttendanceInput {
        val week_attendance = AttendanceInput()
        week_attendance.studentList = students

        val date_format = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        //TODO: Add after testing <<week_attendance.holidayList.add(calendar.time)
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
}
