package com.xenous.storyline

import java.lang.reflect.Array
import java.util.*
import kotlin.collections.HashMap

data class User(
    val nickname : String? = null ,
    val interests : List<Int>? = null,
    val quotes_paths : List<String>? = null,
    val stats : Map<String, Any>? = null
) {

    override fun toString(): String {
        return "User's name is $nickname. \n User's interests are $interests. Paths to quotes are $quotes_paths. User's last date is $stats"
    }

}