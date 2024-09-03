package com.tst.ieqproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.tst.ieqproject.utils.FirebaseUtils
import com.tst.ieqproject.utils.ScoreUtils

class DemographicActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var ieqScoreTextView: TextView
    private lateinit var surveyIdTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demographic)

        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        surveyIdTextView = findViewById(R.id.surveyIdTextView)

        val isPublicSurvey = false // Set this based on your logic

        // Use a callback to ensure the survey ID is ready
        FirebaseUtils.generateAndSaveSurveyId(this, isPublicSurvey) { surveyId ->
            // This code runs after the survey ID is generated
            surveyIdTextView.text = "Survey ID: $surveyId"
            Log.d("DwellingAttributesActivity", "Survey ID displayed: $surveyId")
        }

        // Find the IEQ score TextView
        ieqScoreTextView = findViewById(R.id.ieqScoreTextView)

        // Restore saved data
        restoreData()

        // Update IEQ Score on screen
        updateIEQScore()

        val backButton: Button = findViewById(R.id.backButton)
        val submitButton: Button = findViewById(R.id.submitButton)

        backButton.setOnClickListener {
            saveDataLocally()
            finish()  // Navigate back to the previous activity
        }

        submitButton.setOnClickListener {
            saveDataLocally()
            showSubmitConfirmationDialog()
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
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit Survey")
        builder.setMessage("Are you sure you want to exit the survey? Your progress will not be saved.")

        builder.setPositiveButton("Yes") { dialog, _ ->
            // Clear all data before exiting
            clearAllData()

            // Navigate back to the main screen
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showSubmitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Submit Survey")
        builder.setMessage("Are you sure you want to submit the survey?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            // Submit the data to Firebase
            submitSurveyData()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun submitSurveyData() {
        val isPublic = false // Set to 'true' for public surveys, 'false' for private

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

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("multiracial", findViewById<CheckBox>(R.id.multiracialCheckBox).isChecked)
        editor.putBoolean("americanIndian", findViewById<CheckBox>(R.id.americanIndianCheckBox).isChecked)
        editor.putBoolean("asian", findViewById<CheckBox>(R.id.asianCheckBox).isChecked)
        editor.putBoolean("black", findViewById<CheckBox>(R.id.blackCheckBox).isChecked)
        editor.putBoolean("hispanic", findViewById<CheckBox>(R.id.hispanicCheckBox).isChecked)
        editor.putBoolean("nativeHawaiian", findViewById<CheckBox>(R.id.nativeHawaiianCheckBox).isChecked)
        editor.putBoolean("white", findViewById<CheckBox>(R.id.whiteCheckBox).isChecked)
        editor.putBoolean("other", findViewById<CheckBox>(R.id.otherCheckBox).isChecked)
        editor.putString("otherEthnicity", findViewById<EditText>(R.id.otherEthnicityEditText).text.toString())
        editor.apply()

        // Update the IEQ Score when saving data
        updateIEQScore()
    }

    private fun restoreData() {
        findViewById<CheckBox>(R.id.multiracialCheckBox).isChecked = sharedPreferences.getBoolean("multiracial", false)
        findViewById<CheckBox>(R.id.asianCheckBox).isChecked = sharedPreferences.getBoolean("asian", false)
        findViewById<CheckBox>(R.id.blackCheckBox).isChecked = sharedPreferences.getBoolean("black", false)
        findViewById<CheckBox>(R.id.americanIndianCheckBox).isChecked = sharedPreferences.getBoolean("americanIndian", false)
        findViewById<CheckBox>(R.id.hispanicCheckBox).isChecked = sharedPreferences.getBoolean("hispanic", false)
        findViewById<CheckBox>(R.id.nativeHawaiianCheckBox).isChecked = sharedPreferences.getBoolean("nativeHawaiian", false)
        findViewById<CheckBox>(R.id.whiteCheckBox).isChecked = sharedPreferences.getBoolean("white", false)
        findViewById<CheckBox>(R.id.otherCheckBox).isChecked = sharedPreferences.getBoolean("other", false)
        findViewById<EditText>(R.id.otherEthnicityEditText).setText(sharedPreferences.getString("otherEthnicity", ""))

        // Update the IEQ Score when restoring data
        updateIEQScore()
    }

    private fun clearAllData() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        restoreData()
    }

    private fun updateIEQScore() {
        ScoreUtils.updateIEQScore(this, sharedPreferences, ieqScoreTextView)
    }
}
