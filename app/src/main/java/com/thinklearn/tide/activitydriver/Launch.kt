package com.thinklearn.tide.activitydriver

import android.app.Activity
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
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thinklearn.tide.interactor.ClassroomInteractor
import com.thinklearn.tide.interactor.ClassroomLoaded
import org.json.JSONException
import org.json.JSONObject
import java.io.FileInputStream


data class Classroom(val class_name: String, val school_name: String)

class Launch : AppCompatActivity() {
    lateinit var selected_class_id: String
    //lateinit var selected_tab_mode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getSupportActionBar()?.hide();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            launchStartScreen()
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    4)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchStartScreen()
        }
    }
    fun launchStartScreen() {
        selected_class_id = ClassroomInteractor.configureClass()
        if (selected_class_id == "") {
            showConfigScreen()
        } else {
            loadAndStart(ClassroomInteractor.tab_mode)
        }
    }
    fun loadAndStart(start_mode: String) {
        if(selected_class_id != "") {
            ClassroomInteractor.load("ICDev", selected_class_id, object : ClassroomLoaded {
                override fun onLoadComplete() {
                    ClassroomInteractor.tab_mode = start_mode
                    ClassroomInteractor.removeLoadedEvent()
                    if (ClassroomInteractor.tab_mode == "teacher") {
                        startTeacherLogin()
                    } else if (ClassroomInteractor.tab_mode == "student") {
                        startStudentLogin()
                    }
                }
            })
        } else {
            Toast.makeText(this, R.string.need_school_selection, Toast.LENGTH_LONG).show()
        }
    }
    fun showConfigScreen() {
        setContentView(R.layout.activity_launch)

        val schoolsListView = findViewById<ListView>(R.id.SchoolsList)
        val thisContext = this

        findViewById<Button>(R.id.GoTeacher).setOnClickListener {
            loadAndStart("teacher")
        }
        findViewById<Button>(R.id.GoStudent).setOnClickListener {
            loadAndStart("student")
        }
        findViewById<Button>(R.id.UploadClassroom).setOnClickListener {
            uploadClassroom()
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
                            selected_class_id = school_ids[position]!!
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
        startActivity(teacherLoginIntent)
    }
    fun startStudentLogin() {
        val studentLoginIntent = Intent(this, GradeSelectionActivity::class.java)
        studentLoginIntent.putParcelableArrayListExtra("studentInputList", ClassroomInteractor.students)
        studentLoginIntent.putExtra("purpose", "STUDENT_ACTIVITY")
        studentLoginIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(studentLoginIntent)
    }
    fun uploadClassroom() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, 3)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && data != null) {
            var classroomAndAssetsJSONstr: String = ""
            val uri = data.getData();
            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val buffered = inputStream.bufferedReader()

            var linecount = 0
            val max_linecount = 15000
            var line = buffered.readLine()
            while (line != null && linecount < max_linecount) {
                classroomAndAssetsJSONstr += line;
                line = buffered.readLine()
                linecount += 1
            }
            try {
                val cassetJSON = JSONObject(classroomAndAssetsJSONstr)
                ClassroomInteractor.uploadClassroom(cassetJSON)
            } catch(e: JSONException) {
                Toast.makeText(this, "Error parsing " + uri.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}
