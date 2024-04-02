package cz.cvut.arfittingroom.model

import kotlin.math.sqrt

data class Vector(val x: Float, val y: Float, val z: Float = 0f) {

    fun normalize(): Vector {
        val length: Float = sqrt(x * x + y * y + z * z)
        if (length == 0f) return Vector(0f, 0f, 0f)
        return Vector(x / length, y / length, z / length)
    }

    operator fun minus(other: Vector): Vector {
        return Vector(x - other.x, y - other.y, z - other.z)
    }


    operator fun plus(other: Vector): Vector {
        return Vector(x + other.x, y + other.y, z + other.z)
    }

    operator fun times(scalar: Float): Vector {
        return Vector(x * scalar, y * scalar, z * scalar)
    }
}