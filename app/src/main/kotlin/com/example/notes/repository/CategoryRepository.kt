package com.example.notes.repository

import androidx.lifecycle.LiveData
import com.example.notes.data.Category
import com.example.notes.data.CategoryDao

class CategoryRepository(private val categoryDao: CategoryDao) {
    
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()
    
    suspend fun insert(category: Category): Long {
        return categoryDao.insertCategory(category)
    }
    
    suspend fun update(category: Category) {
        categoryDao.updateCategory(category)
    }
    
    suspend fun delete(category: Category) {
        categoryDao.deleteCategory(category)
    }
    
    suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)
    }
    
    suspend fun getNotesCountInCategory(categoryId: Int): Int {
        return categoryDao.getNotesCountInCategory(categoryId)
    }
}