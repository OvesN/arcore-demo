package com.cvut.arfittingroom.draw

import com.cvut.arfittingroom.draw.command.Command
import com.cvut.arfittingroom.fragment.HistoryChangeListener
import java.util.LinkedList

private const val MAX_NUMBER_OF_ACTIONS_HOLD_IN_HISTORY = 50

/**
 * Singleton object that manages the history of drawing commands
 *
 * @author Veronika Ovsyannikova
 */
object DrawHistoryHolder {
    private val globalHistory = LinkedList<Command>()
    private val undoneActions = LinkedList<Command>()

    private var historyChangeListener: HistoryChangeListener? = null

    /**
     * Sets the listener to be notified when the history changes
     *
     * @param listener The listener to set
     */
    fun setHistoryChangeListener(listener: HistoryChangeListener) {
        historyChangeListener = listener
    }


    /**
     * Undoes the last command and moves it to the undone actions list
     *
     * @return The last undone command, or null if there is no command to undo
     */
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

    /**
     * Redoes the last undone command and moves it back to the history list
     *
     * @return The last redone command, or null if there is no command to redo
     */
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

    /**
     * Adds a command to the history and executes it
     *
     * @param command The command to add to the history
     */
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
