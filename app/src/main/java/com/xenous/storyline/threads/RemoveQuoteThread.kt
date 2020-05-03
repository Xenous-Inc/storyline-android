package com.xenous.storyline.threads

import android.os.Handler
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xenous.storyline.data.User
import com.xenous.storyline.utils.CANCEL_CODE
import com.xenous.storyline.utils.ERROR_CODE
import com.xenous.storyline.utils.SUCCESS_CODE

class RemoveQuoteThread(
    private val quoteIndex : Int,
    private val handler: Handler
) : Thread() {
    
    private companion object {
        const val TAG = "RemoveQuoteThread"
    }
    
    override fun run() {
        super.run()
        
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        
        if(firebaseUser == null) {
            handler.sendEmptyMessage(ERROR_CODE)
            
            return
        }
        
        Firebase.firestore.collection("users").document(firebaseUser.uid).collection("quotes")
            .document(quoteIndex.toString())
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Quote has been removed successfully")
                
                handler.sendEmptyMessage(SUCCESS_CODE)
            }
            .addOnFailureListener {
                Log.d(TAG, "Quote's removing has been failed")
                
                handler.sendEmptyMessage(ERROR_CODE)
            }
            .addOnCanceledListener {
                Log.d(TAG, "Quote's removing has been canceled")
                
                handler.sendEmptyMessage(CANCEL_CODE)
            }
            
           
    }

}