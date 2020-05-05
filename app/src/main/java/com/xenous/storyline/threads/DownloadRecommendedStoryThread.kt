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
import com.xenous.storyline.utils.SUCCESS_CODE
import kotlin.random.Random

class DownloadRecommendedStoryThread(
    private val user: User?,
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
        Firebase.firestore
            .collection("books").get()
            .addOnSuccessListener {snapshot ->
                if(!snapshot.isEmpty) {
                    for(storyDocument in snapshot.documents) {
                        if(storyDocument.exists() && !user.history.contains(storyDocument.id)) {
                            try {
                                val story = storyDocument.toObject(Story::class.java)
                                if(story != null && story.tags.any {user.interests.contains(it)}) {
                                    story.uid = storyDocument.id
                                    val msg = Message.obtain()
                                    msg.apply {
                                        what = SUCCESS_CODE
                                        obj = story
                                    }
                                    onCompleteDownloadStoryHandler.sendMessage(msg)
                                    break
                                }
                            }
                            catch(e: ClassCastException) {
//                                Class Cast Failed, Continue
                            }
                        }
                    }
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
                            try {
                                story = storyDocumentSnapshot.toObject(Story::class.java)
                            }
                            catch(e: ClassCastException) {
//                                Class Cast Failed, Continue
                            }
                        }
                    }
    
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