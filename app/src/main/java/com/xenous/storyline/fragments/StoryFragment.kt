package com.xenous.storyline.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.xenous.storyline.R


class StoryFragment(val onViewLoadedHandler: Handler) : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.story_fragment_layout, container, false)
    }
    
    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val storyWebView = view.findViewById<WebView>(R.id.storyWebView)
        
        val msg = Message.obtain()
        msg.obj = storyWebView
        onViewLoadedHandler.sendMessage(msg)
        
        storyWebView.isLongClickable = false
        storyWebView.settings.javaScriptEnabled = true
    }
}