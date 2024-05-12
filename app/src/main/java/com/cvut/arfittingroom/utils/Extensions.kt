package com.cvut.arfittingroom.utils

import com.cvut.arfittingroom.draw.model.PaintOptions
import com.cvut.arfittingroom.model.to.BrushTO
import com.google.firebase.auth.FirebaseAuth

fun String.makeFirstLetterCapital() =
    replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }

fun FirebaseAuth.currentUserUsername() = this.currentUser?.email?.substringBefore("@")
    .orEmpty()