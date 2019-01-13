package com.thinklearn.tide.interactor

import com.thinklearn.tide.dto.AcademicRecords
import com.thinklearn.tide.dto.ChapterID
import com.thinklearn.tide.dto.Student

object ClassroomProgressInteractor {
    fun getActivitiesStatus(student: Student, subjectID: String, chapterID: String): ArrayList<ActivityStatus> {
        var activitiesStatus: ArrayList<ActivityStatus> = arrayListOf()
        val chapter = ContentInteractor().chapters_and_activities(student.grade, subjectID).getChapter(chapterID)
        if(chapter != null) {
            val activityAcademics = ProgressInteractor
                    .getActivityAcademics(getChapterAcademics(student, subjectID), chapterID)
            activitiesStatus = ProgressInteractor.getActivitiesStatus(chapter, activityAcademics)
        }
        return activitiesStatus
    }
    fun getChapterAcademics(student: Student, subject: String): AcademicRecords.ChapterAcademics? {
        var chapterAcademics: AcademicRecords.ChapterAcademics? = null
        if(student.academicRecords != null) {
            chapterAcademics = student.academicRecords.getSubjectRecords()[subject]
        }
        return chapterAcademics
    }
    fun chapter_id_of_student(student: Student, subject: String, chapters: Chapters): String {
        var classCurrentChapter = ClassroomInteractor.get_active_chapter_id(student.grade, subject)
        if(classCurrentChapter == null) {
            classCurrentChapter = ContentInteractor().first_chapter_id(student.grade, subject)?:"null"
        }
        return ProgressInteractor.computeCurrentChapter(classCurrentChapter, chapters,
                    getChapterAcademics(student, subject))
    }
    fun chapter_status_of_student(student: Student, subject: String, chapterID: ChapterID): String {
        var chapterStatus = ProgressInteractor.none_status
        val chapter = ContentInteractor().chapters_and_activities(student.grade, subject)
                .getChapter(chapterID)
        val activityAcademics = ProgressInteractor
                .getActivityAcademics(getChapterAcademics(student, subject), chapterID)
        if(chapter != null) {
            chapterStatus = ProgressInteractor.activitiesSummaryStatus(chapter, activityAcademics)
        }
        return chapterStatus
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