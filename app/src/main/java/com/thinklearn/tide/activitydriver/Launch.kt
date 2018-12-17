package com.thinklearn.tide.activitydriver

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Window
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONException
import org.json.JSONObject
import com.google.firebase.auth.FirebaseAuth
import com.thinklearn.tide.interactor.*
import java.io.IOException


val DB_TOKEN_REQUEST = 12
val UPLOAD_CLASSROOM_REQUEST = 3


data class Classroom(val class_name: String, val school_name: String)

class Launch : AppCompatActivity() {
    var selected_class_id: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            storagePermissionPresent()
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    4)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            storagePermissionPresent()
        } else {
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show()
            finish()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPLOAD_CLASSROOM_REQUEST) {
            uploadClassFromFile(resultCode, data)
        } else if (requestCode == DB_TOKEN_REQUEST) {
            authenticateCustomToken(resultCode, data)
        }
    }
    fun storagePermissionPresent() {
        authenticateToken()
    }
    fun tokenAuthenticationDone() {
        EnvironmentalContext.listenToOnlining({refreshStatusMessages()})
        launchStartScreen()
    }
    fun authenticateToken() {
        if(BuildConfig.BUILD_TYPE.contentEquals("release") ||
                BuildConfig.BUILD_TYPE.contentEquals("debug")) {
            dbConnectionStatus("Test data connected")
            tokenAuthenticationDone()
            return
        }
        if(FirebaseAuth.getInstance().currentUser != null) {
            dbConnectionStatus(resources.getString(R.string.pre_auth))
            tokenAuthenticationDone()
        } else {
            val authIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                `package` = "com.thinklearn.tide.grida"
                putExtra(Intent.EXTRA_TEXT, "rfgt$%UJ")
                type = "text/plain"
            }
            if (packageManager.queryIntentActivities(authIntent, PackageManager.MATCH_DEFAULT_ONLY).size > 0) {
                startActivityForResult(authIntent, DB_TOKEN_REQUEST)
            } else {
                Toast.makeText(this, "Token provider missing", Toast.LENGTH_LONG).show()
                tokenAuthenticationDone()
            }
        }
    }
    fun launchStartScreen() {
        selected_class_id = ClassroomInteractor.selectedClass()
        val selected_mode = ClassroomInteractor.selectedMode()
        if (selected_class_id != "" && selected_mode != "") {
            loadAndStart(selected_mode)
        } else {
            showConfigScreen()
        }
    }
    fun loadAndStart(start_mode: String) {
        if(selected_class_id != "") {
            setContentView(R.layout.activity_initial_load)
            ClassroomInteractor.load(ClassroomInteractor.learningProject(), selected_class_id, object : DBOpDone {
                override fun onSuccess() {
                    ClassroomInteractor.removeLoadedEvent()
                    if (start_mode == ConfigKeys.teacher_mode_value) {
                        startTeacherLogin()
                    } else if (start_mode == ConfigKeys.student_mode_value) {
                        startStudentLogin()
                    }
                }
                override fun onFailure(msg: String?) {
                    Toast.makeText(this@Launch, "Failed to load teachers and students: " + msg, Toast.LENGTH_LONG).show()
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
        dataStatus("Connecting to classrooms")
        FirebaseDatabase.getInstance().getReference(ClassroomInteractor.learningProject()).child("classrooms").
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
                        dataStatus(resources.getString(R.string.received_classrooms) + i.toString())

                        val adapter = ArrayAdapter(thisContext, android.R.layout.simple_list_item_single_choice, schools)
                        schoolsListView.adapter = adapter
                        schoolsListView.setOnItemClickListener { _, _, position, _ ->
                            println("** #" + position.toString() + " ID = " + school_ids[position] + " is clicked");
                            selected_class_id = school_ids[position]!!
                        }
                    }
                    override fun onCancelled(p0: DatabaseError) {
                        dbConnectionStatus("Classrooms fetch cancelled: ${p0.toException()}")
                    }
                })
    }
    fun startTeacherLogin() {
        val teacherLoginIntent = Intent(this, TeacherLoginActivity::class.java)
        teacherLoginIntent.putParcelableArrayListExtra("TEACHER_LIST", ClassroomInteractor.teachers)
        teacherLoginIntent.putExtra("purpose", "PROFILE_EDIT")
        finish()
        startActivity(teacherLoginIntent)
    }
    fun startStudentLogin() {
        val studentLoginIntent = Intent(this, StudentGradeSelectionActivity::class.java)
        studentLoginIntent.putParcelableArrayListExtra("studentInputList", ClassroomInteractor.students)
        studentLoginIntent.putExtra("purpose", "STUDENT_ACTIVITY")
        finish()
        startActivity(studentLoginIntent)
    }
    fun uploadClassroom() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, UPLOAD_CLASSROOM_REQUEST)
    }

    fun uploadClassFromFile(resultCode: Int, data: Intent?) {
        val MAX_CLASS_FILE_SIZE = 500 * 1024
        if(resultCode == Activity.RESULT_OK && data != null) {
            var classroomAndAssetsJSONstr: String = ""
            val uri = data.getData();
            if(uri == null)
                return
            try {
                val inputStream = contentResolver.openInputStream(uri)
                if(inputStream == null)
                    return
                val buffered = inputStream.bufferedReader()
                val buffer = CharArray(MAX_CLASS_FILE_SIZE)
                buffered.read(buffer)
                classroomAndAssetsJSONstr = String(buffer)
                try {
                    val cassetJSON = JSONObject(classroomAndAssetsJSONstr)
                    ClassroomInteractor.uploadClassroom(cassetJSON, object : DBOpDone {
                        override fun onSuccess() {
                            dbConnectionStatus("Classroom data uploaded successfully")
                        }
                        override fun onFailure(msg: String?) {
                            dbConnectionStatus("Failed to upload classroom data: " + msg)
                        }
                    })
                } catch (e: JSONException) {
                    dbConnectionStatus(resources.getString(R.string.not_class_file))
                }
                //this closes the inputstream as well
                //https://stackoverflow.com/questions/1388602/do-i-need-to-close-both-filereader-and-bufferedreader
                buffered.close()
            } catch(e: IOException) {
                dbConnectionStatus("Error reading file: " + e.message)
            }
        }
    }
    fun authenticateCustomToken(resultCode: Int, data: Intent?) {
        if (data != null) {
            val customToken = data.getStringExtra("CUSTOM_TOKEN")
            val authenticator = FirebaseAuth.getInstance()
            var current = authenticator.currentUser
            println(current.toString())
            authenticator.signInWithCustomToken(customToken)
                    .addOnSuccessListener {
                        current = authenticator.currentUser
                        println(current.toString())
                        Toast.makeText(this, "Authentication successful", Toast.LENGTH_LONG).show()
                        tokenAuthenticationDone()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to authenticate: " + it.localizedMessage, Toast.LENGTH_LONG).show()
                        tokenAuthenticationDone()
                    }
        }
    }
    fun dbConnectionStatus(connectionMsg: String) {
        Toast.makeText(this, connectionMsg, Toast.LENGTH_LONG).show()
        EnvironmentalContext.dbConnectionStatusMsg = connectionMsg
        refreshStatusMessages()
    }
    fun dataStatus(dataMsg: String) {
        EnvironmentalContext.dataStatusMsg = dataMsg
        refreshStatusMessages()
    }
    fun refreshStatusMessages() {
        val connectionStatus = findViewById<TextView>(R.id.ConnectionStatus)
        val dataStatus = findViewById<TextView>(R.id.DataStatus)

        if(connectionStatus != null) {
            connectionStatus.text = EnvironmentalContext.dbConnectionStatusMsg + " " +
                    EnvironmentalContext.dbOnlineStatus
        }
        if(dataStatus != null) {
            dataStatus.text = EnvironmentalContext.dataStatusMsg
        }
    }
}
