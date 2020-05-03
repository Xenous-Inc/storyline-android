package com.xenous.storyline.data

import android.os.Parcel
import android.os.Parcelable

data class User(
    var nickname : String? = null,
    var interests : MutableList<Long>? = null,
    var stats : HashMap<String, Long>? = null
)  {
    
    override fun toString(): String {
        return "User's name is $nickname. \n User's interests are $interests. User's last date is $stats"
    }
    
}