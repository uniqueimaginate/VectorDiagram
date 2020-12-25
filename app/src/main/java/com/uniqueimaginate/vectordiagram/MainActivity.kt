package com.uniqueimaginate.vectordiagram

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
                addVector("$i", 45 * i, 100 * i)
            }
        }
    }
}