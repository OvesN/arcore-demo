package cz.cvut.arfittingroom.draw

import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.model.element.Element
import java.util.LinkedList

object DrawHistoryHolder {
    val globalDrawHistory = LinkedList<Command<out Element>>()
    private val undoneActions = LinkedList<Command<out Element>>()

    fun gelLastAction(): Command<out Element>? {
        if (globalDrawHistory.isEmpty()) {
            return null
        }

        val lastAction = globalDrawHistory.last
        globalDrawHistory.removeLast()

        undoneActions.add(lastAction)
        return lastAction
    }

    fun getLastUndoneAction(): Command<out Element>? {
        if (undoneActions.isEmpty()) {
            return null
        }

        val lastUndoneAction = undoneActions.last
        undoneActions.removeLast()

        globalDrawHistory.add(lastUndoneAction)

        return lastUndoneAction
    }
}