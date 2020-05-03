package com.xenous.storyline.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import com.xenous.storyline.R
import com.xenous.storyline.data.User
import com.xenous.storyline.utils.ERROR_CODE
import com.xenous.storyline.utils.SUCCESS_CODE

class RegistrationDetailsActivity : AppCompatActivity() {
    
    private companion object {
        const val TAG = "RegistrationDetails"
    }

    private lateinit var authentication : FirebaseAuth

    private lateinit var nameEditText : EditText
    private lateinit var interestsLinearLayout : LinearLayout
    private lateinit var nextTextView : TextView
    private lateinit var russianClassicalLiteratureCheckBox : CheckBox
    private lateinit var russianModernLiteratureCheckbox : CheckBox
    private lateinit var foreignClassicLiteratureCheckbox : CheckBox
    private lateinit var foreignModernLiteratureCheckbox : CheckBox

    private lateinit var sendUsersDetailsToDatabaseHandler : Handler

    private val userAnswers = arrayListOf(
        -1, -1, -1, -1
    )

    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_details)
        
        nameEditText = findViewById(R.id.nameEditText)
        interestsLinearLayout = findViewById(R.id.interestsLinearLayout)
        nextTextView = findViewById(R.id.nextTextView)
        russianClassicalLiteratureCheckBox = findViewById(R.id.russianClassicLiteratureCheckbox)
        russianModernLiteratureCheckbox = findViewById(R.id.russianModernLiteratureCheckbox)
        foreignClassicLiteratureCheckbox = findViewById(R.id.foreignClassicLiteratureCheckbox)
        foreignModernLiteratureCheckbox = findViewById(R.id.foreignModernLiteratureCheckbox)
        
        setUpCheckBoxListener()
        
        checkUser()
        
        nextTextView.setOnClickListener {view ->
            if(isNameEmpty()) {
                DynamicToast.makeWarning(this, getString(R.string.not_fully_necessary))
                
                return@setOnClickListener
            }
            
            val userInterests = arrayListOf<Long>()
            
            for(userAnswersCounter in userAnswers.indices) {
                if(userAnswers[userAnswersCounter] > -1) {
                    userInterests.add(userAnswers[userAnswersCounter].toLong())
                }
            }
            
            if(userInterests.size == 0) {
                Log.d(TAG, "User has not chosen anything")
                DynamicToast.makeWarning(this, getString(R.string.not_fully_necessary), Toast.LENGTH_SHORT).show()
                
                return@setOnClickListener
            }
            
            val user = User(
                nameEditText.text.toString(),
                userInterests,
                hashMapOf(
                    "last_date" to 0.toLong(),
                    "level" to 0.toLong(),
                    "streak" to 0.toLong()
                )
            )
    
            initSendUsersDetailsToDatabaseHandler()
            
            SendUsersDetailsToDataBaseThread(user).start()
            
            view.isClickable = false
        }
    }
    
    @SuppressLint("HandlerLeak")
    private fun initSendUsersDetailsToDatabaseHandler() {
        sendUsersDetailsToDatabaseHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                
                nextTextView.isClickable = true
                
                when(msg.what) {
                    SUCCESS_CODE -> startActivity(Intent(this@RegistrationDetailsActivity, MainActivity::class.java))
                    ERROR_CODE -> DynamicToast.makeError(this@RegistrationDetailsActivity, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun checkUser() {
        authentication = FirebaseAuth.getInstance()
        if(authentication.currentUser == null) {
            onBackPressed()
        
            Toast.makeText(this, "Произошла неизвестная ошибка", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setUpCheckBoxListener() {
        val checkBoxes = listOf(
            russianClassicalLiteratureCheckBox,
            russianModernLiteratureCheckbox,
            foreignClassicLiteratureCheckbox,
            foreignModernLiteratureCheckbox
        )
        
        for(checkBoxCounter in checkBoxes.indices) {
            checkBoxes[checkBoxCounter].setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    Log.d(TAG, "The interest number $checkBoxCounter has been chosen")
                    
                    userAnswers[checkBoxCounter] = checkBoxCounter
                }
                else {
                    Log.d(TAG, "The interest number $checkBoxCounter has been unchosen")
                    
                    userAnswers[checkBoxCounter] = -1
                }
            }
        }
    }

    private fun isNameEmpty() : Boolean {
        if(nameEditText.text.isEmpty()) {
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
                .document(authentication.currentUser!!.uid.toString())
                .set(user)
                .addOnSuccessListener {
                    sendUsersDetailsToDatabaseHandler.sendEmptyMessage(SUCCESS_CODE)
                }
                .addOnFailureListener {
                    sendUsersDetailsToDatabaseHandler.sendEmptyMessage(ERROR_CODE)
                }
        }
    }
}
