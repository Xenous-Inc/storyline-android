package com.xenous.storyline.threads

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
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
    companion object {
        const val TAG = "DownloadRecommendStoryThread"
    }
    
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
//        If currentStory exists, load book from here
        if(
            user.currentStory != null &&
            user.currentStory!!.isNotEmpty() &&
            user.currentStory!!.creationTime!!.isSameDayAs(Date().time)
        ) {
            Firebase.firestore
                .collection("books").document(user.currentStory!!.storyUid!!).get()
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
//        If currentStory doesn't exist, load new book
        else {
            val storiesIndexesList = mutableListOf<String>()
            
            Firebase.firestore
                .collection("books").get()
                .addOnSuccessListener {querySnapshot ->
                    if(!querySnapshot.isEmpty) {
                        var storyDocumentSnapshot: DocumentSnapshot? = null
                        var story: Story? = null
                        
                        while(storyDocumentSnapshot == null || story == null) {
                            val storyNumber = Random.nextInt(0, querySnapshot.documents.size)
                            storyDocumentSnapshot = querySnapshot.documents[storyNumber]
                            
                            storiesIndexesList.add(storyDocumentSnapshot.id)
                            
                            if(
                                storyDocumentSnapshot.exists() &&
                                !user.history.contains(storyDocumentSnapshot.id)
                            ) {
                                try {
                                    story = storyDocumentSnapshot.toObject(Story::class.java)
                                    if(story != null) {
                                        if(!story.tags.any { user.interests.contains(it) }) {
                                            story = null
                                        }
                                    }
                                }
                                catch(e: ClassCastException) {
                                    story = null
                                }
                            }
                        }
                        
                        if(storiesIndexesList.size == querySnapshot.size()) {
                            onCompleteDownloadStoryHandler.sendEmptyMessage(
                                THERE_ARE_NOT_SUITABLE_STORY
                            )
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

//        If currentStory exists, load book from here
        if(
            currentStoryUid != null &&
            currentStoryCreationTime != PREFERENCE_NOT_FOUND &&
            currentStoryCreationTime.isSameDayAs(Date().time)
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
//        If currentStory doesn't exist, load new book
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
                                    story = null
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