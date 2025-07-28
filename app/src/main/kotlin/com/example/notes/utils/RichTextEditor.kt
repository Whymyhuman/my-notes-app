package com.example.notes.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.widget.TextView.BufferType
import androidx.appcompat.widget.AppCompatEditText

class RichTextEditor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var isBoldEnabled = false
    private var isItalicEnabled = false
    private var isUnderlineEnabled = false
    
    init {
        // Ensure the EditText is properly initialized
        setText("", BufferType.SPANNABLE)
    }

    fun toggleBold() {
        isBoldEnabled = !isBoldEnabled
        applyFormatting()
    }

    fun toggleItalic() {
        isItalicEnabled = !isItalicEnabled
        applyFormatting()
    }

    fun toggleUnderline() {
        isUnderlineEnabled = !isUnderlineEnabled
        applyFormatting()
    }

    fun isBoldEnabled(): Boolean = isBoldEnabled
    fun isItalicEnabled(): Boolean = isItalicEnabled
    fun isUnderlineEnabled(): Boolean = isUnderlineEnabled

    private fun applyFormatting() {
        val start = selectionStart
        val end = selectionEnd
        
        if (start == end) {
            // No selection, apply formatting to future text
            // For now, we'll just update the button states
            return
        }

        val spannable = text as? Spannable ?: SpannableStringBuilder(text)
        
        // Remove existing formatting in selection
        removeFormattingInRange(spannable, start, end)
        
        // Apply new formatting
        if (isBoldEnabled) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        if (isItalicEnabled) {
            spannable.setSpan(
                StyleSpan(Typeface.ITALIC),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        if (isUnderlineEnabled) {
            spannable.setSpan(
                UnderlineSpan(),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun removeFormattingInRange(spannable: Spannable, start: Int, end: Int) {
        // Remove bold spans
        val boldSpans = spannable.getSpans(start, end, StyleSpan::class.java)
        for (span in boldSpans) {
            if (span.style == Typeface.BOLD || span.style == Typeface.BOLD_ITALIC) {
                spannable.removeSpan(span)
            }
        }
        
        // Remove italic spans
        val italicSpans = spannable.getSpans(start, end, StyleSpan::class.java)
        for (span in italicSpans) {
            if (span.style == Typeface.ITALIC || span.style == Typeface.BOLD_ITALIC) {
                spannable.removeSpan(span)
            }
        }
        
        // Remove underline spans
        val underlineSpans = spannable.getSpans(start, end, UnderlineSpan::class.java)
        for (span in underlineSpans) {
            spannable.removeSpan(span)
        }
    }

    fun getFormattedText(): String {
        val spannable = text as? Spannable ?: return text.toString()
        return try {
            Html.toHtml(spannable, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
        } catch (e: Exception) {
            text.toString()
        }
    }

    fun setFormattedText(htmlText: String) {
        try {
            if (htmlText.isNotEmpty() && htmlText.contains("<")) {
                // It's HTML formatted text
                val spanned = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)
                setText(spanned, BufferType.SPANNABLE)
            } else {
                // Plain text
                setText(htmlText)
            }
        } catch (e: Exception) {
            // Fallback to plain text
            setText(htmlText)
        }
    }

    fun clearFormatting() {
        val spannable = text as? Spannable ?: return
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        for (span in spans) {
            if (span is StyleSpan || span is UnderlineSpan) {
                spannable.removeSpan(span)
            }
        }
        isBoldEnabled = false
        isItalicEnabled = false
        isUnderlineEnabled = false
    }
}
