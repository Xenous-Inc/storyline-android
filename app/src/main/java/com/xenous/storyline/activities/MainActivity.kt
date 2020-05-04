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
import android.view.ActionMode
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import com.xenous.storyline.R
import com.xenous.storyline.data.Quote
import com.xenous.storyline.data.Story
import com.xenous.storyline.data.User
import com.xenous.storyline.fragments.StoryFragment
import com.xenous.storyline.threads.DownloadRecommendedStoryThread
import com.xenous.storyline.threads.DownloadUserThread
import com.xenous.storyline.utils.CANCEL_CODE
import com.xenous.storyline.utils.ERROR_CODE
import com.xenous.storyline.utils.SUCCESS_CODE
import com.xenous.storyline.utils.StoryLayout

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private companion object {
        const val TAG = "MainActivity"
        const val QUOTE_TAG = "StoryFragment : Quote"
        const val DOWNLOAD_TAG = "DownloadingFromDataBase"
        const val UPDATE_USER_STATS_TAG = "UpdateStatsInDatabase"
        
        const val AVAILABLE_QUOTE_LENGTH = 35
    }
    
    private lateinit var fragmentFrameLayout: FrameLayout
    
    private var actionMode : ActionMode? = null
    
    private var storyFragment : StoryFragment? = null
    
    private var firebaseUser: FirebaseUser? = null

    var todayStory : Story? = null
    var todayStoryUrl : String? = null

    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        firebaseUser = FirebaseAuth.getInstance().currentUser
        
        fragmentFrameLayout = findViewById(R.id.fragmentFrameLayout)
        
        DownloadUserThread(getOnCompleteDownloadUserHandler()).start()
        
        makeStatusBarTransparent()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        startActivity(Intent(this, LoginActivity::class.java))
    }
    
    /*override fun onActionModeStarted(mode: ActionMode) {
        actionMode = mode
        val menu = mode.menu
        menu.clear()
        menuInflater.inflate(R.menu.custom_context_menu, menu)
        val menuItems: MutableList<MenuItem> = ArrayList()
        // get custom menu item
        for(i in 0 until menu.size()) {
            menuItems.add(menu.getItem(i))
        }
        menu.clear()
        // reset menu item order
        val size = menuItems.size
        for(i in 0 until size) {
            addMenuItem(menu, menuItems[i], i)
        }
        super.onActionModeStarted(mode)
    
    }*/
    
    /*private fun addMenuItem(
        menu : Menu, item : MenuItem, order : Int
    ) {
        val menuItem = menu.add(
            item.groupId, item.itemId, order, item.title
        )
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menuItem.setOnMenuItemClickListener {menuItem ->
            storyFragment?.storyWebView!!.evaluateJavascript(
                "window.getSelection().toString()"
            ) {value ->
                if(value.isEmpty()) {
                    Log.d(QUOTE_TAG, "The quote is empty")
                
                    return@evaluateJavascript
                }
            
                Log.d(QUOTE_TAG, "The quote's text is $value")
            
                if(value.trim().split(" ").size >= AVAILABLE_QUOTE_LENGTH) {
                    Log.d(QUOTE_TAG, "The quote's text is too long")
                    
                    DynamicToast.makeWarning(
                        this,
                        getString(R.string.too_long_quote_message),
                        Toast.LENGTH_SHORT
                    ).show()
                
                    return@evaluateJavascript
                }
            
                when(menuItem.itemId) {
                    R.id.addToQuotesItem -> addQuoteToDatabase(value)
                    R.id.copyItem        -> Quote(text = value).copyToClipBoard(this)
//                    R.id.shareItem       -> shareQuote(value)
                    R.id.shareItem       -> Quote(todayStory?.author, todayStory?.name, value)
                }
            }
        
            actionMode?.finish()
        
            true
        }
    }*/
    
    /*private fun createUpdatedUserStats() : HashMap<String, Long>?   {
        if(currentUser != null) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
    
            //TODO: Solve problem with matching time
            if(calendar.timeInMillis == currentUser!!.stats!!["last_date"] as Long) {
                return null
            }
    
            val newRating : Long = 0 //This param is for update rating TODO: add rating logic
            val newDate = calendar.timeInMillis
            val streak = currentUser!!.stats["streak"] as Long + 1
    
            return hashMapOf(
                "last_date" to newDate,
                "level" to newRating,
                "streak" to streak
            )
        }
        else {
            return null
        }
    }*/
    
    private fun addQuoteToDatabase(quoteText: String) {
        firebaseUser?.let {
            val quote = Quote(todayStory!!.author, todayStory!!.name, quoteText)
            val reference = Firebase.firestore.collection("users").document(firebaseUser!!.uid).collection("quotes")
            reference.get()
                .addOnSuccessListener {querySnapshot ->
                    reference
                        .document(querySnapshot.documents.size.toString())
                        .set(quote)
                        .addOnSuccessListener {
                            Log.d(QUOTE_TAG, "Quote has been sent to database successfully")
                    
                            DynamicToast.makeSuccess(this, getString(R.string.success_while_sending_quote_to_db), Toast.LENGTH_SHORT).show()
                        }
                        .addOnCanceledListener {
                            Log.d(QUOTE_TAG, "Quote has been canceled while sending to database")
                    
                            DynamicToast.makeError(this, getString(R.string.error_while_sending_quote_to_db), Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Log.d(QUOTE_TAG, "Quote has been failed while sending to database. The cause is ${it.cause.toString()}")
                    
                            DynamicToast.makeError(this, getString(R.string.error_while_sending_quote_to_db), Toast.LENGTH_SHORT).show()
                        }
                }
    
        }
    }

    @SuppressLint("HandlerLeak")
    private fun getOnCompleteDownloadStoryHandler() = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            
            try {
                val story = msg.obj as Story
                buildStoryLayout(story)
            }
            catch(e: ClassCastException) {
                // ToDo: Notify User About This Problem
            }
            if(msg.what == ERROR_CODE || msg.what == CANCEL_CODE) {
                Log.e(TAG, "ERROR OCCURED WHILE GETTING STORY")
                // ToDo: notify user
            }
        }
        
    }
    
    @SuppressLint("HandlerLeak")
    private fun getOnCompleteDownloadUserHandler() = object: Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        
            when(msg.what) {
                SUCCESS_CODE -> {
                    if(msg.obj == null || msg.obj is User) {
                        val currentUser = msg.obj as User?
                        DownloadRecommendedStoryThread(
                            currentUser,
                            getOnCompleteDownloadStoryHandler()
                        ).start()
                    }
                    else {
//                            ToDo: Notify User about yet another error
                    }
                }
                ERROR_CODE -> {
//                        ToDo: Notify User about yet another error
                }
                CANCEL_CODE -> {
//                        ToDo: Notify User about yet another error
                }
            }
        }
    }
    
    
    
    private fun buildStoryLayout(story: Story) {
        val storyLayout = StoryLayout.Builder(
            this,
            story
        ).build(layoutInflater)

        storyFragment = StoryFragment()

        storyLayout.cover.setOnClickListener {
            storyLayout.collapseStoryCover()
//            ToDo: Update user's stats
            /*val newUserStats = createUpdatedUserStats()
            if(newUserStats == null) {
                return@setOnClickListener
            }
            else {
                UpdateUserStatsInDatabaseThread(newUserStats).start()
            }*/
        }
        storyLayout.setCoverImageResource(R.drawable.demo_background)
        storyLayout.setContentFragment(storyFragment!!, supportFragmentManager)
        storyLayout.actionButton.setImageResource(
            if(firebaseUser != null) {
                R.drawable.ic_account_circle_32dp
            }
            else {
                R.drawable.ic_sync_32dp
            }
        )
        storyLayout.actionButton.setOnClickListener {
            if(firebaseUser != null) {
                startActivity(
                    Intent(
                        this@MainActivity, ProfileActivity::class.java
                    )
                )
            }
            else {
                startActivity(
                    Intent(
                        this@MainActivity, LoginActivity::class.java
                    )
                )
            }
        }

        fragmentFrameLayout.addView(storyLayout.view)
    }
    
    private fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if(on) {
            winParams.flags = winParams.flags or bits
        }
        else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }
    
    private fun makeStatusBarTransparent() {
        if(Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(
                this,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                true
            )
        }
        if(Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if(Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(
                this,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                false
            )
            window.statusBarColor = Color.parseColor("#aa000000")
        }
    }
    
    /*
    inner class DownloadDataForUnregisteredUserThread(private val handler: Handler) : Thread() {
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
                        .child(todayStory!!.path_to_text)
                        .downloadUrl
                        .addOnSuccessListener {
                            todayStoryUrl = it.toString()

                            Log.d(tag, "Book's Url has been downloaded completely")

                            handler.sendMessage(handler.obtainMessage())
                        }
                        .addOnFailureListener {
                            Log.d(tag, "Books downloading has been failed")
                        }

                }
                .addOnFailureListener {
                }

        }
    }

    inner class DownloadDataFromUsersDatabaseThread : Thread() {

        private val db = Firebase.firestore

        override fun run() {
            super.run()
            
            db.collection("users")
                .document(user!!.uid)
                .get()
                .addOnSuccessListener { userDocumentSnapshot ->
                    currentUser = userDocumentSnapshot.toObject<User>()
                    
                    Toast
                        .makeText(this@MainActivity, "Current User is $currentUser", Toast.LENGTH_LONG)
                        .show()
                    
                    db.collection("books")
                        .document("0") // Here function findBook() will return index of today's story in the storage
                        .get()
                        .addOnSuccessListener {bookDocumentSnapshot ->
                            
                            todayStory = bookDocumentSnapshot.toObject<Story>()
    
                            val reference = Firebase.storage.reference.child(todayStory!!.path_to_text)
                            reference.downloadUrl
                                .addOnSuccessListener { uri ->
                                    todayStoryUrl = uri.toString()
                                    
                                    downloadDataFromDatabaseHandler.sendEmptyMessage(SUCCESS_CODE)
                                }
                                .addOnFailureListener {
                                    downloadDataFromDatabaseHandler.sendEmptyMessage(ERROR_CODE)
                                }
                                .addOnCanceledListener {
                                    downloadDataFromDatabaseHandler.sendEmptyMessage(ERROR_CODE)
                                }
                            
                        }
                        .addOnFailureListener {
                            Log.d(DOWNLOAD_TAG, "Books downloading has been failed")
                            
                            downloadDataFromDatabaseHandler.sendEmptyMessage(ERROR_CODE)
                        }
                        .addOnCanceledListener {
                            Log.d(DOWNLOAD_TAG, "Books downloading has been failed")
                            
                            downloadDataFromDatabaseHandler.sendEmptyMessage(ERROR_CODE)
                        }
                }
                .addOnFailureListener {
                
                }
        }
        
        private fun findBook() : String {
            // Analyze user's interests and history of his books.
            // After that return a new book, which matches user's interests and has been never read by current user.
            // If There is not such book, warn user about it and offer to wait new stories
            TODO("Bot implemented yet")
        }
    }
    inner class UpdateUserStatsInDatabaseThread(
        private val userStatsMap : HashMap<String, Long>
    
    ) : Thread() {
        override fun run() {
            super.run()
            
            val userToDb = currentUser
            userToDb!!.stats = userStatsMap
    
            Firebase.firestore.collection("users").document(user!!.uid)
                .set(userToDb)
                .addOnSuccessListener {
                    Log.d(UPDATE_USER_STATS_TAG, "User stats have been updated successfully")
                }
                .addOnFailureListener { exception ->
                    Log.d(UPDATE_USER_STATS_TAG, "User stats have been updated unsuccessfully. The cause is ${exception.cause}")
                }
                .addOnCanceledListener {
                    Log.d(UPDATE_USER_STATS_TAG, "User stats' update has been canceled")
                }
        }
    }*/
}