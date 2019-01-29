package com.thinklearn.tide.interactor

import android.util.Log
import com.thinklearn.tide.dto.AcademicRecords
import com.thinklearn.tide.dto.ActivityID
import org.json.JSONException
import org.json.JSONObject

class ActivityRecord(
        val subjectID: String,
        val chapterID: String,
        val activityID: String,
        val time_in_sec: String,
        val max_score: String,
        val actual_score: String,
        var evaluation: ArrayList<String>?,
        @JvmField var assessment_status: String?)

class ActivityStatus(val activityID: String, val status: String)

object ProgressInteractor {
    //Interpreted status
    const val assessment_ready_status = "assessment_ready"
    const val to_be_done_status = "to_be_done"
    const val inprogress_status = "inprogress"
    const val approved_status = "approved"
    const val done_status = "done"
    const val none_status = "none"
    const val correct_result = "correct"
    const val incorrect_result = "incorrect"
    const val not_attempted = "not attempted"

    //Keys from the DB
    val status_key = "status"
    val data_point_key = "data_point"
    //Keys inside the data point
    val time_in_sec_key = "Time taken in seconds"
    val max_score_key = "Maximum score"
    val actual_score_key = "Actual score"

    //For student status screen, where activity status can be set
    //This goes by student record, *independent* of curriculum updates. So all data has to be in the subjectRecords only
    fun getStudentActivityRecords(subjectRecords: HashMap<String, AcademicRecords.ChapterAcademics>): ArrayList<ActivityRecord> {
        val assessments = arrayListOf<ActivityRecord>()
        subjectRecords.forEach {
            val subjectName = it.key
            it.value.chapterRecords.forEach {
                val chapterName = it.key
                it.value.activityRecords.forEach {
                    val activityAttrs = it.value.activityAttributes
                    assessments.add(makeActivityRecord(subjectName, chapterName,
                            activityID = it.key,
                            activityStatus = activityAttrs[status_key],
                            datapoint = activityAttrs[data_point_key]))
                }
            }
        }
        return assessments
    }
    fun findAcademicEntries(activityID: String, activityAcademics: AcademicRecords.ActivityAcademics?)
            :HashMap<ActivityID, AcademicRecords.ActivityAttributes> {
        val academicEntries = hashMapOf<ActivityID, AcademicRecords.ActivityAttributes>()
        val activityIDlower = activityID.toLowerCase()
        val activityID_prefix = activityIDlower + "."
        if(activityAcademics != null) {
            activityAcademics.activityRecords.forEach { activityRecord ->
                val activityRecordID = activityRecord.key.toLowerCase()
                if(activityRecordID == activityIDlower ||
                        activityRecordID.startsWith(activityID_prefix)) {
                    academicEntries[activityRecord.key] = activityRecord.value
                }
            }
        }
        return academicEntries
    }
    //For display in the student-login-grid and for chapter-level-summary to compute current chapter
    fun getActivitiesStatus(chapter: Chapters.Chapter, activityAcademics: AcademicRecords.ActivityAcademics?)
            : ArrayList<ActivityStatus> {
        val activitiesStatus = arrayListOf<ActivityStatus>()
        chapter.activities.forEach {
            val is_mandatory = it.mandatory
            var is_foundInAcademics = false
            val academicEntries = findAcademicEntries(it.activity_identifier, activityAcademics)
            academicEntries.forEach { academicActivity ->
                var statusRecord = ""
                if(academicActivity.value.activityAttributes.containsKey("status")) {
                    statusRecord = academicActivity.value.activityAttributes["status"]!!
                }
                activitiesStatus.add(ActivityStatus(academicActivity.key,
                        interpretActivityStatus(is_mandatory, true, statusRecord)))
                is_foundInAcademics = true
            }
            if(!is_foundInAcademics) {
                activitiesStatus.add(ActivityStatus(it.activity_identifier,
                        interpretActivityStatus(is_mandatory, false, "")))
            }
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

        if(assessment_ready_found) {
            summaryStatus = assessment_ready_status
        } else if(approved_found && !to_do_found) {
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
            val summaryStatus = activitiesSummaryStatus(it, getActivityAcademics(chapterAcademics, it.id))
            if (summaryStatus != done_status && summaryStatus != none_status) {
                return it.id
            }
            //Dont look beyond the class-current-chapter
            if (it.id == classChapter) {
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
    fun repairJSONStr(datapoint: String): String {
        var repairedJSONStr = datapoint
        repairedJSONStr = repairedJSONStr.replace("[n", "[\"n\"")
        repairedJSONStr = repairedJSONStr.replace("n,", " \"n\",")
        repairedJSONStr = repairedJSONStr.replace("n]", " \"n\"]")
        return repairedJSONStr
    }
    fun findEvalKey(evalJson: JSONObject): String? {
        evalJson.keys().forEach { key->
            if(key.contains("correct", true) &&
                    key.contains("attempt", true)) {
                return key
            }
        }
        return null
    }
    fun makeActivityRecord(subjectID: String, chapterID: String, activityID: String,
                           activityStatus: String?, datapoint: String?): ActivityRecord {
        var time_in_sec = ""
        var max_score = ""
        var actual_score = ""
        var assessmentStatus = activityStatus
        //A tab assessment is ready for eval just by virtue of being present
        if(activityStatus == null && activityID.contains("tab assessment")) {
            assessmentStatus = ProgressInteractor.assessment_ready_status
        }
        val evaluation: ArrayList<String> = arrayListOf()
        if(datapoint != null) {
            val datapointJsonStr = repairJSONStr(datapoint)
            try {
                val evalJson = JSONObject(datapointJsonStr)
                time_in_sec = jsonGrab(evalJson, time_in_sec_key)
                max_score = jsonGrab(evalJson, max_score_key)
                actual_score = jsonGrab(evalJson, actual_score_key)
                val eval_key = findEvalKey(evalJson)
                if(eval_key != null) {
                    val evaluationJson = evalJson.getJSONArray(eval_key)
                    for (i in 0..(evaluationJson.length() - 1)) {
                        evaluation.add(translateEvaluation(evaluationJson.get(i).toString()))
                    }
                }
            } catch(j: JSONException) {
                Log.d("Datapoint parse", "Datapoint is not a json string")
            }
        }
        return ActivityRecord(subjectID, chapterID, activityID,
                time_in_sec, max_score, actual_score, evaluation, assessmentStatus)
    }
    fun translateEvaluation(raw_eval: String): String {
        var eval = ProgressInteractor.not_attempted
        when(raw_eval) {
            "1" -> eval = ProgressInteractor.correct_result
            "0" -> eval = ProgressInteractor.incorrect_result
        }
        return eval
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
            if(statusRecord == approved_status) {
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
    fun filter_assessments(activityRecords: ArrayList<ActivityRecord>): ArrayList<ActivityRecord> {
        val assessments: ArrayList<ActivityRecord> = arrayListOf()
        for(activity in activityRecords) {
            if(activity.activityID.contains("assessment", ignoreCase = true)) {
                assessments.add(activity)
            }
        }
        return assessments
    }
}
