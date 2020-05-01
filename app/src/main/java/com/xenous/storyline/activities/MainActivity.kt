package com.xenous.storyline.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.xenous.storyline.R
import com.xenous.storyline.data.Story
import com.xenous.storyline.data.User
import com.xenous.storyline.fragments.StoryFragment
import com.xenous.storyline.utils.StoryLayout


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }
    
    private lateinit var fragmentFrameLayout: FrameLayout

    private lateinit var downloadDataFromDatabaseHandler: Handler

    lateinit var downloadDataFromDatabaseThread: DownloadDataFromUsersDatabaseThread
    lateinit var downloadDataForUnregisteredUserThread: DownloadDataForUnregisteredUserThread

    private var user : FirebaseUser? = null
    private lateinit var authentication : FirebaseAuth

    var todayStory : Story? = null
    var storyUrl : String? = null

    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentFrameLayout = findViewById(R.id.fragmentFrameLayout)
        val storyLayout = StoryLayout
            .Builder(
                this,
                "Хамалеон",
                "Антон Павлович Чехов",
                "Читать 5 минут",
                ""
            ).build(layoutInflater)

        authentication = FirebaseAuth.getInstance()
        user = authentication.currentUser

        downloadDataFromDatabaseHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                val storyFragment = StoryFragment()

                storyLayout.setCoverImageResource(R.drawable.demo_background)
                storyLayout.cover.setOnClickListener {
                    storyLayout.collapseStoryCover()
                }
                storyLayout.setContentFragment(storyFragment, supportFragmentManager)
                
                fragmentFrameLayout.addView(storyLayout.view)
            }
        }

        checkUserStatus()
        makeStatusBarTransparent()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    private fun makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(
                this,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                true
            )
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(
                this,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                false
            )
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun checkUserStatus() {
        downloadDataFromDatabaseThread = DownloadDataFromUsersDatabaseThread(downloadDataFromDatabaseHandler)
        downloadDataForUnregisteredUserThread = DownloadDataForUnregisteredUserThread(downloadDataFromDatabaseHandler)

        if(user == null) {
            downloadDataForUnregisteredUserThread.start()
        }
        else {
            downloadDataFromDatabaseThread.start()
        }
    }


    inner class DownloadDataForUnregisteredUserThread(private val handler: Handler) : Thread() {
        private var url : String? = null

        private val tag = "UnregisterUserDownload"

        private val defaultDocumentReference = Firebase.firestore.collection("books").document("0")

        override fun run() {
            super.run()

            defaultDocumentReference
                .get()
                .addOnSuccessListener {
                    snapShot ->
                    todayStory = snapShot.toObject<Story>()

                    Firebase.storage.reference
                        .child(todayStory!!.path_to_text!!)
                        .downloadUrl
                        .addOnSuccessListener {
                            storyUrl = it.toString()

                            Log.d(tag, "Book's Url has been downloaded completely")

                            handler.sendMessage(handler.obtainMessage())
                        }
                        .addOnFailureListener {
                            Log.d(tag, "Books downloading has been failed")
                        }

                }
                .addOnFailureListener {}

        }
    }

    inner class DownloadDataFromUsersDatabaseThread(private val handler: Handler) : Thread() {

        private val db = Firebase.firestore

        private val tag = "DownloadingFromDataBase"

        private var userInfo: User? = null

        override fun run() {
            super.run()

            db.collection("books")
                .document("0")
                .get()
                .addOnSuccessListener { documentDataSnapshot ->
                    todayStory = documentDataSnapshot.toObject<Story>()

                    db.collection("users").document("test_id")
                        .get()
                        .addOnSuccessListener {
                            userInfo = it.toObject<User>()

                            val reference = Firebase.storage.reference.child(todayStory!!.path_to_text!!)

                            reference.downloadUrl.
                                addOnSuccessListener { uri ->
                                    Log.d(tag, "Book's Url has been downloaded completely")
                                    storyUrl = uri.toString()

                                    handler.sendMessage(handler.obtainMessage())
                                }
                                .addOnFailureListener {
                                    Log.d(tag, "Books downloading has been failed")
                                }

                            Log.d(tag, userInfo.toString())
                        }
                        .addOnCanceledListener {
                            Log.d(tag, "Error")
                        }



                }
        }
    }
}