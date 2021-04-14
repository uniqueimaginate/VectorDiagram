package com.uniqueimaginate.vectordiagram

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.*

class VectorDiagram : View {

    constructor(context: Context) :
            super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet)
            : super(context, attrs) {
        init()
    }


    private var scale: Float = 1f
    private var originX: Float = 0f
    private var originY: Float = 0f

    private var minScale: Float = 0.5f
    private var maxScale: Float = 3f

    private lateinit var gesture: GestureDetector
    private lateinit var scaleGesture: ScaleGestureDetector

    private val vectors = mutableMapOf<String, CustomVector>()

    fun setMinScale(newMinValue: Float) {
        minScale = newMinValue
    }

    fun setMaxScale(newMaxValue: Float) {
        maxScale = newMaxValue
    }

    private val rulerPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
        val interval = FloatArray(3)
        interval[0] = 10f
        interval[1] = 10f
        pathEffect = DashPathEffect(interval, 0f)
    }

    private fun init() {
        gesture = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {


            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {

                if (scale <= 1) {
                    originX -= (distanceX * scale)
                    originY -= (distanceY * scale)
                } else {
                    originX -= (distanceX / (scale * scale))
                    originY -= (distanceY / (scale * scale))
                }

                return true
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                return false
            }

            override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                return false
            }
        })

        scaleGesture = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector?): Boolean {
                    detector?.let {
                        if (it.scaleFactor < 0.01) {
                            return false
                        }

                        scale = Math.min(Math.max(scale * it.scaleFactor, minScale), maxScale)
                    }

                    return true
                }
            })
    }

    fun addVector(
        label: String,
        angle: Int,
        length: Int,
        textSize: Float = 25f,
        strokeWidth: Float = 3f,
        color: Int = Color.BLACK
    ) {
        val radian = Math.toRadians(-angle.toDouble())
        vectors[label] = CustomVector(label, radian, length, textSize, strokeWidth, color)
    }

    fun removeVector(label: String) {
        vectors.remove(label)
    }

    private fun rulerBackground(canvas: Canvas) {

        val verticalCenter = originY
        val horizontalCenter = originX

        canvas.run {
            val path = Path()
            path.moveTo(-originX / scale, verticalCenter)
            path.lineTo(10000f, verticalCenter)
            drawPath(path, rulerPaint)

            path.reset()
            path.moveTo(horizontalCenter, -originY / scale)
            path.lineTo(horizontalCenter, 10000f)
            drawPath(path, rulerPaint)
        }
    }


    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.translate(originX, originY)
        canvas.scale(scale, scale)
        rulerBackground(canvas)
        drawEverything(canvas)
        canvas.restore()
        super.onDraw(canvas)
    }

    private fun drawEverything(canvas: Canvas) {
        vectors.values.forEach { vector ->
            val endX = vector.getX(originX)
            val endY = vector.getY(originY)
            val paint = Paint().apply {
                color = vector.color
                strokeWidth = vector.strokeWidth
                textSize = vector.textSize
            }

            drawCircle(canvas, vector.length)
            drawLine(canvas, endX, endY, paint)
            addTextOnVectors(canvas, endX, endY, paint, vector.radian, vector.length)
        }
    }

    private fun drawLine(canvas: Canvas, endX: Float, endY: Float, paint: Paint) {
        val path = Path()
        path.moveTo(originX, originY)
        path.lineTo(endX, endY)
        path.close()

        canvas.drawPath(path, paint)
        drawArrow(paint, canvas, endX, endY)
    }


    private fun drawArrow(
        paint: Paint,
        canvas: Canvas,
        to_x: Float,
        to_y: Float
    ) {
        var angle = 0f
        var anglerad = 0f
        var radius = 0f
        var lineangle = 0f

        //values to change for other appearance *CHANGE THESE FOR OTHER SIZE ARROWHEADS*
        radius = 20f
        angle = 45f

        paint.style = Paint.Style.FILL_AND_STROKE

        anglerad = ((PI * angle / 180.0f).toFloat())
        lineangle = atan2(to_y - originY, to_x - originX)

        canvas.drawLine(originX, originY, to_x, to_y, paint)

        //tha triangle
        val path = Path()
        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(to_x, to_y)
        path.lineTo(
            (to_x - radius * cos(lineangle - anglerad / 2.0)).toFloat(),
            (to_y - radius * sin(lineangle - anglerad / 2.0)).toFloat()
        )
        path.lineTo(
            (to_x - radius * cos(lineangle + anglerad / 2.0)).toFloat(),
            (to_y - radius * sin(lineangle + anglerad / 2.0)).toFloat()
        )
        path.close()
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.STROKE
    }

    private fun drawCircle(canvas: Canvas, length: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawCircle(originX, originY, length.toFloat() / 2, paint)
    }

    private fun addTextOnVectors(
        canvas: Canvas,
        endX: Float,
        endY: Float,
        paint: Paint,
        radian: Double,
        length: Int
    ) {
        val degree = abs(Math.toDegrees(radian).toInt())

        when (degree % 360) {
            0 -> {
                canvas.drawText("0 \u00B0, $length", endX, endY - 50f, paint)
            }
            in 1..89 -> {
                canvas.drawText("$degree \u00B0, $length", endX, endY - 50f, paint)
            }
            90 -> {
                canvas.drawText("$degree \u00B0, $length", endX - 50f, endY - 50f, paint)
            }
            in 91..179 -> {
                canvas.drawText("$degree \u00B0, $length", endX - 100f, endY - 50f, paint)
            }
            180 -> {
                canvas.drawText("$degree \u00B0, $length", endX - 100f, endY - 50f, paint)
            }
            in 181..269 -> {
                canvas.drawText("$degree \u00B0, $length", endX - 100f, endY + 50f, paint)
            }
            270 -> {
                canvas.drawText("$degree \u00B0, $length", endX - 50f, endY + 50f, paint)
            }
            in 271..359 -> {
                canvas.drawText("$degree \u00B0, $length", endX, endY + 50f, paint)
            }
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gesture.onTouchEvent(event)
        scaleGesture.onTouchEvent(event)
        invalidate()
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        originX = width.toFloat() / 4
        originY = height.toFloat() / 4
    }
}