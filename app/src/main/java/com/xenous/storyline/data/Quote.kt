package com.xenous.storyline.data

data class Quote(
    val author : String? = null,
    val story : String? = null,
    val text : String? = null
) {

    override fun toString(): String {
        return "Book's name is $story. Story's author is $author. Quote's text is $text"
    }
}