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
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tst.ieqproject.utils.FirebaseUtils
import com.tst.ieqproject.utils.ScoreUtils

class HVACActivity : AppCompatActivity() {

    private lateinit var hvacScoreTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var ieqScoreTextView: TextView
    private lateinit var surveyIdTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hvac)

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

        // Initialize UI components for HVAC Attributes
        setupSpinners()

        // Initialize the EditTexts
        val additionalAppliancesEditText: EditText = findViewById(R.id.additionalAppliancesEditText)
        hvacScoreTextView = findViewById(R.id.hvacScoreTextView)
        ieqScoreTextView = findViewById(R.id.ieqScoreTextView)

        // Restore saved data
        restoreData()
        setupListeners()

        // Initialize the navigation buttons
        val backButton: Button = findViewById(R.id.backButton)
        val nextButton: Button = findViewById(R.id.nextButton)

        backButton.setOnClickListener {
            saveDataLocally()
            finish()  // Navigate back to the previous activity
        }

        nextButton.setOnClickListener {
            saveDataLocally()
            val intent = Intent(this, IAQActivity::class.java)
            startActivity(intent)
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
        // Remove all keys except for the FAILED_SUBMISSIONS key.
        val editor = sharedPreferences.edit()
        for (key in sharedPreferences.all.keys) {
            if (key != "FAILED_SUBMISSIONS") {
                editor.remove(key)
            }
        }
        editor.apply()
        restoreData() // If restoreData() is needed to reinitialize any UI data.
    }

    override fun onResume() {
        super.onResume()
        updateIEQScore() // Update the IEQ score whenever the activity is resumed
    }

    private fun setupSpinners() {
        val hasAirConditioningSpinner: Spinner = findViewById(R.id.hasAirConditioningSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.yes_no_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            hasAirConditioningSpinner.adapter = adapter
        }

        val airConditioningWorksSpinner: Spinner = findViewById(R.id.airConditioningWorksSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.yes_no_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            airConditioningWorksSpinner.adapter = adapter
        }

        val airConditioningTypeSpinner: Spinner = findViewById(R.id.airConditioningTypeSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.air_conditioning_type_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            airConditioningTypeSpinner.adapter = adapter
        }

        val heatingTypeSpinner: Spinner = findViewById(R.id.heatingTypeSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.heating_type_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            heatingTypeSpinner.adapter = adapter
        }

        val hasPortableAirFilterSpinner: Spinner = findViewById(R.id.hasPortableAirFilterSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.yes_no_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            hasPortableAirFilterSpinner.adapter = adapter
        }
    }

    private fun setupListeners() {
        val hasAirConditioningSpinner: Spinner = findViewById(R.id.hasAirConditioningSpinner)
        val airConditioningWorksSpinner: Spinner = findViewById(R.id.airConditioningWorksSpinner)
        val airConditioningTypeSpinner: Spinner = findViewById(R.id.airConditioningTypeSpinner)
        val airConditioningQuestionsLayout: LinearLayout = findViewById(R.id.airConditioningQuestionsLayout) // The layout for conditional questions
        val heatingTypeSpinner: Spinner = findViewById(R.id.heatingTypeSpinner)
        val hasPortableAirFilterSpinner: Spinner = findViewById(R.id.hasPortableAirFilterSpinner)
        val additionalAppliancesEditText: EditText = findViewById(R.id.additionalAppliancesEditText)

        val spinnerListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                updateHVACScore()
                saveDataLocally()
                updateIEQScore()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Listener for the 'Is there Air Conditioning?' spinner
        hasAirConditioningSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedOption = hasAirConditioningSpinner.selectedItem.toString()

                // Show or hide air conditioning-related questions based on selection
                if (selectedOption == "Yes") {
                    airConditioningQuestionsLayout.visibility = View.VISIBLE
                } else {
                    airConditioningQuestionsLayout.visibility = View.GONE
                }

                saveDataLocally()
                updateHVACScore()
                updateIEQScore()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        airConditioningWorksSpinner.onItemSelectedListener = spinnerListener
        airConditioningTypeSpinner.onItemSelectedListener = spinnerListener
        heatingTypeSpinner.onItemSelectedListener = spinnerListener
        hasPortableAirFilterSpinner.onItemSelectedListener = spinnerListener
        additionalAppliancesEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                saveDataLocally()
                updateHVACScore()
                updateIEQScore()
            }
            override fun afterTextChanged(s: Editable?) {}
        })



//        hasAirConditioningSpinner.onItemSelectedListener = spinnerListener
//        airConditioningWorksSpinner.onItemSelectedListener = spinnerListener
//        airConditioningTypeSpinner.onItemSelectedListener = spinnerListener
//        heatingTypeSpinner.onItemSelectedListener = spinnerListener
//        hasPortableAirFilterSpinner.onItemSelectedListener = spinnerListener
//        additionalAppliancesEditText.addTextChangedListener(textWatcher)
    }

    private fun updateHVACScore() {
        val hasAirConditioning = findViewById<Spinner>(R.id.hasAirConditioningSpinner).selectedItem.toString() == "Yes"
        val airConditioningWorks = if (hasAirConditioning) findViewById<Spinner>(R.id.airConditioningWorksSpinner).selectedItem.toString() == "Yes" else null
        val heatingType = findViewById<Spinner>(R.id.heatingTypeSpinner).selectedItem.toString()

        // Calculate HVAC Score
        val hvacScore = calculateHVACScore(airConditioningWorks, heatingType)

        // Display the HVAC Score
        hvacScoreTextView.text = "HVAC Score: $hvacScore"
        sharedPreferences.edit().putFloat("hvacScore", hvacScore.toFloat()).apply()
    }

    private fun calculateHVACScore(airConditioningWorks: Boolean?, heatingType: String): Double {
        var score = 0.0

        if (airConditioningWorks == true) {
            score += 5.0
        }

        when (heatingType) {
            "Gravity Heater on wall, baseboard, or in the floor. Gets hot but does not blow air" -> score += 2.5
            "Central Heat with filter warm air blows out vents in wall or ceiling and has a filter to clean the air" -> score += 5.0
            "Central heat WITHOUT Filter Warm air blows out vents in wall or ceiling and does not have a filter to clean the air" -> score += 2.5
            "Radiator Steam through pipes" -> score += 2.5
        }

        return score
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putString("additionalAppliances", findViewById<EditText>(R.id.additionalAppliancesEditText).text.toString())
        editor.putInt("hasAirConditioningPosition", findViewById<Spinner>(R.id.hasAirConditioningSpinner).selectedItemPosition)
        editor.putInt("airConditioningWorksPosition", findViewById<Spinner>(R.id.airConditioningWorksSpinner).selectedItemPosition)
        editor.putInt("airConditioningTypePosition", findViewById<Spinner>(R.id.airConditioningTypeSpinner).selectedItemPosition)
        editor.putInt("heatingTypePosition", findViewById<Spinner>(R.id.heatingTypeSpinner).selectedItemPosition)
        editor.putInt("hasPortableAirFilterPosition", findViewById<Spinner>(R.id.hasPortableAirFilterSpinner).selectedItemPosition)
        editor.putString("airConditioningType", findViewById<Spinner>(R.id.airConditioningTypeSpinner).selectedItem.toString())
        editor.putString("heatingType", findViewById<Spinner>(R.id.heatingTypeSpinner).selectedItem.toString())
        editor.putBoolean("hasAirConditioning", findViewById<Spinner>(R.id.hasAirConditioningSpinner).selectedItem.toString() == "Yes")
        editor.putBoolean("airConditioningWorks", findViewById<Spinner>(R.id.airConditioningWorksSpinner).selectedItem.toString() == "Yes")
        editor.putBoolean("hasPortableAirFilter", findViewById<Spinner>(R.id.hasPortableAirFilterSpinner).selectedItem.toString() == "Yes")
        editor.apply()
    }

    private fun restoreData() {
        findViewById<EditText>(R.id.additionalAppliancesEditText).setText(sharedPreferences.getString("additionalAppliances", ""))
        findViewById<Spinner>(R.id.hasAirConditioningSpinner).setSelection(sharedPreferences.getInt("hasAirConditioningPosition", 0))
        findViewById<Spinner>(R.id.airConditioningWorksSpinner).setSelection(sharedPreferences.getInt("airConditioningWorksPosition", 0))
        findViewById<Spinner>(R.id.airConditioningTypeSpinner).setSelection(sharedPreferences.getInt("airConditioningTypePosition", 0))
        findViewById<Spinner>(R.id.heatingTypeSpinner).setSelection(sharedPreferences.getInt("heatingTypePosition", 0))
        findViewById<Spinner>(R.id.hasPortableAirFilterSpinner).setSelection(sharedPreferences.getInt("hasPortableAirFilterPosition", 0))
        updateHVACScore()
    }

    private fun updateIEQScore() {
        // Update the IEQ score using ScoreUtils
        ScoreUtils.updateIEQScore(this, sharedPreferences, ieqScoreTextView)
    }
}
