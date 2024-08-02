package com.tst.ieqproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)

        // Initialize UI components for Dwelling Attributes
        DwellingAttributes.setupSpinners(this, findViewById(android.R.id.content))

        // Initialize the EditTexts
        val dateEditText: EditText = findViewById(R.id.dateEditText)
        val streetIntersectionEditText: EditText = findViewById(R.id.streetIntersectionEditText)
        val buildingAgeEditText: EditText = findViewById(R.id.buildingAgeEditText)

        // Initialize the navigation button to HVACActivity
        val hvacButton: Button = findViewById(R.id.hvacButton)
        hvacButton.setOnClickListener {
            saveDataLocally()
            val intent = Intent(this, HVACActivity::class.java)
            startActivity(intent)
        }

        restoreData()
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putString("homeType", findViewById<Spinner>(R.id.homeTypeSpinner).selectedItem.toString())
        editor.putString("section8", findViewById<Spinner>(R.id.section8Spinner).selectedItem.toString())
        editor.putString("oaklandHousing", findViewById<Spinner>(R.id.oaklandHousingSpinner).selectedItem.toString())
        editor.putString("numPeople", findViewById<Spinner>(R.id.numberOfPeopleSpinner).selectedItem.toString())
        editor.putString("squareFootage", findViewById<Spinner>(R.id.squareFootageSpinner).selectedItem.toString())
        editor.putString("date", findViewById<EditText>(R.id.dateEditText).text.toString())
        editor.putString("streetIntersection", findViewById<EditText>(R.id.streetIntersectionEditText).text.toString())
        editor.putString("buildingAge", findViewById<EditText>(R.id.buildingAgeEditText).text.toString())
        editor.apply()
    }

    private fun restoreData() {
        // Ensure proper retrieval of boolean values by casting to String first and then checking the condition
        findViewById<Spinner>(R.id.homeTypeSpinner).setSelection(getSpinnerIndex(R.id.homeTypeSpinner, sharedPreferences.getString("homeType", "")!!))

        findViewById<Spinner>(R.id.section8Spinner).setSelection(
            if (sharedPreferences.getString("section8", "") == "Yes") 1 else 0 // Adjusted to compare as String
        )

        findViewById<Spinner>(R.id.oaklandHousingSpinner).setSelection(
            if (sharedPreferences.getString("oaklandHousing", "") == "Yes") 1 else 0 // Adjusted to compare as String
        )

        findViewById<Spinner>(R.id.numberOfPeopleSpinner).setSelection(getSpinnerIndex(R.id.numberOfPeopleSpinner, sharedPreferences.getString("numPeople", "")!!))
        findViewById<Spinner>(R.id.squareFootageSpinner).setSelection(getSpinnerIndex(R.id.squareFootageSpinner, sharedPreferences.getString("squareFootage", "")!!))
        findViewById<EditText>(R.id.dateEditText).setText(sharedPreferences.getString("date", ""))
        findViewById<EditText>(R.id.streetIntersectionEditText).setText(sharedPreferences.getString("streetIntersection", ""))
        findViewById<EditText>(R.id.buildingAgeEditText).setText(sharedPreferences.getString("buildingAge", ""))
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

    // Clear UI components specific to this activity
    fun clearUIComponents() {
        findViewById<Spinner>(R.id.homeTypeSpinner).setSelection(0)
        findViewById<Spinner>(R.id.section8Spinner).setSelection(0)
        findViewById<Spinner>(R.id.oaklandHousingSpinner).setSelection(0)
        findViewById<Spinner>(R.id.numberOfPeopleSpinner).setSelection(0)
        findViewById<Spinner>(R.id.squareFootageSpinner).setSelection(0)
        findViewById<EditText>(R.id.dateEditText).text.clear()
        findViewById<EditText>(R.id.streetIntersectionEditText).text.clear()
        findViewById<EditText>(R.id.buildingAgeEditText).text.clear()
    }
}