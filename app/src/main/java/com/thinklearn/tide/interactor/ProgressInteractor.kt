package com.thinklearn.tide.interactor

import android.util.Log
import com.thinklearn.tide.dto.AcademicRecords
import org.json.JSONException
import org.json.JSONObject

class AssessmentRecord(
        val subject_name: String,
        val chapter_name: String,
        val assessment_name: String,
        val time_in_sec: String,
        val max_score: String,
        val actual_score: String,
        val evaluation: ArrayList<String>?,
        val assessment_status: String?)

class ActivityStatus(val activityID: String, val status: String)

object ProgressInteractor {
    //Interpreted status
    val assessment_ready_status = "assessment_ready"
    val to_be_done_status = "to_be_done"
    val inprogress_status = "inprogress"
    val approved_status = "approved"
    val done_status = "done"
    val none_status = "none"

    //Keys from the DB
    val status_key = "status"
    val data_point_key = "data_point"
    //Keys inside the data point
    val time_in_sec_key = "Time taken in seconds"
    val max_score_key = "Maximum score"
    val actual_score_key = "Actual score"
    val eval_key = "Correct(1),wrong(0),not attempted(n)"

    //For student status screen, where activity status can be set
    //This goes by student record, *independent* of curriculum updates. So all data has to be in the subjectRecords only
    fun getStudentActivityRecords(subjectRecords: HashMap<String, AcademicRecords.ChapterAcademics>): ArrayList<AssessmentRecord> {
        val assessments = arrayListOf<AssessmentRecord>()
        subjectRecords.forEach {
            val subjectName = it.key
            it.value.chapterRecords.forEach {
                val chapterName = it.key
                it.value.activityRecords.forEach {
                    val activityAttrs = it.value.activityAttributes
                    assessments.add(makeAssessmentRecord(subjectName, chapterName,
                            assessmentName = it.key,
                            assessmentStatus = activityAttrs[status_key],
                            datapoint = activityAttrs[data_point_key]))
                }
            }
        }
        return assessments
    }
    //For display in the student-login-grid and for chapter-level-summary to compute current chapter
    fun getActivitiesStatus(chapter: Chapters.Chapter, activityAcademics: AcademicRecords.ActivityAcademics?)
            : ArrayList<ActivityStatus> {
        val activitiesStatus = arrayListOf<ActivityStatus>()
        chapter.activities.forEach {
            val is_mandatory = it.mandatory
            val is_foundInAcademics: Boolean
                    = activityAcademics?.activityRecords?.containsKey(it.activity_identifier)?:false
            var statusRecord = ""
            if(is_foundInAcademics &&
               activityAcademics!!.activityRecords[it.activity_identifier]!!.activityAttributes.containsKey("status")) {
                  statusRecord = activityAcademics.activityRecords[it.activity_identifier]!!.activityAttributes["status"]!!
            }
            activitiesStatus.add(ActivityStatus(it.activity_identifier,
                    interpretActivityStatus(is_mandatory, is_foundInAcademics, statusRecord)))
        }
        return activitiesStatus
    }
    fun activitiesSummaryStatus(chapter: Chapters.Chapter,
                                activityAcademics: AcademicRecords.ActivityAcademics?): String {
        var summaryStatus = none_status
        val activitiesStatus = getActivitiesStatus(chapter, activityAcademics)
        val assessment_ready_found = findActivityStatus(activitiesStatus, assessment_ready_status)
        val to_do_found = findActivityStatus(activitiesStatus, to_be_done_status)
        val done_found = findActivityStatus(activitiesStatus, done_status)
        val approved_found = findActivityStatus(activitiesStatus, approved_status)

        if(approved_found && !to_do_found && !assessment_ready_found) {
            summaryStatus = done_status
        } else if(done_found && to_do_found) {
            summaryStatus = inprogress_status
        } else if(to_do_found) {
            summaryStatus = to_be_done_status
        }
        return summaryStatus
    }
    fun getActivityAcademics(chapterAcademics: AcademicRecords.ChapterAcademics?, chapterID: String)
            : AcademicRecords.ActivityAcademics? {
        var activityAcademics: AcademicRecords.ActivityAcademics? = null
        if(chapterAcademics != null) {
            activityAcademics = chapterAcademics.chapterRecords[chapterID]
        }
        return activityAcademics
    }
    fun computeCurrentChapter(classChapter: String, curriculumChapters: Chapters,
                              chapterAcademics: AcademicRecords.ChapterAcademics?): String {
        curriculumChapters.chapter_list.forEach {
            val summaryStatus = activitiesSummaryStatus(it, getActivityAcademics(chapterAcademics, it.name))
            if (summaryStatus != done_status && summaryStatus != none_status) {
                return it.name
            }
            //Dont look beyond the class-current-chapter
            if (it.name == classChapter) {
                return classChapter
            }
        }
        return classChapter
    }
    fun jsonGrab(json: JSONObject, key: String): String {
        var value = ""
        if(json.has(key)) {
            value = json.get(key).toString()
        }
        return value
    }
    fun makeAssessmentRecord(subjectName: String, chapterName: String, assessmentName: String,
                                    assessmentStatus: String?, datapoint: String?): AssessmentRecord {
        var time_in_sec = ""
        var max_score = ""
        var actual_score = ""
        val evaluation: ArrayList<String> = arrayListOf()
        try {
            val evalJson = JSONObject(datapoint)
            time_in_sec = jsonGrab(evalJson, time_in_sec_key)
            max_score = jsonGrab(evalJson, max_score_key)
            actual_score = jsonGrab(evalJson, actual_score_key)
            if(evalJson.has(eval_key)) {
                val evaluationJson = evalJson.getJSONArray(eval_key)
                for (i in 0..(evaluationJson.length() - 1)) {
                    evaluation.add(evaluationJson.get(i).toString())
                }
            }
        } catch(j: JSONException) {
            Log.d("Datapoint parse error", "Datapoint is not a json string")
        }
        return AssessmentRecord(subjectName, chapterName, assessmentName,
                time_in_sec, max_score, actual_score, evaluation, assessmentStatus)
    }
    fun interpretActivityStatus(is_mandatory: Boolean, is_foundInAcademics: Boolean, statusRecord: String): String {
        if(is_mandatory) {
            return interpretStatusForMandatoryActivity(is_foundInAcademics, statusRecord)
        } else {
            return interpretStatusForNonMandatoryActivity(is_foundInAcademics)
        }
    }
    fun interpretStatusForMandatoryActivity(is_foundInAcademics: Boolean, statusRecord: String): String {
        if(is_foundInAcademics) {
            if(statusRecord == done_status) {
                return approved_status
            } else {
                return assessment_ready_status
            }
        } else {
            return to_be_done_status
        }
    }
    fun interpretStatusForNonMandatoryActivity(is_foundInAcademics: Boolean): String {
        if(is_foundInAcademics) {
            return done_status
        } else {
            return none_status
        }
    }
    fun findActivityStatus(activitiesStatus: ArrayList<ActivityStatus>, statusToFind: String): Boolean {
        activitiesStatus.forEach {
            if(it.status == statusToFind) {
                return true
            }
        }
        return false
    }
}
