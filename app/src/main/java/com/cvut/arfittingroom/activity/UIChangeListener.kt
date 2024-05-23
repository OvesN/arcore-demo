package com.cvut.arfittingroom.activity

/**
 * Allows to open main layout of the activity from attached to it fragment
 *
 * @author Veronika Ovsyannikova
 */
interface UIChangeListener {
    fun showMainLayout(restoreLookTexture: Boolean = false)
}
