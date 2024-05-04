package com.cvut.arfittingroom.utils

import android.content.Context

object ScreenUtil {
    var screenWidth: Int = 0
    var screenHeight: Int = 0

    fun dpToPx(
        dp: Int,
        context: Context,
    ): Int = (dp * context.resources.displayMetrics.density).toInt()
}
