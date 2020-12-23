package com.uniqueimaginate.vectordiagram

import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.sin

data class CustomVector(
    val label: String,
    val radian: Double,
    val length: Int,
    val paint: Paint
){
    fun getX(originX: Float): Float{
        return originX + length * cos(radian).toFloat()
    }

    fun getY(originY: Float): Float{
        return originY + length * sin(radian).toFloat()
    }
}
