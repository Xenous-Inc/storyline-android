package com.xenous.storyline.data

data class CurrentStory(
    val storyUid: String? = null,
    val creationTime: Long? = null
) {
    fun isNotEmpty(): Boolean {
        return storyUid != null && creationTime != null
    }
    
}
