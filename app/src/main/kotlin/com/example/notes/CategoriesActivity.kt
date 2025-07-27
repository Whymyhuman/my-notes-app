package com.example.notes

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notes.adapter.CategoriesAdapter
import com.example.notes.data.Category
import com.example.notes.databinding.ActivityCategoriesBinding
import com.example.notes.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random

class CategoriesActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCategoriesBinding
    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var categoriesAdapter: CategoriesAdapter
    
    private val predefinedColors = arrayOf(
        "#FF5722", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5",
        "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50",
        "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeCategories()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Categories"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
    
    private fun setupRecyclerView() {
        categoriesAdapter = CategoriesAdapter(
            onCategoryClick = { _ ->
                // Navigate to notes filtered by category
                // TODO: Implement category filtering in MainActivity
                finish()
            },
            onCategoryEdit = { category ->
                showEditCategoryDialog(category)
            },
            onCategoryDelete = { category ->
                showDeleteCategoryDialog(category)
            }
        )
        
        binding.recyclerView.apply {
            adapter = categoriesAdapter
            layoutManager = LinearLayoutManager(this@CategoriesActivity)
        }
    }
    
    private fun setupFab() {
        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }
    
    private fun observeCategories() {
        categoryViewModel.allCategories.observe(this) { categories ->
            categoriesAdapter.updateCategories(categories)
            updateEmptyState(categories.isEmpty())
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.recyclerView.visibility = android.view.View.GONE
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
        } else {
            binding.recyclerView.visibility = android.view.View.VISIBLE
            binding.tvEmptyState.visibility = android.view.View.GONE
        }
    }
    
    private fun showAddCategoryDialog() {
        val editText = EditText(this)
        editText.hint = "Category name"
        
        AlertDialog.Builder(this)
            .setTitle("Add Category")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    val randomColor = predefinedColors[Random.nextInt(predefinedColors.size)]
                    val category = Category(name, randomColor, System.currentTimeMillis())
                    categoryViewModel.insertCategory(category)
                } else {
                    Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditCategoryDialog(category: Category) {
        val editText = EditText(this)
        editText.setText(category.name)
        editText.hint = "Category name"
        
        AlertDialog.Builder(this)
            .setTitle("Edit Category")
            .setView(editText)
            .setPositiveButton("Update") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    category.name = name
                    categoryViewModel.updateCategory(category)
                } else {
                    Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteCategoryDialog(category: Category) {
        lifecycleScope.launch {
            val notesCount = categoryViewModel.getNotesCountInCategory(category.id)
            val message = if (notesCount > 0) {
                "This category contains $notesCount note(s). Deleting it will remove the category from all notes. Continue?"
            } else {
                "Are you sure you want to delete this category?"
            }
            
            AlertDialog.Builder(this@CategoriesActivity)
                .setTitle("Delete Category")
                .setMessage(message)
                .setPositiveButton("Delete") { _, _ ->
                    categoryViewModel.deleteCategory(category)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}