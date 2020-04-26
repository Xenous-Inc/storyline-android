package com.example.storyline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.*
import android.widget.LinearLayout
import bg.devlabs.transitioner.Transitioner

class MainActivity : AppCompatActivity() {

    private lateinit var storyPreviewLinearLayout : LinearLayout
    private lateinit var storyPreviewLinearLayoutHolder : LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storyPreviewLinearLayout = findViewById(R.id.storyPreviewLinearLayout)
        storyPreviewLinearLayoutHolder = findViewById(R.id.storyPreviewLinearLayoutHolder)

        val transitioner = Transitioner(storyPreviewLinearLayout, storyPreviewLinearLayoutHolder)

        storyPreviewLinearLayout.setOnClickListener {
            transitioner.duration = 500
            transitioner.interpolator = OvershootInterpolator()
            transitioner.animateTo(1f)
        }
    }
}
