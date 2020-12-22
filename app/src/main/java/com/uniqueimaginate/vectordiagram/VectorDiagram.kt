package com.uniqueimaginate.vectordiagram

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import timber.log.Timber
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


    private var scale: Float = 1f
    private var originX: Float = 0f
    private var originY: Float = 0f
    private var rulerOriginX = 0f
    private var rulerOriginY = 0f

    private var minScale: Float = 0.3f
    private var maxScale: Float = 3f

    private lateinit var gesture: GestureDetector
    private lateinit var scaleGesture: ScaleGestureDetector

    private val vectors = arrayListOf<CustomVector>()


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

    fun addCustomVector(angle: Double, length: Int, paint: Paint) {
        val radian = Math.toRadians(angle)
        vectors.add(CustomVector(radian, length, paint))
    }


    private fun rulerBackground(canvas: Canvas) {

        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

        val xpos: Float = (w / 2)
        val ypos: Float = (h / 2)

        val verticalCenter = ypos + rulerOriginY
        val horizontalCentor = xpos + rulerOriginX

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
        super.onDraw(canvas)
    }

    private fun drawLines(canvas: Canvas) {
        vectors.forEach { vector ->
            val endX = originX + vector.length * cos(vector.radian)
            val endY = originY + vector.length * sin(vector.radian)

            val path = Path()
            path.moveTo(originX, originY)
            path.lineTo(endX.toFloat(), endY.toFloat())
            path.close()
            canvas.drawPath(path, vector.paint)
        }
    }

    private fun drawCircles(canvas: Canvas){
        vectors.forEach { vector ->
            canvas.drawCircle(originX, originY, vector.length.toFloat(), vector.paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gesture.onTouchEvent(event)
        scaleGesture.onTouchEvent(event)
        val point = Point()
        val x = event.x.toInt()
        val y = event.y.toInt()
        point.x = x
        point.y = y

        invalidate()
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        originX = width / 2.toFloat()
        originY = height / 2.toFloat()
        addCustomVector(-30.0, 200, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 3f
        })
        addCustomVector(+30.0, 500, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 3f
        })
    }
}