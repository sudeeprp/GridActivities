package com.thinklearn.tide.interactor

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thinklearn.tide.dto.Student
import com.thinklearn.tide.dto.Teacher
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


fun translate_DB_to_teachers(teachers_data: DataSnapshot, teachers_thumbnails: DataSnapshot): ArrayList<Teacher> {
    var teachers = ArrayList<Teacher>()
    teachers_data.children.forEach {
        val teacher = Teacher()
        teacher.id = it.key.toString()
        teacher.teacherName = it.child("name").value.toString()
        teacher.thumbnail = teachers_thumbnails.child(teacher.id).child("thumbnail").value.toString()
        teachers.add(teacher)
    }
    return teachers
}

fun translate_DB_to_students(students_data: DataSnapshot, students_thumbnails: DataSnapshot): ArrayList<Student> {
    var students = ArrayList<Student>()
    students_data.children.forEach {
        val student = Student()
        student.id = it.key.toString()
        val older = it.child("name").value.toString()
        student.firstName = it.child("first_name").value.toString()
        student.surname = it.child("surname").value.toString()
        student.birthDate = SimpleDateFormat("dd/MM/yyyy").
                parse(it.child("birth_date").child("dd").value.toString() + "/" +
                        it.child("birth_date").child("mm").value.toString() + "/" +
                        it.child("birth_date").child("yyyy").value.toString())
        student.gender = it.child("gender").value.toString()
        student.grade = it.child("grade").value.toString()
        student.thumbnail = students_thumbnails.child(student.id).child("thumbnail").value.toString()
        it.child("current_chapter").children.forEach {
            student.setCurrentChapter(it.key.toString(), it.value.toString())
        }
        students.add(student)
    }
    return students
}

fun translate_DB_to_subject_current(subject_current_chapter: DataSnapshot): HashMap<String, String> {
    val subject_chapter = HashMap<String, String>()
    subject_current_chapter.children.forEach {
        subject_chapter.put(it.key.toString(), it.value.toString())
    }
    return subject_chapter
}

interface ClassroomLoaded {
    fun onLoadComplete()
}
class ClassroomSnapshot(val loadCompleteTrigger: ClassroomLoaded) {
    var classroom_desc: DataSnapshot? = null
    set(value) {
        field = value
        check_and_trigger_complete()
    }
    var classroom_assets: DataSnapshot? = null
    set(value) {
        field = value
        check_and_trigger_complete()
    }
    var class_thumbnails: DataSnapshot? = null
    set(value) {
        field = value
        check_and_trigger_complete()
    }
    fun check_and_trigger_complete() {
        if(classroom_desc?.exists() != null && classroom_assets?.exists() != null &&
            class_thumbnails?.exists() != null) {
            loadCompleteTrigger.onLoadComplete()
        }
    }
}
object ClassroomInteractor {
    lateinit var classroomSnapshot: ClassroomSnapshot //TODO: Can remove
    lateinit var loadedLearningProject: String
    lateinit var loadedClassroomID: String
    lateinit var teachers: ArrayList<Teacher>
    lateinit var students: ArrayList<Student>
    lateinit var subject_current_chapter: HashMap<String, String>

    fun load(learningProject: String, classroom_id: String, loadedEvent: ClassroomLoaded) {
        classroomSnapshot = ClassroomSnapshot(object: ClassroomLoaded {
            override fun onLoadComplete() {
                teachers = translate_DB_to_teachers(classroomSnapshot.classroom_assets!!.child("teachers"),
                        classroomSnapshot.class_thumbnails!!.child("teachers"))
                students = translate_DB_to_students(classroomSnapshot.classroom_assets!!.child("students"),
                        classroomSnapshot.class_thumbnails!!.child("students"))
                subject_current_chapter = translate_DB_to_subject_current(classroomSnapshot.classroom_assets!!.
                        child("class_subject_current"))
                loadedEvent.onLoadComplete()
            }
        })
        loadedLearningProject = learningProject
        loadedClassroomID = classroom_id
        FirebaseDatabase.getInstance().getReference(learningProject).child("classroom_assets").
                child(classroom_id).addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot?) {
                        classroomSnapshot.classroom_assets = p0!!
                    }
                    override fun onCancelled(p0: DatabaseError?) {
                        println("classroom_assets fetch: onCancelled ${p0?.toException()}")
                    }
                })
        FirebaseDatabase.getInstance().getReference(learningProject).child("thumbnails").child("classrooms").
                child(classroom_id).addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot?) {
                        classroomSnapshot.class_thumbnails = p0!!
                    }
                    override fun onCancelled(p0: DatabaseError?) {
                        println("thumbnails fetch: onCancelled ${p0?.toException()}")
                    }
                })
        FirebaseDatabase.getInstance().getReference(learningProject).child("classrooms")
                .child(classroom_id).addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot?) {
                        classroomSnapshot.classroom_desc = p0!!
                    }
                    override fun onCancelled(p0: DatabaseError?) {
                        println("classrooms fetch: onCancelled ${p0?.toException()}")
                    }
                })
    }

    fun get_teachers(): ArrayList<Teacher> {
        return teachers
    }
    fun get_students(): ArrayList<Student> {
        return students
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
            if(it.grade == grade && it.getCurrentChapter(subject) == chapter) {
                students_in_chapter.add(it)
            }
        }
        return students_in_chapter
    }
}
