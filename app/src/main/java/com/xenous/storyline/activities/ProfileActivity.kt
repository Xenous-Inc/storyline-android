package com.xenous.storyline.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import com.xenous.storyline.R
import com.xenous.storyline.data.Quote
import com.xenous.storyline.data.User
import com.xenous.storyline.threads.DownloadQuotesThread
import com.xenous.storyline.threads.OldDownloadUserThread
import com.xenous.storyline.threads.RemoveQuoteThread
import com.xenous.storyline.utils.ERROR_CODE
import com.xenous.storyline.utils.SUCCESS_CODE
import com.xenous.storyline.utils.StoryLineDialog


class ProfileActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "Profile Activity"
    }
    
    private lateinit var userNameTextView : TextView
    private lateinit var streakInfoTextView : TextView
    private lateinit var quotesRecyclerView : RecyclerView
    
    private lateinit var oldDownloadUserThread : OldDownloadUserThread
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
                val storyLineDialog = StoryLineDialog(this)
                storyLineDialog.messageText = getString(R.string.exit_from_account_message)
                storyLineDialog.positiveText = getString(R.string.yes)
                storyLineDialog.setPositiveClickListener(
                    View.OnClickListener {
                        FirebaseAuth.getInstance().signOut()
                        startActivity(
                            Intent(
                                this, MainActivity::class.java
                            ).setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            )
                        )
                        
                        storyLineDialog.cancel()
                    }
                )
                storyLineDialog.negativeText = getString(R.string.no)
                storyLineDialog.setNegativeClickListener(View.OnClickListener {
                    storyLineDialog.cancel()
                })
                storyLineDialog.show()
            }
        
        oldDownloadUserThread = OldDownloadUserThread(getDownloadUserHandler())
        oldDownloadUserThread.start()
        getDownloadQuotesHandler()
        
    }
    
    private fun updateUserInfo(user : User) {
        userNameTextView.text = user.nickname
        streakInfoTextView.text = "Вы читаете уже ${user.stats["streak"]} дней подряд"
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
    
    @SuppressLint("HandlerLeak")
    private fun getDownloadQuotesHandler() : Handler {
        return object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
            
                when(msg.what) {
                    SUCCESS_CODE -> run<Handler, Unit> {
                        
                        val quotesList = downloadQuotesThread.quotesList
                        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                        if(quotesList.isEmpty()) {
                            findViewById<LinearLayout>(R.id.noQuotesLinearLayout).visibility = View.VISIBLE
                        }
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
                        val user = oldDownloadUserThread.currentUser
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
    private val quotesList : MutableList<Quote>
) : RecyclerView.Adapter<QuotesRecyclerViewAdapter.QuotesRecyclerViewHolder>() {
    
    inner class QuotesRecyclerViewHolder(
        itemView: View) : RecyclerView.ViewHolder(itemView) {
        var quoteTextView : TextView = itemView.findViewById(R.id.quoteTextView)
        var storyTextView : TextView = itemView.findViewById(R.id.storyTextView)
        var authorTextView : TextView = itemView.findViewById(R.id.authorTextView)
        var quoteCardView : CardView = itemView.findViewById(R.id.quoteCardView)
    
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
        
        holder.quoteCardView.setOnLongClickListener {
            activity.startActionMode(getActionMode(quotesList[position], position))
            
            return@setOnLongClickListener true
        }
    }
    
    override fun getItemCount() : Int {
        return quotesList.size
    }
    
    private fun getActionMode(currentQuote : Quote, quotePosition: Int) : ActionMode.Callback {
        return object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                mode?.finish()
                
                when(item?.itemId) {
                    R.id.shareItem -> {
                        currentQuote.shareQuote(activity)
                        
                        return true
                    }
                    R.id.removeQuoteItem -> {
                        startRemovingQuote(quotePosition)
                        
                        return true
                    }
                }
                
                return false
            }
            
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                val inflater = mode?.menuInflater
                inflater?.inflate(R.menu.quotes_action_menu, menu)
                
                return true
            }
            
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return true
            }
            
            override fun onDestroyActionMode(mode: ActionMode?) {
                mode?.finish()
            }
        }
    }
    
    private fun startRemovingQuote(quotePosition: Int) {
        RemoveQuoteThread(quotesList[quotePosition].quoteUID!!, getRemovingQuoteHandler(quotePosition)).start()
    }
    
    @SuppressLint("HandlerLeak")
    private fun getRemovingQuoteHandler(quotePosition : Int) : Handler {
        return object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                Log.d("Removing Quote Handler", "Message has been caught")
                
                when(msg.what) {
                    SUCCESS_CODE -> {
                        quotesList.removeAt(quotePosition)
                        this@QuotesRecyclerViewAdapter.notifyDataSetChanged()
                        DynamicToast.makeSuccess(
                            activity,
                            activity.getString(R.string.removing_quote_success)
                        ).show()
                        
                        if(quotesList.isEmpty()) {
                            activity.findViewById<LinearLayout>(R.id.noQuotesLinearLayout)
                                .visibility = View.VISIBLE
                        }
                    }
                    ERROR_CODE -> {
                        DynamicToast.makeError(
                            activity,
                            activity.getString(R.string.removing_quote_error)
                        ).show()
                    }
                }
            }
        }
    }
}