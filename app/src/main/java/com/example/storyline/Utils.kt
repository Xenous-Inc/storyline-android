package com.x3noku.story

import android.content.Context
import android.util.DisplayMetrics

fun Int.dpToPx(context: Context): Int =
    this * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)

fun Float.dpToPx(context: Context): Float =
    this * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)