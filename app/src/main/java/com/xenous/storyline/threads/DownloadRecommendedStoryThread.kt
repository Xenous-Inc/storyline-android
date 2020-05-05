package com.xenous.storyline.threads

import android.content.Context
import android.os.Handler
import android.os.Message
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xenous.storyline.R
import com.xenous.storyline.data.Story
import com.xenous.storyline.data.User
import com.xenous.storyline.utils.*
import java.util.*
import kotlin.random.Random

class DownloadRecommendedStoryThread(
    private val user: User?,
    private val context: Context,
    private val onCompleteDownloadStoryHandler: Handler
) : Thread() {
    override fun run() {
        super.run()
        
        if(user != null) {
            getStoryForRegisteredUser(user)
        }
        else {
            getStoryForUnregisteredUser()
        }
    }
    
    private fun getStoryForRegisteredUser(user: User) {
        if(
            user.currentStory != null &&
            user.currentStory.isNotEmpty() &&
            user.currentStory.creationTime!!.isInSameDay(Date().time)
        ) {
            Firebase.firestore
                .collection("books").document(user.currentStory.storyUid!!).get()
                .addOnSuccessListener { storyDocument ->
                    if(storyDocument.exists()) {
                        try {
                            val story = storyDocument.toObject(Story::class.java)
                            if(story != null) {
                                story.uid = storyDocument.id
                                val msg = Message.obtain()
                                msg.apply {
                                    what = SUCCESS_CODE
                                    obj = story
                                }
                                onCompleteDownloadStoryHandler.sendMessage(msg)
                            }
                            else {
                                onCompleteDownloadStoryHandler.sendEmptyMessage(ERROR_CODE)
                            }
                        }
                        catch(e: ClassCastException) {
                            onCompleteDownloadStoryHandler.sendEmptyMessage(ERROR_CODE)
                        }
                    }
                    else {
                        onCompleteDownloadStoryHandler.sendEmptyMessage(ERROR_CODE)
                    }
                }
                .addOnFailureListener {
                    onCompleteDownloadStoryHandler.sendEmptyMessage(ERROR_CODE)
                }
                .addOnCanceledListener {
                    onCompleteDownloadStoryHandler.sendEmptyMessage(CANCEL_CODE)
                }
        }
        else {
            Firebase.firestore
                .collection("books").get()
                .addOnSuccessListener {snapshot ->
                    if(!snapshot.isEmpty) {
                        var storyDocumentSnapshot: DocumentSnapshot? = null
                        var story: Story? = null
                        
                        while(storyDocumentSnapshot == null || story == null) {
                            val storyNumber = Random.nextInt(0, snapshot.documents.size)
                            storyDocumentSnapshot = snapshot.documents[storyNumber]
                            
                            if(storyDocumentSnapshot.exists()) {
                                try {
                                    story = storyDocumentSnapshot.toObject(Story::class.java)
                                }
                                catch(e: ClassCastException) {
//                                Class Cast Failed, Continue
                                }
                            }
                        }
                        
                        story.uid = storyDocumentSnapshot.id
                        val msg = Message.obtain()
                        msg.apply {
                            what = SUCCESS_CODE
                            obj = story
                        }
                        onCompleteDownloadStoryHandler.sendMessage(msg)
                    }
                    else {
                        onCompleteDownloadStoryHandler.sendEmptyMessage(QUERY_IS_EMPTY)
                    }
                }
                .addOnFailureListener {
                    onCompleteDownloadStoryHandler.sendEmptyMessage(ERROR_CODE)
                }
                .addOnCanceledListener {
                    onCompleteDownloadStoryHandler.sendEmptyMessage(CANCEL_CODE)
                }
        }
        
    }
    
    private fun getStoryForUnregisteredUser() {
        val sharedPreferences =
            context.getSharedPreferences(context.getString(R.string.preferences_name), Context.MODE_PRIVATE)
        val currentStoryCreationTime =
            sharedPreferences.getLong(context.getString(R.string.preferences_date_of_last_reading), PREFERENCE_NOT_FOUND)
        val currentStoryUid =
            sharedPreferences.getString(context.getString(R.string.preferences_last_book), null)
        
        if(
            currentStoryUid != null &&
            currentStoryCreationTime != PREFERENCE_NOT_FOUND &&
            currentStoryCreationTime.isInSameDay(Date().time)
        ) {
            Firebase.firestore
                .collection("books").document(currentStoryUid).get()
                .addOnSuccessListener { storyDocumentSnapshot ->
                    if(storyDocumentSnapshot.exists()) {
                        try {
                            val story = storyDocumentSnapshot.toObject(Story::class.java)
                            if(story != null) {
                                story.uid = storyDocumentSnapshot.id
                                val msg = Message.obtain()
                                msg.apply {
                                    what = SUCCESS_CODE
                                    obj = story
                                }
                                onCompleteDownloadStoryHandler.sendMessage(msg)
                            }
                            else {
                                onCompleteDownloadStoryHandler.sendEmptyMessage(ERROR_CODE)
                            }
                        }
                        catch(e: ClassCastException) {
                            onCompleteDownloadStoryHandler.sendEmptyMessage(ERROR_CODE)
                        }
                    }
                    else {
                        onCompleteDownloadStoryHandler.sendEmptyMessage(ERROR_CODE)
                    }
                }
                .addOnFailureListener {
                    onCompleteDownloadStoryHandler.sendEmptyMessage(ERROR_CODE)
                }
                .addOnCanceledListener {
                    onCompleteDownloadStoryHandler.sendEmptyMessage(CANCEL_CODE)
                }
        }
        else {
            Firebase.firestore
                .collection("books")
                .get()
                .addOnSuccessListener {querySnapshot ->
                    if(!querySnapshot.isEmpty) {
                        var storyDocumentSnapshot: DocumentSnapshot? = null
                        var story: Story? = null
                
                        while(storyDocumentSnapshot == null || story == null) {
                            val storyNumber = Random.nextInt(0, querySnapshot.documents.size)
                            storyDocumentSnapshot = querySnapshot.documents[storyNumber]
                            if(storyDocumentSnapshot.exists()) {
                                try {
                                    story = storyDocumentSnapshot.toObject(Story::class.java)
                                }
                                catch(e: ClassCastException) {
//                                Class Cast Failed, Continue
                                }
                            }
                        }
                
                        story.uid = storyDocumentSnapshot.id
                        val msg = Message.obtain()
                        msg.apply {
                            what = SUCCESS_CODE
                            obj = story
                        }
                        onCompleteDownloadStoryHandler.sendMessage(msg)
                    }
                    else {
                        onCompleteDownloadStoryHandler.sendEmptyMessage(QUERY_IS_EMPTY)
                    }
                }
                .addOnFailureListener {
                    onCompleteDownloadStoryHandler.sendEmptyMessage(ERROR_CODE)
                }
                .addOnCanceledListener {
                    onCompleteDownloadStoryHandler.sendEmptyMessage(CANCEL_CODE)
                }
        }
    }
}