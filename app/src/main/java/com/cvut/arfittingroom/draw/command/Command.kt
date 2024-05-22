package com.cvut.arfittingroom.draw.command

/**
 * Command that will be stored in the draw history
 *
 * @author Veronika Ovsyannikova
 */
interface Command {
    val description: String

    fun execute()

    fun revert()
}
