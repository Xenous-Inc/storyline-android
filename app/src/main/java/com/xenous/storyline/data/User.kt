package com.xenous.storyline.data

data class User(
    var nickname : String? = null,
    var interests : List<Long>? = null,
    var stats : Map<String, Any>? = null
) {

    override fun toString(): String {
        return "User's name is $nickname. \n User's interests are $interests. User's last date is $stats"
    }

}