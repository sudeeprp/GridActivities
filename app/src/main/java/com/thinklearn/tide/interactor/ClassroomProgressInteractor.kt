package com.thinklearn.tide.interactor

import com.thinklearn.tide.dto.Student

object ClassroomProgressInteractor {
    fun getActivitiesStatus(studentID: String, subjectID: String, chapterID: String): ArrayList<ActivityStatus> {
        return arrayListOf()
    }
    fun chapter_id_of_student(student: Student, subject: String, chapters: Chapters): String {
        //TODO: Search thru chapters up to class-current and find the one where the student has pending activities
        var current_chapter_id = ClassroomInteractor.get_active_chapter_id(student.grade, subject)
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
        for(student in ClassroomInteractor.students.filter { it.grade == grade && !it.qualifier.contains("guest") }) {
            val current_chapter = chapter_id_of_student(student, subject, chapters)
            student_chapters_map[current_chapter]?.add(student)
        }
        return student_chapters_map
    }

}