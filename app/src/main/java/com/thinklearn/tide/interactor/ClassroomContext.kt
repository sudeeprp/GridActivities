package com.thinklearn.tide.interactor

import android.content.Context
import android.content.pm.PackageManager
import com.thinklearn.tide.activitydriver.R
import com.thinklearn.tide.dto.Student
import com.thinklearn.tide.dto.Teacher

object ClassroomContext {
    @JvmField
    var selectedTeacher: Teacher? = null
    @JvmField
    var selectedStudent: Student? = null
}

object AppInfo {
    @JvmStatic
    fun appVersion(context: Context): String {
        var version: String = "Unk"
        try {
            version = context.resources.getString(R.string.version_prefix) + " " +
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return version
    }
}
