package com.xenous.storyline

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.*
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.net.PasswordAuthentication

class RegistrationDetailsActivity : AppCompatActivity() {

    companion object {
        const val SUCCESS_CODE = 1
        const val ERROR_CODE = 0
    }

    private lateinit var authentication : FirebaseAuth

    private lateinit var nameEditText : EditText
    private lateinit var interestsLinearLayout : LinearLayout
    private lateinit var nextTextView : TextView

    private lateinit var sendUsersDetailsToDatabaseHandler : Handler

    private val userAnswers = arrayListOf(
        -1, -1, -1, -1
    )

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
        interestsLinearLayout = findViewById(R.id.interestsLinearLayout)
        nextTextView = findViewById(R.id.nextTextView)

        val interestsTexts = listOf<String>(
            "Русская классическая литература",
            "Русская современная литература",
            "Зарубежная классическая литература",
            "Зарубежная современная литература"
        )

        for(checkBoxCounter in 0..3) {
            val checkBox = CheckBox(this)
            checkBox.text = interestsTexts[checkBoxCounter]
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked) {
                    userAnswers[checkBoxCounter] = checkBoxCounter
                }
                else {
                    userAnswers[checkBoxCounter] = -1
                }
            }

            interestsLinearLayout.addView(checkBox)
        }

        sendUsersDetailsToDatabaseHandler = object : Handler() {

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
            }
        }

        nextTextView.setOnClickListener {
            if(isAllNecessaryFieldsEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
            else {
                val userInterests = arrayListOf<Long>()

                for(userInterestsCounter in 0 until userAnswers.size) {
                    if(userAnswers[userInterestsCounter] > -1) {
                        userInterests.add(userAnswers[userInterestsCounter].toLong())
                    }
                }
            }
        }

    }

    private fun isAllNecessaryFieldsEmpty() : Boolean {
        var isUsersAnswersListEmpty = false

        for(userAnswersCounter in 0 until userAnswers.size) {
            if(userAnswers[userAnswersCounter] > -1) {
                isUsersAnswersListEmpty = true

                break
            }
        }

        if(nameEditText.text.isBlank() || !isUsersAnswersListEmpty) {
            return true
        }

        return false
    }

    inner class SendUsersDetailsToDataBaseThread(
        private val user : FirebaseUser
    ) : Thread() {
        override fun run() {
            super.run()

            Firebase.firestore
                .collection("users")
                .document(authentication.currentUser.toString())
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
