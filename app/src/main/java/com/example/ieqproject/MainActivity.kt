package com.example.ieqproject

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var dataSizeTextView: TextView // Declare the TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize the TextView for displaying data size
        dataSizeTextView = findViewById(R.id.dataSizeTextView)

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

        // Listen for changes in the data size
        database.child("dwellingAttributes").addValueEventListener(dataSizeListener)
    }

    // Listener for onDataChange this will help us see live data updates on the bottom
    private val dataSizeListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val jsonString = dataSnapshot.value.toString()
            val dataSizeInBytes = jsonString.toByteArray(StandardCharsets.UTF_8).size
            val dataSizeText = formatSize(dataSizeInBytes)

            // Update UI on the main thread
            runOnUiThread {
                dataSizeTextView.text = "Data Size: $dataSizeText"
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Handle error
        }
    }

    private fun formatSize(sizeInBytes: Int): String {
        val kilobytes = sizeInBytes / 1024.0
        val megabytes = kilobytes / 1024.0
        val gigabytes = megabytes / 1024.0

        return when {
            gigabytes >= 1 -> String.format("%.2f GB", gigabytes)
            megabytes >= 1 -> String.format("%.2f MB", megabytes)
            kilobytes >= 1 -> String.format("%.2f KB", kilobytes)
            else -> String.format("%d Bytes", sizeInBytes)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the ValueEventListener when the activity is destroyed
        database.child("dwellingAttributes").removeEventListener(dataSizeListener)
    }
}
