package com.xenous.storyline.data

data class User(
    var nickname : String,
    var interests : MutableList<Long>,
    var stats : HashMap<String, Long>,
    val history: MutableList<String>
)  {
    
    override fun toString(): String {
        return "User's name is $nickname. \n User's interests are $interests. User's last date is $stats"
    }
    
}