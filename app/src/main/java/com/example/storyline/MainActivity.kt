package com.example.storyline

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.x3noku.story.StoryLayout

class MainActivity : AppCompatActivity() {

    private lateinit var fragmentFrameLayout : FrameLayout

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

        fragmentFrameLayout = findViewById(R.id.fragmentFrameLayout)

        val storyView = StoryLayout
            .Builder (
                this,
                "Хамалеон",
                "Антон Павлович Чехов",
                "Читать 5 минут",
                ""
        ).build(layoutInflater)

        storyView.setStoryCoverImageResource(R.drawable.demo_background)

        storyView.storyCoverCardView.setOnClickListener {
            storyView.collapseStoryCover()
        }

        storyView.setContentFragment(storyFragment, supportFragmentManager)

        fragmentFrameLayout.addView(storyView.view)
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
