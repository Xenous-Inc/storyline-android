package com.xenous.storyline.threads

import android.os.Handler
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.xenous.storyline.data.User
import com.xenous.storyline.utils.CANCEL_CODE
import com.xenous.storyline.utils.ERROR_CODE
import com.xenous.storyline.utils.SUCCESS_CODE

class OldDownloadUserThread(
    private val handler: Handler
) : Thread() {
    
    private companion object {
        const val TAG = "DownloadUserThread"
    }
    
    var currentUser : User? = null
    
    override fun run() {
        super.run()
        
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        
        if(firebaseUser == null) {
            Log.d(TAG, "Firebase User is null")
            
            return
        }
        
        Firebase.firestore.collection("users").document(firebaseUser.uid)
            .get()
            .addOnSuccessListener { userDocumentSnapshot ->
                Log.d(TAG, "User's info has been downloaded completely")
                currentUser = userDocumentSnapshot.toObject<User>()
                
                handler.sendEmptyMessage(SUCCESS_CODE)
            }
            .addOnFailureListener {
                Log.d(TAG, "User's info downloading has been failed")
                
                handler.sendEmptyMessage(ERROR_CODE)
            }
            .addOnCanceledListener {
                Log.d(TAG, "User's info downloading has been canceled")
                
                handler.sendEmptyMessage(CANCEL_CODE)
            }
    }
}