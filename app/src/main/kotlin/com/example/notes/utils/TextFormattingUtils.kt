package com.example.notes.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.*
import androidx.core.text.toSpannable

object TextFormattingUtils {
    
    // HTML-like formatting tags
    private const val BOLD_START = "<b>"
    private const val BOLD_END = "</b>"
    private const val ITALIC_START = "<i>"
    private const val ITALIC_END = "</i>"
    private const val UNDERLINE_START = "<u>"
    private const val UNDERLINE_END = "</u>"
    private const val STRIKE_START = "<s>"
    private const val STRIKE_END = "</s>"
    private const val HIGHLIGHT_START = "<mark>"
    private const val HIGHLIGHT_END = "</mark>"
    
    /**
     * Convert SpannableStringBuilder to a formatted string that can be stored in database
     */
    fun spannableToFormattedString(spannable: SpannableStringBuilder): String {
        val text = spannable.toString()
        val spans = spannable.getSpans(0, spannable.length, CharacterStyle::class.java)
        
        if (spans.isEmpty()) return text
        
        val result = StringBuilder()
        var lastIndex = 0
        
        // Sort spans by start position
        val sortedSpans = spans.sortedBy { spannable.getSpanStart(it) }
        
        for (span in sortedSpans) {
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            
            // Add text before span
            if (start > lastIndex) {
                result.append(text.substring(lastIndex, start))
            }
            
            // Add opening tag
            when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        Typeface.BOLD -> result.append(BOLD_START)
                        Typeface.ITALIC -> result.append(ITALIC_START)
                    }
                }
                is UnderlineSpan -> result.append(UNDERLINE_START)
                is StrikethroughSpan -> result.append(STRIKE_START)
                is BackgroundColorSpan -> result.append(HIGHLIGHT_START)
            }
            
            // Add span text
            result.append(text.substring(start, end))
            
            // Add closing tag
            when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        Typeface.BOLD -> result.append(BOLD_END)
                        Typeface.ITALIC -> result.append(ITALIC_END)
                    }
                }
                is UnderlineSpan -> result.append(UNDERLINE_END)
                is StrikethroughSpan -> result.append(STRIKE_END)
                is BackgroundColorSpan -> result.append(HIGHLIGHT_END)
            }
            
            lastIndex = end
        }
        
        // Add remaining text
        if (lastIndex < text.length) {
            result.append(text.substring(lastIndex))
        }
        
        return result.toString()
    }
    
    /**
     * Convert formatted string back to SpannableStringBuilder
     */
    fun formattedStringToSpannable(formattedText: String, highlightColor: Int): SpannableStringBuilder {
        var text = formattedText
        val spannable = SpannableStringBuilder()
        
        while (text.isNotEmpty()) {
            val nextTag = findNextTag(text)
            
            if (nextTag == null) {
                // No more tags, add remaining text
                spannable.append(text)
                break
            }
            
            // Add text before tag
            if (nextTag.start > 0) {
                spannable.append(text.substring(0, nextTag.start))
            }
            
            // Find closing tag
            val closingTag = findClosingTag(text, nextTag)
            if (closingTag != null) {
                val spanStart = spannable.length
                val spanText = text.substring(nextTag.end, closingTag.start)
                spannable.append(spanText)
                val spanEnd = spannable.length
                
                // Apply span
                when (nextTag.type) {
                    TagType.BOLD -> spannable.setSpan(
                        StyleSpan(Typeface.BOLD), 
                        spanStart, spanEnd, 
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    TagType.ITALIC -> spannable.setSpan(
                        StyleSpan(Typeface.ITALIC), 
                        spanStart, spanEnd, 
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    TagType.UNDERLINE -> spannable.setSpan(
                        UnderlineSpan(), 
                        spanStart, spanEnd, 
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    TagType.STRIKETHROUGH -> spannable.setSpan(
                        StrikethroughSpan(), 
                        spanStart, spanEnd, 
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    TagType.HIGHLIGHT -> spannable.setSpan(
                        BackgroundColorSpan(highlightColor), 
                        spanStart, spanEnd, 
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                
                text = text.substring(closingTag.end)
            } else {
                // No closing tag found, treat as regular text
                spannable.append(text.substring(nextTag.start))
                break
            }
        }
        
        return spannable
    }
    
    private fun findNextTag(text: String): Tag? {
        val tags = listOf(
            BOLD_START to TagType.BOLD,
            ITALIC_START to TagType.ITALIC,
            UNDERLINE_START to TagType.UNDERLINE,
            STRIKE_START to TagType.STRIKETHROUGH,
            HIGHLIGHT_START to TagType.HIGHLIGHT
        )
        
        var earliestTag: Tag? = null
        var earliestIndex = Int.MAX_VALUE
        
        for ((tagText, tagType) in tags) {
            val index = text.indexOf(tagText)
            if (index != -1 && index < earliestIndex) {
                earliestIndex = index
                earliestTag = Tag(tagType, index, index + tagText.length)
            }
        }
        
        return earliestTag
    }
    
    private fun findClosingTag(text: String, openingTag: Tag): Tag? {
        val closingTagText = when (openingTag.type) {
            TagType.BOLD -> BOLD_END
            TagType.ITALIC -> ITALIC_END
            TagType.UNDERLINE -> UNDERLINE_END
            TagType.STRIKETHROUGH -> STRIKE_END
            TagType.HIGHLIGHT -> HIGHLIGHT_END
        }
        
        val index = text.indexOf(closingTagText, openingTag.end)
        return if (index != -1) {
            Tag(openingTag.type, index, index + closingTagText.length)
        } else null
    }
    
    /**
     * Extract plain text from formatted string
     */
    fun extractPlainText(formattedText: String): String {
        return formattedText
            .replace(BOLD_START, "")
            .replace(BOLD_END, "")
            .replace(ITALIC_START, "")
            .replace(ITALIC_END, "")
            .replace(UNDERLINE_START, "")
            .replace(UNDERLINE_END, "")
            .replace(STRIKE_START, "")
            .replace(STRIKE_END, "")
            .replace(HIGHLIGHT_START, "")
            .replace(HIGHLIGHT_END, "")
    }
    
    /**
     * Check if text contains formatting
     */
    fun hasFormatting(text: String): Boolean {
        return text.contains(BOLD_START) || 
               text.contains(ITALIC_START) || 
               text.contains(UNDERLINE_START) || 
               text.contains(STRIKE_START) || 
               text.contains(HIGHLIGHT_START)
    }
    
    /**
     * Convert markdown-style formatting to spannable
     */
    fun markdownToSpannable(markdown: String, highlightColor: Int): SpannableStringBuilder {
        var text = markdown
        val spannable = SpannableStringBuilder()
        
        // Convert **bold** to <b>bold</b>
        text = text.replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
        
        // Convert *italic* to <i>italic</i>
        text = text.replace(Regex("\\*(.*?)\\*"), "<i>$1</i>")
        
        // Convert __underline__ to <u>underline</u>
        text = text.replace(Regex("__(.*?)__"), "<u>$1</u>")
        
        // Convert ~~strikethrough~~ to <s>strikethrough</s>
        text = text.replace(Regex("~~(.*?)~~"), "<s>$1</s>")
        
        return formattedStringToSpannable(text, highlightColor)
    }
    
    /**
     * Convert spannable to markdown-style text
     */
    fun spannableToMarkdown(spannable: SpannableStringBuilder): String {
        return spannableToFormattedString(spannable)
            .replace(BOLD_START, "**")
            .replace(BOLD_END, "**")
            .replace(ITALIC_START, "*")
            .replace(ITALIC_END, "*")
            .replace(UNDERLINE_START, "__")
            .replace(UNDERLINE_END, "__")
            .replace(STRIKE_START, "~~")
            .replace(STRIKE_END, "~~")
            .replace(HIGHLIGHT_START, "==")
            .replace(HIGHLIGHT_END, "==")
    }
    
    private data class Tag(
        val type: TagType,
        val start: Int,
        val end: Int
    )
    
    private enum class TagType {
        BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, HIGHLIGHT
    }
}
