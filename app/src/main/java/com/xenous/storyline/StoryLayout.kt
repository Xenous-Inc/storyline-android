package com.x3noku.story

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.xenous.storyline.R


class StoryLayout(
    val view: View,
    storyTitle: String,
    storyAuthor: String,
    storyDuration: String,
    private val storyText: String,
    private val context: Context
) {

    var collapsedStoryCoverHeightInPx = 120F.dpToPx(context)
    var collapsedStoryCoverRadiusInPx = 32F.dpToPx(context)

    fun setStoryCoverImageResource(resourceId: Int) =
        view.findViewById<ImageView>(R.id.storyCoverImageView).setImageResource(resourceId)

    fun setContentFragment(fragment: Fragment, fragmentManager: FragmentManager) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.storyContentFrameLayout, fragment)
        fragmentTransaction.commit()
    }

    val storyCoverCardView: CardView =
        view.findViewById(R.id.storyCoverCardView)

    private val storyCoverTitleTextView =
        view.findViewById<TextView>(R.id.storyCoverTitleTextView)

    private val storyContentFrameLayout =
        view.findViewById<FrameLayout>(R.id.storyContentFrameLayout)

    init {
        storyCoverTitleTextView.text = storyTitle
        view
            .findViewById<TextView>(R.id.storyCoverAuthorTextView)
            .text = storyAuthor
        view
            .findViewById<TextView>(R.id.storyCoverDurationTextView)
            .text = storyDuration
    }

    fun collapseStoryCover() {
        val coverHeightInPx = storyCoverCardView.measuredHeight.toFloat()
        val coverWidthInPx = storyCoverCardView.measuredWidth.toFloat()

        val coverVerticalShift = coverHeightInPx - collapsedStoryCoverHeightInPx
        val titleVerticalShift = coverVerticalShift/2
        val titleHorizontalShift =
            (coverWidthInPx - storyCoverTitleTextView.measuredWidth) / 2 -
                    collapsedStoryCoverRadiusInPx

        run {
            storyContentFrameLayout.translationY = coverHeightInPx

            Animator(storyCoverCardView, context)
                .moveVerticallyTo(-coverVerticalShift)
                .setRadiusTo(collapsedStoryCoverRadiusInPx)

            Animator(storyCoverTitleTextView, context)
                .moveVerticallyTo(titleVerticalShift)
                .moveHorizontallyTo(-titleHorizontalShift)

            Animator(storyContentFrameLayout, context)
                .moveVerticallyTo(-coverVerticalShift)
        }

        //Fade in with other objects, besides story.html name
        run {
            val disappearAnimation = AlphaAnimation(1f, 0f)
            disappearAnimation.duration = 800L
            disappearAnimation.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    view
                        .findViewById<TextView>(R.id.storyCoverAuthorTextView)
                        .visibility = View.INVISIBLE
                    view
                        .findViewById<TextView>(R.id.storyCoverDurationTextView)
                        .visibility = View.INVISIBLE
                }
                override fun onAnimationRepeat(animation: Animation) {}
            })

            view
                .findViewById<TextView>(R.id.storyCoverAuthorTextView).startAnimation(disappearAnimation)
            view
                .findViewById<TextView>(R.id.storyCoverDurationTextView).startAnimation(disappearAnimation)
        }
    }


    class Animator(
        private val view: View,
        private val context: Context,
        private val duration: Long = 1500L
    ) {
        fun moveVerticallyTo(expectingShift: Float, inDp: Boolean = false): Animator {
            val expectingShiftInPx =
                view.translationY +
                        if(inDp) expectingShift.dpToPx(context) else expectingShift

            ObjectAnimator
                .ofFloat(view, "translationY", expectingShiftInPx)
                .apply {
                    duration = this@Animator.duration
                    start()
                }

            return this
        }

        fun moveHorizontallyTo(expectingShift: Float, inDp: Boolean = false): Animator {
            val expectingShiftInPx =
                view.translationX +
                        if(inDp) expectingShift.dpToPx(context) else expectingShift

            ObjectAnimator
                .ofFloat(view, "translationX", expectingShiftInPx)
                .apply {
                    duration = this@Animator.duration
                    start()
                }

            return this
        }

        fun setRadiusTo(expectingRadius: Float, inDp: Boolean = false): Animator {
            view as CardView

            val currentRadiusInPx = view.radius
            val expectingRadiusInPx =
                if(inDp) expectingRadius.dpToPx(context) else expectingRadius

            val valueAnimator =
                ValueAnimator.ofFloat(currentRadiusInPx, expectingRadiusInPx)
            with(valueAnimator) {
                addUpdateListener { animator ->
                    val animatedValue = animator.animatedValue as Float
                    view.radius = animatedValue
                }
                duration = this@Animator.duration
                start()
            }

            return this
        }
    }

    class Builder(
        private val context: Context,
        private val title: String,
        private val author: String,
        private val timeToRead: String,
        private val text: String
    ) {
        fun build(layoutInflater: LayoutInflater) : StoryLayout {
            val view =
                layoutInflater.inflate(R.layout.story_layout, null, false)

            return StoryLayout(view, title, author, timeToRead, text, context)
        }
    }
}

