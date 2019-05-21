package com.kotlin.widget

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switch_button_drawable

        switch_button.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){

            }
        }


    }
}
