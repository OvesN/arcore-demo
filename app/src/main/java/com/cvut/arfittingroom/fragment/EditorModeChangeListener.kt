package com.cvut.arfittingroom.fragment

import com.cvut.arfittingroom.draw.model.enums.EEditorMode

/**
 * Editor mode change listener
 *
 * @author Veronika Ovsyannikova
 */
interface EditorModeChangeListener {
    fun onEditingModeExit(newMode: EEditorMode)
}
