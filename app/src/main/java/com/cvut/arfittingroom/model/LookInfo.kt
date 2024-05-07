package com.cvut.arfittingroom.model

data class LookInfo(
    val lookId: String,
    val appliedMakeup: List<String>,
    val appliedModels: List<String>,
    val history: String,
    val name: String,
    val previewRef: String
)