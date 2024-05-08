package com.cvut.arfittingroom.model

import com.google.gson.annotations.SerializedName

data class MakeupInfo(
    @SerializedName("ref") val ref: String,
    @SerializedName("type") val type: String,
    @SerializedName("color") val color: Int
)
