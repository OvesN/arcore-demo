package com.cvut.arfittingroom.utils

import android.content.Context

/**
 * Utility class for screen-related conversions
 *
 * @author Veronika Ovsyannikova
 */
object ScreenUtil {
    /**
     * Converts density-independent pixels (dp) to pixels (px)
     *
     * @param dp density-independent pixels
     * @param context
     * @return
     */
    fun dpToPx(
        dp: Int,
        context: Context,
    ): Int = (dp * context.resources.displayMetrics.density).toInt()
}
