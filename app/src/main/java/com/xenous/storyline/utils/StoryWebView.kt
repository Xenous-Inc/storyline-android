package com.xenous.storyline.utils

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView

class StoryWebView(
    context: Context?, attrs: AttributeSet?
): WebView(context, attrs) {
    private val callback: ActionMode.Callback = object: ActionMode.Callback {
        override fun onCreateActionMode(
            mode: ActionMode, menu: Menu
        ): Boolean {
            return false
        }

        override fun onPrepareActionMode(
            mode: ActionMode, menu: Menu
        ): Boolean {
            return false
        }

        override fun onActionItemClicked(
            mode: ActionMode, item: MenuItem
        ): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {}
    }
    
    var onScrollChangeListener: OnScrollChangeListener? = null
    
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        Log.w("StoryLayout", "$t")
        onScrollChangeListener?.onScrollChange(this, l, t, oldl, oldt)
    }
    
    interface OnScrollChangeListener {
        fun onScrollChange(
            view: WebView,
            scrollX: Int,
            scrollY: Int,
            oldScrollX: Int,
            oldScrollY: Int
        )
    }
}