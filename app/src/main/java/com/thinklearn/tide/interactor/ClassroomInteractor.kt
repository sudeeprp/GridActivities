package com.thinklearn.tide.interactor

import com.google.firebase.database.*
import com.thinklearn.tide.dto.AttendanceInput
import com.thinklearn.tide.dto.Student
import com.thinklearn.tide.dto.Teacher
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


interface ClassroomLoaded {
    fun onLoadComplete()
}
object ClassroomInteractor {
    lateinit var loadedLearningProject: String
    lateinit var loadedClassroomID: String
    @JvmField
    var teachers = ArrayList<Teacher>()
    @JvmField
    var students = ArrayList<Student>()
    var subject_current_chapter = HashMap<String, String>()
    var loadedEvent: ClassroomLoaded? = null
    var absentees: MutableMap<String, MutableList<String>> = hashMapOf()

    fun db_student_reference(studentId: String): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(loadedLearningProject).
                child("classroom_assets").child(loadedClassroomID).child("students").child(studentId)
    }
    fun db_student_activity_reference(studentRef: DatabaseReference, subject: String, chapter: String,
                                      activity_identifier: String): DatabaseReference {
        return studentRef.child("finished_activities").child(subject).
                child(chapter).child(activity_id_to_firebase_id(activity_identifier))
    }
    fun activity_id_to_firebase_id(activity_id: String?): String? {
        return activity_id?.replace('.', '^')
    }
    fun firebase_id_to_activity_id(firebase_id: String?): String? {
        return firebase_id?.replace('^', '.')
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
            //TODO: Remove this current-chapter in student
            //it.child("current_chapter").children.forEach {
            //    students[i].setCurrentChapter(it.key.toString(), it.value.toString())
            //}
            //Fill first chapter into any subjects that haven't started yet
            //val subjects = ContentInteractor().get_subjects(students[i].grade)
            //for(subject in subjects) {
            //    if(students[i].getCurrentChapter(subject) == null) {
            //        val first_chapter = ContentInteractor().first_chapter(students[i].grade, subject)
            //        students[i].setCurrentChapter(subject, first_chapter)
            //    }
            //}
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
        //TODO: Remove this current-chapter in student
        //for((index, student) in students.filter {it.grade == grade}.withIndex()) {
        //    val updated_subject_chapter = recompute_student_chapter(student, subject)
        //    students[index].setCurrentChapter(subject, updated_subject_chapter)
        //    val studentRef = db_student_reference(student.id)
        //    studentRef.child("current_chapter").child(subject).setValue(updated_subject_chapter)
        //}
    }
    fun get_students_in_chapter(grade: String, subject: String, chapter: String): ArrayList<Student> {
        val students_in_chapter = ArrayList<Student>()
        students.forEach {
            //TODO: Remove this current-chapter in student
            //if(it.grade == grade && it.getCurrentChapter(subject) == chapter) {
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
        //TODO: Remove this current-chapter in student
        //var chapterName = student.getCurrentChapter(subject)
        var chapterName = get_active_chapter(student.grade, subject)
        if(chapterName == null) {
            chapterName = ContentInteractor().first_chapter(student.grade, subject)
        }
        return ContentInteractor().chapters_directory(student.grade, subject) + "/" + chapterName + "/index.html"
    }
    //fun recompute_student_chapter(student: Student?, subject: String): String {
    //    var recomputed_chapter_name = ""
    //    if(student != null) {
    //        val chapters = ContentInteractor().chapters_and_activities(student.grade, subject)
    //        val current_class_chapter = get_active_chapter(student.grade, subject)
    //        for(chapter in chapters.chapter_list) {
    //            if(chapter.name == current_class_chapter) {
    //                recomputed_chapter_name = chapter.name
    //                break
    //            }
    //        }
    //    } else {
    //        Log.e("Recompute chapter", "Student not found")
    //    }
    //    return recomputed_chapter_name
    //}
    fun set_student_activity_status(studentId: String, activity_subject: String, activity_chapter: String,
                               activity_identifier: String, activity_datapoint: String) {
        val studentRef = db_student_reference(studentId)
        val studentActivityRef =
                db_student_activity_reference(studentRef, activity_subject, activity_chapter, activity_identifier)

        studentActivityRef.child("data_point").setValue(activity_datapoint)
        val currentTime = Calendar.getInstance().getTime()
        val currentTimestamp = SimpleDateFormat("yyyy-MM-dd").format(currentTime) + "T" +
                SimpleDateFormat("HH:mm:ssz").format(currentTime)
        studentActivityRef.child("time_stamp").setValue(currentTimestamp)

        //TODO: Remove this current-chapter in student
        //Log.d("Recompute chapter", "For student id" + studentId)
        //val updated_subject_chapter = recompute_student_chapter(get_student(studentId), activity_subject)
        //studentRef.child("current_chapter").child(activity_subject).setValue(updated_subject_chapter)
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
}
