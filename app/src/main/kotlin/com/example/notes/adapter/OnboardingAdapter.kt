package com.example.notes.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.OnboardingPage
import com.example.notes.databinding.OnboardingPageBinding
import com.example.notes.utils.AnimationUtils

class OnboardingAdapter(
    private val pages: List<OnboardingPage>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = OnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    class OnboardingViewHolder(
        private val binding: OnboardingPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(page: OnboardingPage) {
            binding.apply {
                // Set content
                tvTitle.text = page.title
                tvDescription.text = page.description
                ivIllustration.setImageResource(page.illustration)
                
                // Set colors
                val color = ContextCompat.getColor(root.context, page.primaryColor)
                tvTitle.setTextColor(color)
                
                // Animate elements when bound
                AnimationUtils.fadeIn(ivIllustration, 600)
                AnimationUtils.slideInFromRight(tvTitle, 400)
                AnimationUtils.slideInFromRight(tvDescription, 500)
            }
        }
    }
}

