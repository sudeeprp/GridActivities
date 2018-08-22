package com.thinklearn.tide.activitydriver

import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thinklearn.tide.interactor.ClassroomInteractor
import com.thinklearn.tide.interactor.ClassroomLoaded

data class Classroom(val class_name: String, val school_name: String)

class Launch : AppCompatActivity() {
    lateinit var selected_school_id: String
    //lateinit var selected_tab_mode: String

    fun get_directory_permission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            println("Directory access permission not granted")
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    4)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getSupportActionBar()?.hide();
        setContentView(R.layout.activity_launch)

        val schoolsListView = findViewById<ListView>(R.id.SchoolsList)
        val thisContext = this

        get_directory_permission()

        val goTeacher = findViewById<Button>(R.id.GoTeacher)
        goTeacher.setOnClickListener {
            ClassroomInteractor.load("ICDev", selected_school_id, object: ClassroomLoaded {
                override fun onLoadComplete() {
                    ClassroomInteractor.removeLoadedEvent()
                    startTeacherLogin()
                }
            })
        }
        val goStudent = findViewById<Button>(R.id.GoStudent)
        goStudent.setOnClickListener {
            ClassroomInteractor.load("ICDev", selected_school_id, object: ClassroomLoaded {
                override fun onLoadComplete() {
                    ClassroomInteractor.removeLoadedEvent()
                    startStudentLogin()
                }
            })
        }

        //TODO: Check max number of schools (<50) before querying!
        FirebaseDatabase.getInstance().getReference("ICDev").child("classrooms").
                addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(classrooms_snapshot: DataSnapshot) {
                        val schools = arrayOfNulls<String>(classrooms_snapshot.childrenCount.toInt())
                        val school_ids = arrayOfNulls<String>(classrooms_snapshot.childrenCount.toInt())
                        var i: Int = 0
                        classrooms_snapshot.children.forEach {
                            schools[i] = it.key + ": " + it.child("class_name").value.toString() +
                                        " / " + it.child("school_name").value.toString()
                            school_ids[i] = it.key
                            i++
                        }
                        val adapter = ArrayAdapter(thisContext, android.R.layout.simple_list_item_single_choice, schools)
                        schoolsListView.adapter = adapter
                        schoolsListView.setOnItemClickListener { parent, view, position, id ->
                            println("** #" + position.toString() + " ID = " + school_ids[position] + " is clicked");
                            selected_school_id = school_ids[position]!!
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        println("ICDev fetch: onCancelled ${p0?.toException()}")
                    }
                })
    }
    fun startTeacherLogin() {
        val teacherLoginIntent = Intent(this, TeacherLoginActivity::class.java)
        teacherLoginIntent.putParcelableArrayListExtra("TEACHER_LIST", ClassroomInteractor.teachers)
        teacherLoginIntent.putExtra("purpose", "PROFILE_EDIT")
        startActivityForResult(teacherLoginIntent, 0)
    }
    fun startStudentLogin() {
        val studentLoginIntent = Intent(this, GradeSelectionActivity::class.java)
        studentLoginIntent.putParcelableArrayListExtra("studentInputList", ClassroomInteractor.students)
        studentLoginIntent.putExtra("purpose", "STUDENT_ACTIVITY")
        studentLoginIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(studentLoginIntent)
    }
}
