package com.cvut.arfittingroom.draw.command

/**
 * Command
 *
 * @author Veronika Ovsyannikova
 */
interface Command {
    val description: String

    fun execute()

    fun revert()
}
