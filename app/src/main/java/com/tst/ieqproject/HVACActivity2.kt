package com.tst.ieqproject

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tst.ieqproject.utils.FirebaseUtils
import com.tst.ieqproject.utils.ScoreUtils2

class HVACActivity2 : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var hvacScoreTextView: TextView
    private lateinit var airConditioningSpinner: Spinner
    private lateinit var typeOfAirConditioningSpinner: Spinner
    private lateinit var heatingTypeSpinner: Spinner
    private lateinit var additionalAppliancesSpinner: Spinner
    private lateinit var portableAirFilterSpinner: Spinner
    private lateinit var ieqScoreTextView2: TextView
    private lateinit var surveyIdTextView: TextView
    private var hvacScore2: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hvac2)

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
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize UI Components
        hvacScoreTextView = findViewById(R.id.hvacScoreTextView)
        airConditioningSpinner = findViewById(R.id.airConditioningSpinner)
        typeOfAirConditioningSpinner = findViewById(R.id.typeOfAirConditioningSpinner)
        heatingTypeSpinner = findViewById(R.id.heatingTypeSpinner)
        additionalAppliancesSpinner = findViewById(R.id.additionalAppliancesSpinner)
        portableAirFilterSpinner = findViewById(R.id.portableAirFilterSpinner)
        ieqScoreTextView2 = findViewById(R.id.ieqScoreTextView2)

        // Setup Spinner Listeners
        airConditioningSpinner.onItemSelectedListener = hvacScoreListener
        typeOfAirConditioningSpinner.onItemSelectedListener = hvacScoreListener
        heatingTypeSpinner.onItemSelectedListener = hvacScoreListener
        additionalAppliancesSpinner.onItemSelectedListener = hvacScoreListener
        portableAirFilterSpinner.onItemSelectedListener = hvacScoreListener

        // Setup Next Button
        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            saveDataLocally()
            // Navigate to the next activity or section
            val intent = Intent(this, IAQActivity2::class.java)
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

        // Update HVAC score initially
        updateHVACScore()
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

    private val hvacScoreListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            updateHVACScore()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            // Do nothing
        }
    }

    private fun updateHVACScore() {
        hvacScore2 = 0.0

        // Add score based on Air Conditioning
        if (airConditioningSpinner.selectedItem.toString() == "Yes") {
            hvacScore2 += 5.0
        }

        // Add score based on Heating Type
        hvacScore2 += when (heatingTypeSpinner.selectedItem.toString()) {
            "No Heat" -> 0.0
            "Gravity Heater on wall, baseboard, or in the floor. Gets hot but does not blow air" -> 2.5
            "Central Heat with Filter Warm air blows out vents in wall or ceiling and has a filter to clean the air" -> 5.0
            "Central Heat WITHOUT Filter Warm air blows out vents in wall or ceiling and does not have a filter to clean the air" -> 2.5
            "Radiator Steam heat through pipes" -> 2.5
            else -> 0.0
        }

        // Update the HVAC score TextView
        hvacScoreTextView.text = "HVAC Score: ${String.format("%.1f", hvacScore2)}"

        // Save the score to SharedPreferences
        sharedPreferences.edit().putFloat("hvacScore2", hvacScore2.toFloat()).apply()

        // Update the IEQ score
        ScoreUtils2.updateIEQScore2(this, sharedPreferences, ieqScoreTextView2)
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putString("airConditioning", airConditioningSpinner.selectedItem.toString())
        editor.putString("typeOfAirConditioning", typeOfAirConditioningSpinner.selectedItem.toString())
        editor.putString("heatingType", heatingTypeSpinner.selectedItem.toString())
        editor.putString("additionalAppliances", additionalAppliancesSpinner.selectedItem.toString())
        editor.putString("portableAirFilter", portableAirFilterSpinner.selectedItem.toString())
        editor.putFloat("hvacScore2", hvacScore2.toFloat())
        editor.apply()
    }

    private fun restoreData() {
        airConditioningSpinner.setSelection(getSpinnerIndex(airConditioningSpinner, sharedPreferences.getString("airConditioning", "")!!))
        typeOfAirConditioningSpinner.setSelection(getSpinnerIndex(typeOfAirConditioningSpinner, sharedPreferences.getString("typeOfAirConditioning", "")!!))
        heatingTypeSpinner.setSelection(getSpinnerIndex(heatingTypeSpinner, sharedPreferences.getString("heatingType", "")!!))
        additionalAppliancesSpinner.setSelection(getSpinnerIndex(additionalAppliancesSpinner, sharedPreferences.getString("additionalAppliances", "")!!))
        portableAirFilterSpinner.setSelection(getSpinnerIndex(portableAirFilterSpinner, sharedPreferences.getString("portableAirFilter", "")!!))
        updateHVACScore()  // Update the score after restoring data
    }

    private fun getSpinnerIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
    }
}
