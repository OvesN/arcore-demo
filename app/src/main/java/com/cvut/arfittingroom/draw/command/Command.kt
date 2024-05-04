package com.cvut.arfittingroom.draw.command

interface Command {
    val description: String

    fun execute()

    fun revert()
}
