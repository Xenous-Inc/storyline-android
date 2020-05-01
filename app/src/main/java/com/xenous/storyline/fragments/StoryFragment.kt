package com.xenous.storyline.fragments

import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.xenous.storyline.R

class StoryFragment : Fragment() {
    private lateinit var storyWebView : WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.story_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storyWebView = view.findViewById(R.id.storyWebView)

        val url = "https://www.york.ac.uk/teaching/cws/wws/webpage1.html"
        storyWebView.loadUrl(url)

        registerForContextMenu(storyWebView)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        activity!!.menuInflater.inflate(R.menu.custom_context_menu, menu)
    }
}