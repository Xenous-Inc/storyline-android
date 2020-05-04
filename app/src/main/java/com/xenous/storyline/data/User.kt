package com.xenous.storyline.data

data class User(
    var nickname : String = "Username",
    var interests : MutableList<Long> = mutableListOf(),
    var stats : HashMap<String, Long> = hashMapOf("last_date" to 0L, "level" to 0L, "streak" to 0L),
    val history: MutableList<String> = mutableListOf()
)  {
    
    override fun toString(): String {
        return "User's name is $nickname. \n User's interests are $interests. User's last date is $stats"
    }
    
}