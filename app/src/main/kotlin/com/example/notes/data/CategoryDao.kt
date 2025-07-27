package com.example.notes.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories ORDER BY created_at DESC")
    fun getAllCategories(): LiveData<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long
    
    @Update
    suspend fun updateCategory(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
    
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)
    
    @Query("SELECT COUNT(*) FROM notes WHERE category_id = :categoryId AND is_deleted = 0")
    suspend fun getNotesCountInCategory(categoryId: Int): Int
    
    @Query("UPDATE notes SET category_id = NULL WHERE category_id = :categoryId")
    suspend fun removeNotesFromCategory(categoryId: Int)
    
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}