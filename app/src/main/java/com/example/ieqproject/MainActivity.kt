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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize the home type spinner
        val homeTypeSpinner: Spinner = findViewById(R.id.homeTypeSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.home_type_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            homeTypeSpinner.adapter = adapter
        }

        // Initialize the section 8 spinner
        val section8Spinner: Spinner = findViewById(R.id.section8Spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.yes_no_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            section8Spinner.adapter = adapter
        }

        val submitButton: Button = findViewById(R.id.submitButton)

        // Set up the submit button click listener
        submitButton.setOnClickListener {
            val homeType = homeTypeSpinner.selectedItem.toString()
            val section8 = section8Spinner.selectedItem.toString()

            // Save the data in the Realtime Database
            val data = mapOf(
                "homeType" to homeType,
                "section8" to section8
            )

            database.child("dwellingAttributes").push().setValue(data)
                .addOnSuccessListener {
                    // Handle success
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }
    }
}