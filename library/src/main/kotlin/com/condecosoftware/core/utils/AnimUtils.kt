package com.condecosoftware.core.utils

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.view.View

/**
 * Collection of functions for implementing animations
 */
fun createFadeOutAnimation(fadeOutView: View): Animator {
    val context = fadeOutView.context
    val fadeOutAnim = AnimatorInflater.loadAnimator(context, android.R.animator.fade_out)
    fadeOutAnim.setTarget(fadeOutView)
    fadeOutAnim.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            fadeOutView.visibility = View.GONE
        }

        override fun onAnimationCancel(animation: Animator) {
            fadeOutView.visibility = View.GONE
        }

    })
    return fadeOutAnim
}

fun createFadeInAnimation(fadeInView: View): Animator {
    val context = fadeInView.context
    val fadeInAnim = AnimatorInflater.loadAnimator(context, android.R.animator.fade_in)
    fadeInAnim.setTarget(fadeInView)
    fadeInAnim.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) {
            fadeInView.alpha = 0.0f
            fadeInView.visibility = View.VISIBLE
        }

        override fun onAnimationCancel(animation: Animator) {
            fadeInView.alpha = 1.0f
        }
    })
    return fadeInAnim
}

/**
 * Used to create animator that fades in one view and at the same time fades out the other.
 * At the end of animation and faded out view's visibility is set to View.GONE
 *
 * @param fadeInView  View to be faded in.
 * @param fadeOutView View to be faded out and made GONE.
 * @return If the fadeInView is already visible then the function returns null otherwise
 * [AnimatorSet] is returned that can be used to play the animation.
 */
fun createViewsCrossFadeAnimator(fadeInView: View, fadeOutView: View): AnimatorSet? {
    val animations = mutableListOf<Animator>()
    if (fadeInView.visibility != View.VISIBLE) {
        animations.add(createFadeInAnimation(fadeInView))
    }

    if (fadeOutView.visibility == View.VISIBLE) {
        animations.add(createFadeOutAnimation(fadeOutView))
    }

    if (animations.size == 0)
        return null

    //Create animator set
    val animatorSet = AnimatorSet()
    animatorSet.playTogether(animations)
    return animatorSet
}

/**
 * Used to create animator that fades in and fades out specified views.
 */
fun createViewsCrossFadeAnimator(fadeInViews: Iterable<View>, fadeOutViews: Iterable<View>): AnimatorSet? {
    val animations = mutableListOf<Animator>()
    fadeInViews.forEach { view ->
        if (view.visibility != View.VISIBLE)
            animations.add(createFadeInAnimation(view))
    }

    fadeOutViews.forEach { view ->
        if (view.visibility == View.VISIBLE)
            animations.add(createFadeOutAnimation(view))
    }
    if (animations.size == 0)
        return null

    //Create animator set
    val animatorSet = AnimatorSet()
    animatorSet.playTogether(animations)
    return animatorSet
}