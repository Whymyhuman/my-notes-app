package com.example.notes

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
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
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        categoriesAdapter = CategoriesAdapter(
            onCategoryClick = { category ->
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
        
        binding.recyclerView.apply {\n            adapter = categoriesAdapter\n            layoutManager = LinearLayoutManager(this@CategoriesActivity)\n        }\n    }\n    \n    private fun setupFab() {\n        binding.fabAddCategory.setOnClickListener {\n            showAddCategoryDialog()\n        }\n    }\n    \n    private fun observeCategories() {\n        categoryViewModel.allCategories.observe(this) { categories ->\n            categoriesAdapter.updateCategories(categories)\n            updateEmptyState(categories.isEmpty())\n        }\n    }\n    \n    private fun updateEmptyState(isEmpty: Boolean) {\n        if (isEmpty) {\n            binding.recyclerView.visibility = android.view.View.GONE\n            binding.tvEmptyState.visibility = android.view.View.VISIBLE\n        } else {\n            binding.recyclerView.visibility = android.view.View.VISIBLE\n            binding.tvEmptyState.visibility = android.view.View.GONE\n        }\n    }\n    \n    private fun showAddCategoryDialog() {\n        val editText = EditText(this)\n        editText.hint = \"Category name\"\n        \n        AlertDialog.Builder(this)\n            .setTitle(\"Add Category\")\n            .setView(editText)\n            .setPositiveButton(\"Add\") { _, _ ->\n                val name = editText.text.toString().trim()\n                if (name.isNotEmpty()) {\n                    val randomColor = predefinedColors[Random.nextInt(predefinedColors.size)]\n                    val category = Category(name, randomColor, System.currentTimeMillis())\n                    categoryViewModel.insertCategory(category)\n                } else {\n                    Toast.makeText(this, \"Category name cannot be empty\", Toast.LENGTH_SHORT).show()\n                }\n            }\n            .setNegativeButton(\"Cancel\", null)\n            .show()\n    }\n    \n    private fun showEditCategoryDialog(category: Category) {\n        val editText = EditText(this)\n        editText.setText(category.name)\n        editText.hint = \"Category name\"\n        \n        AlertDialog.Builder(this)\n            .setTitle(\"Edit Category\")\n            .setView(editText)\n            .setPositiveButton(\"Update\") { _, _ ->\n                val name = editText.text.toString().trim()\n                if (name.isNotEmpty()) {\n                    category.name = name\n                    categoryViewModel.updateCategory(category)\n                } else {\n                    Toast.makeText(this, \"Category name cannot be empty\", Toast.LENGTH_SHORT).show()\n                }\n            }\n            .setNegativeButton(\"Cancel\", null)\n            .show()\n    }\n    \n    private fun showDeleteCategoryDialog(category: Category) {\n        lifecycleScope.launch {\n            val notesCount = categoryViewModel.getNotesCountInCategory(category.id)\n            val message = if (notesCount > 0) {\n                \"This category contains $notesCount note(s). Deleting it will remove the category from all notes. Continue?\"\n            } else {\n                \"Are you sure you want to delete this category?\"\n            }\n            \n            AlertDialog.Builder(this@CategoriesActivity)\n                .setTitle(\"Delete Category\")\n                .setMessage(message)\n                .setPositiveButton(\"Delete\") { _, _ ->\n                    categoryViewModel.deleteCategory(category)\n                }\n                .setNegativeButton(\"Cancel\", null)\n                .show()\n        }\n    }\n}"