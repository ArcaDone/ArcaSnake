package com.arcadan.arcasnake.snake

import android.content.Context
import android.util.AttributeSet
import android.view.View

class BackgroundView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    init {
        isFocusable = true
    }
}