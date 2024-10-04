package com.tst.ieqproject

import android.annotation.SuppressLint
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
import com.tst.ieqproject.utils.FirebaseUtils
import com.tst.ieqproject.utils.ScoreUtils2

class AcousticComfortActivity2 : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var acousticComfortScoreTextView: TextView
    private lateinit var ieqScoreTextView: TextView
    private lateinit var indoorDecibelEditText: EditText
    private lateinit var outdoorDecibelEditText: EditText
    private lateinit var surveyIdTextView: TextView
    private lateinit var indoorNoiseSourcesEditText: EditText
    private lateinit var outdoorNoiseSourcesEditText: EditText

    private var acousticComfortScore2: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acoustic_comfort2)

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
        acousticComfortScoreTextView = findViewById(R.id.acousticComfortScoreTextView2)
        ieqScoreTextView = findViewById(R.id.ieqScoreTextView2)
        indoorDecibelEditText = findViewById(R.id.indoorDecibelEditText2)
        outdoorDecibelEditText = findViewById(R.id.outdoorDecibelEditText2)
        indoorNoiseSourcesEditText = findViewById(R.id.indoorNoiseSourcesEditText2)
        outdoorNoiseSourcesEditText = findViewById(R.id.outdoorNoiseSourcesEditText2)
        // Setup TextWatchers for the EditTexts
        indoorDecibelEditText.addTextChangedListener(decibelTextWatcher)
        outdoorDecibelEditText.addTextChangedListener(decibelTextWatcher)

        // Setup Buttons
        val submitButton: Button = findViewById(R.id.submitButton)
        submitButton.setOnClickListener {
            saveDataLocally()
            showSubmitConfirmationDialog()
        }

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            saveDataLocally()
            finish()  // Navigate back to the previous activity
        }

        val exitButton: ImageButton = findViewById(R.id.exitButtonWithIcon)
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

        // Update Acoustic Comfort and IEQ scores initially
        updateAcousticComfortScore()
    }

    private val decibelTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateAcousticComfortScore()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun updateAcousticComfortScore() {
        acousticComfortScore2 = 0.0

        // Calculate Acoustic Comfort Score based on indoor decibel level
        val indoorDecibel = indoorDecibelEditText.text.toString().toDoubleOrNull()
        if (indoorDecibel != null && indoorDecibel < 46.0) {
            acousticComfortScore2 = 10.0
        }

        // Update the Acoustic Comfort score TextView
        acousticComfortScoreTextView.text = String.format("Acoustic Comfort Score: %.1f", acousticComfortScore2)

        // Save Acoustic Comfort Score in SharedPreferences
        sharedPreferences.edit().putFloat("acousticComfortScore2", acousticComfortScore2.toFloat()).apply()

        // Update the IEQ score
        updateIEQScore()
    }

    private fun updateIEQScore() {
        // Use ScoreUtils2 to calculate the total IEQ score based on all relevant categories
        ScoreUtils2.updateIEQScore2(this, sharedPreferences, ieqScoreTextView)
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putString("indoorDecibel2", indoorDecibelEditText.text.toString())
        editor.putString("outdoorDecibel2", outdoorDecibelEditText.text.toString())
        editor.putString("indoorNoiseSources2", indoorNoiseSourcesEditText.text.toString())
        editor.putString("outdoorNoiseSources2", outdoorNoiseSourcesEditText.text.toString())
        editor.putFloat("acousticComfortScore2", acousticComfortScore2.toFloat())
        editor.apply()

        updateIEQScore()
    }

    @SuppressLint("DefaultLocale")
    private fun restoreData() {
        // Retrieve values safely with appropriate type casts
        indoorDecibelEditText.setText(sharedPreferences.getString("indoorDecibel2", "") ?: "")
        outdoorDecibelEditText.setText(sharedPreferences.getString("outdoorDecibel2", "") ?: "")
        indoorNoiseSourcesEditText.setText(sharedPreferences.getString("indoorNoiseSources2", ""))
        outdoorNoiseSourcesEditText.setText(sharedPreferences.getString("outdoorNoiseSources2", ""))
        // If you've stored any float value, retrieve it with getFloat
        val acousticComfortScore = sharedPreferences.getFloat("acousticComfortScore2", 0f)
        acousticComfortScoreTextView.text = String.format("Acoustic Comfort Score: %.1f", acousticComfortScore.toDouble())

        // Ensure IEQ score is updated properly
        updateAcousticComfortScore()
    }

    private fun submitSurveyData() {
        val isPublic = true // Set to 'true' for public surveys, 'false' for private

        // Submit the data to Firebase
        FirebaseUtils.submitSurveyDataToFirebase(this, sharedPreferences, isPublic) { success, identifier ->
            if (success) {
                Toast.makeText(this, "Survey data submitted successfully!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, SubmissionActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("isPublicSurvey", isPublic)  // Pass the survey type
                intent.putExtra("surveyIdentifier", identifier)  // Pass the unique identifier

                startActivity(intent)
            } else {
                Toast.makeText(this, "Failed to submit survey data. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
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

    private fun showSubmitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Submit Survey")
        builder.setMessage("Are you sure you want to submit the survey?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            submitSurveyData() // Handle submission and transition to the submission activity
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}
