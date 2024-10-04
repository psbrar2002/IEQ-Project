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

class IAQActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var iaqScoreTextView: TextView
    private lateinit var ieqScoreTextView: TextView
    private lateinit var database: DatabaseReference
    private var iaqScore: Double = 0.0
    private lateinit var surveyIdTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iaq)

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

        // Initialize UI components for IAQ Attributes
        setupSpinners()

        iaqScoreTextView = findViewById(R.id.iaqScoreTextView)
        ieqScoreTextView = findViewById(R.id.ieqScoreTextView)

        // Restore saved data
        restoreData()
        setupIAQScoreListener()

        // Listeners for real-time IAQ score update
        val kitchenStoveTypeSpinner: Spinner = findViewById(R.id.kitchenStoveTypeSpinner)
        val kitchenStoveFanSpinner: Spinner = findViewById(R.id.kitchenStoveFanSpinner)
        val bathroomVentilationSpinner: Spinner = findViewById(R.id.bathroomVentilationSpinner)
        val moldPresentSpinner: Spinner = findViewById(R.id.moldPresentSpinner)
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
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                updateIAQScore()
                saveDataLocally()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        val playStoreLink = findViewById<TextView>(R.id.openPlayStoreLink)
        playStoreLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=pl.llp.aircasting&hl=en_US&pli=1"))
            startActivity(intent)
        }
        kitchenStoveTypeSpinner.onItemSelectedListener = listener
        kitchenStoveFanSpinner.onItemSelectedListener = listener
        bathroomVentilationSpinner.onItemSelectedListener = listener
        moldPresentSpinner.onItemSelectedListener = listener

        val backButton: Button = findViewById(R.id.backButton)
        val nextButton: Button = findViewById(R.id.nextButton)

        backButton.setOnClickListener {
            saveDataLocally()
            finish()  // Navigate back to the previous activity
        }

        nextButton.setOnClickListener {
            saveDataLocally()
            val intent = Intent(this, ThermalComfortActivity::class.java)
            startActivity(intent)
        }
        val exitButton: ImageButton = findViewById(R.id.exitButtonWithIcon)
        exitButton.setOnClickListener {
            showExitConfirmationDialog()
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

    override fun onResume() {
        super.onResume()
        updateIAQScore()
    }

    private fun setupSpinners() {
        val kitchenStoveTypeSpinner: Spinner = findViewById(R.id.kitchenStoveTypeSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.kitchen_stove_type_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            kitchenStoveTypeSpinner.adapter = adapter
        }

        val kitchenStoveFanSpinner: Spinner = findViewById(R.id.kitchenStoveFanSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.kitchen_stove_fan_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            kitchenStoveFanSpinner.adapter = adapter
        }

        val bathroomVentilationSpinner: Spinner = findViewById(R.id.bathroomVentilationSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.bathroom_ventilation_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            bathroomVentilationSpinner.adapter = adapter
        }

        val moldPresentSpinner: Spinner = findViewById(R.id.moldPresentSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.yes_no_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            moldPresentSpinner.adapter = adapter
        }
    }

    private fun setupIAQScoreListener() {
        val livingRoomBeforeCookingEditText = findViewById<EditText>(R.id.livingRoomBeforeCookingEditText)
        livingRoomBeforeCookingEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateIAQScore()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateIAQScore() {
        iaqScore = 0.0 // Initialize score to 0

        val kitchenStoveType = findViewById<Spinner>(R.id.kitchenStoveTypeSpinner).selectedItem.toString()
        val kitchenStoveFan = findViewById<Spinner>(R.id.kitchenStoveFanSpinner).selectedItem.toString()
        val bathroomVentilation = findViewById<Spinner>(R.id.bathroomVentilationSpinner).selectedItem.toString()
        val livingRoomBeforeCooking = findViewById<EditText>(R.id.livingRoomBeforeCookingEditText).text.toString().toDoubleOrNull()

        if (kitchenStoveType.isNotBlank()) {
            iaqScore += when (kitchenStoveType) {
                "Electric Stove", "Induction" -> 2.0
                else -> 0.0
            }
        }

        if (kitchenStoveFan.isNotBlank()) {
            iaqScore += when (kitchenStoveFan) {
                "Exhaust Fan that blows to the outdoors" -> 2.0
                else -> 0.0
            }
        }

        if (bathroomVentilation.isNotBlank()) {
            iaqScore += when (bathroomVentilation) {
                "Exhaust Fan" -> 2.0
                else -> 0.0
            }
        }

        // Only score if the input is not null and valid
        if (livingRoomBeforeCooking != null && livingRoomBeforeCooking <= 12) {
            iaqScore += 4.0
        }

        // Display the IAQ Score
        iaqScoreTextView.text = "IAQ Score: $iaqScore"
        sharedPreferences.edit().putFloat("iaqScore", iaqScore.toFloat()).apply()
        ScoreUtils.updateIEQScore(this, sharedPreferences, ieqScoreTextView)
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putInt("kitchenStoveTypePosition", findViewById<Spinner>(R.id.kitchenStoveTypeSpinner).selectedItemPosition)
        editor.putInt("kitchenStoveFanPosition", findViewById<Spinner>(R.id.kitchenStoveFanSpinner).selectedItemPosition)
        editor.putInt("bathroomVentilationPosition", findViewById<Spinner>(R.id.bathroomVentilationSpinner).selectedItemPosition)
        editor.putInt("moldPresentPosition", findViewById<Spinner>(R.id.moldPresentSpinner).selectedItemPosition)
        editor.putString("kitchenStoveType", findViewById<Spinner>(R.id.kitchenStoveTypeSpinner).selectedItem.toString())
        editor.putString("kitchenStoveFan", findViewById<Spinner>(R.id.kitchenStoveFanSpinner).selectedItem.toString())
        editor.putString("bathroomVentilation", findViewById<Spinner>(R.id.bathroomVentilationSpinner).selectedItem.toString())
        editor.putString("moldPresent", findViewById<Spinner>(R.id.moldPresentSpinner).selectedItem.toString())
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
        findViewById<Spinner>(R.id.bathroomVentilationSpinner).setSelection(sharedPreferences.getInt("bathroomVentilationPosition", 0))
        findViewById<Spinner>(R.id.moldPresentSpinner).setSelection(sharedPreferences.getInt("moldPresentPosition", 0))
        findViewById<EditText>(R.id.livingRoomBeforeCookingEditText).setText(sharedPreferences.getString("livingRoomBeforeCooking", ""))
        findViewById<EditText>(R.id.livingRoomAfterCookingEditText).setText(sharedPreferences.getString("livingRoomAfterCooking", ""))
        findViewById<EditText>(R.id.livingRoomHumidityEditText).setText(sharedPreferences.getString("livingRoomHumidity", ""))
        findViewById<EditText>(R.id.outdoorPM25EditText).setText(sharedPreferences.getString("outdoorPM25", ""))
        findViewById<EditText>(R.id.outdoorHumidityEditText).setText(sharedPreferences.getString("outdoorHumidity", ""))
        updateIAQScore()
    }
}
