package com.xenous.storyline.utils

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.xenous.storyline.R


class StoryLayout(
    val view: View,
    storyTitle: String,
    storyAuthor: String,
    storyDuration: String,
    private val context: Context
) {
    companion object {
        const val TAG = "StoryLayout"
    }
    
    /*
    * Класс конструктора объекта
    * Позволяет создать объект класса
    * с гарантированно инициальизированным View
    * */
    class Builder(
        private val context: Context,
        private val title: String,
        private val author: String,
        private val timeToRead: String,
        private val text: String
    ) {
        @SuppressLint("InflateParams")
        fun build(layoutInflater: LayoutInflater) : StoryLayout {
            val view =
                layoutInflater.inflate(R.layout.story_layout, null, false)

            return StoryLayout(view, title, author, timeToRead, context)
        }
    }

//    View обложки истории
    val cover: CardView =
        view.findViewById(R.id.storyCoverCardView)

//    Поле свернутости обложки
    var isCollapsed = false
        private set

//    Свойство высоты обложки в свернутом состоянии
    var collapsedCoverHeight = 100F.dpToPx(context)

//    Свойство радиуса скругления обложки в свернутом состоянии
    var collapsedCoverRadius = 30F.dpToPx(context)

//    Длительность анимации сворачивания
    var collapsingAnimationDuration: Long = 800
    
//    Длительность анимации скрытия
    var hidingAnimationDuration: Long = 150

//    Метод установки ресурса для ImageView обложки
    fun setCoverImageResource(resourceId: Int) =
        view.findViewById<ImageView>(R.id.storyCoverImageView).setImageResource(resourceId)

//    Метод установки Fragment в FrameLayout для контента
    fun setContentFragment(fragment: Fragment, fragmentManager: FragmentManager) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.storyContentFrameLayout, fragment)
        fragmentTransaction.commit()
    }

//    Метод сворачивания обложки
    fun collapseStoryCover() {
        if(!isCollapsed) {
            val storyCoverTitleTextView =
                view.findViewById<TextView>(R.id.storyCoverTitleTextView)
            val storyContentFrameLayout =
                view.findViewById<FrameLayout>(R.id.storyContentFrameLayout)
            val storyCoverSubtitleLinearLayout =
                view.findViewById<LinearLayout>(R.id.storyCoverSubtitleLinearLayout)

            val coverHeightInPx = cover.measuredHeight.toFloat()
            val coverWidthInPx = cover.measuredWidth.toFloat()

            val coverVerticalShift = coverHeightInPx - collapsedCoverHeight
            val titleVerticalShift = coverVerticalShift/2
            val titleHorizontalShift =
                (coverWidthInPx - storyCoverTitleTextView.measuredWidth) / 2 -
                        collapsedCoverRadius
            
            run {
                val storyContentLayoutParams =
                    storyContentFrameLayout.layoutParams as ConstraintLayout.LayoutParams
                
                storyContentFrameLayout.layoutParams =
                    storyContentLayoutParams
                        .apply {
                            height = storyContentFrameLayout.measuredHeight - collapsedCoverHeight.toInt()
                        }
            }
            
            run {
                storyContentFrameLayout.translationY = coverHeightInPx

                Animator(cover, context, collapsingAnimationDuration)
                    .moveVerticallyTo(-coverVerticalShift)
                    .setRadiusTo(collapsedCoverRadius)

                Animator(storyCoverTitleTextView, context, collapsingAnimationDuration)
                    .moveVerticallyTo(titleVerticalShift)
                    .moveHorizontallyTo(-titleHorizontalShift)

                Animator(storyContentFrameLayout, context, collapsingAnimationDuration)
                    .moveVerticallyTo(-coverVerticalShift)

                Animator(storyCoverSubtitleLinearLayout, context, collapsingAnimationDuration/2)
                    .setAlphaTo(0F)
            }

            isCollapsed = true
        }
    }
    
//    Метод скрытия обложки при скроллинге указанного View
    fun hideCoverOnScroll(view: View) {
        val storyContentFrameLayout = this@StoryLayout.view
            .findViewById<FrameLayout>(R.id.storyContentFrameLayout)
        
        var currentScrollValue: Int
        var previousScrollValue = 0
        var isHidden = false
        var isRunning = false
    
        view.viewTreeObserver.addOnScrollChangedListener {
            currentScrollValue = view.scrollY
            previousScrollValue = when {
                isHidden ->
                    if(currentScrollValue > previousScrollValue) currentScrollValue
                    else previousScrollValue
                else ->
                    if(currentScrollValue < previousScrollValue) currentScrollValue
                    else previousScrollValue
            }
            
            if(!isRunning && !isHidden &&
                    currentScrollValue - previousScrollValue >= collapsedCoverHeight/5
            ) {
                isRunning = true
                
                Animator(cover, context, hidingAnimationDuration)
                    .moveVerticallyTo(-collapsedCoverHeight)
                Animator(storyContentFrameLayout, context, hidingAnimationDuration)
                    .moveVerticallyTo(-collapsedCoverHeight)
                
                isHidden = true
                
                Thread {
                    Thread.sleep(hidingAnimationDuration)
                    previousScrollValue = view.scrollY
                    if( view.scrollY < collapsedCoverHeight/5 ) {
                        view.scrollY = (collapsedCoverHeight/5).toInt()
                        previousScrollValue = (collapsedCoverHeight/5).toInt()
                    }
                    
                    Thread.sleep(hidingAnimationDuration)
                    isRunning = false
                }.start()
            }
            else if(!isRunning && isHidden &&
                currentScrollValue - previousScrollValue <= -collapsedCoverHeight/5
            ) {
                isRunning = true
    
                Animator(cover, context, hidingAnimationDuration)
                    .moveVerticallyTo(collapsedCoverHeight)
                Animator(storyContentFrameLayout, context, hidingAnimationDuration)
                    .moveVerticallyTo(collapsedCoverHeight)
                Animator(view, context, hidingAnimationDuration)
                    .increaseScrollYTo(0)
    
                isHidden = false
    
                Thread {
                    Thread.sleep(hidingAnimationDuration)
                    previousScrollValue = view.scrollY
                    isRunning = false
                }.start()
            }
        }
    }
    
    init {
        view
            .findViewById<TextView>(R.id.storyCoverTitleTextView)
            .text = storyTitle
        view
            .findViewById<TextView>(R.id.storyCoverAuthorTextView)
            .text = storyAuthor
        view
            .findViewById<TextView>(R.id.storyCoverDurationTextView)
            .text = storyDuration
    }

    private class Animator  (
        private val view: View,
        private val context: Context,
        private val duration: Long
    ) {
        fun moveVerticallyTo(expectingShift: Float, inDp: Boolean = false): Animator {
            val expectingY =
                view.translationY +
                        if(inDp) expectingShift.dpToPx(context) else expectingShift

            ObjectAnimator
                .ofFloat(view, "translationY", expectingY)
                .apply {
                    duration = this@Animator.duration
                    start()
                }

            return this
        }

        fun moveHorizontallyTo(expectingShift: Float, inDp: Boolean = false): Animator {
            val expectingX =
                view.translationX +
                        if(inDp) expectingShift.dpToPx(context) else expectingShift

            ObjectAnimator
                .ofFloat(view, "translationX", expectingX)
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
        
        fun setAlphaTo(expectingAlpha: Float): Animator {
            val currentAlpha = view.alpha

            val disappearAnimation = AlphaAnimation(currentAlpha, expectingAlpha)
            disappearAnimation.duration = this@Animator.duration
            disappearAnimation.setAnimationListener(object : AnimationListener {
                override fun onAnimationEnd(p0: Animation?) {
                    view.visibility = View.GONE
                }
                override fun onAnimationStart(p0: Animation?) {}
                override fun onAnimationRepeat(p0: Animation?) {}
            })
            view.startAnimation(disappearAnimation)

            return this
        }
        
        fun increaseScrollYTo(value: Int) {
            val expectingValue = view.scrollY + value
            ObjectAnimator
                .ofInt(view, "scrollY", expectingValue)
                .apply {
                    duration = this@Animator.duration
                    start()
                }
        }
    }
}

