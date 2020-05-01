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
    
    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(
            mode: ActionMode, menu: Menu?
        ): Boolean { // Inflate a menu resource providing context menu items
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.custom_context_menu, menu)
            
            return true
        }
        
        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false // Return false if nothing is done
        }
        
        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return false
        }
        
        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode?) {
        }
    }
    
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
    
    
    
    
    fun setWebViewLongClickListener(view : View) {
        activity!!.openContextMenu(view)
    }
}