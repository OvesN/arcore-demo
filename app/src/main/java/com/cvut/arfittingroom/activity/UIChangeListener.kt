package com.cvut.arfittingroom.activity

/**
 * Allows to open main layout of the activity from attached to it fragment
 *
 * @author Veronika Ovsyannikova
 */
interface UIChangeListener {

    /**
     * Shows main layout of the activity
     *
     * @param restoreLookMask Defines if applied look face mask
     * should be download and applied again, used when we want to restore look state
     */
    fun showMainLayout(restoreLookMask: Boolean = false)
}
