package com.cvut.arfittingroom.utils


import com.google.gson.Gson


object DeserializationUtil {

    fun <T> deserializeFromMap(map: Map<String, Any?>, clazz: Class<T>): T {
        val gson = Gson()
        val jsonString = gson.toJson(map)
        return gson.fromJson(jsonString, clazz)
    }


}