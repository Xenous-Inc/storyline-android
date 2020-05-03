package com.xenous.storyline.threads

import android.os.Handler
import android.os.Message
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xenous.storyline.data.Story
import com.xenous.storyline.data.User

class DownloadRecommendedStoryThread(
    private val user: User?,
    private val handler: Handler
) : Thread() {
    
    override fun run() {
        super.run()
        
        if(user != null) {
            getStoryForRegisteredUser(user)
        }
        else {
            // ToDo: get story for unregistered
        }
    }
    
    private fun getStoryForRegisteredUser(user: User) {
        Firebase.firestore
            .collection("books").get()
            .addOnSuccessListener {snapshot ->
                if( !snapshot.isEmpty ) {
                    for(storyDocument in snapshot.documents) {
                        if(storyDocument.exists() && !user.history.contains(storyDocument.id)) {
                            val story = storyDocument.toObject(Story::class.java)
                            if(story != null && story.genres.any {user.interests.contains(it)}) {
                                val msg = Message.obtain()
                                msg.obj = story
                                handler.sendMessage(msg)
                                break
                            }
                        }
                    }
                }
            }
    }
    
    private fun getStoryForUnregisteredUser() {
    
    }
}