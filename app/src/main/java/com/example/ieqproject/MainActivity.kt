package com.example.ieqproject
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.ieqproject.DwellingAttributes
import com.example.ieqproject.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the spinner
        val homeTypeSpinner: Spinner = findViewById(R.id.homeTypeSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.home_type_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            homeTypeSpinner.adapter = adapter
        }
    }
}
