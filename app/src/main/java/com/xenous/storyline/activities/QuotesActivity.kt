package com.xenous.storyline.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import com.xenous.storyline.R
import com.xenous.storyline.data.Quote
import com.xenous.storyline.utils.ERROR_CODE
import com.xenous.storyline.utils.SUCCESS_CODE

class QuotesActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "Quotes Activity"
    }
    
    private lateinit var quotesRecyclerView : RecyclerView

    private lateinit var authentication: FirebaseAuth
    private var user : FirebaseUser? = null

    private lateinit var downloadQuotesHandler: Handler

    private val quotesList = arrayListOf<Quote>()

    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotes)
        
        quotesRecyclerView = findViewById(R.id.quotesRecyclerView)

        checkUserState()

        downloadQuotesHandler = object : Handler() {
            override fun handleMessage( msg: Message) {
                super.handleMessage(msg)

                when(msg.what) {
                    SUCCESS_CODE -> run {
                        quotesRecyclerView.adapter = QuotesRecyclerViewAdapter(this@QuotesActivity, quotesList)
                        quotesRecyclerView.layoutManager = LinearLayoutManager(this@QuotesActivity)
                    }
                    ERROR_CODE -> run {
                        DynamicToast.makeError(this@QuotesActivity, getString(R.string.error_while_download_quotes), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        DownloadQuotesThread().start()
    }

    private fun checkUserState() {
        authentication = FirebaseAuth.getInstance()
        user = authentication.currentUser

        authentication.addAuthStateListener { authentication ->
            if(authentication.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }
    
    inner class DownloadQuotesThread : Thread() {

        override fun run() {
            super.run()

            Firebase
                .firestore
                .collection("users")
                .document(user!!.uid)
                .collection("quotes")
                .get()
                .addOnSuccessListener {
                    Log.d(TAG, "Request has been managed")

                    for(document in it.documents) {
                        val quote = document.toObject<Quote>()

                        quotesList.add(quote!!)

                        Log.d(TAG, "Current quote's text is ${quote.text}")
                    }

                    Log.d(TAG, "Quotes amount is ${quotesList.size} ")

                    downloadQuotesHandler.sendEmptyMessage(SUCCESS_CODE)
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "Request failed")
                    Log.d(TAG, e.toString())

                    downloadQuotesHandler.sendEmptyMessage(ERROR_CODE)
                }
                .addOnCanceledListener {
                    Log.d(TAG, "Request canceled")

                    downloadQuotesHandler.sendEmptyMessage(ERROR_CODE)
                }
        }
    }
}

class QuotesRecyclerViewAdapter(
    private val activity : QuotesActivity,
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

