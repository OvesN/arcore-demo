package com.cvut.arfittingroom.utils

import com.google.gson.Gson

object SerializationUtil {
    fun serializeToJson(value: Any): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    fun serializeToStringOfJson(values: List<Any>): List<String> {
        val gson = Gson()

      return values.map { gson.toJson(it) }
    }

}