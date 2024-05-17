package com.cvut.arfittingroom.draw

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.fragment.HistoryChangeListener
import java.util.LinkedList

private const val MAX_NUMBER_OF_ACTIONS_HOLD_IN_HISTORY = 50
object DrawHistoryHolder {
    private val globalHistory = LinkedList<Command>()
    private val undoneActions = LinkedList<Command>()

    private var historyChangeListener: HistoryChangeListener? = null

    fun setHistoryChangeListener(listener: HistoryChangeListener) {
        historyChangeListener = listener
    }

    fun undo(): Command? {
        if (globalHistory.isEmpty()) {
            return null
        }

        val lastAction = globalHistory.last
        globalHistory.removeLast()

        undoneActions.add(lastAction)

        lastAction.revert()

        notifyHistoryChanged()

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

        notifyHistoryChanged()

        return lastUndoneAction
    }

    fun addToHistory(command: Command) {
        if (globalHistory.size == MAX_NUMBER_OF_ACTIONS_HOLD_IN_HISTORY) {
            globalHistory.removeFirst()
        }
        globalHistory.add(command)
        undoneActions.clear()
        command.execute()

        notifyHistoryChanged()
    }

    fun clearHistory() {
        globalHistory.clear()
        undoneActions.clear()

        notifyHistoryChanged()
    }

    fun isNotEmpty() = globalHistory.isNotEmpty()

    fun getHistorySize() = globalHistory.size
    fun getUndoneActionsSize() = undoneActions.size

    private fun notifyHistoryChanged() {
        historyChangeListener?.onHistoryChanged()
    }
}
