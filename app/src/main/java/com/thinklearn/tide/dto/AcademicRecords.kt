package com.thinklearn.tide.dto

typealias SubjectID = String
typealias ChapterID = String
typealias ActivityID = String
typealias ActivityAttrKey = String

class AcademicRecords {
    class ActivityAttributes(val activityAttributes: HashMap<ActivityAttrKey, String> = hashMapOf())
    class ActivityAcademics(val activityRecords: HashMap<ActivityID, ActivityAttributes> = hashMapOf())
    class ChapterAcademics(val chapterRecords: HashMap<ChapterID, ActivityAcademics> = hashMapOf())
    private val subjectRecords: HashMap<SubjectID, ChapterAcademics> = hashMapOf()

    fun setActivityAttribute(subjectID: String, chapterID: String, activityID: String,
                             activityKey: String, activityValue: String) {
        if (!subjectRecords.containsKey(subjectID)) {
            subjectRecords[subjectID] = ChapterAcademics()
        }
        if (!subjectRecords[subjectID]!!.chapterRecords.containsKey(chapterID)) {
            subjectRecords[subjectID]!!.chapterRecords[chapterID] = ActivityAcademics()
        }
        if (!subjectRecords[subjectID]!!.chapterRecords[chapterID]!!.activityRecords.containsKey(activityID)) {
            subjectRecords[subjectID]!!.chapterRecords[chapterID]!!.activityRecords[activityID] =
                    ActivityAttributes()
        }
        subjectRecords[subjectID]!!.chapterRecords[chapterID]!!.activityRecords[activityID]!!
                .activityAttributes[activityKey] = activityValue
    }
    fun getActivityAttribute(subjectID: String, chapterID: String, activityID: String, activityKey: String): String {
        return subjectRecords[subjectID]!!.chapterRecords[chapterID]!!.activityRecords[activityID]!!
                .activityAttributes[activityKey].toString()
    }
    fun getSubjectRecords(): HashMap<SubjectID, ChapterAcademics> {
        return subjectRecords
    }
}
