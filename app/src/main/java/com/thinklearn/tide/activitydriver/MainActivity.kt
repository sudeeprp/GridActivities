package com.thinklearn.tide.activitydriver

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView


enum class Activities {
    TEACHER_IDENTIFICATION, STUDENT_IDENTIFICATION, STUDENT_PROFILE, ATTENDANCE
}

data class ActivityInputOutput( val activityIdent: Activities, val inputJson: String, val expectedOutput: String)

val teacherListActivityDesc = ActivityInputOutput(
        activityIdent = Activities.TEACHER_IDENTIFICATION,
        inputJson = """
        {
          "teachers":
            [{
               "teacher_id": 1,
               "name": "teacher one",
               "thumbnail": "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCABAAEADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDB+JMjeVploOju8h/AAD+ZrF05QqqOmRW38R0/4mmlDB/1Un06isi1TEKk9cV5bfuI9uK99s6bTipUDFdJYxjbggVyWnMQQM11VpN5aDNZJ2Lki8Ywj54xVW7ZdhFPkuRjIrLvrkrGxquYlQMnUXBRkPQ8VT8En91qMGT8kocDsAR/9ao57nzGYE5p/gwY1XURjjy07e5qvsMm1po3fH9mkun2VyCu+CXlSRkq3B/XFczBanYOw9a0/HSSjWkMxzaPbqQCOhBPI984qncPst8iMyY/hB61DeiRrFatlm3aOCRR5qk9+eldJZPb3DKglAJFefNda3MAtvp9okQ4G/lgPXNWNOfU47iM7Iy+fmEJyM+mKbpvluT7S8rHob2DKGZpV2LyTWBf3dkw2tOqr6k8/lUWpXWpXFi6QxSA4+bAJrintNQN1kXkaDcM+WcMBjnqKKdPm3dhVJuPmdYU0+62x2smG9GGCat+GbKO2nv5GYCWWTYqnuFGTj865nRbDUvOMs90JUjPU9TW5pEE914rFxvPk28ZJHuRTlpdXBa2bOo8YaUb7RfOjG6S3JYjHJQ8N+XB/CuQgQzIoU8njNeliQMpVlBVhgg9xXFaraQ6fq7RQII4Squig5x6j8xUPY0h5lq00Cza28y6bcFGTTbW+sYbkBJYbK1H3ZHAAk57VFdXbHTZE6Db82PSuZuPEMFzeLY7I3VRgbmwmfrShFyCTSZ6DJreiLbmOK9SBpOsgcE/QVm2S2OoStDqFlG1wnR2QAsOxrnrvboaG7e208LDKqFxIGKsRns3Ix7GtS11uDVQLsMqyqMED9apxcVchWehdvtPtrdi1thVx92ptBshBYtOeXnbd9AOAKwb3U28wgHOeBXWx5gtYYumxAP0qI7altWORu7/AFSdGD3sij0j+X+Vcz9pnsrtpJGeQN94k5NdNId4+6fyrJv7IyqcIfyrRdiC1FrUE1tsJBB4PNMhTToZyZ7dGtJOQwHKH61y1xp1zG5MYdT7Cr2n65JZwm3vYGIHRwpOfrVqm1rEOdX1Op1HUdJvrNI5vMuRER5UKAfNgYGT6YrBS5hsdzBI45GOWVBgfSo38RwRE/Z7dmOOAEPWsd473UrkyyIUVj90A01GTVpbEynG90dDpYk1W+EjcQxnP1NdtbxSmMb55P8AvrNYHh+yaC3UbSPwroGk8tehz9Khrohczep//9k="
             },
             {
               "teacher_id": 2,
               "name": "teacher two",
               "thumbnail": "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"
             }]
        }
        """.trimIndent(),
        expectedOutput = """
        {
          "selected_teacher_id": 2
        }
        """.trimIndent()
)


val studentListActivityDesc = ActivityInputOutput(
        activityIdent = Activities.STUDENT_IDENTIFICATION,
        inputJson = """
        {
          "students":
            [{"student_id": 1,
              "name": "student one",
              "birth_date": {"dd": 12, "mm": 2, "yyyy": 2013"},
              "gender": "boy",
              "grade": 1,
              "thumbnail": "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",
              "next_activity": [{"subject": "French", "next": "F-Basic words"}, {"subject": "Mathematics", "next": "M-Counting"}]},
             {"student_id": 2,
              "name": "student two",
              "birth_date": "dd": 15, "mm": 04, "yyyy": 2013"},
              "gender": "girl",
              "grade": 2,
              "thumbnail": "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",
              "next_activity": [{"subject": "French", "next": "F-Advanced words"}, {"subject": "Mathematics", "next": "M-Addition"}]}]
        }
        """.trimIndent(),
        expectedOutput = """
        {
          "selected_student_ids": [1]
        }
        """.trimIndent()
)


val studentProfileActivityDesc = ActivityInputOutput(
        activityIdent = Activities.STUDENT_PROFILE,
        inputJson = """
        {
          "student":
            {"student_id": 1,
             "name": "student one",
             "birth_date": {"dd": 12, "mm": 2, "yyyy": 2013"},
             "gender": "boy",
             "grade": 1,
             "thumbnail": "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCABAAEADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDB+JMjeVploOju8h/AAD+ZrF05QqqOmRW38R0/4mmlDB/1Un06isi1TEKk9cV5bfuI9uK99s6bTipUDFdJYxjbggVyWnMQQM11VpN5aDNZJ2Lki8Ywj54xVW7ZdhFPkuRjIrLvrkrGxquYlQMnUXBRkPQ8VT8En91qMGT8kocDsAR/9ao57nzGYE5p/gwY1XURjjy07e5qvsMm1po3fH9mkun2VyCu+CXlSRkq3B/XFczBanYOw9a0/HSSjWkMxzaPbqQCOhBPI984qncPst8iMyY/hB61DeiRrFatlm3aOCRR5qk9+eldJZPb3DKglAJFefNda3MAtvp9okQ4G/lgPXNWNOfU47iM7Iy+fmEJyM+mKbpvluT7S8rHob2DKGZpV2LyTWBf3dkw2tOqr6k8/lUWpXWpXFi6QxSA4+bAJrintNQN1kXkaDcM+WcMBjnqKKdPm3dhVJuPmdYU0+62x2smG9GGCat+GbKO2nv5GYCWWTYqnuFGTj865nRbDUvOMs90JUjPU9TW5pEE914rFxvPk28ZJHuRTlpdXBa2bOo8YaUb7RfOjG6S3JYjHJQ8N+XB/CuQgQzIoU8njNeliQMpVlBVhgg9xXFaraQ6fq7RQII4Squig5x6j8xUPY0h5lq00Cza28y6bcFGTTbW+sYbkBJYbK1H3ZHAAk57VFdXbHTZE6Db82PSuZuPEMFzeLY7I3VRgbmwmfrShFyCTSZ6DJreiLbmOK9SBpOsgcE/QVm2S2OoStDqFlG1wnR2QAsOxrnrvboaG7e208LDKqFxIGKsRns3Ix7GtS11uDVQLsMqyqMED9apxcVchWehdvtPtrdi1thVx92ptBshBYtOeXnbd9AOAKwb3U28wgHOeBXWx5gtYYumxAP0qI7altWORu7/AFSdGD3sij0j+X+Vcz9pnsrtpJGeQN94k5NdNId4+6fyrJv7IyqcIfyrRdiC1FrUE1tsJBB4PNMhTToZyZ7dGtJOQwHKH61y1xp1zG5MYdT7Cr2n65JZwm3vYGIHRwpOfrVqm1rEOdX1Op1HUdJvrNI5vMuRER5UKAfNgYGT6YrBS5hsdzBI45GOWVBgfSo38RwRE/Z7dmOOAEPWsd473UrkyyIUVj90A01GTVpbEynG90dDpYk1W+EjcQxnP1NdtbxSmMb55P8AvrNYHh+yaC3UbSPwroGk8tehz9Khrohczep//9k=",
             "next_activity": [{"subject": "French", "next": "F-Basic words"}, {"subject": "Mathematics", "next": "M-Counting"}]
            }
        }
        """.trimIndent(),
        expectedOutput = """
        {
          "student":
            {"student_id": 1,
             "name": "student one",
             "birth_date": {"dd": 12, "mm": 2, "yyyy": 2013"},
             "gender": "boy",
             "grade": 1,
             "thumbnail": "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",
             "next_activity": [{"subject": "French", "next": "F-Basic words"}, {"subject": "Mathematics", "next": "M-Counting"}]
            }
        }
        """.trimIndent()
)


val attendanceActivityDesc = ActivityInputOutput(
        activityIdent = Activities.ATTENDANCE,
        inputJson = """
        {
          "attendance":
                  [{"date": {"dd": 1, "mm": 10, "yyyy": 2018"}, "absent": [1, 3]},
                   {"date": {"dd": 2, "mm": 10, "yyyy": 2018"}, "absent": [3]}],
          "students":
                  [{"student_id": 1,
                    "name": "student one",
                    "birth_date": {"dd": 12, "mm": 2, "yyyy": 2013"},
                    "gender": "boy", "grade": 1,
                    "thumbnail": "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",
                    "next_activity": [{"subject": "French", "next": "F-Basic words"}]},
                   {"student_id": 2,
                    "name": "student two",
                    "birth_date": {"dd": 15, "mm": 4, "yyyy": 2013"},
                    "gender": "girl",
                    "grade": 2,
                    "thumbnail": "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",
                    "next_activity": [{"subject": "French", "next": "F-Advanced words"}]}]
                   {"student_id": 3,
                    "name": "student three",
                    "birth_date": {"dd": 1, "mm": 3, "yyyy": 2013"},
                    "gender": "boy",
                    "grade": 2,
                    "thumbnail": "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",
                    "next_activity": [{"subject": "French", "next": "F-Letters"}]}]
                   {"student_id": 4,
                    "name": "student four",
                    "birth_date": {"dd": 27, "mm": 1, "yyyy": 2013"},
                    "gender": "girl",
                    "grade": 1,
                    "thumbnail": "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",
                    "next_activity": [{"subject": "French", "next": "F-Sounds"}]}]
        }
        """.trimIndent(),
        expectedOutput = """
        {
          "absents": { "date": {"dd": 3, "mm": 10, "yyyy": "2018"},
                       "absent": [2, 4, 15]
                     }
        }
        """.trimIndent()
)


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getSupportActionBar()?.hide();
        setContentView(R.layout.activity_main)

        val teacherIdentButton = findViewById<Button>(R.id.StartTeacherIdent)
        teacherIdentButton.setOnClickListener {
            PrepAndStartActivity(Intent(this, TeacherIdentification::class.java),
                    teacherListActivityDesc) }

        val studentIdentButton = findViewById<Button>(R.id.StartStudentIdent)
        studentIdentButton.setOnClickListener {
            PrepAndStartActivity(Intent(this, StudentIdentification::class.java),
                    studentListActivityDesc) }

        val studentProfileButton = findViewById<Button>(R.id.StartStudentProfile)
        studentProfileButton.setOnClickListener {
            PrepAndStartActivity(Intent(this, StudentProfile::class.java),
                    studentProfileActivityDesc) }

        val attendanceButton = findViewById<Button>(R.id.StartAttendance)
        attendanceButton.setOnClickListener {
            PrepAndStartActivity(Intent(this, Attendance::class.java),
                    attendanceActivityDesc) }
    }

    fun PrepAndStartActivity(intent: Intent, desc: ActivityInputOutput) {
        findViewById<TextView>(R.id.ExpectedTitle).text = desc.activityIdent.name + " expected output sample"
        findViewById<TextView>(R.id.ExpectedOutput).text = desc.expectedOutput
        findViewById<TextView>(R.id.ActualTitle).text = desc.activityIdent.name + " actual output"
        findViewById<TextView>(R.id.ActualOutput).text = ""
        intent.putExtra(desc.activityIdent.name, desc.inputJson)
        startActivityForResult(intent, desc.activityIdent.ordinal)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            val result = "(" + requestCode.toString() + ")\n" + data?.getStringExtra("result")
            findViewById<TextView>(R.id.ActualOutput).text = result
        }
    }

}
