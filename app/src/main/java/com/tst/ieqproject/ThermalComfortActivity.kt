package com.tst.ieqproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ThermalComfortActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var thermalComfortScoreTextView: TextView
    private lateinit var database: DatabaseReference
    private var thermalComfortScore: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thermal_comfort)

        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference

        thermalComfortScoreTextView = findViewById(R.id.thermalComfortScoreTextView)

        // Restore saved data
        restoreData()
        setupThermalComfortListener()

        // Listeners for real-time Thermal Comfort score update
        val indoorTempEditText: EditText = findViewById(R.id.indoorTempEditText)
        val outdoorTempEditText: EditText = findViewById(R.id.outdoorTempEditText)

        val listener = object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                updateThermalComfortScore()
                saveDataLocally()
                return false
            }
        }

        indoorTempEditText.setOnEditorActionListener(listener)
        outdoorTempEditText.setOnEditorActionListener(listener)

        val backButton: Button = findViewById(R.id.backButton)
        val nextButton: Button = findViewById(R.id.nextButton)


        backButton.setOnClickListener {
            saveDataLocally()
            finish()  // Navigate back to the previous activity
        }

        nextButton.setOnClickListener {
            saveDataLocally()
            val intent = Intent(this, AcousticComfortActivity::class.java)
            startActivity(intent)
        }


    }

    private fun setupThermalComfortListener() {
        val indoorTempEditText = findViewById<EditText>(R.id.indoorTempEditText)
        indoorTempEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateThermalComfortScore()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateThermalComfortScore() {
        var thermalComfortScore = 0.0

        val indoorTemp = findViewById<EditText>(R.id.indoorTempEditText).text.toString().toDoubleOrNull() ?: 0.0
        if (indoorTemp in 68.0..78.0) {
            thermalComfortScore = 10.0
        }

        // Display the Thermal Comfort Score
        thermalComfortScoreTextView.text = "Thermal Comfort Score: $thermalComfortScore"
        sharedPreferences.edit().putFloat("thermalComfortScore", thermalComfortScore.toFloat()).apply()
    }


    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putString("indoorTemp", findViewById<EditText>(R.id.indoorTempEditText).text.toString())
        editor.putString("outdoorTemp", findViewById<EditText>(R.id.outdoorTempEditText).text.toString())
        editor.apply()
    }

    private fun restoreData() {
        findViewById<EditText>(R.id.indoorTempEditText).setText(sharedPreferences.getString("indoorTemp", ""))
        findViewById<EditText>(R.id.outdoorTempEditText).setText(sharedPreferences.getString("outdoorTemp", ""))
        updateThermalComfortScore()
    }

//    private fun submitDataToFirebase() {
//        // Get Thermal Comfort data from SharedPreferences
//        val indoorTemp = sharedPreferences.getString("indoorTemp", "")
//        val outdoorTemp = sharedPreferences.getString("outdoorTemp", "")
//        val thermalComfortScore = sharedPreferences.getFloat("thermalComfortScore", 0f)
//
//        val thermalComfortAttributes = ThermalComfortAttributes(
//            indoorTemp ?: "",
//            outdoorTemp ?: "",
//            thermalComfortScore.toDouble()
//        )
//
//        database.child("thermalComfortAttributes").push().setValue(thermalComfortAttributes)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Failed to submit data", Toast.LENGTH_SHORT).show()
//            }
//    }
}
