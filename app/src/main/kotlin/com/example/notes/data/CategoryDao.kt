package com.example.notes.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?
    
    @Insert
    suspend fun insertCategory(category: Category): Long
    
    @Update
    suspend fun updateCategory(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
    
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)
    
    @Query("SELECT COUNT(*) FROM notes WHERE category_id = :categoryId")
    suspend fun getNotesCountInCategory(categoryId: Int): Int
}