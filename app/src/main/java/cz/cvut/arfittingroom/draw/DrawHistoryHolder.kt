package cz.cvut.arfittingroom.draw

import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.LinkedList

object DrawHistoryHolder {
    val globalHistory = LinkedList<Command<out Element>>()
    private val undoneActions = LinkedList<Command<out Element>>()

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

    fun addToHistory(command: Command<Element>){
        globalHistory.add(command)
        command.execute()
    }

}