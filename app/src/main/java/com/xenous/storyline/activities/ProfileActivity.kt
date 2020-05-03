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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import com.xenous.storyline.R
import com.xenous.storyline.data.Quote
import com.xenous.storyline.data.User
import com.xenous.storyline.threads.DownloadQuotesThread
import com.xenous.storyline.threads.DownloadUserThread
import com.xenous.storyline.utils.ERROR_CODE
import com.xenous.storyline.utils.SUCCESS_CODE

class ProfileActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "Profile Activity"
    }
    
    private lateinit var userNameTextView : TextView
    private lateinit var streakInfoTextView : TextView
    private lateinit var quotesRecyclerView : RecyclerView
    
    private lateinit var downloadUserThread : DownloadUserThread
    private lateinit var downloadQuotesThread: DownloadQuotesThread
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        makeStatusBarTransparent()
        
        userNameTextView = findViewById(R.id.userNameTextView)
        streakInfoTextView = findViewById(R.id.streakInfoTextView)
        quotesRecyclerView = findViewById(R.id.quotesRecyclerView)
        findViewById<ImageButton>(R.id.exitImageButton)
            .setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        
        downloadUserThread = DownloadUserThread(getDownloadUserHandler())
        downloadUserThread.start()
        getDownloadQuotesHandler()
        
    }
    
    private fun updateUserInfo(user : User) {
        userNameTextView.text = user!!.nickname
        streakInfoTextView.text = "Вы читаете уже ${user!!.stats!!["streak"]} дней подряд"
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
            window.statusBarColor = Color.TRANSPARENT
        }
    }
    
    @SuppressLint("HandlerLeak")
    private fun getDownloadQuotesHandler() : Handler {
        return object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
            
                when(msg.what) {
                    SUCCESS_CODE -> run<Handler, Unit> {
                        
                        val quotesList = downloadQuotesThread.quotesList
                        quotesRecyclerView.adapter = QuotesRecyclerViewAdapter(this@ProfileActivity, quotesList)
                        quotesRecyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity)
                    }
                    ERROR_CODE -> run<Handler, Unit> {
                        DynamicToast.makeError(this@ProfileActivity,
                            getString(R.string.error_while_download_quotes), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    @SuppressLint("HandlerLeak")
    private fun getDownloadUserHandler() : Handler {
        return object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                Log.d(TAG, "DownloadUserHandler has caught message")
                
                when(msg.what) {
                    SUCCESS_CODE -> run {
                        val user = downloadUserThread.currentUser
                        updateUserInfo(user!!)
                        
                        downloadQuotesThread = DownloadQuotesThread(getDownloadQuotesHandler())
                        downloadQuotesThread.start()
                    }
                    ERROR_CODE -> run{}
                }
            }
        }
    }
}

class QuotesRecyclerViewAdapter(
    private val activity : ProfileActivity,
    private val quotesList : List<Quote>
) : RecyclerView.Adapter<QuotesRecyclerViewAdapter.QuotesRecyclerViewHolder>() {
    
    inner class QuotesRecyclerViewHolder(
        itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var quoteTextView : TextView
        lateinit var storyTextView : TextView
        lateinit var authorTextView : TextView
        lateinit var quoteCardView : CardView
        
        init {
            quoteTextView = itemView.findViewById(R.id.quoteTextView)
            storyTextView = itemView.findViewById(R.id.storyTextView)
            authorTextView = itemView.findViewById(R.id.authorTextView)
            quoteCardView = itemView.findViewById(R.id.quoteCardView)
        }
    }
    
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) : QuotesRecyclerViewHolder {
        return QuotesRecyclerViewHolder(LayoutInflater.from(activity).inflate(R.layout.quote_cell, parent, false))
    }
    
    override fun onBindViewHolder(holder: QuotesRecyclerViewHolder, position: Int) {
        holder.quoteTextView.text = quotesList[position].text
        holder.storyTextView.text = "\"" + quotesList[position].story + "\""
        holder.authorTextView.text = quotesList[position].author
        
        holder.quoteCardView.setOnClickListener {
            //TODO: Implements actions with quotes
        }
    }
    
    override fun getItemCount() : Int {
        return quotesList.size
    }
}