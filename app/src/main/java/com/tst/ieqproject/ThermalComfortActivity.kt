package com.tst.ieqproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tst.ieqproject.utils.FirebaseUtils
import com.tst.ieqproject.utils.ScoreUtils

class ThermalComfortActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var thermalComfortScoreTextView: TextView
    private lateinit var ieqScoreTextView: TextView
    private lateinit var database: DatabaseReference
    private var thermalComfortScore: Double = 0.0
    private lateinit var surveyIdTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thermal_comfort)

        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        surveyIdTextView = findViewById(R.id.surveyIdTextView)

        val isPublicSurvey = false // Set this based on your logic

        // Use a callback to ensure the survey ID is ready
        FirebaseUtils.generateAndSaveSurveyId(this, isPublicSurvey) { surveyId ->
            // This code runs after the survey ID is generated
            surveyIdTextView.text = "Survey ID: $surveyId"
            Log.d("DwellingAttributesActivity", "Survey ID displayed: $surveyId")
        }
        database = FirebaseDatabase.getInstance().reference

        thermalComfortScoreTextView = findViewById(R.id.thermalComfortScoreTextView)
        ieqScoreTextView = findViewById(R.id.ieqScoreTextView)

        // Restore saved data
        restoreData()
        setupThermalComfortListener()

        // Listeners for real-time Thermal Comfort score update
        val indoorTempEditText: EditText = findViewById(R.id.indoorTempEditText)
        val outdoorTempEditText: EditText = findViewById(R.id.outdoorTempEditText)
        val openInstructionsLink = findViewById<TextView>(R.id.openInstructionsStoreLink)
        val content = "IEQ Survey Instructions"
        val spannableString = SpannableString(content)
        spannableString.setSpan(UnderlineSpan(), 0, content.length, 0)
        openInstructionsLink.text = spannableString

// If you want to set it as a clickable link, you can use:
        openInstructionsLink.setOnClickListener {
            val url = "https://drive.google.com/file/d/1XI4uJaBIzbrHDUG1hekpgHTL1s_HHA9A/view"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        val listener = TextWatcherAdapter { updateThermalComfortScore() }

        indoorTempEditText.addTextChangedListener(listener)
        outdoorTempEditText.addTextChangedListener(listener)

        // Setup navigation buttons
        setupNavigationButtons()
    }

    override fun onResume() {
        super.onResume()
        updateThermalComfortScore()
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
        ScoreUtils.updateIEQScore(this, sharedPreferences, ieqScoreTextView)
    }

    private fun saveDataLocally() {
        val indoorTemp = findViewById<EditText>(R.id.indoorTempEditText).text.toString()
        val outdoorTemp = findViewById<EditText>(R.id.outdoorTempEditText).text.toString()
        val editor = sharedPreferences.edit()
        editor.putString("indoorTemperature", indoorTemp)
        editor.putString("outdoorTemperature", outdoorTemp)
        editor.apply()

        // Log the saved values
        Log.d("ThermalComfortActivity", "Saved indoorTemperature: $indoorTemp")
        Log.d("ThermalComfortActivity", "Saved outdoorTemperature: $outdoorTemp")
    }

    private fun restoreData() {
        val indoorTemp = sharedPreferences.getString("indoorTemperature", "")
        val outdoorTemp = sharedPreferences.getString("outdoorTemperature", "")
        findViewById<EditText>(R.id.indoorTempEditText).setText(indoorTemp)
        findViewById<EditText>(R.id.outdoorTempEditText).setText(outdoorTemp)

        // Log the restored values
        Log.d("ThermalComfortActivity", "Restored indoorTemperature: $indoorTemp")
        Log.d("ThermalComfortActivity", "Restored outdoorTemperature: $outdoorTemp")

        updateThermalComfortScore()
    }
    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit Survey")
        builder.setMessage("Are you sure you want to exit the survey? Your progress will not be saved.")

        builder.setPositiveButton("Yes") { dialog, which ->
            // Clear all data before exiting
            clearAllData()

            // Navigate back to the main screen
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun clearAllData() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        restoreData()
    }


    private fun setupNavigationButtons() {
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
        val exitButton: Button = findViewById(R.id.exitButton)
        exitButton.setOnClickListener {
            showExitConfirmationDialog()
        }
    }
}
