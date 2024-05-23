package com.cvut.arfittingroom.fragment

/**
 * Interface to listen for changes in the drawing history
 * Implemented by classes that need to be notified when the history of drawing actions changes
 *
 * @author Veronika Ovsyannikova
 */
interface HistoryChangeListener {
    fun onHistoryChanged()
}