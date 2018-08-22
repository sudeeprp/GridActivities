package com.thinklearn.tide.interactor

import com.thinklearn.tide.dto.Student
import com.thinklearn.tide.dto.Teacher

object ClassroomContext {
    @JvmField
    var selectedTeacher: Teacher? = null
    @JvmField
    var selectedStudent: Student? = null
}
