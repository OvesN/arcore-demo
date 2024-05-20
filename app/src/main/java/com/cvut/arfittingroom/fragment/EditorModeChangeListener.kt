package com.cvut.arfittingroom.fragment

import com.cvut.arfittingroom.draw.model.enums.EEditorMode

interface EditorModeChangeListener {
    fun onEditingModeExit(newMode: EEditorMode)
}
