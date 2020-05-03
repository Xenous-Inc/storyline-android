package com.xenous.storyline.utils

import android.content.Context
import android.util.DisplayMetrics

const val ERROR_CODE = 9000
const val SUCCESS_CODE = 9001
const val CANCEL_CODE = 9002
const val QUERY_IS_EMPTY = 9003

fun Int.dpToPx(context: Context): Int =
    this * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)

fun Float.dpToPx(context: Context): Float =
    this * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)