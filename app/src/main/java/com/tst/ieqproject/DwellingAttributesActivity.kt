package com.tst.ieqproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tst.ieqproject.utils.ScoreUtils
import kotlinx.coroutines.*
import java.io.IOException

class DwellingAttributesActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var streetIntersectionEditText: EditText
    private lateinit var suggestionsListView: ListView
    private lateinit var ieqScoreTextView: TextView
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dwelling_attributes)

        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference

        // Initialize UI components for Dwelling Attributes
        DwellingAttributes.setupSpinners(this, findViewById(android.R.id.content))

        // Initialize the EditTexts
        streetIntersectionEditText = findViewById(R.id.streetIntersectionEditText)
        suggestionsListView = findViewById(R.id.suggestionsListView)
        ieqScoreTextView = findViewById(R.id.ieqScoreTextView)

        // Restore saved data
        restoreData()

        // Add listener for street intersection suggestions


        // Initialize the navigation button to HVACActivity
        val nextButton: Button = findViewById(R.id.hvacButton)
        nextButton.setOnClickListener {
            saveDataLocally()
            val intent = Intent(this, HVACActivity::class.java)
            startActivity(intent)
        }
        val exitButton: Button = findViewById(R.id.exitButton)
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

    private fun searchStreetIntersections(query: String) {
        coroutineScope.launch {
            try {
                val places = withContext(Dispatchers.IO) { NominatimAPI.search(query) }
                val placeNames = places.map { it.displayName }
                suggestionsListView.adapter = ArrayAdapter(
                    this@DwellingAttributesActivity,
                    android.R.layout.simple_list_item_1,
                    placeNames
                )
                suggestionsListView.setOnItemClickListener { _, _, position, _ ->
                    streetIntersectionEditText.setText(placeNames[position])
                    suggestionsListView.adapter = null
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putString("homeType", findViewById<Spinner>(R.id.homeTypeSpinner).selectedItem.toString())
        editor.putString("section8", findViewById<Spinner>(R.id.section8Spinner).selectedItem.toString())
        editor.putString("oaklandHousing", findViewById<Spinner>(R.id.oaklandHousingSpinner).selectedItem.toString())
        editor.putString("numPeople", findViewById<Spinner>(R.id.numberOfPeopleSpinner).selectedItem.toString())
        editor.putString("squareFootage", findViewById<Spinner>(R.id.squareFootageSpinner).selectedItem.toString())
        editor.putString("date", findViewById<EditText>(R.id.dateEditText).text.toString())
        editor.putString("streetIntersection", streetIntersectionEditText.text.toString())
        editor.putString("buildingAge", findViewById<EditText>(R.id.buildingAgeEditText).text.toString())
        editor.apply()
    }

    private fun restoreData() {
        findViewById<Spinner>(R.id.homeTypeSpinner).setSelection(getSpinnerIndex(R.id.homeTypeSpinner, sharedPreferences.getString("homeType", "")!!))
        findViewById<Spinner>(R.id.section8Spinner).setSelection(if (sharedPreferences.getString("section8", "") == "Yes") 1 else 0)
        findViewById<Spinner>(R.id.oaklandHousingSpinner).setSelection(if (sharedPreferences.getString("oaklandHousing", "") == "Yes") 1 else 0)
        findViewById<Spinner>(R.id.numberOfPeopleSpinner).setSelection(getSpinnerIndex(R.id.numberOfPeopleSpinner, sharedPreferences.getString("numPeople", "")!!))
        findViewById<Spinner>(R.id.squareFootageSpinner).setSelection(getSpinnerIndex(R.id.squareFootageSpinner, sharedPreferences.getString("squareFootage", "")!!))
        findViewById<EditText>(R.id.dateEditText).setText(sharedPreferences.getString("date", ""))
        streetIntersectionEditText.setText(sharedPreferences.getString("streetIntersection", ""))
        findViewById<EditText>(R.id.buildingAgeEditText).setText(sharedPreferences.getString("buildingAge", ""))
        updateIEQScore()
    }

    private fun getSpinnerIndex(spinnerId: Int, value: String): Int {
        val spinner: Spinner = findViewById(spinnerId)
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
    }

    private fun updateIEQScore() {
        // Update the IEQ score using ScoreUtils
        ScoreUtils.updateIEQScore(this, sharedPreferences, ieqScoreTextView)
    }

    override fun onResume() {
        super.onResume()
        updateIEQScore()
    }

    // Clear UI components specific to this activity
    fun clearUIComponents() {
        findViewById<Spinner>(R.id.homeTypeSpinner).setSelection(0)
        findViewById<Spinner>(R.id.section8Spinner).setSelection(0)
        findViewById<Spinner>(R.id.oaklandHousingSpinner).setSelection(0)
        findViewById<Spinner>(R.id.numberOfPeopleSpinner).setSelection(0)
        findViewById<Spinner>(R.id.squareFootageSpinner).setSelection(0)
        findViewById<EditText>(R.id.dateEditText).text.clear()
        streetIntersectionEditText.text.clear()
        findViewById<EditText>(R.id.buildingAgeEditText).text.clear()
    }
}
