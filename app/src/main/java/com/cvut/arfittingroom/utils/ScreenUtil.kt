package com.cvut.arfittingroom.utils

import android.content.Context

/**
 * Screen util
 *
 * @author Veronika Ovsyannikova
 */
object ScreenUtil {
    fun dpToPx(
        dp: Int,
        context: Context,
    ): Int = (dp * context.resources.displayMetrics.density).toInt()
}
