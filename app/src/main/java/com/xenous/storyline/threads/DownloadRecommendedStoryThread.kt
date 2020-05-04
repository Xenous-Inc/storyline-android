package com.xenous.storyline.threads

import android.os.Handler
import android.os.Message
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xenous.storyline.data.Story
import com.xenous.storyline.data.User
import com.xenous.storyline.utils.CANCEL_CODE
import com.xenous.storyline.utils.ERROR_CODE
import com.xenous.storyline.utils.QUERY_IS_EMPTY
import kotlin.random.Random

class DownloadRecommendedStoryThread(
    private val user: User?,
    private val onDownloadStoryHandler: Handler
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
        Firebase.firestore
            .collection("books").get()
            .addOnSuccessListener {snapshot ->
                if(!snapshot.isEmpty) {
                    for(storyDocument in snapshot.documents) {
                        if(storyDocument.exists() && !user.history.contains(storyDocument.id)) {
                            val story = storyDocument.toObject(Story::class.java)
                            if(story != null && story.tags.any {user.interests.contains(it)}) {
                                val msg = Message.obtain()
                                msg.obj = story
                                onDownloadStoryHandler.sendMessage(msg)
                                break
                            }
                        }
                    }
                }
                else {
                    onDownloadStoryHandler.sendEmptyMessage(QUERY_IS_EMPTY)
                }
            }
            .addOnFailureListener {
                onDownloadStoryHandler.sendEmptyMessage(ERROR_CODE)
            }
            .addOnCanceledListener {
                onDownloadStoryHandler.sendEmptyMessage(CANCEL_CODE)
            }
    }
    
    private fun getStoryForUnregisteredUser() {
        Firebase.firestore
            .collection("books")
            .get()
            .addOnSuccessListener {snapshot ->
                if(!snapshot.isEmpty) {
                    var story: Story? = null
    
                    while(story == null) {
                        val storyNumber = Random.nextInt(0, snapshot.documents.size)
                        val storyDocumentSnapshot = snapshot.documents[storyNumber]
                        if(storyDocumentSnapshot.exists()) {
                            story = storyDocumentSnapshot.toObject(Story::class.java)
                        }
                    }
    
                    val msg = Message.obtain()
                    msg.obj = story
                    onDownloadStoryHandler.sendMessage(msg)
                }
                else {
                    onDownloadStoryHandler.sendEmptyMessage(QUERY_IS_EMPTY)
                }
            }
            .addOnFailureListener {
                onDownloadStoryHandler.sendEmptyMessage(ERROR_CODE)
            }
            .addOnCanceledListener {
                onDownloadStoryHandler.sendEmptyMessage(CANCEL_CODE)
            }
    }
}