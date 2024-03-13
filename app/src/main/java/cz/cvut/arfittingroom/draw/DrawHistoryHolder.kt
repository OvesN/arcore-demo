package cz.cvut.arfittingroom.draw

import cz.cvut.arfittingroom.draw.command.Command
import cz.cvut.arfittingroom.draw.path.DrawablePath
import java.util.LinkedList

object DrawHistoryHolder {
    val paths = LinkedHashMap<DrawablePath, PaintOptions>()
    val lastPaths = LinkedHashMap<DrawablePath, PaintOptions>()
    val undonePaths = LinkedHashMap<DrawablePath, PaintOptions>()

    val actions = LinkedList<Command>()
    private val undoneActions = LinkedList<Command>()

    fun addPath(path: DrawablePath, options: PaintOptions) {
        paths[path] = options
    }

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