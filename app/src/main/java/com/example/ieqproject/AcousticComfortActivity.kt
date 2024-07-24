package com.example.ieqproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AcousticComfortActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var acousticComfortScoreTextView: TextView
    private lateinit var ieqScoreTextView: TextView
    private lateinit var database: DatabaseReference
    private var acousticComfortScore: Double = 0.0
    private var iaqScore: Double = 0.0
    private var hvacScore: Double = 0.0
    private var ieqScore: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acoustic_comfort)

        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference

        acousticComfortScoreTextView = findViewById(R.id.acousticComfortScoreTextView)
        ieqScoreTextView = findViewById(R.id.ieqScoreTextView)

        // Restore saved data
        restoreData()

        // Listeners for real-time Acoustic Comfort score update
        setupListeners()

        // Setup navigation and submit buttons
        setupNavigationButtons()
    }

    private fun setupListeners() {
        val indoorDecibelEditText: EditText = findViewById(R.id.indoorDecibelEditText)
        val outdoorDecibelEditText: EditText = findViewById(R.id.outdoorDecibelEditText)
        val indoorNoiseSourcesEditText: EditText = findViewById(R.id.indoorNoiseSourcesEditText)
        val outdoorNoiseSourcesEditText: EditText = findViewById(R.id.outdoorNoiseSourcesEditText)

        val listener = TextView.OnEditorActionListener { _, _, _ ->
            updateAcousticComfortScore()
            saveDataLocally()
            false
        }

        indoorDecibelEditText.setOnEditorActionListener(listener)
        outdoorDecibelEditText.setOnEditorActionListener(listener)
        indoorNoiseSourcesEditText.setOnEditorActionListener(listener)
        outdoorNoiseSourcesEditText.setOnEditorActionListener(listener)
    }

    private fun updateAcousticComfortScore() {
        val indoorDecibel = findViewById<EditText>(R.id.indoorDecibelEditText).text.toString().toDoubleOrNull() ?: 0.0
        val outdoorDecibel = findViewById<EditText>(R.id.outdoorDecibelEditText).text.toString().toDoubleOrNull() ?: 0.0

        // Example logic: average of indoor and outdoor decibel readings
        acousticComfortScore = (indoorDecibel + outdoorDecibel) / 2

        // Display the Acoustic Comfort Score
        acousticComfortScoreTextView.text = "Acoustic Comfort Score: $acousticComfortScore"
        sharedPreferences.edit().putFloat("acousticComfortScore", acousticComfortScore.toFloat()).apply()

        updateIEQScore()
    }

    private fun updateIEQScore() {
        // Retrieve HVAC and IAQ scores
        hvacScore = sharedPreferences.getFloat("hvacScore", 0f).toDouble()
        iaqScore = sharedPreferences.getFloat("iaqScore", 0f).toDouble()

        val totalScore = hvacScore + iaqScore + acousticComfortScore
        ieqScore = (totalScore / 40) * 100

        ieqScoreTextView.text = "IEQ Score: %.2f%%".format(ieqScore)
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putString("indoorDecibel", findViewById<EditText>(R.id.indoorDecibelEditText).text.toString())
        editor.putString("outdoorDecibel", findViewById<EditText>(R.id.outdoorDecibelEditText).text.toString())
        editor.putString("indoorNoiseSources", findViewById<EditText>(R.id.indoorNoiseSourcesEditText).text.toString())
        editor.putString("outdoorNoiseSources", findViewById<EditText>(R.id.outdoorNoiseSourcesEditText).text.toString())
        editor.putFloat("ieqScore", ieqScore.toFloat())
        editor.apply()
    }

    private fun restoreData() {
        findViewById<EditText>(R.id.indoorDecibelEditText).setText(sharedPreferences.getString("indoorDecibel", ""))
        findViewById<EditText>(R.id.outdoorDecibelEditText).setText(sharedPreferences.getString("outdoorDecibel", ""))
        findViewById<EditText>(R.id.indoorNoiseSourcesEditText).setText(sharedPreferences.getString("indoorNoiseSources", ""))
        findViewById<EditText>(R.id.outdoorNoiseSourcesEditText).setText(sharedPreferences.getString("outdoorNoiseSources", ""))
        updateAcousticComfortScore()
    }

    private fun setupNavigationButtons() {
        val backButton: Button = findViewById(R.id.backButton)
        val nextButton: Button = findViewById(R.id.nextButton)
        val submitButton: Button = findViewById(R.id.submitButton)

        backButton.setOnClickListener {
            saveDataLocally()
            finish()  // Navigate back to the previous activity
        }

        nextButton.setOnClickListener {
            saveDataLocally()
            val intent = Intent(this, DemographicActivity::class.java)
            startActivity(intent)
        }

        submitButton.setOnClickListener {
            saveDataLocally()
            FirebaseUtils.submitDataToFirebase(this, sharedPreferences)
        }
    }

//    private fun submitDataToFirebase() {
//        // Retrieve data from SharedPreferences
//        val indoorDecibel = sharedPreferences.getString("indoorDecibel", "")
//        val outdoorDecibel = sharedPreferences.getString("outdoorDecibel", "")
//        val indoorNoiseSources = sharedPreferences.getString("indoorNoiseSources", "")
//        val outdoorNoiseSources = sharedPreferences.getString("outdoorNoiseSources", "")
//        val acousticComfortScore = sharedPreferences.getFloat("acousticComfortScore", 0f).toDouble()
//        val hvacScore = sharedPreferences.getFloat("hvacScore", 0f).toDouble()
//        val iaqScore = sharedPreferences.getFloat("iaqScore", 0f).toDouble()
//        val ieqScore = sharedPreferences.getFloat("ieqScore", 0f).toDouble()
//
//        // Create a data class or map to structure the data
//        val surveyData = mapOf(
//            "indoorDecibel" to indoorDecibel,
//            "outdoorDecibel" to outdoorDecibel,
//            "indoorNoiseSources" to indoorNoiseSources,
//            "outdoorNoiseSources" to outdoorNoiseSources,
//            "acousticComfortScore" to acousticComfortScore,
//            "hvacScore" to hvacScore,
//            "iaqScore" to iaqScore,
//            "ieqScore" to ieqScore
//        )
//
//        // Push data to Firebase under a single reference
//        database.child("surveyData").push().setValue(surveyData)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Failed to submit data", Toast.LENGTH_SHORT).show()
//            }
//    }

}