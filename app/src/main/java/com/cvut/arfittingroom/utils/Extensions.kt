package com.cvut.arfittingroom.utils

import com.google.firebase.auth.FirebaseAuth

fun String.makeFirstLetterCapital() =
    replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }

fun FirebaseAuth.currentUserUsername() = this.currentUser?.email?.substringBefore("@")
    .orEmpty()
