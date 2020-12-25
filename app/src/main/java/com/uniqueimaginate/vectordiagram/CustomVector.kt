package com.uniqueimaginate.vectordiagram

import kotlin.math.cos
import kotlin.math.sin

data class CustomVector(
    val label: String,
    val radian: Double,
    val length: Int,
    val textSize: Float,
    val strokeWidth: Float,
    val color: Int
){
    fun getX(originX: Float): Float{
        return originX + length * cos(radian).toFloat() / 2
    }

    fun getY(originY: Float): Float {
        return originY + length * sin(radian).toFloat() / 2
    }
}
