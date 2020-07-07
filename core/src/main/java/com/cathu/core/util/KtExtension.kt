package com.cathu.core.util

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue

/**
 *       Created by Cathu on 2020/7/5 0:40
 */

fun Float.applyDimension(unit: Int,context: Context): Float {
    if (context!is Activity){
        return 0f
    }
    val display = DisplayMetrics()
    context.windowManager.defaultDisplay.getMetrics(display)

    return TypedValue.applyDimension(unit, this, display)
}