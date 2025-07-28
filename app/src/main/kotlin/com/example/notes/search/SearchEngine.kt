package com.example.notes.search

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.notes.data.NoteDao
import com.example.notes.data.dao.TagDao
import com.example.notes.data.Note
import com.example.notes.data.entity.Tag
import com.example.notes.utils.TextFormattingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SearchEngine(
    private val context: Context,
    private val noteDao: NoteDao,
    private val tagDao: TagDao
) {
    
    companion object {
        private const val TAG = "SearchEngine"
        private const val MIN_SEARCH_LENGTH = 2
    }
    
    data class SearchFilters(
        val query: String = "",
        val tags: List<Tag> = emptyList(),
        val dateFrom: Date? = null,
        val dateTo: Date? = null,
        val hasImages: Boolean? = null,
        val hasAudio: Boolean? = null,
        val hasFiles: Boolean? = null,
        val sortBy: SortBy = SortBy.RELEVANCE,
        val sortOrder: SortOrder = SortOrder.DESC
    )
    
    enum class SortBy {
        RELEVANCE, DATE_CREATED, DATE_MODIFIED, TITLE, SIZE
    }
    
    enum class SortOrder {
        ASC, DESC
    }
    
    data class SearchResult(
        val notes: List<Note>,
        val totalCount: Int,
        val searchTime: Long,
        val suggestions: List<String> = emptyList()
    )
    
    private val _searchResults = MutableLiveData<SearchResult>()
    val searchResults: LiveData<SearchResult> = _searchResults
    
    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean> = _isSearching
    
    private val recentSearches = mutableListOf<String>()
    private val maxRecentSearches = 10
    
    /**
     * Perform comprehensive search with filters
     */
    suspend fun search(filters: SearchFilters): SearchResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        _isSearching.postValue(true)
        
        try {
            val query = filters.query.trim()
            
            // If query is too short, return empty results
            if (query.isNotEmpty() && query.length < MIN_SEARCH_LENGTH) {
                return@withContext SearchResult(
                    notes = emptyList(),
                    totalCount = 0,
                    searchTime = System.currentTimeMillis() - startTime
                )
            }
            
            // Add to recent searches if not empty
            if (query.isNotEmpty()) {
                addToRecentSearches(query)
            }
            
            // Get base notes
            val allNotes = if (query.isEmpty()) {
                noteDao.getAllNotesSync()
            } else {
                searchNotesByText(query)
            }
            
            // Apply filters
            var filteredNotes = allNotes
            
            // Filter by tags
            if (filters.tags.isNotEmpty()) {
                filteredNotes = filterByTags(filteredNotes, filters.tags)
            }
            
            // Filter by date range
            if (filters.dateFrom != null || filters.dateTo != null) {
                filteredNotes = filterByDateRange(filteredNotes, filters.dateFrom, filters.dateTo)
            }
            
            // Filter by media types
            filteredNotes = filterByMediaTypes(filteredNotes, filters.hasImages, filters.hasAudio, filters.hasFiles)
            
            // Sort results
            val sortedNotes = sortNotes(filteredNotes, filters.sortBy, filters.sortOrder, query)
            
            // Generate suggestions if no results
            val suggestions = if (sortedNotes.isEmpty() && query.isNotEmpty()) {
                generateSearchSuggestions(query)
            } else {
                emptyList()
            }
            
            val searchTime = System.currentTimeMillis() - startTime
            val result = SearchResult(
                notes = sortedNotes,
                totalCount = sortedNotes.size,
                searchTime = searchTime,
                suggestions = suggestions
            )
            
            _searchResults.postValue(result)
            result
        } finally {
            _isSearching.postValue(false)
        }
    }
    
    /**
     * Quick search for autocomplete
     */
    suspend fun quickSearch(query: String, limit: Int = 5): List<Note> = withContext(Dispatchers.IO) {
        if (query.length < MIN_SEARCH_LENGTH) return@withContext emptyList()
        
        val notes = searchNotesByText(query)
        notes.take(limit)
    }
    
    /**
     * Search notes by text content
     */
    private suspend fun searchNotesByText(query: String): List<Note> {
        val searchTerms = query.lowercase().split(" ").filter { it.isNotBlank() }
        val allNotes = noteDao.getAllNotesSync()
        
        return allNotes.filter { note ->
            val title = note.title.lowercase()
            val content = TextFormattingUtils.extractPlainText(note.content).lowercase()
            val combinedText = "$title $content"
            
            // Check if all search terms are present
            searchTerms.all { term ->
                combinedText.contains(term)
            }
        }.sortedByDescending { note ->
            // Calculate relevance score
            calculateRelevanceScore(note, searchTerms)
        }
    }
    
    /**
     * Calculate relevance score for search results
     */
    private fun calculateRelevanceScore(note: Note, searchTerms: List<String>): Double {
        val title = note.title.lowercase()
        val content = TextFormattingUtils.extractPlainText(note.content).lowercase()
        
        var score = 0.0
        
        for (term in searchTerms) {
            // Title matches are more important
            val titleMatches = title.split(" ").count { it.contains(term) }
            score += titleMatches * 3.0
            
            // Content matches
            val contentMatches = content.split(" ").count { it.contains(term) }
            score += contentMatches * 1.0
            
            // Exact matches get bonus points
            if (title.contains(term)) score += 2.0
            if (content.contains(term)) score += 1.0
        }
        
        // Boost score for recent notes
        val daysSinceCreated = (System.currentTimeMillis() - note.timestamp) / (1000 * 60 * 60 * 24)
        if (daysSinceCreated < 7) score += 1.0
        if (daysSinceCreated < 1) score += 2.0
        
        return score
    }
    
    /**
     * Filter notes by tags
     */
    private suspend fun filterByTags(notes: List<Note>, tags: List<Tag>): List<Note> {
        if (tags.isEmpty()) return notes
        
        val tagIds = tags.map { it.id }.toSet()
        
        return notes.filter { note ->
            val noteTags = tagDao.getTagsForNoteSync(note.id.toLong())
            val noteTagIds = noteTags.map { tag -> tag.id }.toSet()
            
            // Note must have all specified tags
            tagIds.all { tagId -> tagId in noteTagIds }
        }
    }
    
    /**
     * Filter notes by date range
     */
    private fun filterByDateRange(notes: List<Note>, dateFrom: Date?, dateTo: Date?): List<Note> {
        return notes.filter { note ->
            val noteDate = Date(note.timestamp)
            
            val afterFrom: Boolean = dateFrom?.let { noteDate >= it } ?: true
            val beforeTo: Boolean = dateTo?.let { noteDate <= it } ?: true
            
            afterFrom && beforeTo
        }
    }
    
    /**
     * Filter notes by media types
     */
    private fun filterByMediaTypes(
        notes: List<Note>,
        hasImages: Boolean?,
        hasAudio: Boolean?,
        hasFiles: Boolean?
    ): List<Note> {
        return notes.filter { note ->
            val imageMatch = hasImages?.let { 
                if (it) !note.imagePaths.isNullOrEmpty() else note.imagePaths.isNullOrEmpty()
            } ?: true
            
            val audioMatch = hasAudio?.let {
                // Note: Audio paths not implemented in current Note model
                false
            } ?: true
            
            val fileMatch = hasFiles?.let {
                // Note: File paths not implemented in current Note model
                false
            } ?: true
            
            imageMatch && audioMatch && fileMatch
        }
    }
    
    /**
     * Sort notes based on criteria
     */
    private fun sortNotes(
        notes: List<Note>,
        sortBy: SortBy,
        sortOrder: SortOrder,
        query: String = ""
    ): List<Note> {
        val sorted = when (sortBy) {
            SortBy.RELEVANCE -> {
                if (query.isNotEmpty()) {
                    val searchTerms = query.lowercase().split(" ").filter { it.isNotBlank() }
                    notes.sortedByDescending { calculateRelevanceScore(it, searchTerms) }
                } else {
                    notes.sortedByDescending { it.timestamp }
                }
            }
            SortBy.DATE_CREATED -> notes.sortedBy { it.timestamp }
            SortBy.DATE_MODIFIED -> notes.sortedBy { it.timestamp }
            SortBy.TITLE -> notes.sortedBy { it.title.lowercase() }
            SortBy.SIZE -> notes.sortedBy { it.content.length }
        }
        
        return if (sortOrder == SortOrder.DESC && sortBy != SortBy.RELEVANCE) {
            sorted.reversed()
        } else {
            sorted
        }
    }
    
    /**
     * Generate search suggestions
     */
    private suspend fun generateSearchSuggestions(query: String): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Add similar words from existing notes
        val allNotes = noteDao.getAllNotesSync()
        val allWords = mutableSetOf<String>()
        
        allNotes.forEach { note ->
            val title = note.title.lowercase()
            val content = TextFormattingUtils.extractPlainText(note.content).lowercase()
            val words = "$title $content".split(Regex("\\W+")).filter { it.length > 2 }
            allWords.addAll(words)
        }
        
        // Find similar words
        val queryLower = query.lowercase()
        val similarWords = allWords.filter { word ->
            word.contains(queryLower) || queryLower.contains(word) || 
            levenshteinDistance(word, queryLower) <= 2
        }.take(5)
        
        suggestions.addAll(similarWords)
        
        // Add tag suggestions
        val allTags = tagDao.getAllTagsSync()
        val matchingTags = allTags.filter { tag ->
            tag.name.lowercase().contains(queryLower)
        }.map { it.name }.take(3)
        
        suggestions.addAll(matchingTags)
        
        return suggestions.distinct().take(8)
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j
        
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[len1][len2]
    }
    
    /**
     * Add query to recent searches
     */
    private fun addToRecentSearches(query: String) {
        recentSearches.remove(query) // Remove if already exists
        recentSearches.add(0, query) // Add to beginning
        
        // Keep only the most recent searches
        if (recentSearches.size > maxRecentSearches) {
            recentSearches.removeAt(recentSearches.size - 1)
        }
    }
    
    /**
     * Get recent searches
     */
    fun getRecentSearches(): List<String> = recentSearches.toList()
    
    /**
     * Clear recent searches
     */
    fun clearRecentSearches() {
        recentSearches.clear()
    }
    
    /**
     * Get popular tags for suggestions
     */
    suspend fun getPopularTags(limit: Int = 10): List<Tag> = withContext(Dispatchers.IO) {
        val allTags = tagDao.getAllTagsSync()
        
        // Sort by usage count (this would require additional tracking in real implementation)
        allTags.take(limit)
    }
    
    /**
     * Search tags by name
     */
    suspend fun searchTags(query: String): List<Tag> = withContext(Dispatchers.IO) {
        tagDao.searchTags(query)
    }
}
