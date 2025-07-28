package com.example.notes.sharing

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.notes.data.Note
import com.example.notes.export.ExportManager
import com.example.notes.utils.TextFormattingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ShareManager(
    private val context: Context,
    private val exportManager: ExportManager
) {
    
    companion object {
        private const val TAG = "ShareManager"
        private const val FILE_PROVIDER_AUTHORITY = "com.example.notes.fileprovider"
    }
    
    enum class ShareFormat {
        TEXT, MARKDOWN, PDF, HTML
    }
    
    data class ShareOptions(
        val format: ShareFormat = ShareFormat.TEXT,
        val includeTitle: Boolean = true,
        val includeTimestamp: Boolean = false,
        val includeAttachments: Boolean = false
    )
    
    /**
     * Share note as text
     */
    suspend fun shareAsText(note: Note, options: ShareOptions = ShareOptions()): Intent = withContext(Dispatchers.IO) {
        val content = buildShareContent(note, options)
        
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
            if (options.includeTitle) {
                putExtra(Intent.EXTRA_SUBJECT, note.title)
            }
        }
    }
    
    /**
     * Share note as file
     */
    suspend fun shareAsFile(note: Note, format: ShareFormat): Intent = withContext(Dispatchers.IO) {
        val file = when (format) {
            ShareFormat.PDF -> exportManager.exportToPdf(note)
            ShareFormat.HTML -> exportManager.exportToHtml(note)
            ShareFormat.MARKDOWN -> exportManager.exportToMarkdown(note)
            ShareFormat.TEXT -> exportManager.exportToText(note)
        }
        
        val uri = FileProvider.getUriForFile(
            context,
            FILE_PROVIDER_AUTHORITY,
            file
        )
        
        val mimeType = when (format) {
            ShareFormat.PDF -> "application/pdf"
            ShareFormat.HTML -> "text/html"
            ShareFormat.MARKDOWN -> "text/markdown"
            ShareFormat.TEXT -> "text/plain"
        }
        
        Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, note.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    /**
     * Share multiple notes
     */
    suspend fun shareMultipleNotes(notes: List<Note>, format: ShareFormat): Intent = withContext(Dispatchers.IO) {
        val files = notes.map { note ->
            when (format) {
                ShareFormat.PDF -> exportManager.exportToPdf(note)
                ShareFormat.HTML -> exportManager.exportToHtml(note)
                ShareFormat.MARKDOWN -> exportManager.exportToMarkdown(note)
                ShareFormat.TEXT -> exportManager.exportToText(note)
            }
        }
        
        val uris = files.map { file ->
            FileProvider.getUriForFile(
                context,
                FILE_PROVIDER_AUTHORITY,
                file
            )
        }
        
        val mimeType = when (format) {
            ShareFormat.PDF -> "application/pdf"
            ShareFormat.HTML -> "text/html"
            ShareFormat.MARKDOWN -> "text/markdown"
            ShareFormat.TEXT -> "text/plain"
        }
        
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            putExtra(Intent.EXTRA_SUBJECT, "Notes Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    /**
     * Share to specific app
     */
    suspend fun shareToApp(note: Note, packageName: String, options: ShareOptions = ShareOptions()): Intent? = withContext(Dispatchers.IO) {
        val baseIntent = shareAsText(note, options)
        
        // Check if app is installed
        val packageManager = context.packageManager
        val appIntent = packageManager.getLaunchIntentForPackage(packageName)
        
        if (appIntent != null) {
            baseIntent.setPackage(packageName)
            baseIntent
        } else {
            null
        }
    }
    
    /**
     * Share via email
     */
    suspend fun shareViaEmail(
        note: Note, 
        recipients: List<String> = emptyList(),
        subject: String? = null,
        format: ShareFormat = ShareFormat.TEXT
    ): Intent = withContext(Dispatchers.IO) {
        
        val intent = if (format == ShareFormat.TEXT) {
            val content = buildShareContent(note, ShareOptions(format = format, includeTitle = false))
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
            }
        } else {
            shareAsFile(note, format)
        }
        
        intent.apply {
            if (recipients.isNotEmpty()) {
                putExtra(Intent.EXTRA_EMAIL, recipients.toTypedArray())
            }
            putExtra(Intent.EXTRA_SUBJECT, subject ?: note.title)
            
            // Try to use email apps specifically
            selector = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
            }
        }
    }
    
    /**
     * Share via social media
     */
    suspend fun shareViaSocialMedia(note: Note, platform: SocialPlatform): Intent? = withContext(Dispatchers.IO) {
        val content = buildSocialMediaContent(note, platform)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
        }
        
        when (platform) {
            SocialPlatform.TWITTER -> {
                intent.setPackage("com.twitter.android")
                // Twitter has character limit
                val limitedContent = if (content.length > 280) {
                    content.take(277) + "..."
                } else content
                intent.putExtra(Intent.EXTRA_TEXT, limitedContent)
            }
            SocialPlatform.FACEBOOK -> {
                intent.setPackage("com.facebook.katana")
            }
            SocialPlatform.LINKEDIN -> {
                intent.setPackage("com.linkedin.android")
            }
            SocialPlatform.WHATSAPP -> {
                intent.setPackage("com.whatsapp")
            }
            SocialPlatform.TELEGRAM -> {
                intent.setPackage("org.telegram.messenger")
            }
        }
        
        // Check if app is installed
        val packageManager = context.packageManager
        val resolveInfo = packageManager.resolveActivity(intent, 0)
        
        if (resolveInfo != null) intent else null
    }
    
    /**
     * Create share chooser
     */
    fun createShareChooser(shareIntent: Intent, title: String = "Share Note"): Intent {
        return Intent.createChooser(shareIntent, title)
    }
    
    /**
     * Get available sharing apps
     */
    fun getAvailableSharingApps(): List<SharingApp> {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
        }
        
        val packageManager = context.packageManager
        val resolveInfos = packageManager.queryIntentActivities(shareIntent, 0)
        
        return resolveInfos.map { resolveInfo ->
            SharingApp(
                packageName = resolveInfo.activityInfo.packageName,
                name = resolveInfo.loadLabel(packageManager).toString(),
                icon = resolveInfo.loadIcon(packageManager)
            )
        }
    }
    
    // Private helper methods
    private fun buildShareContent(note: Note, options: ShareOptions): String {
        val content = StringBuilder()
        
        if (options.includeTitle) {
            content.append(note.title)
            content.append("\n\n")
        }
        
        when (options.format) {
            ShareFormat.TEXT -> {
                content.append(TextFormattingUtils.extractPlainText(note.content))
            }
            ShareFormat.MARKDOWN -> {
                // Convert formatted content to markdown
                content.append(note.content) // Simplified - would need proper conversion
            }
            else -> {
                content.append(TextFormattingUtils.extractPlainText(note.content))
            }
        }
        
        if (options.includeTimestamp) {
            content.append("\n\n")
            content.append("Created: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(note.timestamp))}")
        }
        
        return content.toString()
    }
    
    private fun buildSocialMediaContent(note: Note, platform: SocialPlatform): String {
        val content = StringBuilder()
        
        when (platform) {
            SocialPlatform.TWITTER -> {
                // Twitter format - concise with hashtags
                content.append(note.title)
                if (note.content.isNotEmpty()) {
                    content.append("\n\n")
                    val plainContent = TextFormattingUtils.extractPlainText(note.content)
                    content.append(plainContent.take(200)) // Leave room for hashtags
                }
                content.append("\n\n#notes #productivity")
            }
            
            SocialPlatform.LINKEDIN -> {
                // LinkedIn format - professional
                content.append(note.title)
                content.append("\n\n")
                content.append(TextFormattingUtils.extractPlainText(note.content))
                content.append("\n\n#productivity #notes #organization")
            }
            
            else -> {
                // Default format
                content.append(note.title)
                if (note.content.isNotEmpty()) {
                    content.append("\n\n")
                    content.append(TextFormattingUtils.extractPlainText(note.content))
                }
            }
        }
        
        return content.toString()
    }
    
    enum class SocialPlatform {
        TWITTER, FACEBOOK, LINKEDIN, WHATSAPP, TELEGRAM
    }
    
    data class SharingApp(
        val packageName: String,
        val name: String,
        val icon: android.graphics.drawable.Drawable
    )
}
