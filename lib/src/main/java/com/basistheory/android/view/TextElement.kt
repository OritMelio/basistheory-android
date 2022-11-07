package com.basistheory.android.view

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import com.basistheory.android.*
import com.basistheory.android.event.BlurEvent
import com.basistheory.android.event.ChangeEvent
import com.basistheory.android.event.ElementEventListeners
import com.basistheory.android.event.FocusEvent

class TextElement : FrameLayout {
    private var attrs: AttributeSet? = null
    private var defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
    private var input: AppCompatEditText = AppCompatEditText(context, attrs, defStyleAttr)
    private var defaultBackground = input.background // todo: find a better way to reference this
    private val eventListeners = ElementEventListeners()

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.attrs = attrs

        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.attrs = attrs
        this.defStyleAttr = defStyleAttr

        initialize()
    }

    // this MUST be internal to prevent host apps from accessing the raw input values
    internal fun getText(): Editable? = input.text

    fun setText(value: String?) =
        input.setText(value)

    var textColor: Int
        get() = input.currentTextColor
        set(value) = input.setTextColor(value)

    var hint: CharSequence?
        get() = input.hint
        set(value) {
            input.hint = value
        }

    var removeDefaultStyles: Boolean
        get() = input.background == null
        set(value) { input.background = if (value) null else defaultBackground }

    fun addChangeEventListener(listener: (ChangeEvent) -> Unit) {
        eventListeners.change.add(listener)
    }

    fun addFocusEventListener(listener: (FocusEvent) -> Unit) {
        eventListeners.focus.add(listener)
    }

    fun addBlurEventListener(listener: (BlurEvent) -> Unit) {
        eventListeners.blur.add(listener)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        return input.onCreateInputConnection(outAttrs)
    }


    private fun initialize() {
        context.theme.obtainStyledAttributes(attrs, R.styleable.TextElement, defStyleAttr, 0)
            .apply {
                try {
                    textColor = getColor(R.styleable.TextElement_textColor, Color.BLACK)
                    hint = getString(R.styleable.TextElement_hint)
                    removeDefaultStyles = getBoolean(R.styleable.TextElement_removeDefaultStyles, false)
                    setText(getString(R.styleable.TextElement_text))
                } finally {
                    recycle()
                }
            }

        input.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        super.addView(input)

        subscribeToInputEvents()
    }

    private fun subscribeToInputEvents() {
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(value: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(value: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                eventListeners.change.forEach {
                    it(ChangeEvent(true, editable?.isEmpty() != false, listOf()))
                }
            }
        })

        input.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                eventListeners.focus.forEach { it(FocusEvent()) }
            else
                eventListeners.blur.forEach { it(BlurEvent()) }
        }
    }
}