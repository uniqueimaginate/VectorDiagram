package com.uniqueimaginate.vectordiagram

import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.uniqueimaginate.vectordiagram.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vectorDiagram.apply {
            for(i in 1..8){
                addCustomVector(i.toString(), 45*i, 100*i, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    textSize = 30f
                })
            }
        }
    }
}