package com.cvut.arfittingroom.fragment

import com.cvut.arfittingroom.draw.model.enums.EEditorMode

/**
 * Interface for listening to changes in the editor mode
 * Implemented by classes that need to respond to the user exiting the editing mode
 *
 * @author Veronika Ovsyannikova
 */
interface EditorModeChangeListener {
    fun onEditingModeExit(newMode: EEditorMode)
}
