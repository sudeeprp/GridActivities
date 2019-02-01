package com.thinklearn.tide.interactor

import com.thinklearn.tide.dto.AcademicRecords
import com.thinklearn.tide.dto.ChapterID
import com.thinklearn.tide.dto.Student

object ClassroomProgressInteractor {
    @JvmStatic
    fun getActivityRecords(student: Student): ArrayList<ActivityRecord>? {
        var activityRecords: ArrayList<ActivityRecord>? = null
        if(student.academicRecords != null) {
            activityRecords = ProgressInteractor.getStudentActivityRecords(student.academicRecords.getSubjectRecords())
        }
        return activityRecords
    }
    @JvmStatic
    fun getAssessmentRecords(student: Student): ArrayList<ActivityRecord>? {
        var assessmentRecords: ArrayList<ActivityRecord>? = null
        val activityRecords = getActivityRecords(student)
        if (activityRecords != null) {
            assessmentRecords = ProgressInteractor.filter_assessments(activityRecords)
        }
        return assessmentRecords
    }
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
    fun chapter_id_of_student(student: Student, subject: String): String {
        return chapter_id_of_student(student, subject,
                ContentInteractor().chapters_and_activities(student.grade, subject))
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
            student_chapters_map[chapter.id] = ArrayList<Student>()
        }
        for(student in ClassroomInteractor.students.filter { it.grade == grade && !ClassroomInteractor.isGuest(it) }) {
            val current_chapter = chapter_id_of_student(student, subject, chapters)
            student_chapters_map[current_chapter]?.add(student)
        }
        return student_chapters_map
    }
    fun assessment_is_pending(students: List<Student>): Boolean {
        var pending_found = false
        for(student in students) {
            val pending_count = ClassroomProgressInteractor.getActivityRecords(student)?.
                    filter { it.assessment_status == ProgressInteractor.assessment_ready_status }?.size
            if ( pending_count != null && pending_count > 0) {
                pending_found = true
                break
            }
        }
        return pending_found
    }
    @JvmStatic
    fun class_has_assessment_pending(): Boolean {
        return assessment_is_pending(ClassroomInteractor.students)
    }
    @JvmStatic
    fun grade_has_assessment_pending(grade: String): Boolean {
        return assessment_is_pending(ClassroomInteractor.students.
                filter { it.grade == grade && !ClassroomInteractor.isGuest(it) })
    }
    @JvmStatic
    fun subject_has_assessment_pending(grade: String, subjectID: String): Boolean {
        var assessment_pending = false
        val students_in_grade = ClassroomInteractor.students.
                filter { it.grade == grade && !ClassroomInteractor.isGuest(it) }
        for(student in students_in_grade) {
            val activityRecords = ClassroomProgressInteractor.getActivityRecords(student)?.
                    filter { it.assessment_status == ProgressInteractor.assessment_ready_status &&
                             it.subjectID == subjectID}
            if(activityRecords != null && activityRecords.size > 0) {
                assessment_pending = true
                break
            }
        }
        return assessment_pending
    }
}
