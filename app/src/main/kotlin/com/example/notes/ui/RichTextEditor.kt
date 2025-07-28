package com.example.notes.ui

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.*
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.example.notes.R
import com.example.notes.utils.TextFormattingUtils

class RichTextEditor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var formattingListener: FormattingListener? = null
    private var isFormattingEnabled = true
    
    interface FormattingListener {
        fun onFormattingChanged(hasFormatting: Boolean)
        fun onSelectionChanged(start: Int, end: Int)
    }

    init {
        setupEditor()
    }

    private fun setupEditor() {
        // Enable rich text editing
        customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                if (!isFormattingEnabled) return false
                
                menu?.add(0, R.id.action_bold, 0, "Bold")?.setIcon(R.drawable.ic_format_bold)
                menu?.add(0, R.id.action_italic, 0, "Italic")?.setIcon(R.drawable.ic_format_italic)
                menu?.add(0, R.id.action_underline, 0, "Underline")?.setIcon(R.drawable.ic_format_underlined)
                menu?.add(0, R.id.action_strikethrough, 0, "Strike")
                menu?.add(0, R.id.action_highlight, 0, "Highlight")
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.action_bold -> {
                        toggleBold()
                        return true
                    }
                    R.id.action_italic -> {
                        toggleItalic()
                        return true
                    }
                    R.id.action_underline -> {
                        toggleUnderline()
                        return true
                    }
                    R.id.action_strikethrough -> {
                        toggleStrikethrough()
                        return true
                    }
                    R.id.action_highlight -> {
                        toggleHighlight()
                        return true
                    }
                }
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {}
        }

        // Add text watcher for formatting detection
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                formattingListener?.onFormattingChanged(hasFormatting())
            }
        })
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        formattingListener?.onSelectionChanged(selStart, selEnd)
    }

    fun setFormattingListener(listener: FormattingListener) {
        this.formattingListener = listener
    }

    fun setFormattingEnabled(enabled: Boolean) {
        this.isFormattingEnabled = enabled
    }

    // Formatting methods
    fun toggleBold() {
        val start = selectionStart
        val end = selectionEnd
        if (start == end) return

        val spans = text?.getSpans(start, end, StyleSpan::class.java) ?: return
        val boldSpan = spans.find { it.style == Typeface.BOLD }

        if (boldSpan != null) {
            text?.removeSpan(boldSpan)
        } else {
            text?.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun toggleItalic() {
        val start = selectionStart
        val end = selectionEnd
        if (start == end) return

        val spans = text?.getSpans(start, end, StyleSpan::class.java) ?: return
        val italicSpan = spans.find { it.style == Typeface.ITALIC }

        if (italicSpan != null) {
            text?.removeSpan(italicSpan)
        } else {
            text?.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun toggleUnderline() {
        val start = selectionStart
        val end = selectionEnd
        if (start == end) return

        val spans = text?.getSpans(start, end, UnderlineSpan::class.java) ?: return
        
        if (spans.isNotEmpty()) {
            spans.forEach { text?.removeSpan(it) }
        } else {
            text?.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun toggleStrikethrough() {
        val start = selectionStart
        val end = selectionEnd
        if (start == end) return

        val spans = text?.getSpans(start, end, StrikethroughSpan::class.java) ?: return
        
        if (spans.isNotEmpty()) {
            spans.forEach { text?.removeSpan(it) }
        } else {
            text?.setSpan(StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun toggleHighlight() {
        val start = selectionStart
        val end = selectionEnd
        if (start == end) return

        val spans = text?.getSpans(start, end, BackgroundColorSpan::class.java) ?: return
        
        if (spans.isNotEmpty()) {
            spans.forEach { text?.removeSpan(it) }
        } else {
            val highlightColor = ContextCompat.getColor(context, R.color.highlight_yellow)
            text?.setSpan(BackgroundColorSpan(highlightColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun applyTextColor(color: Int) {
        val start = selectionStart
        val end = selectionEnd
        if (start == end) return

        // Remove existing color spans
        val spans = text?.getSpans(start, end, ForegroundColorSpan::class.java) ?: return
        spans.forEach { text?.removeSpan(it) }

        // Apply new color
        text?.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    fun insertBulletPoint() {
        val start = selectionStart
        val bulletText = "• "
        text?.insert(start, bulletText)
        setSelection(start + bulletText.length)
    }

    fun insertNumberedList(number: Int) {
        val start = selectionStart
        val numberedText = "$number. "
        text?.insert(start, numberedText)
        setSelection(start + numberedText.length)
    }

    fun insertCheckbox(checked: Boolean = false) {
        val start = selectionStart
        val checkboxText = if (checked) "☑ " else "☐ "
        text?.insert(start, checkboxText)
        setSelection(start + checkboxText.length)
    }

    // Utility methods
    fun hasFormatting(): Boolean {
        val text = text ?: return false
        return text.getSpans(0, text.length, CharacterStyle::class.java).isNotEmpty()
    }

    fun clearFormatting() {
        val text = text ?: return
        val spans = text.getSpans(0, text.length, CharacterStyle::class.java)
        spans.forEach { text.removeSpan(it) }
    }

    fun getFormattedText(): SpannableStringBuilder {
        return SpannableStringBuilder(text ?: "")
    }

    fun setFormattedText(spannableText: SpannableStringBuilder) {
        setText(spannableText, BufferType.SPANNABLE)
    }

    // Check current formatting at cursor
    fun isBoldAtCursor(): Boolean {
        val start = selectionStart
        val spans = text?.getSpans(start, start, StyleSpan::class.java) ?: return false
        return spans.any { it.style == Typeface.BOLD }
    }

    fun isItalicAtCursor(): Boolean {
        val start = selectionStart
        val spans = text?.getSpans(start, start, StyleSpan::class.java) ?: return false
        return spans.any { it.style == Typeface.ITALIC }
    }

    fun isUnderlineAtCursor(): Boolean {
        val start = selectionStart
        val spans = text?.getSpans(start, start, UnderlineSpan::class.java) ?: return false
        return spans.isNotEmpty()
    }

    fun isStrikethroughAtCursor(): Boolean {
        val start = selectionStart
        val spans = text?.getSpans(start, start, StrikethroughSpan::class.java) ?: return false
        return spans.isNotEmpty()
    }
}
