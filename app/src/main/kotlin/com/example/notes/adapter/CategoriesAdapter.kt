package com.example.notes.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.data.Category
import com.example.notes.databinding.ItemCategoryBinding

class CategoriesAdapter(
    private val onCategoryClick: (Category) -> Unit,
    private val onCategoryEdit: (Category) -> Unit,
    private val onCategoryDelete: (Category) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {
    
    private var categories = listOf<Category>()
    
    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }
    
    override fun getItemCount(): Int = categories.size
    
    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(category: Category) {
            binding.apply {
                tvCategoryName.text = category.name
                
                // Set category color
                try {
                    val color = Color.parseColor(category.color)
                    viewColorIndicator.setBackgroundColor(color)
                } catch (e: IllegalArgumentException) {
                    // Fallback to default color if parsing fails
                    viewColorIndicator.setBackgroundColor(Color.GRAY)
                }
                
                // Click listeners
                root.setOnClickListener {
                    onCategoryClick(category)
                }
                
                btnEdit.setOnClickListener {
                    onCategoryEdit(category)
                }
                
                btnDelete.setOnClickListener {
                    onCategoryDelete(category)
                }
            }
        }
    }
}