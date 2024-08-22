package com.tst.ieqproject

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.tst.ieqproject.utils.ScoreUtils2

class IAQActivity2 : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var iaqScoreTextView: TextView
    private lateinit var ieqScoreTextView: TextView
    private lateinit var kitchenSpinner: Spinner
    private lateinit var kitchenStoveTypeSpinner: Spinner
    private lateinit var kitchenStoveFanSpinner: Spinner
    private lateinit var bathroomSpinner: Spinner
    private lateinit var bathroomVentilationTypeSpinner: Spinner
    private lateinit var moldPresentSpinner: Spinner
    private lateinit var pm25EditText: EditText
    private lateinit var indoorHumidityEditText: EditText
    private lateinit var outdoorPM25EditText: EditText
    private lateinit var outdoorHumidityEditText: EditText

    private var iaqScore2: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iaq2)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)

        // Initialize UI Components
        iaqScoreTextView = findViewById(R.id.iaqScoreTextView2)
        ieqScoreTextView = findViewById(R.id.ieqScoreTextView2)
        kitchenSpinner = findViewById(R.id.kitchenSpinner2)
        kitchenStoveTypeSpinner = findViewById(R.id.kitchenStoveTypeSpinner2)
        kitchenStoveFanSpinner = findViewById(R.id.kitchenStoveFanSpinner2)
        bathroomSpinner = findViewById(R.id.bathroomSpinner2)
        bathroomVentilationTypeSpinner = findViewById(R.id.bathroomVentilationSpinner2)
        moldPresentSpinner = findViewById(R.id.moldPresentSpinner2)
        pm25EditText = findViewById(R.id.pm25BeforeCookingEditText2)
        indoorHumidityEditText = findViewById(R.id.indoorHumidityEditText2)
        outdoorPM25EditText = findViewById(R.id.outdoorPM25EditText2)
        outdoorHumidityEditText = findViewById(R.id.outdoorHumidityEditText2)

        // Setup Spinner Listeners
        kitchenSpinner.onItemSelectedListener = iaqScoreListener
        kitchenStoveTypeSpinner.onItemSelectedListener = iaqScoreListener
        kitchenStoveFanSpinner.onItemSelectedListener = iaqScoreListener
        bathroomSpinner.onItemSelectedListener = iaqScoreListener
        bathroomVentilationTypeSpinner.onItemSelectedListener = iaqScoreListener
        moldPresentSpinner.onItemSelectedListener = iaqScoreListener
        pm25EditText.addTextChangedListener(pm25TextWatcher)

        // Setup Buttons
        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            saveDataLocally()
            // Navigate to the next activity
            val intent = Intent(this, ThermalComfortActivity2::class.java)
            startActivity(intent)
        }

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            saveDataLocally()
            finish()
        }

        val exitButton: Button = findViewById(R.id.exitButton)
        exitButton.setOnClickListener {
            showExitConfirmationDialog()
        }

        // Restore Data if available
        restoreData()

        // Update IAQ and IEQ scores initially
        updateIAQScore()
    }

    private val iaqScoreListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            updateIAQScore()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            // Do nothing
        }
    }

    private val pm25TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateIAQScore()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    override fun onResume() {
        super.onResume()
        updateIAQScore()
    }

    private fun updateIAQScore() {
        // Start with the base IAQ score components that do not depend on the PM 2.5 reading
        val baseScore = 0.0 +
                when (kitchenStoveTypeSpinner.selectedItem.toString()) {
                    "Electric", "Induction" -> 0.5
                    else -> 0.0
                } +
                when (kitchenStoveFanSpinner.selectedItem.toString()) {
                    "Exhaust Fan that blows to the outdoors" -> 0.5
                    else -> 0.0
                } +
                when (bathroomVentilationTypeSpinner.selectedItem.toString()) {
                    "Exhaust Fan" -> 0.5
                    else -> 0.0
                }

        // Initialize iaqScore2 with baseScore
        iaqScore2 = baseScore

        // Conditionally add to the score based on the PM 2.5 value if it's valid
        val pm25Text = pm25EditText.text.toString()
        if (pm25Text.isNotEmpty()) { // Check if the input field is not empty
            pm25Text.toDoubleOrNull()?.let { pm25Value ->
                // Add score based on PM 2.5 value, adding 8.5 if the value is less than or equal to 12
                if (pm25Value <= 12) {
                    iaqScore2 += 8.5
                }
            } ?: run {
                // If the input is not a valid double, you can notify the user or log this issue
                iaqScoreTextView.text = "Invalid PM 2.5 value"
                return // Exit the function if the PM value is invalid
            }
        }

        // Update the IAQ score TextView with the total calculated score
        iaqScoreTextView.text = String.format("IAQ Score: %.1f", iaqScore2)

        // Save the IAQ score in SharedPreferences
        sharedPreferences.edit().putFloat("iaqScore2", iaqScore2.toFloat()).apply()

        // Update the IEQ score
        updateIEQScore()
    }

    private fun updateIEQScore() {
        // Use ScoreUtils2 to calculate the total IEQ score based on all relevant categories
        ScoreUtils2.updateIEQScore2(this, sharedPreferences, ieqScoreTextView)
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putString("isKitchen2", kitchenSpinner.selectedItem.toString())
        editor.putString("kitchenStoveType2", kitchenStoveTypeSpinner.selectedItem.toString())
        editor.putString("kitchenStoveFan2", kitchenStoveFanSpinner.selectedItem.toString())
        editor.putString("isBathroom2", bathroomSpinner.selectedItem.toString())
        editor.putString("bathroomVentilationType2", bathroomVentilationTypeSpinner.selectedItem.toString())
        editor.putString("isMoldPresent2", moldPresentSpinner.selectedItem.toString())
        editor.putString("pm25Value2", pm25EditText.text.toString())
        editor.putString("indoorHumidity2", indoorHumidityEditText.text.toString())
        editor.putString("outdoorPM252", outdoorPM25EditText.text.toString())
        editor.putString("outdoorHumidity2", outdoorHumidityEditText.text.toString())
        editor.putFloat("iaqScore2", iaqScore2.toFloat())
        editor.apply()

        updateIEQScore()
    }

    private fun restoreData() {
        kitchenSpinner.setSelection(getSpinnerIndex(kitchenSpinner, sharedPreferences.getString("isKitchen2", "")!!))
        kitchenStoveTypeSpinner.setSelection(getSpinnerIndex(kitchenStoveTypeSpinner, sharedPreferences.getString("kitchenStoveType2", "")!!))
        kitchenStoveFanSpinner.setSelection(getSpinnerIndex(kitchenStoveFanSpinner, sharedPreferences.getString("kitchenStoveFan2", "")!!))
        bathroomSpinner.setSelection(getSpinnerIndex(bathroomSpinner, sharedPreferences.getString("isBathroom2", "")!!))
        bathroomVentilationTypeSpinner.setSelection(getSpinnerIndex(bathroomVentilationTypeSpinner, sharedPreferences.getString("bathroomVentilationType2", "")!!))
        moldPresentSpinner.setSelection(getSpinnerIndex(moldPresentSpinner, sharedPreferences.getString("isMoldPresent2", "")!!))
        pm25EditText.setText(sharedPreferences.getString("pm25Value2", ""))
        indoorHumidityEditText.setText(sharedPreferences.getString("indoorHumidity2", ""))
        outdoorPM25EditText.setText(sharedPreferences.getString("outdoorPM252", ""))
        outdoorHumidityEditText.setText(sharedPreferences.getString("outdoorHumidity2", ""))
        updateIAQScore()
    }

    private fun getSpinnerIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit Survey")
        builder.setMessage("Are you sure you want to exit the survey? Your progress will not be saved.")

        builder.setPositiveButton("Yes") { dialog, which ->
            clearAllData()
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
