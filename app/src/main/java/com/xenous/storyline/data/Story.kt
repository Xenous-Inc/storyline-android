package com.xenous.storyline.data

data class Story(
    val author : String = "Author",
    val name : String = "Title",
    val path_to_text: String = "books/book.html",
    val time_to_read: Long = 0,
    val tags: List<Long> = mutableListOf(),
    var uid: String? = null
) {
    override fun toString(): String {
        return "Story's author is $author. Story's name is $name. Path to story.html's text in the storage is $path_to_text. Time to read of this story.html is $time_to_read"
    }
}