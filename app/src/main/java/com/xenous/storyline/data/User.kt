package com.xenous.storyline.data

data class User(
    val nickname : String? = null ,
    val interests : List<Long>? = null,
    val stats : Map<String, Any>? = null
) {

    override fun toString(): String {
        return "User's name is $nickname. \n User's interests are $interests. User's last date is $stats"
    }

}