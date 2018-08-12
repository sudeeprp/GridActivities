package com.thinklearn.tide.activitydriver

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thinklearn.tide.dto.Teacher
import com.thinklearn.tide.interactor.ClassroomInteractor
import com.thinklearn.tide.interactor.ClassroomLoaded

data class Classroom(val class_name: String, val school_name: String)

class Launch : AppCompatActivity() {
    var classroomInteractor = ClassroomInteractor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getSupportActionBar()?.hide();
        setContentView(R.layout.activity_launch)

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        val schoolsListView = findViewById<ListView>(R.id.SchoolsList)
        val thisContext = this

        //TODO: Check max number of schools (<50) before querying!
        FirebaseDatabase.getInstance().getReference("ICDev").child("classrooms").
                addListenerForSingleValueEvent(object: ValueEventListener {
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
                        val adapter = ArrayAdapter(thisContext, android.R.layout.simple_list_item_1, schools)
                        schoolsListView.adapter = adapter
                        schoolsListView.setOnItemClickListener { parent, view, position, id ->
                            println("** #" + position.toString() + " ID = " + school_ids[position] + " is clicked");
                            classroomInteractor.load("ICDev", school_ids[position]!!, object: ClassroomLoaded {
                                override fun onLoadComplete() {
                                    startTeacherLogin(classroomInteractor.get_teachers())
                                }
                            }) }
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        println("ICDev fetch: onCancelled ${p0?.toException()}")
                    }
                })
    }
    fun startTeacherLogin(teachers: ArrayList<Teacher>) {
        var teacherLoginIntent = Intent(this, TeacherLoginActivity::class.java)
        teacherLoginIntent.putParcelableArrayListExtra("TEACHER_LIST", teachers)
        startActivityForResult(teacherLoginIntent, 0)
    }
}
