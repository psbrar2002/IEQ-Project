package com.tst.ieqproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startSurveyButton: Button = findViewById(R.id.startSurveyButton)
        startSurveyButton.setOnClickListener {
            val intent = Intent(this, DwellingAttributesActivity::class.java)
            startActivity(intent)
        }
    }
}
