package com.example.gymbuddy.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.TextView

@SuppressLint("AppCompatCustomView")
 class MarqueeTextView : TextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if(focused)super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if(hasWindowFocus)super.onWindowFocusChanged(hasWindowFocus)
    }

    override fun isSelected(): Boolean {
        return true
    }

    override fun isFocused(): Boolean {
        return true
    }
}