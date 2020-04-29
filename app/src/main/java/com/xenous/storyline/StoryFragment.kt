package com.xenous.storyline

import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.Fragment

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

        Toast.makeText(context, "dfdf", Toast.LENGTH_LONG).show()

        val url = "file:///android_asset/text.html"
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