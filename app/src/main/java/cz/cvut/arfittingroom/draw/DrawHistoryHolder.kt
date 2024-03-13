package cz.cvut.arfittingroom.draw

import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.path.DrawablePath
import java.util.LinkedList

object DrawHistoryHolder {
    val actions = LinkedList<Command>()
    private val undoneActions = LinkedList<Command>()

    fun undo() {
        if (actions.isEmpty()) {
            return
        }

        val lastAction = actions.last
        actions.removeLast()

        undoneActions.add(lastAction)
    }

    fun redo() {
        if (undoneActions.isEmpty()) {
            return
        }

        val lastUndoneAction = undoneActions.last
        undoneActions.removeLast()

        actions.add(lastUndoneAction)
    }
}