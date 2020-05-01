package com.xenous.storyline.activities

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.x3noku.story.StoryLayout
import com.x3noku.story.dpToPx
import com.xenous.storyline.R
import com.xenous.storyline.fragments.StoryFragment

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(
                this,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                true
            )
        }
        if(Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if(Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(
                this,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                false
            )
            window.statusBarColor = Color.TRANSPARENT
        }

        val storyFragment = StoryFragment()

        val fragmentFrameLayout = findViewById<FrameLayout>(R.id.fragmentFrameLayout)

        val storyLayout = StoryLayout
            .Builder (
                this,
                "Хамалеон",
                "Антон Павлович Чехов",
                "Читать 5 минут",
                ""
        ).build(layoutInflater)

        with(storyLayout) {
            setContentFragment(storyFragment, supportFragmentManager)
            setCoverImageResource(R.drawable.demo_background)
            cover.setOnClickListener {
                storyLayout.collapseStoryCover()
            }
            collapsedCoverHeight = 80F.dpToPx(this@MainActivity)
            
            Thread {
                while(storyFragment.view == null) { }
                storyLayout
                    .hideCoverOnScroll(storyFragment.view!!.findViewById(R.id.storyWebView))
            }.start()
        }

        fragmentFrameLayout.addView(storyLayout.view)
    }

    fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

}
