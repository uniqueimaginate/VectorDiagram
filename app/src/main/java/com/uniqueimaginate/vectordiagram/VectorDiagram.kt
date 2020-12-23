package com.uniqueimaginate.vectordiagram

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import timber.log.Timber
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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
    private var rulerOriginX = 0f
    private var rulerOriginY = 0f

    private var minScale: Float = 0.3f
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
        Timber.e("originX : $originX, originY : $originX")

        gesture = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                originX -= distanceX / scale
                originY -= distanceY / scale
                rulerOriginX -= distanceX / scale
                rulerOriginY -= distanceY / scale
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
                        val fx = it.focusX
                        val fy = it.focusY

                        originX -= fx / it.scaleFactor
                        originY -= fy / it.scaleFactor
                        rulerOriginX -= fx / it.scaleFactor
                        rulerOriginY -= fy / it.scaleFactor

                        scale = Math.min(Math.max(scale * it.scaleFactor, minScale), maxScale)

                        originX += fx / it.scaleFactor
                        originY += fy / it.scaleFactor
                        rulerOriginX += fx / it.scaleFactor
                        rulerOriginY += fy / it.scaleFactor
                    }

                    return true
                }
            })
    }

    fun addCustomVector(label: String, angle: Double, length: Int, paint: Paint) {
        val radian = Math.toRadians(angle)
        vectors[label] = CustomVector(label, radian, length, paint)
    }


    private fun rulerBackground(canvas: Canvas) {

        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

        val verticalCenter = originY
        val horizontalCentor = originX

        canvas.run {
            val path = Path()
            path.moveTo(0f, verticalCenter)
            path.lineTo(w * 1 / scale, verticalCenter)
            drawPath(path, rulerPaint)

            path.reset()
            path.moveTo(horizontalCentor, 0f)
            path.lineTo(horizontalCentor, h * 1 / scale)
            drawPath(path, rulerPaint)
        }
    }


    override fun onDraw(canvas: Canvas) {
        canvas.scale(scale, scale)
        drawCircles(canvas)
        rulerBackground(canvas)
        drawLines(canvas)
        drawArrows(canvas)
        super.onDraw(canvas)
    }

    private fun drawLines(canvas: Canvas) {
        vectors.values.forEach { vector ->
            val endX = originX + vector.length * cos(vector.radian)
            val endY = originY + vector.length * sin(vector.radian)

            val path = Path()
            path.moveTo(originX, originY)
            path.lineTo(endX.toFloat(), endY.toFloat())
            path.close()
            canvas.drawPath(path, vector.paint)
        }
    }

    private fun drawArrows(canvas: Canvas){
        vectors.values.forEach { vector ->
            val endX = originX + vector.length * cos(vector.radian)
            val endY = originY + vector.length * sin(vector.radian)
            drawArrow(vector.paint, canvas, endX.toFloat(), endY.toFloat())
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
        path.lineTo((to_x - radius * cos(lineangle - anglerad / 2.0)).toFloat(),
            (to_y - radius * sin(lineangle - anglerad / 2.0)).toFloat())
        path.lineTo((to_x - radius * cos(lineangle + anglerad / 2.0)).toFloat(),
            (to_y - radius * sin(lineangle + anglerad / 2.0)).toFloat())
        path.close()
        canvas.drawPath(path, paint)

        paint.style = Paint.Style.STROKE
    }

    private fun drawCircles(canvas: Canvas){
        vectors.values.forEach { vector ->
            canvas.drawCircle(originX, originY, vector.length.toFloat(), vector.paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gesture.onTouchEvent(event)
        scaleGesture.onTouchEvent(event)
        val x = event.x.toInt()
        val y = event.y.toInt()
        invalidate()
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        originX = width.toFloat()
        originY = height.toFloat()

    }
}