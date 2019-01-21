package com.thinklearn.tide.activitydriver

import com.thinklearn.tide.dto.AcademicRecords
import com.thinklearn.tide.interactor.Chapters
import com.thinklearn.tide.interactor.ProgressInteractor
import org.junit.Test

import org.junit.Assert.*

class AssessmentUnitTest {
    val big_datapoint = "{\\\"Time taken in seconds\\\": 12,\\\"Maximum score\\\": 3,\\\"Actual score\\\": 3,\\\"No.of errors\\\": 0,\\\"Correct(1),wrong(0),not attempted(n)\\\":[1,1,1],\\\"No. of error prompts\\\":0}"
    val brief_datapoint = "done"
    fun fill_curriculum(): Chapters {
        val chapterList = arrayListOf<Chapters.Chapter>()
        chapterList.add(Chapters.Chapter(
                "chap11", "chap11",
                arrayListOf(Chapters.ActivityInChapter("activity111", false),
                            Chapters.ActivityInChapter("activity112", true),
                            Chapters.ActivityInChapter("activity113", false)
                )
        ))
        chapterList.add(Chapters.Chapter(
                "chap12", "chap11",
                arrayListOf(Chapters.ActivityInChapter("activity121", false),
                        Chapters.ActivityInChapter("activity122", true),
                        Chapters.ActivityInChapter("activity123", false)
                )
        ))
        chapterList.add(Chapters.Chapter(
                "chap13", "chap11",
                arrayListOf(Chapters.ActivityInChapter("activity131", false),
                        Chapters.ActivityInChapter("activity132", true),
                        Chapters.ActivityInChapter("activity133", false)
                )
        ))
        return Chapters(chapterList)
    }
    fun academicsNotCompletedFirstChapter(): AcademicRecords {
        val academicRecords = AcademicRecords()
        with(academicRecords) {
            setActivityAttribute("sub1", "chap11", "activity111", "data_point", big_datapoint)
            setActivityAttribute("sub1", "chap12", "activity121", "data_point", brief_datapoint)
        }
        return academicRecords
    }
    fun academicsWithMissingChapters(): AcademicRecords {
        val academicRecords = AcademicRecords()
        with(academicRecords) {
            setActivityAttribute("sub1", "chap13", "activity131", "data_point", big_datapoint)
            setActivityAttribute("sub1", "chap13", "activity131", "data_point", brief_datapoint)
        }
        return academicRecords
    }
    fun academicsCompletedFirstChapter(): AcademicRecords {
        val academicRecords = AcademicRecords()
        with(academicRecords) {
            setActivityAttribute("sub1", "chap11", "activity111", "status", "done")
            setActivityAttribute("sub1", "chap11", "activity111", "data_point", big_datapoint)
            setActivityAttribute("sub1", "chap11", "activity112", "status", "done")
            setActivityAttribute("sub1", "chap11", "activity112", "data_point", big_datapoint)
            setActivityAttribute("sub1", "chap12", "activity121", "data_point", brief_datapoint)
        }
        return academicRecords
    }
    fun fill_academicRecords(): AcademicRecords {
        val academicRecords = AcademicRecords()
        with(academicRecords) {
            setActivityAttribute("sub1", "chap11", "activity111", "status", "done")
            setActivityAttribute("sub1", "chap11", "activity111", "data_point", big_datapoint)
            setActivityAttribute("sub1", "chap12", "activity121", "data_point", brief_datapoint)
        }
        return academicRecords
    }
    @Test
    fun activityAttribute_isPersisted() {
        val academicRecords = fill_academicRecords()
        val activityAttr1 = academicRecords.getActivityAttribute("sub1", "chap11",
                "activity111", "status")
        assertEquals("done", activityAttr1)
        val activityAttr2 = academicRecords.getActivityAttribute("sub1", "chap11",
                "activity111", "data_point")
        assertEquals(big_datapoint, activityAttr2)
        val activityAttr12 = academicRecords.getActivityAttribute("sub1", "chap12",
                "activity121", "data_point")
        assertEquals(brief_datapoint, activityAttr12)
    }
    @Test
    fun activityAttribute_isOverwritten() {
        val academicRecords = fill_academicRecords()
        academicRecords.setActivityAttribute("sub1", "chap11",
                "activity111", "key1111", "value1111-")
        val activityAttr = academicRecords.getActivityAttribute("sub1", "chap11",
                "activity111", "key1111")
        assertEquals("value1111-", activityAttr)
    }
    //@Test
    fun studentActivityRecord_isCorrect() {
        val academicRecords = fill_academicRecords()
        val assessmentRecords = ProgressInteractor.getStudentActivityRecords(academicRecords.getSubjectRecords())
        assessmentRecords.forEach {
            println("{" + it.subjectID + " " + it.chapterID + " " + it.activityID + " " + it.assessment_status +
                    " " + it.time_in_sec + " " + it.max_score + " " + it.actual_score + " " + it.evaluation + "}")
        }
        assertEquals(2, assessmentRecords.size)
    }
    @Test
    fun activitiesStatus_isCorrect() {
        val chapter = Chapters.Chapter(id = "chap11", name = "chap11",
                activities = arrayListOf(Chapters.ActivityInChapter(activity_identifier = "activity111", mandatory = false)))
        val activityAcademics = fill_academicRecords().getSubjectRecords()["sub1"]!!.chapterRecords["chap11"]!!
        val activitiesStatus = ProgressInteractor.getActivitiesStatus(chapter, activityAcademics)
        activitiesStatus.forEach {
            println("{" + it.activityID + " " + it.status + "}")
            assertEquals("done", it.status)
        }
    }
    @Test
    fun whenAssessmentsComplete_currentChapter_isCorrect() {
        val chapterRecords = academicsCompletedFirstChapter().getSubjectRecords()["sub1"]!!
        val chapters = fill_curriculum()
        val currentChapter = ProgressInteractor.computeCurrentChapter("chap12", chapters, chapterRecords)
        assertEquals("chap12", currentChapter)
    }
    @Test
    fun whenAssessmentsNotComplete_currentChapter_isCorrect() {
        val chapterRecords = academicsNotCompletedFirstChapter().getSubjectRecords()["sub1"]!!
        val chapters = fill_curriculum()
        val currentChapter = ProgressInteractor.computeCurrentChapter("chap12", chapters, chapterRecords)
        assertEquals("chap11", currentChapter)
    }
    @Test
    fun whenNoAcademicRecords_currentChapter_isCorrect() {
        val chapters = fill_curriculum()
        val currentChapter = ProgressInteractor.computeCurrentChapter("chap12", chapters, null)
        assertEquals("chap11", currentChapter)
    }
    @Test
    fun whenNoChapterRecords_currentChapter_isCorrect() {
        val chapterAcademics = academicsWithMissingChapters().getSubjectRecords()["sub1"]!!
        val chapters = fill_curriculum()
        val currentChapter = ProgressInteractor.computeCurrentChapter("chap12", chapters, chapterAcademics)
        assertEquals("chap11", currentChapter)
    }
}
