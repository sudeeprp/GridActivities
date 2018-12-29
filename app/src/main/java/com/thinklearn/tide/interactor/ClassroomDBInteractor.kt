package com.thinklearn.tide.interactor

import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

object ClassroomDBInteractor {
    var loadedEvent: DBOpDone? = null
    fun removeLoadedEvent() {
        loadedEvent = null
    }
    fun load(learningProject: String, classroom_id: String, loaded_event: DBOpDone) {
        ClassroomConfig.writeConfig(ClassroomConfig.learning_project_file, ClassroomConfig.project_name_key, learningProject)
        ClassroomConfig.writeConfig(ClassroomConfig.selected_class_file, ClassroomConfig.selected_class_key, classroom_id)
        ClassroomInteractor.loadedLearningProject = learningProject
        ClassroomInteractor.loadedClassroomID = classroom_id
        loadedEvent = loaded_event
        ClassroomInteractor.reset_lists()
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
                        ClassroomInteractor.class_name = p0.child("class_name").value.toString()
                        ClassroomInteractor.school_name = p0.child("school_name").value.toString()
                    }
                    override fun onCancelled(p0: DatabaseError) {
                        println("classrooms fetch: onCancelled ${p0.toException()}")
                    }
                })
    }
    fun db_classrooms_reference(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(ClassroomInteractor.learningProject()).child("classrooms")
    }
    fun db_classroom_assets_reference(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(ClassroomInteractor.learningProject()).child("classroom_assets")
    }
    fun db_teachers_reference(classroomId: String): DatabaseReference {
        return db_classroom_assets_reference().child(classroomId).child("teachers")
    }
    fun db_students_reference(classroomId: String): DatabaseReference {
        return db_classroom_assets_reference().child(classroomId).child("students")
    }
    fun db_onestudent_reference(studentId: String): DatabaseReference {
        return db_students_reference(ClassroomInteractor.loadedClassroomID).child(studentId)
    }
    fun db_student_activity_reference(studentRef: DatabaseReference, subject: String, chapter: String,
                                      activity_identifier: String): DatabaseReference {
        return studentRef.child("finished_activities").child(subject).
                child(chapter).child(activity_id_to_firebase_id(activity_identifier))
    }
    fun db_activity_log_reference(): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(ClassroomInteractor.learningProject()).child("activity_log")
    }
    fun activity_id_to_firebase_id(activity_id: String): String {
        return activity_id.replace('.', '^')
    }
    fun firebase_id_to_activity_id(firebase_id: String): String {
        return firebase_id.replace('^', '.')
    }
    fun fill_assets_into_teachers(teachers_snapshot: DataSnapshot?) {
        teachers_snapshot?.children?.forEach {
            val i = ClassroomInteractor.get_teacher_index(it.key!!)
            ClassroomInteractor.teachers[i].teacherName = it.child("name").value.toString()
        }
    }
    fun fill_thumbnails_into_teachers(teachers_thumbnails: DataSnapshot?) {
        teachers_thumbnails?.children?.forEach {
            val i = ClassroomInteractor.get_teacher_index(it.key!!)
            ClassroomInteractor.teachers[i].thumbnail = it.child("thumbnail").value.toString()
        }
    }
    fun fill_assets_into_students(students_snapshot: DataSnapshot?) {
        students_snapshot?.children?.forEach {
            val i = ClassroomInteractor.get_student_index(it.key!!)
            ClassroomInteractor.students[i].firstName = it.child("first_name").value.toString()
            ClassroomInteractor.students[i].surname = it.child("surname").value.toString()

            var dob_day = it.child("birth_date").child("dd").value
            var dob_month = it.child("birth_date").child("mm").value
            val dob_year = it.child("birth_date").child("yyyy").value
            if(dob_year != null && dob_year != "") {
                if(dob_day == null || dob_day == "") dob_day = "1"
                if(dob_month == null || dob_month == "") dob_month = "1"
                ClassroomInteractor.students[i].birthDate = SimpleDateFormat("dd/MM/yyyy").parse(dob_day.toString() + "/" + dob_month.toString() + "/" + dob_year.toString())
            }
            ClassroomInteractor.students[i].gender = it.child("gender").value.toString()
            ClassroomInteractor.students[i].grade = it.child("grade").value.toString()
            ClassroomInteractor.students[i].qualifier = it.child("qualifier").value.toString()
            if(ClassroomInteractor.students[i].qualifier == null) ClassroomInteractor.students[i].qualifier = ""
        }
        ClassroomInteractor.students.sortWith(compareBy { it.surname })
    }
    fun fill_thumbnails_into_students(students_thumbnails: DataSnapshot?) {
        students_thumbnails?.children?.forEach {
            val i = ClassroomInteractor.get_student_index(it.key!!)
            ClassroomInteractor.students[i].thumbnail = it.child("thumbnail").value.toString()
        }
    }
    @JvmStatic
    fun set_student_thumbnail(studentId: String, thumbnail: String) {
        FirebaseDatabase.getInstance().getReference(ClassroomInteractor.loadedLearningProject).child("thumbnails").
                child("classrooms").child(ClassroomInteractor.loadedClassroomID).child("students").child(studentId).
                child("thumbnail").setValue(thumbnail)
    }
    fun fill_assets_into_current_chapters(subject_chapter: DataSnapshot?) {
        ClassroomInteractor.subject_current_chapter.clear()
        subject_chapter?.children?.forEach {
            ClassroomInteractor.subject_current_chapter.put(it.key!!, it.value.toString())
        }
    }
    fun fill_assets_into_present(attendance_snapshot: DataSnapshot?) {
        ClassroomInteractor.presentAM.clear()
        ClassroomInteractor.presentPM.clear()
        val t = object : GenericTypeIndicator<ArrayList<String>>() {}
        attendance_snapshot?.children?.forEach {
            val date = it.key.toString()
            val presentAM_ids = it.child("presentAM").getValue(t)
            if(presentAM_ids != null) ClassroomInteractor.presentAM[date] = presentAM_ids
            val presentPM_ids = it.child("presentPM").getValue(t)
            if(presentPM_ids != null) ClassroomInteractor.presentPM[date] = presentPM_ids
        }
    }
    @JvmStatic
    fun set_day_presents(date: String, dayPresentAM: List<String>?, dayPresentPM: List<String>?) {
        //If we get an attendance-set-request before students, ignore
        //presentees is coming from java, where it can be null.
        if(ClassroomInteractor.students.size == 0) {
            return
        }
        val attendance_date_ref = FirebaseDatabase.getInstance().getReference(ClassroomInteractor.loadedLearningProject).child("classroom_assets").
                child(ClassroomInteractor.loadedClassroomID).child("attendance").child(date)
        attendance_date_ref.child("presentAM").setValue(dayPresentAM)
        attendance_date_ref.child("presentPM").setValue(dayPresentPM)
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
                activity, ClassroomInteractor.loadedClassroomID,
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date()))
        activityLogRef.push().setValue(activityLogEntry)
    }
    fun set_active_chapter(grade: String, subject: String, chapter: String) {
        val gradesub = ClassroomInteractor.grade_subject(grade, subject)
        FirebaseDatabase.getInstance().getReference(ClassroomInteractor.loadedLearningProject).child("classroom_assets").child(ClassroomInteractor.loadedClassroomID).child("class_subject_current").child(gradesub).setValue(chapter)
        ClassroomInteractor.subject_current_chapter[gradesub] = chapter
        write_activity_log("{\"activity\": \"Chapter activation\", " +
                "\"grade\": \"$grade\", " +
                "\"subject\": \"$subject\", " +
                "\"chapter\": \"$chapter\"}")
    }
}
