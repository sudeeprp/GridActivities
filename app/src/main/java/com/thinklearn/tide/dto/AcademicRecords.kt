package com.thinklearn.tide.dto

class AcademicRecords {
    class ActivityAttributes(val activityAttributes: HashMap<String, String> = hashMapOf())
    class ActivityRecords(val activityRecords: HashMap<String, ActivityAttributes> = hashMapOf())
    class ChapterRecords(val chapterRecords: HashMap<String, ActivityRecords> = hashMapOf())
    private val subjectRecords: HashMap<String, ChapterRecords> = hashMapOf<String, ChapterRecords>()

    fun setActivityAttribute(subjectID: String, chapterID: String, activityID: String, activityKey: String, activityValue: String) {
        if (!subjectRecords.containsKey(subjectID)) {
            subjectRecords[subjectID] = ChapterRecords()
        }
        if (!subjectRecords[subjectID]!!.chapterRecords.containsKey(chapterID)) {
            subjectRecords[subjectID]!!.chapterRecords[chapterID] = ActivityRecords()
        }
        if (!subjectRecords[subjectID]!!.chapterRecords[chapterID]!!.activityRecords.containsKey(activityID)) {
            subjectRecords[subjectID]!!.chapterRecords[chapterID]!!.activityRecords[activityID] =
                    ActivityAttributes()
        }
        subjectRecords[subjectID]!!.chapterRecords[chapterID]!!.activityRecords[activityID]!!.activityAttributes[activityKey] = activityValue
    }
    fun getActivityAttribute(subjectID: String, chapterID: String, activityID: String, activityKey: String): String {
        return subjectRecords[subjectID]!!.chapterRecords[chapterID]!!.activityRecords[activityID]!!.activityAttributes[activityKey].toString()
    }
    fun getSubjectRecords(): HashMap<String, ChapterRecords> {
        return subjectRecords
    }
}
