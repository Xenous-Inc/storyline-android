package com.xenous.storyline.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import com.xenous.storyline.R
import com.xenous.storyline.broadcasts.NotificationBroadcastReceiver
import com.xenous.storyline.data.CurrentStory
import com.xenous.storyline.data.Quote
import com.xenous.storyline.data.Story
import com.xenous.storyline.data.User
import com.xenous.storyline.fragments.StoryFragment
import com.xenous.storyline.threads.DownloadRecommendedStoryThread
import com.xenous.storyline.threads.DownloadUserThread
import com.xenous.storyline.utils.*
import java.util.*
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "MainActivity"
        const val QUOTE_TAG = "StoryFragment : Quote"
        
        const val AVAILABLE_QUOTE_LENGTH = 35
    }
    
    private var actionMode : ActionMode? = null
    private var firebaseUser: FirebaseUser? = null
    private var currentUser: User? = null
    private var story: Story? = null
    private var storyWebView: WebView? = null
    
    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        firebaseUser = FirebaseAuth.getInstance().currentUser
        DownloadUserThread(getOnCompleteDownloadUserHandler()).start()
        makeStatusBarTransparent()
    
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(this)
        }
        startAlarm()
    }
    
    override fun onActionModeStarted(mode: ActionMode) {
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
            createMenu(menu, menuItems[i], i)
        }
        
        super.onActionModeStarted(mode)
    }
    
    /*
    * Handler, that will be called after User was downloaded
    * It will start DownloadRecommendedStoryThread
    * */
    @SuppressLint("HandlerLeak")
    private fun getOnCompleteDownloadUserHandler() = object: Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        
            when(msg.what) {
                SUCCESS_CODE -> {
                    if(msg.obj == null || msg.obj is User) {
                        currentUser = msg.obj as User?
                        
                        if(currentUser!!.interests.isEmpty()) {
                            Log.d(TAG, "User's interests are empty")
                            
                            DynamicToast.makeWarning(this@MainActivity, getString(R.string.user_does_not_interesting)).show()
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    RegistrationDetailsActivity::class.java
                                ).setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                )
                            )
                        }
                        
                        DownloadRecommendedStoryThread(
                            currentUser,
                            applicationContext,
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
    
    /*
    * Handler, that will be called after Story was downloaded
    * It will call buildStoryLayout function
    * */
    @SuppressLint("HandlerLeak")
    private fun getOnCompleteDownloadStoryHandler() = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            
            try {
                val story = msg.obj as Story
                this@MainActivity.story = story
                updateCurrentStory()
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
    
    /*
    * Handler, that will be called after View created in StoryFragment
    * It will load the page to WebVew
    * */
    @SuppressLint("HandlerLeak")
    private  fun getOnCompleteLoadStoryFragment(story: Story, storyLayout: StoryLayout) =
        object: Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
        
                if(msg.obj is WebView) {
                    storyWebView = msg.obj as WebView
            
                    Firebase.storage.reference.child(story.path_to_text).downloadUrl.addOnSuccessListener {
                        val storyUrl = it.toString()
                        storyWebView?.apply {
                            this.loadUrl(storyUrl)
                            storyLayout.hideCoverOnScroll(this)
                        }
                    }
                }
            }
        }
    
    @SuppressLint("HandlerLeak")
    private fun buildStoryLayout(story: Story) {
        val storyLayout = StoryLayout.Builder(this, story).build(layoutInflater)
        val storyFragment = StoryFragment(getOnCompleteLoadStoryFragment(story, storyLayout))
        storyLayout.apply {
            coverTitleVerticalBias = 0.4F
            setCoverImageResource(R.drawable.profile_header_six)
            setContentFragment(storyFragment, supportFragmentManager)
            actionButton.setImageResource(
                if(firebaseUser != null) {
                    R.drawable.ic_account_circle_32dp
                }
                else {
                    R.drawable.ic_sync_32dp
                }
            )
            actionButton.setOnClickListener {
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
            cover.setOnClickListener {
                storyLayout.collapseStoryCover()
                
                updateUserStats()
            }
        }
        
        val fragmentFrameLayout = findViewById<FrameLayout>(R.id.fragmentFrameLayout)
        fragmentFrameLayout.addView(storyLayout.view)
    }

    @SuppressLint("CommitPrefEdits")
    private fun updateCurrentStory() {
        if(firebaseUser != null && currentUser != null) {
            if(story != null && story!!.uid != null) {
                val currentStory = CurrentStory(story!!.uid, Date().time)
                Firebase.firestore
                    .collection("users")
                    .document(firebaseUser!!.uid)
                    .update("currentStory", currentStory)
            }
        }
        else {
            if(story != null && story!!.uid != null) {
                val sharedPreferences =
                    getSharedPreferences(getString(R.string.preferences_name), Context.MODE_PRIVATE)
                sharedPreferences.edit()
                    .putString(getString(R.string.preferences_last_book), story!!.uid)
                    .putLong(getString(R.string.preferences_date_of_last_reading), Date().time)
                    .apply()
            }
        }
    }
    
    private fun startAlarm() {
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent: PendingIntent
    
        val calendar: Calendar = GregorianCalendar()
        calendar[Calendar.HOUR_OF_DAY] = 12
        calendar[Calendar.MINUTE] = 0
    
        val myIntent = Intent(this@MainActivity, NotificationBroadcastReceiver::class.java)
        pendingIntent =
            PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        manager.cancel(pendingIntent)
    
        if(Date().time <= calendar.timeInMillis) {
            manager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = pendingIntent
        }
        else {
            manager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + MILLIS_IN_DAY, pendingIntent)
        }
    }
    
    private fun updateUserStats() {
    
        if(currentUser == null) {
            Log.d(TAG, "UpdateUserStats : Current user is null")
        
            return
        }
    
        val isDateAfter = getTimeInMillisAtZeroHours(System.currentTimeMillis()) > currentUser!!.stats["last_date"]!!
    
        if(isDateAfter) {
            Log.d(TAG, "UpdateUserStats : Current user is null")
        
            val rating: Long = 0 // ToDO: add rating logic
            var streak =
                if(
                    getTimeInMillisAtZeroHours(System.currentTimeMillis()) - currentUser!!.stats["last_date"]!! > MILLIS_IN_DAY
                ) { currentUser!!.stats["streak"]!! }
                else { 0 }
        
            streak += 1
        
            val newUser = currentUser!!
            newUser.history.add(story!!.uid!!)
            newUser.stats = hashMapOf(
                "last_date" to getTimeInMillisAtZeroHours(System.currentTimeMillis()),
                "level" to rating,
                "streak" to streak
            )
        
            Firebase.firestore.collection("users").document(firebaseUser!!.uid)
                .set(
                    newUser
                )
                .addOnSuccessListener {
                    Log.d(TAG, "UpdateUserStats : User's info has been updated successfully")
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "UpdateUserStats : User's info updating has been failed. The cause is ${exception.cause}")
                }
                .addOnCanceledListener {
                    Log.d(TAG, "UpdateUserStats : User's info updating has been canceled")
                }
        }
    }
    
    private fun addQuoteToDatabase(story: Story, quoteText: String) {
        if(firebaseUser != null) {
            Log.d(QUOTE_TAG, "Start adding quote to database")
            
            val quote = Quote(story.author, story.name, quoteText)
            
            Firebase.firestore
                .collection("users")
                .document(firebaseUser!!.uid)
                .collection("quotes")
                .add(quote)
                .addOnSuccessListener {
                    Log.d(QUOTE_TAG, "Quote has been sent to database successfully")
                    DynamicToast
                        .makeSuccess(
                            this,
                            getString(R.string.success_while_sending_quote_to_db),
                            Toast.LENGTH_SHORT
                        ).show()
                }
                .addOnCanceledListener {
                    Log.d(QUOTE_TAG, "Quote has been canceled while sending to database")
                    DynamicToast
                        .makeError(
                            this,
                            getString(R.string.error_while_sending_quote_to_db),
                            Toast.LENGTH_SHORT
                        ).show()
                }
                .addOnFailureListener { exception ->
                    Log.d(QUOTE_TAG, "Quote has been failed while sending to database. The cause is ${exception.cause.toString()}")
        
                    DynamicToast
                        .makeError(
                            this,
                            getString(R.string.error_while_sending_quote_to_db),
                            Toast.LENGTH_SHORT
                        ).show()
                }
        }
        else {
            Log.d(QUOTE_TAG, "Add quote to database : Firebase user is null")
        }
    }
    
    private fun createMenu(
        menu: Menu,
        item: MenuItem,
        order : Int
    ) {
        val menuItems = menu.add(item.groupId, item.itemId, order, item.title)
        menuItems.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        
        menuItems.setOnMenuItemClickListener {menuItem ->
            storyWebView
                ?.evaluateJavascript("window.getSelection().toString()") {value ->
                    
                    var quoteText = value
                    quoteText = quoteText.replace("\"", "")
                    quoteText = quoteText.replace("\n", "")
                    
                    
                    if(quoteText.isBlank()) {
                        return@evaluateJavascript
                    }
                    if(quoteText.trim().split(" ").size >= AVAILABLE_QUOTE_LENGTH) {
                        Log.d(QUOTE_TAG, "The quote's text is too long")
                        
                        DynamicToast.makeWarning(
                            this,
                            getString(R.string.too_long_quote_message),
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        return@evaluateJavascript
                    }
                    else {
                        Log.d(QUOTE_TAG, "The quote meets the standards")
                    }
                    
                    Log.d(QUOTE_TAG, "Quotes text is $quoteText")
                    
                    when(menuItem.itemId) {
                        R.id.addToQuotesItem -> if(story != null) {
                            addQuoteToDatabase(story!!, quoteText)
                        }
                        R.id.copyItem        -> Quote(text = quoteText).copyToClipBoard(this)
                        R.id.shareItem       -> if(story != null) {
                            Quote(story!!.author, story!!.name, quoteText).shareQuote(this)
                        }
                    }
                }
            
            actionMode?.finish()
            
            true
        }
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
            window.statusBarColor = ContextCompat.getColor(this, R.color.blackTransparentColor)
        }
    }
}