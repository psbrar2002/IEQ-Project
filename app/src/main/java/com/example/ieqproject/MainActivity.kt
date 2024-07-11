package com.example.ieqproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.database.*
import java.io.File
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var dataSizeTextView: TextView

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

        // Initialize the Oakland Housing Authority spinner
        val oaklandHousingSpinner: Spinner = findViewById(R.id.oaklandHousingSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.yes_no_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            oaklandHousingSpinner.adapter = adapter
        }

        // Initialize the number of people spinner
        val numberOfPeopleSpinner: Spinner = findViewById(R.id.numberOfPeopleSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.number_of_people_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            numberOfPeopleSpinner.adapter = adapter
        }

        // Initialize the square footage spinner
        val squareFootageSpinner: Spinner = findViewById(R.id.squareFootageSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.square_footage_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            squareFootageSpinner.adapter = adapter
        }

        // Initialize the EditTexts
        val dateEditText: EditText = findViewById(R.id.dateEditText)
        val streetIntersectionEditText: EditText = findViewById(R.id.streetIntersectionEditText)
        val buildingAgeEditText: EditText = findViewById(R.id.buildingAgeEditText)

        val submitButton: Button = findViewById(R.id.submitButton)

        // Set up the submit button click listener
        submitButton.setOnClickListener {
            val homeType = homeTypeSpinner.selectedItem.toString()
            val section8 = section8Spinner.selectedItem.toString() == "Yes"
            val oaklandHousing = oaklandHousingSpinner.selectedItem.toString() == "Yes"
            val numPeople = numberOfPeopleSpinner.selectedItem.toString()
            val squareFootage = squareFootageSpinner.selectedItem.toString()
            val date = dateEditText.text.toString()
            val streetIntersection = streetIntersectionEditText.text.toString()
            val buildingAge = buildingAgeEditText.text.toString()

            // Save the data in the Realtime Database
            val data = DwellingAttributes(
                homeType,
                section8,
                oaklandHousing,
                numPeople,
                squareFootage,
                date,
                streetIntersection,
                if (buildingAge.isBlank()) null else buildingAge
            )

            database.child("dwellingAttributes").push().setValue(data)
                .addOnSuccessListener {
                    // Handle success
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }

        // Initialize the data size text view
//        dataSizeTextView = findViewById(R.id.dataSizeTextView)

        // Listen for changes in the data size
//        database.child("dwellingAttributes").addValueEventListener(dataSizeListener)
//
//        // Set up a button to export data
//        val exportButton: Button = findViewById(R.id.exportButton)
//        exportButton.setOnClickListener {
//            retrieveData()
        }
    }

//    // Listener for onDataChange
//    private val dataSizeListener = object : ValueEventListener {
//        override fun onDataChange(dataSnapshot: DataSnapshot) {
//            val jsonString = dataSnapshot.value.toString()
//            val dataSizeInBytes = jsonString.toByteArray(StandardCharsets.UTF_8).size
//            val dataSizeText = formatSize(dataSizeInBytes)
//
//            // Update UI on the main thread
//            runOnUiThread {
//                dataSizeTextView.text = "Data Size: $dataSizeText"
//            }
//        }
//
//        override fun onCancelled(databaseError: DatabaseError) {
//            // Handle error
//        }
//    }
//
//    private fun formatSize(sizeInBytes: Int): String {
//        val kilobytes = sizeInBytes / 1024.0
//        val megabytes = kilobytes / 1024.0
//        val gigabytes = megabytes / 1024.0
//
//        return when {
//            gigabytes >= 1 -> String.format("%.2f GB", gigabytes)
//            megabytes >= 1 -> String.format("%.2f MB", megabytes)
//            kilobytes >= 1 -> String.format("%.2f KB", kilobytes)
//            else -> String.format("%d Bytes", sizeInBytes)
//        }
//    }
//
//    private fun retrieveData() {
//        database.child("dwellingAttributes").addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // Handle data snapshot
//                val data = dataSnapshot.value
//                // Call methods to export the data
//                exportDataAsCsv(data)
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Handle error
//            }
//        })
//    }
//
//    private fun exportDataAsCsv(data: Any?) {
//        val csvBuilder = StringBuilder()
//
//        // Add CSV header
//        csvBuilder.append("key,homeType,section8,oaklandHousing,numPeople,squareFootage,date,streetIntersection,buildingAge\n")
//
//        if (data is Map<*, *>) {
//            for ((key, value) in data) {
//                if (value is Map<*, *>) {
//                    val homeType = value["homeType"] ?: ""
//                    val section8 = value["section8"] ?: ""
//                    val oaklandHousing = value["oaklandHousing"] ?: ""
//                    val numPeople = value["numPeople"] ?: ""
//                    val squareFootage = value["squareFootage"] ?: ""
//                    val date = value["date"] ?: ""
//                    val streetIntersection = value["streetIntersection"] ?: ""
//                    val buildingAge = value["buildingAge"] ?: ""
//                    csvBuilder.append("$key,$homeType,$section8,$oaklandHousing,$numPeople,$squareFootage,$date,$streetIntersection,$buildingAge\n")
//                }
//            }
//        }
//
//        val file = saveToFile("data.csv", csvBuilder.toString())
//        sendEmailWithAttachment(file)
//    }
//
//    private fun saveToFile(fileName: String, content: String): File {
//        val file = File(getExternalFilesDir(null), fileName)
//        file.writeText(content)
//        return file
//    }
//
//    private fun sendEmailWithAttachment(file: File) {
//        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
//        val intent = Intent(Intent.ACTION_SEND).apply {
//            type = "text/csv"
//            putExtra(Intent.EXTRA_EMAIL, arrayOf("your_email@example.com")) // Replace with your email
//            putExtra(Intent.EXTRA_SUBJECT, "Firebase Data Export")
//            putExtra(Intent.EXTRA_TEXT, "Please find the attached CSV file with the exported data.")
//            putExtra(Intent.EXTRA_STREAM, uri)
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//        startActivity(Intent.createChooser(intent, "Send Email"))
//    }
//}
