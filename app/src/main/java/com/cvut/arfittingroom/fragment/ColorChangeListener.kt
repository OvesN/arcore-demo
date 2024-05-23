package com.cvut.arfittingroom.fragment

/**
 * Interface for listening to color changes
 * Implemented by classes that need to respond to changes in color selection
 *
 * @author Veronika Ovsyannikova
 */
interface ColorChangeListener {
    fun onColorChanged(newColor: Int, fill: Boolean)
}