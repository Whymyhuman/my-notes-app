package com.example.notes.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils as AndroidAnimationUtils
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.example.notes.R

object AnimationUtils {
    
    /**
     * Fade in animation for views
     */
    fun fadeIn(view: View, duration: Long = 300) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
    }
    
    /**
     * Fade out animation for views
     */
    fun fadeOut(view: View, duration: Long = 300, onComplete: (() -> Unit)? = null) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    onComplete?.invoke()
                }
            })
            .start()
    }
    
    /**
     * Scale in animation with bounce effect
     */
    fun scaleIn(view: View, duration: Long = 250) {
        view.scaleX = 0.8f
        view.scaleY = 0.8f
        view.alpha = 0f
        view.visibility = View.VISIBLE
        
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(OvershootInterpolator(1.2f))
            .start()
    }
    
    /**
     * Scale out animation
     */
    fun scaleOut(view: View, duration: Long = 200, onComplete: (() -> Unit)? = null) {
        view.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    onComplete?.invoke()
                }
            })
            .start()
    }
    
    /**
     * Slide in from right animation
     */
    fun slideInFromRight(view: View, duration: Long = 300) {
        view.translationX = view.width.toFloat()
        view.alpha = 0f
        view.visibility = View.VISIBLE
        
        view.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
    }
    
    /**
     * Slide out to left animation
     */
    fun slideOutToLeft(view: View, duration: Long = 300, onComplete: (() -> Unit)? = null) {
        view.animate()
            .translationX(-view.width.toFloat())
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    onComplete?.invoke()
                }
            })
            .start()
    }
    
    /**
     * Pulse animation for highlighting views
     */
    fun pulse(view: View, duration: Long = 600) {
        val scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f)
        val scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1f)
        
        scaleUp.duration = duration / 2
        scaleUpY.duration = duration / 2
        scaleDown.duration = duration / 2
        scaleDownY.duration = duration / 2
        
        scaleUp.interpolator = FastOutSlowInInterpolator()
        scaleUpY.interpolator = FastOutSlowInInterpolator()
        scaleDown.interpolator = FastOutSlowInInterpolator()
        scaleDownY.interpolator = FastOutSlowInInterpolator()
        
        scaleUp.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                scaleDown.start()
                scaleDownY.start()
            }
        })
        
        scaleUp.start()
        scaleUpY.start()
    }
    
    /**
     * Shake animation for error states
     */
    fun shake(view: View, duration: Long = 500) {
        val shake = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        shake.duration = duration
        shake.interpolator = AccelerateDecelerateInterpolator()
        shake.start()
    }
    
    /**
     * Haptic feedback for interactions
     */
    fun performHapticFeedback(context: Context, type: HapticFeedbackType = HapticFeedbackType.LIGHT) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = ContextCompat.getSystemService(context, VibratorManager::class.java)
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                ContextCompat.getSystemService(context, Vibrator::class.java)
            }
            
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = when (type) {
                        HapticFeedbackType.LIGHT -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                        HapticFeedbackType.MEDIUM -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                        HapticFeedbackType.HEAVY -> VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                        HapticFeedbackType.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50), -1)
                        HapticFeedbackType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1)
                    }
                    it.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    when (type) {
                        HapticFeedbackType.LIGHT -> it.vibrate(50)
                        HapticFeedbackType.MEDIUM -> it.vibrate(100)
                        HapticFeedbackType.HEAVY -> it.vibrate(150)
                        HapticFeedbackType.SUCCESS -> it.vibrate(longArrayOf(0, 50, 50, 50), -1)
                        HapticFeedbackType.ERROR -> it.vibrate(longArrayOf(0, 100, 50, 100), -1)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore haptic feedback errors
        }
    }
    
    /**
     * Load animation from resources
     */
    fun loadAnimation(context: Context, animRes: Int) = AndroidAnimationUtils.loadAnimation(context, animRes)
    
    /**
     * Staggered animation for list items
     */
    fun animateListItems(views: List<View>, delay: Long = 50) {
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 50f
            
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(index * delay)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }
    }
    
    enum class HapticFeedbackType {
        LIGHT, MEDIUM, HEAVY, SUCCESS, ERROR
    }
}

