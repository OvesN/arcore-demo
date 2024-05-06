package com.cvut.arfittingroom.utils

fun String.makeFirstLetterCapital() =
    replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
