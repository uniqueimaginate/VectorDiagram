package com.uniqueimaginate.vectordiagram

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import timber.log.Timber

class VectorDiagram : View{

    constructor(context: Context):
        super(context){
            init()
        }

    constructor(context: Context, attrs: AttributeSet)
        : super(context){
            init()
        }


    private var scale: Float = 1f
    private var originX: Float = resources.displayMetrics.widthPixels / 2.toFloat()
    private var originY: Float = resources.displayMetrics.heightPixels / 2.toFloat()
    private var maxOriginX = 0f
    private var maxOriginY = 0f

    private val minScale: Float = 0.5f
    private val maxScale: Float = 2.5f

    private lateinit var gesture: GestureDetector
    private lateinit var scaleGesture: ScaleGestureDetector

    private lateinit var rectF: RectF

    val paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
    }

    private fun init(){
        originX = width/2.toFloat()
        originY = height/2.toFloat()
        gesture = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener(){
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                Timber.i("onScroll")
                originX += distanceX/scale
                originY += distanceY/scale

                Timber.i("onScroll originX = ${originX} originY = ${originY}")
                originX = originX.coerceIn(0f, maxOriginX)
                originY = originY.coerceIn(0f, maxOriginY)

                return true
            }


        })

        scaleGesture = ScaleGestureDetector(context, object: ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                detector?.let{
                    if(it.scaleFactor < 0.01){
                        return false
                    }
                    Timber.i("Scalefactor : ${it.scaleFactor}")
                    val fx = it.focusX
                    val fy = it.focusY

                    originX += fx / scale / 2
                    originY += fy / scale / 2

                    scale = Math.min(Math.max(scale * it.scaleFactor, minScale), maxScale)

                    originX -= fx / scale / 2
                    originY -= fy / scale / 2

                    originX = originX.coerceIn(0f, maxOriginX)
                    originY = originY.coerceIn(0f, maxOriginY)

                }

                return true
            }
        })
    }



    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        maxOriginX = w
        maxOriginY = h

        canvas.scale(scale, scale)
        canvas.drawCircle(originX, originY, 100f, paint)

        canvas.scale(1/scale, 1/scale)
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        gesture.onTouchEvent(event)
        scaleGesture.onTouchEvent(event)
        invalidate()
        return true
    }
}