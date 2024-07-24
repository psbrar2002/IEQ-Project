package com.example.ieqproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class IAQActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var iaqScoreTextView: TextView
    private lateinit var database: DatabaseReference
    private var iaqScore: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iaq)

        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference

        // Initialize UI components for IAQ Attributes
        IAQAttributes.setupSpinners(this, findViewById(android.R.id.content))

        iaqScoreTextView = findViewById(R.id.iaqScoreTextView)

        // Restore saved data
        restoreData()

        // Listeners for real-time IAQ score update
        val kitchenStoveTypeSpinner: Spinner = findViewById(R.id.kitchenStoveTypeSpinner)
        val kitchenStoveFanSpinner: Spinner = findViewById(R.id.kitchenStoveFanSpinner)
        val moldPresentSpinner: Spinner = findViewById(R.id.moldPresentSpinner)

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                updateIAQScore()
                saveDataLocally()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        kitchenStoveTypeSpinner.onItemSelectedListener = listener
        kitchenStoveFanSpinner.onItemSelectedListener = listener
        moldPresentSpinner.onItemSelectedListener = listener

        val backButton: Button = findViewById(R.id.backButton)
        val nextButton: Button = findViewById(R.id.nextButton)
        val submitButton: Button = findViewById(R.id.submitButton)

        backButton.setOnClickListener {
            saveDataLocally()
            finish()  // Navigate back to the previous activity
        }

        nextButton.setOnClickListener {
            saveDataLocally()
            val intent = Intent(this, ThermalComfortActivity::class.java)
            startActivity(intent)
        }

        submitButton.setOnClickListener {
            saveDataLocally()
            FirebaseUtils.submitDataToFirebase(this, sharedPreferences)
        }
    }

    private fun updateIAQScore() {
        iaqScore = 0.0

        val kitchenStoveType = findViewById<Spinner>(R.id.kitchenStoveTypeSpinner).selectedItem.toString()
        val kitchenStoveFan = findViewById<Spinner>(R.id.kitchenStoveFanSpinner).selectedItem.toString()
        val livingRoomBeforeCooking = findViewById<EditText>(R.id.livingRoomBeforeCookingEditText).text.toString().toDoubleOrNull() ?: 0.0

        // Calculate IAQ Score based on kitchen stove type and fan
        iaqScore += when (kitchenStoveType) {
            "Electric Stove", "Induction" -> 2.0
            else -> 0.0
        }

        iaqScore += when (kitchenStoveFan) {
            "Exhaust Fan that blows to the outdoors" -> 2.0
            else -> 0.0
        }

        // Scoring logic for PM 2.5 Reading Before Cooking
        if (livingRoomBeforeCooking <= 12) {
            iaqScore += 4.0
        }

        // Display the IAQ Score
        iaqScoreTextView.text = "IAQ Score: $iaqScore"
        sharedPreferences.edit().putFloat("iaqScore", iaqScore.toFloat()).apply()
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putInt("kitchenStoveTypePosition", findViewById<Spinner>(R.id.kitchenStoveTypeSpinner).selectedItemPosition)
        editor.putInt("kitchenStoveFanPosition", findViewById<Spinner>(R.id.kitchenStoveFanSpinner).selectedItemPosition)
        editor.putInt("moldPresentPosition", findViewById<Spinner>(R.id.moldPresentSpinner).selectedItemPosition)
        editor.putString("livingRoomBeforeCooking", findViewById<EditText>(R.id.livingRoomBeforeCookingEditText).text.toString())
        editor.putString("livingRoomAfterCooking", findViewById<EditText>(R.id.livingRoomAfterCookingEditText).text.toString())
        editor.putString("livingRoomHumidity", findViewById<EditText>(R.id.livingRoomHumidityEditText).text.toString())
        editor.putString("outdoorPM25", findViewById<EditText>(R.id.outdoorPM25EditText).text.toString())
        editor.putString("outdoorHumidity", findViewById<EditText>(R.id.outdoorHumidityEditText).text.toString())
        editor.apply()
    }

    private fun restoreData() {
        findViewById<Spinner>(R.id.kitchenStoveTypeSpinner).setSelection(sharedPreferences.getInt("kitchenStoveTypePosition", 0))
        findViewById<Spinner>(R.id.kitchenStoveFanSpinner).setSelection(sharedPreferences.getInt("kitchenStoveFanPosition", 0))
        findViewById<Spinner>(R.id.moldPresentSpinner).setSelection(sharedPreferences.getInt("moldPresentPosition", 0))
        findViewById<EditText>(R.id.livingRoomBeforeCookingEditText).setText(sharedPreferences.getString("livingRoomBeforeCooking", ""))
        findViewById<EditText>(R.id.livingRoomAfterCookingEditText).setText(sharedPreferences.getString("livingRoomAfterCooking", ""))
        findViewById<EditText>(R.id.livingRoomHumidityEditText).setText(sharedPreferences.getString("livingRoomHumidity", ""))
        findViewById<EditText>(R.id.outdoorPM25EditText).setText(sharedPreferences.getString("outdoorPM25", ""))
        findViewById<EditText>(R.id.outdoorHumidityEditText).setText(sharedPreferences.getString("outdoorHumidity", ""))
        updateIAQScore()
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
//        // Retrieve IAQ data from SharedPreferences
//        val kitchenStoveType = sharedPreferences.getString("kitchenStoveType", "")
//        val kitchenStoveFan = sharedPreferences.getString("kitchenStoveFan", "")
//        val livingRoomBeforeCooking = sharedPreferences.getString("livingRoomBeforeCooking", "")
//        val livingRoomAfterCooking = sharedPreferences.getString("livingRoomAfterCooking", "")
//        val livingRoomHumidity = sharedPreferences.getString("livingRoomHumidity", "")
//        val outdoorPM25 = sharedPreferences.getString("outdoorPM25", "")
//        val outdoorHumidity = sharedPreferences.getString("outdoorHumidity", "")
//        val moldPresent = sharedPreferences.getString("moldPresent", "")
//
//        // Create an IAQAttributes object
//        val iaqAttributes = IAQAttributes(
//            kitchenStoveType ?: "",
//            kitchenStoveFan ?: "",
//            livingRoomBeforeCooking ?: "",
//            livingRoomAfterCooking ?: "",
//            livingRoomHumidity ?: "",
//            outdoorPM25 ?: "",
//            outdoorHumidity ?: "",
//            moldPresent ?: "",
//            iaqScore
//        )
//
//        // Create a map or data class to structure the survey data
//        val surveyData = mapOf(
//            "indoorDecibel" to indoorDecibel,
//            "outdoorDecibel" to outdoorDecibel,
//            "indoorNoiseSources" to indoorNoiseSources,
//            "outdoorNoiseSources" to outdoorNoiseSources,
//            "acousticComfortScore" to acousticComfortScore,
//            "hvacScore" to hvacScore,
//            "iaqScore" to iaqScore,
//            "ieqScore" to ieqScore,
//            "iaqAttributes" to iaqAttributes
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
