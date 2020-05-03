package com.xenous.storyline.utils

import android.content.Context
import android.util.DisplayMetrics

const val ERROR_CODE = 0
const val SUCCESS_CODE = 1
const val CANCEL_CODE = 2

fun Int.dpToPx(context: Context): Int =
    this * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)

fun Float.dpToPx(context: Context): Float =
    this * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)