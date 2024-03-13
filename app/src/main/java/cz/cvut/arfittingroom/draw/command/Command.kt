package cz.cvut.arfittingroom.draw.command

import android.graphics.Canvas

interface Command {
    fun execute(canvas: Canvas)
}