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
    private var originX: Float = 0f
    private var originY: Float = 0f
    private var minOriginX = 0f
    private var minOriginY = 0f
    private var maxOriginX = 0f
    private var maxOriginY = 0f

    private var rulerOriginX = 0f
    private var rulerOriginY = 0f
    private var maxRulerOriginX = 0f
    private var maxRulerOriginY = 0f


    private val minScale: Float = 0.5f
    private val maxScale: Float = 2.5f

    private lateinit var gesture: GestureDetector
    private lateinit var scaleGesture: ScaleGestureDetector


    val paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
    }

    private fun init(){
        Timber.e("originX : $originX, originY : $originX")

        gesture = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener(){
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                Timber.i("onScroll")
                originX -= distanceX/scale
                originY -= distanceY/scale
                rulerOriginX -= distanceX/scale
                rulerOriginY -= distanceY/scale

                Timber.i("onScroll originX = ${originX} originY = ${originY}")
                Timber.i("onScroll rulerOriginX = ${rulerOriginX} rulerOriginY = ${rulerOriginY}")


                return true
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                return false
            }

            override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                return false
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


    private fun rulerBackground(canvas: Canvas) {

        val w: Int = canvas.width
        val h: Int = canvas.height

        val xpos: Float = (w / 2).toFloat()
        val ypos: Float = (h / 2).toFloat()

        canvas.run {
            val paint  = Paint(Paint.ANTI_ALIAS_FLAG) // 화면에 그려줄 도구를 셋팅하는 객체
            paint.color = Color.DKGRAY // 색상을 지정
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            val interval = FloatArray(3)
            interval[0] = 10f
            interval[1] = 10f
            paint.pathEffect = DashPathEffect(interval, 0f)


            val path = Path()
            path.moveTo(0f, ypos + rulerOriginY)
            path.lineTo(w.toFloat() * 1/scale, ypos + rulerOriginY)
            drawPath(path, paint)

//
            path.reset()
            path.moveTo(xpos + rulerOriginX, 0f)
            path.lineTo(xpos + rulerOriginX, h.toFloat() * 1/scale)
            drawPath(path, paint)
        }
    }


    override fun onDraw(canvas: Canvas) {
        Timber.i("originX : $originX, originY : $originX")

        canvas.scale(scale, scale)
        canvas.drawCircle(originX, originY, 100f, paint)


        rulerBackground(canvas)
        canvas.scale(1/scale, 1/scale)
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gesture.onTouchEvent(event)
        scaleGesture.onTouchEvent(event)
        invalidate()
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        originX = width/2.toFloat()
        originY = height/2.toFloat()
//        minOriginX = width/2.toFloat()
//        minOriginY = height/2.toFloat()
//        maxOriginX = width.toFloat()
//        maxOriginY = height.toFloat()
//        maxRulerOriginX = width.toFloat()
//        maxRulerOriginY = height.toFloat()
    }
}