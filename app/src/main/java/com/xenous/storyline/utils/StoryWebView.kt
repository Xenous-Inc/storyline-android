package com.xenous.storyline.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.webkit.WebView
import com.xenous.storyline.activities.MainActivity
import com.xenous.storyline.fragments.StoryFragment


class StoryWebView(
    context: Context?,
    attrs: AttributeSet?
) : WebView(context, attrs) {
    
    private val gt : GestureDetector
    
    private lateinit var fragment : StoryFragment
    
    private val simpleOnGestureListener : SimpleOnGestureListener = object: SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent) : Boolean {
            return true
        }
    
        override fun onLongPress(event: MotionEvent) {
            fragment.setWebViewLongClickListener(this@StoryWebView)
        }
    }
    
    init {
        gt = GestureDetector(context, simpleOnGestureListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gt.onTouchEvent(event)
    }
}