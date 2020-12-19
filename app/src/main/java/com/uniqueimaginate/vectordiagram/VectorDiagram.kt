package com.uniqueimaginate.vectordiagram

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

class VectorDiagram : View{

    constructor(context: Context):
        super(context){
            init()
        }

    constructor(context: Context, attrs: AttributeSet)
        : super(context){
            init()
        }


    private val scale: Float = 1f
    private var originX: Float = 0f
    private var originY: Float = 0f

    private val minScale: Float = 0.5f
    private val maxScale: Float = 2.5f

    private lateinit var gesture: GestureDetector
    private lateinit var scaleGesture: ScaleGestureDetector

    private fun init(){
        gesture = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener(){
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                originX += distanceX/scale
                originY += distanceY/scale

                invalidate()
                return true
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                var x = 0f
                var y = 0f

                e?.let{ motionEvent ->
                   x = motionEvent.x
                   y = motionEvent.y
                }

                x = x / scale + originX
                y = y / scale + originY

                return true
            }

        })

        scaleGesture = ScaleGestureDetector(context, object: ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                detector?.let{
                    if(it.scaleFactor < 0.01){
                        return false
                    }

                    val fx = it.focusX
                    val fy = it.focusY
                }

                return super.onScale(detector)
            }
        })
    }


    override fun onDraw(canvas: Canvas) {
        val w = width
        val h = height

        canvas.scale(scale, scale)



        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(!gesture.onTouchEvent(event)){
            return scaleGesture.onTouchEvent(event)
        }
        return true
    }
}