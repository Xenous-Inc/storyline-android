package com.xenous.storyline.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.xenous.storyline.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private var user : FirebaseUser? = null

    private lateinit var signInTextView : TextView
    private lateinit var googleSignInImageView : ImageButton
    private lateinit var createAccountTextView: TextView
    private lateinit var emailEditText : EditText
    private lateinit var passwordEditText: EditText

    private var googleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        if(user != null) {
            startActivity(Intent(this, RegistrationDetailsActivity::class.java))
        }

        signInTextView = findViewById(R.id.signInTextView)
        googleSignInImageView = findViewById(R.id.signInWithGoogleButton)
        createAccountTextView = findViewById(R.id.createAccountTextView)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        signInTextView.setOnClickListener {
            if(isEditTextsEmpty()) {
                Toast.makeText(this, "Вы не заполнили все поля", Toast.LENGTH_LONG).show()
            }
            else {
                auth.signInWithEmailAndPassword(emailEditText.text.toString(), passwordEditText.text.toString())
                    .addOnSuccessListener {
                        user = it.user
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_LONG).show()
                    }
            }
        }

        createAccountTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        googleSignInImageView.setOnClickListener {
            val signInIntent: Intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
            startActivityForResult(signInIntent, 1)
        }

        val googleSignInOptions = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleApiClient = GoogleApiClient.Builder(applicationContext)
            .enableAutoManage(this) {
                Toast.makeText(
                    applicationContext,
                    "произошла неизвестная ошибка",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            try { // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            }
            catch (e: ApiException) { // Google Sign In failed, update UI appropriately
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                else {
                    Toast.makeText(this, "Произошла ошибка в регистрации", Toast.LENGTH_LONG).show()
                }

            }
    }

    private fun isEditTextsEmpty() : Boolean {
        if(emailEditText.text.isBlank() || passwordEditText.text.isBlank()) {
            return  true
        }

        return false
    }
}
