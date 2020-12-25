package com.uniqueimaginate.vectordiagram

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import timber.log.Timber
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


    private var scale: Float = 0.5f
    private var originX: Float = 0f
    private var originY: Float = 0f

    private var minScale: Float = 0.3f
    private var maxScale: Float = 4f

    private lateinit var gesture: GestureDetector
    private lateinit var scaleGesture: ScaleGestureDetector

    private var scaling = false

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

                if (scale < 1) {
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

    fun addCustomVector(label: String, angle: Int, length: Int, paint: Paint) {
        val radian = Math.toRadians(-angle.toDouble())
        vectors[label] = CustomVector(label, radian, length, paint)
    }


    private fun rulerBackground(canvas: Canvas) {

        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

        val verticalCenter = originY
        val horizontalCenter = originX

        canvas.run {
            Timber.i("$originX")
            Timber.i("$originY")
            val path = Path()
            path.moveTo(-originX / scale , verticalCenter)
            path.lineTo( 2 *  (abs(originX) / scale) +1000f, verticalCenter)
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
        drawCircles(canvas)
        rulerBackground(canvas)
        drawLines(canvas)
        drawArrows(canvas)
        addTextOnVectors(canvas)
        canvas.restore()
        super.onDraw(canvas)
    }

    private fun drawLines(canvas: Canvas) {
        vectors.values.forEach { vector ->
            val endX = vector.getX(originX)
            val endY = vector.getY(originY)

            val path = Path()
            path.moveTo(originX, originY)
            path.lineTo(endX, endY)
            path.close()
            canvas.drawPath(path, vector.paint)
        }
    }

    private fun drawArrows(canvas: Canvas) {
        vectors.values.forEach { vector ->
            val endX = vector.getX(originX)
            val endY = vector.getY(originY)
            if (vector.length == 0)
                return@forEach
            drawArrow(vector.paint, canvas, endX, endY)
        }
    }

    private fun drawArrow(
        paint: Paint,
        canvas: Canvas,
        to_x: Float,
        to_y: Float
    ) {
        var angle = 0f
        var anglerad: Float = 0f
        var radius: Float = 0f
        var lineangle: Float = 0f

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

    private fun drawCircles(canvas: Canvas) {
        vectors.values.forEach { vector ->
            canvas.drawCircle(
                originX,
                originY,
                vector.length.toFloat(),
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.GRAY
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                })
        }
    }

    private fun addTextOnVectors(canvas: Canvas) {
        vectors.values.forEach { vector ->
            val endX = vector.getX(originX)
            val endY = vector.getY(originY)

            val degree = Math.abs(Math.toDegrees(vector.radian).toInt())

            when (degree % 360) {
                0 -> {
                    canvas.drawText(vector.length.toString(), endX, endY - 50f, vector.paint)
                }
                in 1..89 -> {
                    canvas.drawText(vector.length.toString(), endX, endY - 50f, vector.paint)
                }
                90 -> {
                    canvas.drawText(vector.length.toString(), endX - 25f, endY - 50f, vector.paint)
                }
                in 91..179 -> {
                    canvas.drawText(vector.length.toString(), endX - 50f, endY - 50f, vector.paint)
                }
                180 -> {
                    canvas.drawText(vector.length.toString(), endX - 50f, endY - 50f, vector.paint)
                }
                in 181..269 -> {
                    canvas.drawText(vector.length.toString(), endX - 50f, endY + 50f, vector.paint)
                }
                270 -> {
                    canvas.drawText(vector.length.toString(), endX - 25f, endY + 50f, vector.paint)
                }
                in 271..359 -> {
                    canvas.drawText(vector.length.toString(), endX, endY + 50f, vector.paint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Timber.i("x : ${event.x}")
        Timber.i("y : ${event.y}")
        gesture.onTouchEvent(event)
        scaleGesture.onTouchEvent(event)
        invalidate()
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        originX = width.toFloat() / 3
        originY = height.toFloat() / 3
    }
}