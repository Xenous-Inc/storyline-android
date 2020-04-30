package com.xenous.storyline

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.net.PasswordAuthentication

class RegistrationDetailsActivity : AppCompatActivity() {

    private lateinit var authentication : FirebaseAuth

    private lateinit var nameEditText : EditText
    private lateinit var interestsRecyclerView : RecyclerView
    private lateinit var nextTextView : TextView

    private lateinit var sendUsersDetailsToDatabaseHandler : Handler

    private val interestsIndexesList = arrayListOf<Int>()

    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_details)

        authentication = FirebaseAuth.getInstance()
        if(authentication.currentUser == null) {
            onBackPressed()

            Toast.makeText(this, "Произошла неизвестная ошибка", Toast.LENGTH_SHORT).show()
        }

        nameEditText = findViewById(R.id.nameEditText)
        interestsRecyclerView = findViewById(R.id.interestsRecyclerView)
        nextTextView = findViewById(R.id.nextTextView)

        sendUsersDetailsToDatabaseHandler = object : Handler() {

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
            }
        }

        

    }

    private fun isAllNecessaryFieldsEmpty() : Boolean {
        if(nameEditText.text.isBlank() || interestsIndexesList.size == 0) {
            return true
        }

        return false
    }

    inner class SendUsersDetailsToDataBaseThread(
        private val user : User
    ) : Thread() {
        override fun run() {
            super.run()

            Firebase.firestore
                .collection("users")
                .document(authentication.currentUser.toString())
                .set(user)
                .addOnSuccessListener {
                    sendUsersDetailsToDatabaseHandler.sendMessage(sendUsersDetailsToDatabaseHandler.obtainMessage())
                }
                .addOnFailureListener {

                }
        }
    }
}
