package com.xenous.storyline.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.xenous.storyline.R

class StoryLineDialog(
    context: Context
) {
    private companion object {
        const val LAYOUT = R.layout.confirm_dialog_layout
    }
    
    private var dialog: Dialog = Dialog(context)
    
    private var confirmMessageTextView: TextView
    private var confirmNoTextView: TextView
    private var confirmYesTextView: TextView
    
    var messageText = ""
    var positiveText = ""
    var negativeText = ""
    
    init {
        dialog.setContentView(LAYOUT)
        
        confirmMessageTextView = dialog.findViewById(R.id.confirmMessageTextView)
        confirmNoTextView = dialog.findViewById(R.id.confirmNoTextView)
        confirmYesTextView = dialog.findViewById(R.id.confirmYesTextView)
    }
    
    fun setPositiveClickListener(clickListener: View.OnClickListener) {
        confirmYesTextView.setOnClickListener(clickListener)
    }
    
    fun setNegativeClickListener(clickListener: View.OnClickListener) {
        confirmNoTextView.setOnClickListener(clickListener)
    }
    
    fun show() {
        this.confirmMessageTextView.text = messageText
        if(negativeText.isNotEmpty()) {
            this.confirmNoTextView.visibility = View.VISIBLE
            this.confirmNoTextView.text = negativeText
        }
        
        this.confirmYesTextView.text = positiveText
        
        this.dialog.show()
    }
    
    fun cancel() {
        this.dialog.dismiss()
    }
    
    private fun makeBackgroundTransparent() {
        this.dialog.window!!.decorView.setBackgroundColor(Color.TRANSPARENT)
    }
}