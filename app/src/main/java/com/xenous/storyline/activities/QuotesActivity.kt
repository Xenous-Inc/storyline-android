package com.xenous.storyline.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.xenous.storyline.R
import com.xenous.storyline.data.Quote
import com.xenous.storyline.utils.ERROR_CODE
import com.xenous.storyline.utils.SUCCESS_CODE

class QuotesActivity : AppCompatActivity() {

    private companion object {
        const val TAG = "Quotes Activity"
    }

    private lateinit var authentication: FirebaseAuth
    private var user : FirebaseUser? = null

    private lateinit var downloadQuotesHandler: Handler

    private val quotesList = arrayListOf<Quote>()

    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotes)

        checkUserState()

        downloadQuotesHandler = object : Handler() {
            override fun handleMessage( msg: Message) {
                super.handleMessage(msg)

                when(msg.what) {
                    SUCCESS_CODE -> run {}
                    ERROR_CODE -> run {}
                }
            }
        }

        DownloadQuotesThread().start()
    }

    private fun checkUserState() {
        authentication = FirebaseAuth.getInstance()
        user = authentication.currentUser

        authentication.addAuthStateListener {
            if(it.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }


    inner class DownloadQuotesThread : Thread() {

        override fun run() {
            super.run()

            Firebase.firestore.collection("users").document(user!!.uid).collection("quotes")
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
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Request failed")
                    Log.d(TAG, exception.toString())

                    downloadQuotesHandler.sendEmptyMessage(ERROR_CODE)
                }
                .addOnCanceledListener {
                    Log.d(TAG, "Request canceled")

                    downloadQuotesHandler.sendEmptyMessage(ERROR_CODE)
                }
        }
    }
}
