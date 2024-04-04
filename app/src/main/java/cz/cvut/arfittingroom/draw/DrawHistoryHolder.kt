package cz.cvut.arfittingroom.draw

import cz.cvut.arfittingroom.draw.command.Command
import java.util.LinkedList

object DrawHistoryHolder {
    private val globalHistory = LinkedList<Command>()
    private val undoneActions = LinkedList<Command>()

    fun undo() {
        if (globalHistory.isEmpty()) {
            return
        }

        val lastAction = globalHistory.last
        globalHistory.removeLast()

        undoneActions.add(lastAction)

        lastAction.revert()
    }

    fun redo() {
        if (undoneActions.isEmpty()) {
            return
        }

        val lastUndoneAction = undoneActions.last
        undoneActions.removeLast()

        globalHistory.add(lastUndoneAction)

        lastUndoneAction.execute()
    }

    fun addToHistory(command: Command){
        globalHistory.add(command)
        command.execute()
    }

    fun clearHistory() {
        globalHistory.clear()
    }

}