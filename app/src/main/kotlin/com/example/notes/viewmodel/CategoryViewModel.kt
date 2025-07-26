package com.example.notes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.notes.data.Category
import com.example.notes.data.NoteDatabase
import com.example.notes.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: CategoryRepository
    val allCategories: LiveData<List<Category>>
    
    init {
        val categoryDao = NoteDatabase.getDatabase(application).categoryDao()
        repository = CategoryRepository(categoryDao)
        allCategories = repository.allCategories
    }
    
    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.insert(category)
    }
    
    fun updateCategory(category: Category) = viewModelScope.launch {
        repository.update(category)
    }
    
    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.delete(category)
    }
    
    suspend fun getCategoryById(id: Int): Category? {
        return repository.getCategoryById(id)
    }
    
    suspend fun getNotesCountInCategory(categoryId: Int): Int {
        return repository.getNotesCountInCategory(categoryId)
    }
}