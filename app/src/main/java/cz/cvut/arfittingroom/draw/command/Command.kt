package cz.cvut.arfittingroom.draw.command

interface Command {
    fun execute()
    fun revert()
}