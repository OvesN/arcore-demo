package com.cvut.arfittingroom.utils

import android.content.Context

object ScreenUtil {
    fun dpToPx(
        dp: Int,
        context: Context,
    ): Int = (dp * context.resources.displayMetrics.density).toInt()
}
