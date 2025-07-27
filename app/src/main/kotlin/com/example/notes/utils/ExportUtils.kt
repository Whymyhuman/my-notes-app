package com.example.notes.utils

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Html
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.notes.data.Note
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {
    
    fun exportNoteToPdf(context: Context, note: Note): String? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            val titlePaint = TextPaint().apply {
                textSize = 24f
                isFakeBoldText = true
                color = android.graphics.Color.BLACK
            }
            
            val contentPaint = TextPaint().apply {
                textSize = 14f
                color = android.graphics.Color.BLACK
            }
            
            val datePaint = TextPaint().apply {
                textSize = 12f
                color = android.graphics.Color.GRAY
            }
            
            var yPosition = 80f
            val margin = 50f
            val pageWidth = pageInfo.pageWidth - (margin * 2)
            
            // Draw title
            val title = note.title.ifEmpty { "Untitled" }
            val titleLayout = StaticLayout.Builder.obtain(title, 0, title.length, titlePaint, pageWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .build()
            
            canvas.save()
            canvas.translate(margin, yPosition)
            titleLayout.draw(canvas)
            canvas.restore()
            yPosition += titleLayout.height + 20f
            
            // Draw date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            val dateText = "Created: ${dateFormat.format(Date(note.timestamp))}"
            canvas.drawText(dateText, margin, yPosition, datePaint)
            yPosition += 40f
            
            // Draw content
            val content = if (note.content.contains("<")) {
                // Remove HTML tags for PDF
                Html.fromHtml(note.content, Html.FROM_HTML_MODE_COMPACT).toString()
            } else {
                note.content
            }
            
            val contentLayout = StaticLayout.Builder.obtain(content, 0, content.length, contentPaint, pageWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .build()
            
            canvas.save()
            canvas.translate(margin, yPosition)
            contentLayout.draw(canvas)
            canvas.restore()
            
            pdfDocument.finishPage(page)
            
            // Save PDF
            val fileName = "note_${note.id}_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    
    fun shareNote(context: Context, note: Note) {
        val shareText = buildString {
            if (note.title.isNotEmpty()) {
                append(note.title)
                append("\n\n")
            }
            
            val content = if (note.content.contains("<")) {
                Html.fromHtml(note.content, Html.FROM_HTML_MODE_COMPACT).toString()
            } else {
                note.content
            }
            append(content)
            
            append("\n\n---\n")
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            append("Created: ${dateFormat.format(Date(note.timestamp))}")
        }
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, note.title.ifEmpty { "Shared Note" })
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share Note"))
    }
    
    fun exportAllNotesToText(context: Context, notes: List<Note>): String? {
        try {
            val fileName = "all_notes_${System.currentTimeMillis()}.txt"
            val file = File(context.getExternalFilesDir(null), fileName)
            val content = buildString {
                append("My Notes Export\n")
                append("Generated on: ${SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(Date())}\n")
                append("Total notes: ${notes.size}\n")
                append("=".repeat(50))
                append("\n\n")
                
                notes.forEachIndexed { index, note ->
                    append("${index + 1}. ${note.title.ifEmpty { "Untitled" }}\n")
                    append("-".repeat(30))
                    append("\n")
                    
                    val noteContent = if (note.content.contains("<")) {
                        Html.fromHtml(note.content, Html.FROM_HTML_MODE_COMPACT).toString()
                    } else {
                        note.content
                    }
                    append(noteContent)
                    append("\n\n")
                    
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                    append("Created: ${dateFormat.format(Date(note.timestamp))}")
                    if (note.isPinned) append(" (Pinned)")
                    append("\n")
                    append("=".repeat(50))
                    append("\n\n")
                }
            }
            
            file.writeText(content)
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}