package com.xenous.storyline.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.xenous.storyline.R
import com.xenous.storyline.activities.MainActivity


class StoryFragment : Fragment() {
    
    lateinit var storyWebView : WebView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.story_fragment_layout, container, false)
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storyWebView = view.findViewById(R.id.storyWebView)
        
        val url = (activity) as MainActivity

        storyWebView.loadUrl(url.storyUrl)
        
        storyWebView.isLongClickable = false
      
        storyWebView.settings.javaScriptEnabled = true
    }
}