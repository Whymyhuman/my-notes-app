package com.example.notes

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.notes.adapter.OnboardingAdapter
import com.example.notes.databinding.ActivityOnboardingBinding
import com.example.notes.utils.AnimationUtils
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var sharedPreferences: SharedPreferences
    
    private val onboardingPages = listOf(
        OnboardingPage(
            title = "Welcome to QuickNotes Pro",
            description = "Your thoughts, organized beautifully. Create, edit, and manage your notes with ease.",
            illustration = R.drawable.onboarding_illustration_1,
            primaryColor = R.color.onboarding_color_1
        ),
        OnboardingPage(
            title = "Rich Text & Formatting",
            description = "Make your notes stand out with bold, italic, underline, and more formatting options.",
            illustration = R.drawable.onboarding_illustration_2,
            primaryColor = R.color.onboarding_color_2
        ),
        OnboardingPage(
            title = "Organize with Categories",
            description = "Keep your notes organized with custom categories and colors. Find what you need instantly.",
            illustration = R.drawable.onboarding_illustration_3,
            primaryColor = R.color.onboarding_color_3
        ),
        OnboardingPage(
            title = "Backup & Sync",
            description = "Never lose your notes. Export, backup, and share your thoughts across devices.",
            illustration = R.drawable.onboarding_illustration_4,
            primaryColor = R.color.onboarding_color_4
        )
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        
        setupViewPager()
        setupClickListeners()
        setupPageChangeListener()
        
        // Animate initial elements
        AnimationUtils.fadeIn(binding.viewPager, 500)
        AnimationUtils.slideInFromRight(binding.btnNext, 300)
    }
    
    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(onboardingPages)
        binding.viewPager.adapter = onboardingAdapter
        
        // Setup tab indicator
        TabLayoutMediator(binding.tabIndicator, binding.viewPager) { _, _ -> }.attach()
        
        // Disable swipe for controlled navigation
        binding.viewPager.isUserInputEnabled = false
    }
    
    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            AnimationUtils.performHapticFeedback(this, AnimationUtils.HapticFeedbackType.LIGHT)
            
            val currentItem = binding.viewPager.currentItem
            if (currentItem < onboardingPages.size - 1) {
                // Go to next page
                binding.viewPager.currentItem = currentItem + 1
                AnimationUtils.pulse(binding.btnNext)
            } else {
                // Finish onboarding
                finishOnboarding()
            }
        }
        
        binding.btnSkip.setOnClickListener {
            AnimationUtils.performHapticFeedback(this, AnimationUtils.HapticFeedbackType.LIGHT)
            finishOnboarding()
        }
        
        binding.btnPrevious.setOnClickListener {
            AnimationUtils.performHapticFeedback(this, AnimationUtils.HapticFeedbackType.LIGHT)
            
            val currentItem = binding.viewPager.currentItem
            if (currentItem > 0) {
                binding.viewPager.currentItem = currentItem - 1
                AnimationUtils.pulse(binding.btnPrevious)
            }
        }
    }
    
    private fun setupPageChangeListener() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateNavigationButtons(position)
                updateColors(position)
            }
        })
    }
    
    private fun updateNavigationButtons(position: Int) {
        // Update previous button visibility
        if (position == 0) {
            AnimationUtils.fadeOut(binding.btnPrevious, 200)
        } else {
            AnimationUtils.fadeIn(binding.btnPrevious, 200)
        }
        
        // Update next/finish button text
        if (position == onboardingPages.size - 1) {
            binding.btnNext.text = getString(R.string.get_started)
            binding.btnNext.icon = ContextCompat.getDrawable(this, R.drawable.ic_check)
            AnimationUtils.fadeOut(binding.btnSkip, 200)
        } else {
            binding.btnNext.text = getString(R.string.next)
            binding.btnNext.icon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_forward)
            AnimationUtils.fadeIn(binding.btnSkip, 200)
        }
        
        // Animate button changes
        AnimationUtils.pulse(binding.btnNext, 400)
    }
    
    private fun updateColors(position: Int) {
        val page = onboardingPages[position]
        val color = ContextCompat.getColor(this, page.primaryColor)
        
        // Animate color changes
        binding.btnNext.setBackgroundColor(color)
        binding.tabIndicator.setSelectedTabIndicatorColor(color)
    }
    
    private fun finishOnboarding() {
        // Mark onboarding as completed
        sharedPreferences.edit()
            .putBoolean("onboarding_completed", true)
            .apply()
        
        // Animate exit
        AnimationUtils.scaleOut(binding.root, 300) {
            // Start main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            
            // Custom transition
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        
        // Success haptic feedback
        AnimationUtils.performHapticFeedback(this, AnimationUtils.HapticFeedbackType.SUCCESS)
    }
    
    companion object {
        fun shouldShowOnboarding(sharedPreferences: SharedPreferences): Boolean {
            return !sharedPreferences.getBoolean("onboarding_completed", false)
        }
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val illustration: Int,
    val primaryColor: Int
)

