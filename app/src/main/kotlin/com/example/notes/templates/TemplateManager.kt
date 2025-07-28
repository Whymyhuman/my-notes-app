package com.example.notes.templates

import android.content.Context
import com.example.notes.data.entity.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TemplateManager(private val context: Context) {
    
    companion object {
        private const val TAG = "TemplateManager"
    }
    
    data class NoteTemplate(
        val id: String,
        val name: String,
        val description: String,
        val category: TemplateCategory,
        val icon: String,
        val title: String,
        val content: String,
        val tags: List<String> = emptyList(),
        val isCustom: Boolean = false
    )
    
    enum class TemplateCategory {
        PRODUCTIVITY, PERSONAL, BUSINESS, EDUCATION, CREATIVE, CUSTOM
    }
    
    /**
     * Get all available templates
     */
    suspend fun getAllTemplates(): List<NoteTemplate> = withContext(Dispatchers.IO) {
        val templates = mutableListOf<NoteTemplate>()
        
        // Add built-in templates
        templates.addAll(getProductivityTemplates())
        templates.addAll(getPersonalTemplates())
        templates.addAll(getBusinessTemplates())
        templates.addAll(getEducationTemplates())
        templates.addAll(getCreativeTemplates())
        
        // Add custom templates (would be loaded from database in real implementation)
        templates.addAll(getCustomTemplates())
        
        templates
    }
    
    /**
     * Get templates by category
     */
    suspend fun getTemplatesByCategory(category: TemplateCategory): List<NoteTemplate> = withContext(Dispatchers.IO) {
        getAllTemplates().filter { it.category == category }
    }
    
    /**
     * Get template by ID
     */
    suspend fun getTemplateById(id: String): NoteTemplate? = withContext(Dispatchers.IO) {
        getAllTemplates().find { it.id == id }
    }
    
    /**
     * Create note from template
     */
    suspend fun createNoteFromTemplate(templateId: String): Note? = withContext(Dispatchers.IO) {
        val template = getTemplateById(templateId) ?: return@withContext null
        
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        // Replace placeholders in title and content
        val processedTitle = template.title
            .replace("{date}", dateFormat.format(Date(currentTime)))
            .replace("{time}", timeFormat.format(Date(currentTime)))
            .replace("{day}", SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(currentTime)))
        
        val processedContent = template.content
            .replace("{date}", dateFormat.format(Date(currentTime)))
            .replace("{time}", timeFormat.format(Date(currentTime)))
            .replace("{day}", SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(currentTime)))
        
        Note(
            title = processedTitle,
            content = processedContent,
            createdAt = currentTime,
            updatedAt = currentTime
        )
    }
    
    /**
     * Save custom template
     */
    suspend fun saveCustomTemplate(template: NoteTemplate): Boolean = withContext(Dispatchers.IO) {
        // In real implementation, this would save to database
        true
    }
    
    /**
     * Delete custom template
     */
    suspend fun deleteCustomTemplate(templateId: String): Boolean = withContext(Dispatchers.IO) {
        // In real implementation, this would delete from database
        true
    }
    
    // Built-in template definitions
    private fun getProductivityTemplates(): List<NoteTemplate> {
        return listOf(
            NoteTemplate(
                id = "meeting_notes",
                name = "Meeting Notes",
                description = "Template for meeting notes with agenda and action items",
                category = TemplateCategory.PRODUCTIVITY,
                icon = "ic_meeting",
                title = "Meeting Notes - {date}",
                content = """
                    **Meeting:** 
                    **Date:** {date}
                    **Time:** {time}
                    **Attendees:** 
                    
                    ## Agenda
                    - 
                    - 
                    - 
                    
                    ## Discussion Points
                    
                    
                    ## Action Items
                    - [ ] 
                    - [ ] 
                    - [ ] 
                    
                    ## Next Steps
                    
                    
                    ## Follow-up Date
                    
                """.trimIndent(),
                tags = listOf("meeting", "work", "productivity")
            ),
            
            NoteTemplate(
                id = "todo_list",
                name = "To-Do List",
                description = "Simple checklist template for daily tasks",
                category = TemplateCategory.PRODUCTIVITY,
                icon = "ic_checklist",
                title = "To-Do List - {date}",
                content = """
                    # To-Do List for {day}
                    
                    ## High Priority
                    - [ ] 
                    - [ ] 
                    - [ ] 
                    
                    ## Medium Priority
                    - [ ] 
                    - [ ] 
                    - [ ] 
                    
                    ## Low Priority
                    - [ ] 
                    - [ ] 
                    - [ ] 
                    
                    ## Notes
                    
                """.trimIndent(),
                tags = listOf("todo", "tasks", "productivity")
            ),
            
            NoteTemplate(
                id = "project_planning",
                name = "Project Planning",
                description = "Comprehensive project planning template",
                category = TemplateCategory.PRODUCTIVITY,
                icon = "ic_project",
                title = "Project Plan - ",
                content = """
                    # Project: 
                    
                    ## Overview
                    **Start Date:** 
                    **End Date:** 
                    **Team Members:** 
                    
                    ## Objectives
                    - 
                    - 
                    - 
                    
                    ## Milestones
                    - [ ] **Phase 1:** 
                    - [ ] **Phase 2:** 
                    - [ ] **Phase 3:** 
                    
                    ## Resources Needed
                    - 
                    - 
                    - 
                    
                    ## Risks & Mitigation
                    | Risk | Impact | Mitigation |
                    |------|--------|------------|
                    |      |        |            |
                    
                    ## Success Criteria
                    - 
                    - 
                    - 
                """.trimIndent(),
                tags = listOf("project", "planning", "work")
            )
        )
    }
    
    private fun getPersonalTemplates(): List<NoteTemplate> {
        return listOf(
            NoteTemplate(
                id = "daily_journal",
                name = "Daily Journal",
                description = "Daily reflection and journaling template",
                category = TemplateCategory.PERSONAL,
                icon = "ic_journal",
                title = "Journal Entry - {date}",
                content = """
                    # {day}, {date}
                    
                    ## How I'm Feeling
                    
                    
                    ## Today's Highlights
                    - 
                    - 
                    - 
                    
                    ## Challenges I Faced
                    - 
                    - 
                    
                    ## What I Learned
                    
                    
                    ## Tomorrow's Goals
                    - [ ] 
                    - [ ] 
                    - [ ] 
                    
                    ## Gratitude
                    I'm grateful for:
                    - 
                    - 
                    - 
                """.trimIndent(),
                tags = listOf("journal", "personal", "reflection")
            ),
            
            NoteTemplate(
                id = "goal_setting",
                name = "Goal Setting",
                description = "Template for setting and tracking personal goals",
                category = TemplateCategory.PERSONAL,
                icon = "ic_goal",
                title = "Goals - ",
                content = """
                    # Goal: 
                    
                    ## Why This Goal Matters
                    
                    
                    ## Specific Objectives
                    - [ ] 
                    - [ ] 
                    - [ ] 
                    
                    ## Timeline
                    **Start Date:** 
                    **Target Date:** 
                    
                    ## Action Steps
                    1. [ ] 
                    2. [ ] 
                    3. [ ] 
                    4. [ ] 
                    5. [ ] 
                    
                    ## Resources Needed
                    - 
                    - 
                    - 
                    
                    ## Potential Obstacles
                    - 
                    - 
                    
                    ## Success Metrics
                    - 
                    - 
                    - 
                    
                    ## Progress Updates
                    
                """.trimIndent(),
                tags = listOf("goals", "personal", "planning")
            ),
            
            NoteTemplate(
                id = "book_review",
                name = "Book Review",
                description = "Template for reviewing books you've read",
                category = TemplateCategory.PERSONAL,
                icon = "ic_book",
                title = "Book Review: ",
                content = """
                    # Book Review: 
                    
                    **Author:** 
                    **Genre:** 
                    **Pages:** 
                    **Date Finished:** {date}
                    **Rating:** ⭐⭐⭐⭐⭐ (out of 5)
                    
                    ## Summary
                    
                    
                    ## Key Takeaways
                    - 
                    - 
                    - 
                    
                    ## Favorite Quotes
                    > 
                    
                    > 
                    
                    ## What I Liked
                    - 
                    - 
                    - 
                    
                    ## What I Didn't Like
                    - 
                    - 
                    
                    ## Would I Recommend?
                    
                    
                    ## Similar Books to Read
                    - 
                    - 
                    - 
                """.trimIndent(),
                tags = listOf("books", "review", "reading")
            )
        )
    }
    
    private fun getBusinessTemplates(): List<NoteTemplate> {
        return listOf(
            NoteTemplate(
                id = "client_meeting",
                name = "Client Meeting",
                description = "Template for client meeting notes",
                category = TemplateCategory.BUSINESS,
                icon = "ic_business",
                title = "Client Meeting - ",
                content = """
                    # Client Meeting Notes
                    
                    **Client:** 
                    **Date:** {date}
                    **Time:** {time}
                    **Location/Platform:** 
                    **Attendees:** 
                    
                    ## Meeting Purpose
                    
                    
                    ## Client Requirements
                    - 
                    - 
                    - 
                    
                    ## Discussion Points
                    
                    
                    ## Decisions Made
                    - 
                    - 
                    - 
                    
                    ## Action Items
                    - [ ] **Client:** 
                    - [ ] **Our Team:** 
                    - [ ] **Our Team:** 
                    
                    ## Next Meeting
                    **Date:** 
                    **Agenda:** 
                    
                    ## Follow-up Required
                    - [ ] 
                    - [ ] 
                """.trimIndent(),
                tags = listOf("client", "meeting", "business")
            )
        )
    }
    
    private fun getEducationTemplates(): List<NoteTemplate> {
        return listOf(
            NoteTemplate(
                id = "lecture_notes",
                name = "Lecture Notes",
                description = "Template for taking lecture notes",
                category = TemplateCategory.EDUCATION,
                icon = "ic_school",
                title = "Lecture: ",
                content = """
                    # Lecture Notes
                    
                    **Course:** 
                    **Date:** {date}
                    **Professor:** 
                    **Topic:** 
                    
                    ## Key Concepts
                    - 
                    - 
                    - 
                    
                    ## Detailed Notes
                    
                    
                    ## Important Formulas/Definitions
                    
                    
                    ## Questions to Ask
                    - 
                    - 
                    
                    ## Assignment/Homework
                    - [ ] 
                    - [ ] 
                    
                    ## Study for Next Class
                    - 
                    - 
                """.trimIndent(),
                tags = listOf("lecture", "education", "study")
            )
        )
    }
    
    private fun getCreativeTemplates(): List<NoteTemplate> {
        return listOf(
            NoteTemplate(
                id = "story_outline",
                name = "Story Outline",
                description = "Template for outlining creative stories",
                category = TemplateCategory.CREATIVE,
                icon = "ic_create",
                title = "Story: ",
                content = """
                    # Story Outline: 
                    
                    ## Basic Info
                    **Genre:** 
                    **Target Length:** 
                    **Target Audience:** 
                    
                    ## Main Characters
                    - **Protagonist:** 
                    - **Antagonist:** 
                    - **Supporting:** 
                    
                    ## Setting
                    **Time:** 
                    **Place:** 
                    **Atmosphere:** 
                    
                    ## Plot Structure
                    
                    ### Act 1 - Setup
                    - **Hook:** 
                    - **Inciting Incident:** 
                    - **Plot Point 1:** 
                    
                    ### Act 2 - Confrontation
                    - **Rising Action:** 
                    - **Midpoint:** 
                    - **Plot Point 2:** 
                    
                    ### Act 3 - Resolution
                    - **Climax:** 
                    - **Falling Action:** 
                    - **Resolution:** 
                    
                    ## Themes
                    - 
                    - 
                    
                    ## Notes & Ideas
                    
                """.trimIndent(),
                tags = listOf("story", "creative", "writing")
            )
        )
    }
    
    private fun getCustomTemplates(): List<NoteTemplate> {
        // In real implementation, this would load from database
        return emptyList()
    }
}
