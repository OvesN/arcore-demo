package com.cvut.arfittingroom.draw

import com.cvut.arfittingroom.draw.command.Command
import java.util.LinkedList

object DrawHistoryHolder {
    private val globalHistory = LinkedList<Command>()
    private val undoneActions = LinkedList<Command>()

    fun undo(): Command? {
        if (globalHistory.isEmpty()) {
            return null
        }

        val lastAction = globalHistory.last
        globalHistory.removeLast()

        undoneActions.add(lastAction)

        lastAction.revert()
        return lastAction
    }

    fun redo(): Command? {
        if (undoneActions.isEmpty()) {
            return null
        }

        val lastUndoneAction = undoneActions.last
        undoneActions.removeLast()

        globalHistory.add(lastUndoneAction)

        lastUndoneAction.execute()

        return lastUndoneAction
    }

    fun addToHistory(command: Command) {
        globalHistory.add(command)
        undoneActions.clear()
        command.execute()
    }

    fun clearHistory() {
        globalHistory.clear()
        undoneActions.clear()
    }

    fun isNotEmpty() = globalHistory.isNotEmpty()
}
