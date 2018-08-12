package com.thinklearn.tide.interactor

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thinklearn.tide.dto.Teacher


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
class ClassroomInteractor {
    lateinit var classroomSnapshot: ClassroomSnapshot

    fun load(learningProject: String, classroom_id: String, loadedEvent: ClassroomLoaded) {
        classroomSnapshot = ClassroomSnapshot(loadedEvent)
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
        return translate_DB_to_teachers(classroomSnapshot.classroom_assets!!.child("teachers"),
                classroomSnapshot.class_thumbnails!!.child("teachers"))
    }
}
