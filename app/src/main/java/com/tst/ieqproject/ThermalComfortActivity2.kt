package com.tst.ieqproject

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.tst.ieqproject.utils.FirebaseUtils
import com.tst.ieqproject.utils.ScoreUtils2

class ThermalComfortActivity2 : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var thermalComfortScoreTextView: TextView
    private lateinit var ieqScoreTextView: TextView
    private lateinit var indoorTempEditText: EditText
    private lateinit var outdoorTempEditText: EditText
    private lateinit var surveyIdTextView: TextView
    private var thermalComfortScore2: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thermal_comfort2)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        surveyIdTextView = findViewById(R.id.surveyIdTextView)

        val isPublicSurvey = true // Set this based on your logic

        // Use a callback to ensure the survey ID is ready
        FirebaseUtils.generateAndSaveSurveyId(this, isPublicSurvey) { surveyId ->
            // This code runs after the survey ID is generated
            surveyIdTextView.text = "Survey ID: $surveyId"
            Log.d("DwellingAttributesActivity", "Survey ID displayed: $surveyId")
        }
        // Initialize UI Components
        thermalComfortScoreTextView = findViewById(R.id.thermalComfortScoreTextView2)
        ieqScoreTextView = findViewById(R.id.ieqScoreTextView2)
        indoorTempEditText = findViewById(R.id.indoorTempEditText2)
        outdoorTempEditText = findViewById(R.id.outdoorTempEditText2)

        // Setup TextWatchers for the EditTexts
        indoorTempEditText.addTextChangedListener(tempTextWatcher)
        outdoorTempEditText.addTextChangedListener(tempTextWatcher)

        // Setup Buttons
        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            saveDataLocally()
            // Navigate to the next activity
            val intent = Intent(this, AcousticComfortActivity2::class.java)
            startActivity(intent)
        }

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            saveDataLocally()
            finish()  // Navigate back to the previous activity
        }

        val exitButton: Button = findViewById(R.id.exitButton)
        exitButton.setOnClickListener {
            showExitConfirmationDialog()
        }
        val openInstructionsLink = findViewById<TextView>(R.id.openInstructionsStoreLink)
        val content = "IEQ Survey Instructions"
        val spannableString = SpannableString(content)
        spannableString.setSpan(UnderlineSpan(), 0, content.length, 0)
        openInstructionsLink.text = spannableString

// If you want to set it as a clickable link, you can use:
        openInstructionsLink.setOnClickListener {
            val url = "https://drive.google.com/file/d/1PTKGWSZ3O_qd8TFKXs3WwYSBKgdVfHx0/view"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        // Restore Data if available
        restoreData()

        // Update Thermal Comfort and IEQ scores initially
        updateThermalComfortScore()
    }

    private val tempTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateThermalComfortScore()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun updateThermalComfortScore() {
        thermalComfortScore2 = 0.0

        // Calculate Thermal Comfort Score based on indoor temperature
        val indoorTemp = indoorTempEditText.text.toString().toDoubleOrNull()
        if (indoorTemp != null && indoorTemp in 68.0..78.0) {
            thermalComfortScore2 = 10.0
        }

        // Update the Thermal Comfort score TextView
        thermalComfortScoreTextView.text = String.format("Thermal Comfort Score: %.1f", thermalComfortScore2)

        // Save Thermal Comfort Score in SharedPreferences
        sharedPreferences.edit().putFloat("thermalComfortScore2", thermalComfortScore2.toFloat()).apply()

        // Update the IEQ score
        updateIEQScore()
    }

    private fun updateIEQScore() {
        // Use ScoreUtils2 to calculate the total IEQ score based on all relevant categories
        ScoreUtils2.updateIEQScore2(this, sharedPreferences, ieqScoreTextView)
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()

        // Save temperature values as Float to ensure consistent type
        editor.putFloat("indoorTemp2", indoorTempEditText.text.toString().toFloatOrNull() ?: 0f)
        editor.putFloat("outdoorTemp2", outdoorTempEditText.text.toString().toFloatOrNull() ?: 0f)
        editor.putFloat("thermalComfortScore2", thermalComfortScore2.toFloat())
        editor.apply()

        updateIEQScore()
    }

    private fun restoreData() {
        // Retrieve temperature data as Float and set it to EditTexts as String
        val indoorTemp = sharedPreferences.getFloat("indoorTemp2", 0f)
        val outdoorTemp = sharedPreferences.getFloat("outdoorTemp2", 0f)

        // Set the retrieved temperature values in the EditTexts
        indoorTempEditText.setText(if (indoorTemp == 0f) "" else indoorTemp.toString())
        outdoorTempEditText.setText(if (outdoorTemp == 0f) "" else outdoorTemp.toString())

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
}
