package com.xenous.storyline.threads

import android.os.Handler
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.xenous.storyline.data.Quote
import com.xenous.storyline.utils.CANCEL_CODE
import com.xenous.storyline.utils.ERROR_CODE
import com.xenous.storyline.utils.SUCCESS_CODE

class DownloadQuotesThread(
    private val handler: Handler
) : Thread() {
    
    private companion object {
        const val TAG = "DownloadQuotesThread"
    }
    
    val quotesList= mutableListOf<Quote>()
    
    override fun run() {
        super.run()
        
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        
        if(firebaseUser == null) {
            Log.d(TAG, "Firebase User is null")
            handler.sendEmptyMessage(ERROR_CODE)
            
            return
        }
        
        Firebase.firestore.collection("users").document(firebaseUser.uid).collection("quotes")
            .get()
            .addOnSuccessListener { quotesDocumentSnapshot ->
                Log.d(TAG, "Quotes has been downloaded successfully")
    
                for(document in quotesDocumentSnapshot.documents) {
                    val quote = document.toObject<Quote>()
        
                    quotesList.add(quote!!)
        
                    Log.d(TAG, "Current quote's text is ${quote.text}")
                }
    
                Log.d(TAG, "Quotes amount is ${quotesList.size} ")
                
                handler.sendEmptyMessage(SUCCESS_CODE)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Quotes downloading has been failed. The cause is ${exception.cause}")
                
                handler.sendEmptyMessage(ERROR_CODE)
            }
            .addOnCanceledListener {
                Log.d(TAG, "Quotes downloading has been canceled")
                
                handler.sendEmptyMessage(CANCEL_CODE)
            }
    }
}