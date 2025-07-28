package com.example.notes.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.util.Log
import com.example.notes.data.entity.Note
import com.example.notes.utils.TextFormattingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExportManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ExportManager"
        private const val EXPORT_DIR = "exports"
        private const val PDF_PAGE_WIDTH = 595 // A4 width in points
        private const val PDF_PAGE_HEIGHT = 842 // A4 height in points
        private const val PDF_MARGIN = 50
    }
    
    private val exportDir = File(context.filesDir, EXPORT_DIR)
    
    init {
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
    }
    
    /**
     * Export note to PDF
     */
    suspend fun exportToPdf(note: Note): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${sanitizeFileName(note.title)}_$timestamp.pdf"
        val file = File(exportDir, fileName)
        
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Set up paint for text
            val titlePaint = Paint().apply {
                textSize = 24f
                isFakeBoldText = true
                color = android.graphics.Color.BLACK
            }
            
            val bodyPaint = Paint().apply {
                textSize = 14f
                color = android.graphics.Color.BLACK
            }
            
            val metaPaint = Paint().apply {
                textSize = 10f
                color = android.graphics.Color.GRAY
            }
            
            var yPosition = PDF_MARGIN + 30f
            
            // Draw title
            canvas.drawText(note.title, PDF_MARGIN.toFloat(), yPosition, titlePaint)
            yPosition += 40f
            
            // Draw metadata
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val createdDate = dateFormat.format(Date(note.createdAt))
            canvas.drawText("Created: $createdDate", PDF_MARGIN.toFloat(), yPosition, metaPaint)
            yPosition += 30f
            
            // Draw content
            val content = TextFormattingUtils.extractPlainText(note.content)
            val lines = content.split("\n")
            
            for (line in lines) {
                if (yPosition > PDF_PAGE_HEIGHT - PDF_MARGIN) {
                    // Start new page if needed
                    pdfDocument.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create()
                    val newPage = pdfDocument.startPage(newPageInfo)
                    canvas = newPage.canvas
                    yPosition = PDF_MARGIN + 20f
                }
                
                // Handle long lines by wrapping
                val words = line.split(" ")
                var currentLine = ""
                
                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    val textWidth = bodyPaint.measureText(testLine)
                    
                    if (textWidth > PDF_PAGE_WIDTH - 2 * PDF_MARGIN) {
                        if (currentLine.isNotEmpty()) {
                            canvas.drawText(currentLine, PDF_MARGIN.toFloat(), yPosition, bodyPaint)
                            yPosition += 20f
                            currentLine = word
                        } else {
                            // Single word is too long, draw it anyway
                            canvas.drawText(word, PDF_MARGIN.toFloat(), yPosition, bodyPaint)
                            yPosition += 20f
                        }
                    } else {
                        currentLine = testLine
                    }
                }
                
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, PDF_MARGIN.toFloat(), yPosition, bodyPaint)
                }
                yPosition += 20f
            }
            
            pdfDocument.finishPage(page)
            
            // Write to file
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()
            
            Log.d(TAG, "PDF exported successfully: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting to PDF", e)
            throw e
        }
    }
    
    /**
     * Export note to HTML
     */
    suspend fun exportToHtml(note: Note): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${sanitizeFileName(note.title)}_$timestamp.html"
        val file = File(exportDir, fileName)
        
        try {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val createdDate = dateFormat.format(Date(note.createdAt))
            val updatedDate = dateFormat.format(Date(note.updatedAt))
            
            val htmlContent = buildString {
                append("<!DOCTYPE html>\n")
                append("<html lang=\"en\">\n")
                append("<head>\n")
                append("    <meta charset=\"UTF-8\">\n")
                append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
                append("    <title>${escapeHtml(note.title)}</title>\n")
                append("    <style>\n")
                append("        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }\n")
                append("        .container { max-width: 800px; margin: 0 auto; padding: 20px; }\n")
                append("        .title { font-size: 2em; font-weight: bold; margin-bottom: 10px; }\n")
                append("        .meta { color: #666; font-size: 0.9em; margin-bottom: 20px; }\n")
                append("        .content { line-height: 1.6; white-space: pre-wrap; }\n")
                append("        .footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; color: #999; font-size: 0.8em; }\n")
                append("    </style>\n")
                append("</head>\n")
                append("<body>\n")
                append("    <div class=\"container\">\n")
                append("        <h1 class=\"title\">${escapeHtml(note.title)}</h1>\n")
                append("        <div class=\"meta\">\n")
                append("            Created: $createdDate<br>\n")
                append("            Updated: $updatedDate\n")
                append("        </div>\n")
                append("        <div class=\"content\">\n")
                append("            ${escapeHtml(TextFormattingUtils.extractPlainText(note.content))}\n")
                append("        </div>\n")
                append("        <div class=\"footer\">\n")
                append("            Exported from My Notes App\n")
                append("        </div>\n")
                append("    </div>\n")
                append("</body>\n")
                append("</html>\n")
            }
            
            file.writeText(htmlContent)
            Log.d(TAG, "HTML exported successfully: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting to HTML", e)
            throw e
        }
    }
    
    /**
     * Export note to Markdown
     */
    suspend fun exportToMarkdown(note: Note): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${sanitizeFileName(note.title)}_$timestamp.md"
        val file = File(exportDir, fileName)
        
        try {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val createdDate = dateFormat.format(Date(note.createdAt))
            val updatedDate = dateFormat.format(Date(note.updatedAt))
            
            val markdownContent = buildString {
                append("# ${note.title}\n\n")
                append("**Created:** $createdDate  \n")
                append("**Updated:** $updatedDate\n\n")
                append("---\n\n")
                
                // Convert formatted content to markdown if possible
                val content = if (TextFormattingUtils.hasFormatting(note.content)) {
                    // Convert HTML-like tags to markdown
                    note.content
                        .replace("<b>", "**")
                        .replace("</b>", "**")
                        .replace("<i>", "*")
                        .replace("</i>", "*")
                        .replace("<u>", "__")
                        .replace("</u>", "__")
                        .replace("<s>", "~~")
                        .replace("</s>", "~~")
                        .replace("<mark>", "==")
                        .replace("</mark>", "==")
                } else {
                    note.content
                }
                
                append(content)
                append("\n\n---\n")
                append("*Exported from My Notes App*")
            }
            
            file.writeText(markdownContent)
            Log.d(TAG, "Markdown exported successfully: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting to Markdown", e)
            throw e
        }
    }
    
    /**
     * Export note to plain text
     */
    suspend fun exportToText(note: Note): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${sanitizeFileName(note.title)}_$timestamp.txt"
        val file = File(exportDir, fileName)
        
        try {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val createdDate = dateFormat.format(Date(note.createdAt))
            val updatedDate = dateFormat.format(Date(note.updatedAt))
            
            val textContent = buildString {
                append("${note.title}\n")
                append("=".repeat(note.title.length))
                append("\n\n")
                append("Created: $createdDate\n")
                append("Updated: $updatedDate\n")
                append("\n")
                append("-".repeat(50))
                append("\n\n")
                append(TextFormattingUtils.extractPlainText(note.content))
                append("\n\n")
                append("-".repeat(50))
                append("\nExported from My Notes App")
            }
            
            file.writeText(textContent)
            Log.d(TAG, "Text exported successfully: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting to Text", e)
            throw e
        }
    }
    
    /**
     * Export multiple notes to a single file
     */
    suspend fun exportMultipleNotes(notes: List<Note>, format: ExportFormat): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "notes_export_$timestamp.${format.extension}"
        val file = File(exportDir, fileName)
        
        when (format) {
            ExportFormat.TEXT -> exportMultipleToText(notes, file)
            ExportFormat.MARKDOWN -> exportMultipleToMarkdown(notes, file)
            ExportFormat.HTML -> exportMultipleToHtml(notes, file)
            ExportFormat.PDF -> exportMultipleToPdf(notes, file)
        }
        
        file
    }
    
    /**
     * Get export directory
     */
    fun getExportDirectory(): File = exportDir
    
    /**
     * Clean up old export files
     */
    suspend fun cleanupOldExports(maxAgeHours: Int = 24) = withContext(Dispatchers.IO) {
        try {
            val cutoffTime = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000)
            val files = exportDir.listFiles() ?: return@withContext
            
            var deletedCount = 0
            for (file in files) {
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
            
            Log.d(TAG, "Cleaned up $deletedCount old export files")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up exports", e)
        }
    }
    
    // Private helper methods
    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(50) // Limit length
    }
    
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
    
    private suspend fun exportMultipleToText(notes: List<Note>, file: File) {
        val content = buildString {
            append("NOTES EXPORT\n")
            append("=".repeat(50))
            append("\n")
            append("Exported: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
            append("Total Notes: ${notes.size}\n")
            append("=".repeat(50))
            append("\n\n")
            
            notes.forEachIndexed { index, note ->
                append("${index + 1}. ${note.title}\n")
                append("-".repeat(note.title.length + 3))
                append("\n")
                append("Created: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(note.createdAt))}\n\n")
                append(TextFormattingUtils.extractPlainText(note.content))
                append("\n\n")
                if (index < notes.size - 1) {
                    append("=".repeat(50))
                    append("\n\n")
                }
            }
        }
        
        file.writeText(content)
    }
    
    private suspend fun exportMultipleToMarkdown(notes: List<Note>, file: File) {
        val content = buildString {
            append("# Notes Export\n\n")
            append("**Exported:** ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())}  \n")
            append("**Total Notes:** ${notes.size}\n\n")
            append("---\n\n")
            
            notes.forEach { note ->
                append("## ${note.title}\n\n")
                append("**Created:** ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(note.createdAt))}\n\n")
                append("${note.content}\n\n")
                append("---\n\n")
            }
        }
        
        file.writeText(content)
    }
    
    private suspend fun exportMultipleToHtml(notes: List<Note>, file: File) {
        val content = buildString {
            append("<!DOCTYPE html>\n<html>\n<head>\n")
            append("<title>Notes Export</title>\n")
            append("<style>body{font-family:Arial,sans-serif;margin:40px;}</style>\n")
            append("</head>\n<body>\n")
            append("<h1>Notes Export</h1>\n")
            append("<p>Exported: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())}</p>\n")
            append("<p>Total Notes: ${notes.size}</p>\n<hr>\n")
            
            notes.forEach { note ->
                append("<h2>${escapeHtml(note.title)}</h2>\n")
                append("<p><em>Created: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(note.createdAt))}</em></p>\n")
                append("<div>${escapeHtml(TextFormattingUtils.extractPlainText(note.content))}</div>\n")
                append("<hr>\n")
            }
            
            append("</body>\n</html>")
        }
        
        file.writeText(content)
    }
    
    private suspend fun exportMultipleToPdf(notes: List<Note>, file: File) {
        // Simplified PDF export for multiple notes
        // In a real implementation, this would create a proper multi-page PDF
        exportToPdf(notes.first()) // For now, just export the first note
    }
    
    enum class ExportFormat(val extension: String) {
        TEXT("txt"),
        MARKDOWN("md"),
        HTML("html"),
        PDF("pdf")
    }
}
