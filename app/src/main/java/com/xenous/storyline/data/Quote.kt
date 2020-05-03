package com.xenous.storyline.data

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.ClipboardManager
import androidx.core.content.ContextCompat.startActivity

data class Quote(
    val author : String? = null,
    val story : String? = null,
    val text : String? = null
) {

    override fun toString(): String {
        return "Book's name is $story. Story's author is $author. Quote's text is $text"
    }
    
    fun buildQuoteMessage() : String {
        return text!! + "\n" + author + " - " + "\"" + story + "\""
    }
    
    fun copyToClipBoard(context: Context) {
        val sdk = Build.VERSION.SDK_INT
        if(sdk < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = this.buildQuoteMessage()
        }
        else {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText("CopyQuote", this.buildQuoteMessage())
            clipboard.setPrimaryClip(clip)
        }
    }
    
    fun shareQuote(context: Context) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, this.buildQuoteMessage())
        shareIntent.type = "text/plain"
    
        context.startActivity(shareIntent)
    }
}