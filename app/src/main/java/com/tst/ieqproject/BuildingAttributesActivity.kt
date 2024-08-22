package com.tst.ieqproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tst.ieqproject.utils.ScoreUtils2

class BuildingAttributesActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var buildingTypeSpinner: Spinner
    private lateinit var buildingTypeOtherEditText: EditText
    private lateinit var typeOfRoomSpinner: Spinner
    private lateinit var typeOfRoomOtherEditText: EditText
    private lateinit var squareFootageSpinner: Spinner
    private lateinit var gpsLocationEditText: EditText
    private lateinit var ageOfBuildingEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var timeOfDayEditText: EditText
    private lateinit var seasonSpinner: Spinner
    private lateinit var ieqScoreTextView2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_attributes)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize UI Components
        buildingTypeSpinner = findViewById(R.id.buildingTypeSpinner)
        buildingTypeOtherEditText = findViewById(R.id.buildingTypeOtherEditText)
        typeOfRoomSpinner = findViewById(R.id.typeOfRoomSpinner)
        typeOfRoomOtherEditText = findViewById(R.id.typeOfRoomOtherEditText)
        squareFootageSpinner = findViewById(R.id.squareFootageSpinner)
        gpsLocationEditText = findViewById(R.id.gpsLocationEditText)
        ageOfBuildingEditText = findViewById(R.id.ageOfBuildingEditText)
        dateEditText = findViewById(R.id.dateEditText)
        timeOfDayEditText = findViewById(R.id.timeOfDayEditText)
        seasonSpinner = findViewById(R.id.seasonSpinner)
        ieqScoreTextView2 = findViewById(R.id.ieqScoreTextView2)

        // Setup Spinner Listeners
        buildingTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedOption = parent.getItemAtPosition(position).toString()
                buildingTypeOtherEditText.visibility = if (selectedOption == "Other") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        typeOfRoomSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedOption = parent.getItemAtPosition(position).toString()
                typeOfRoomOtherEditText.visibility = if (selectedOption == "Other") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Setup Exit Button
        val exitButton: Button = findViewById(R.id.exitButton)
        exitButton.setOnClickListener {
            showExitConfirmationDialog()
        }

        // Setup Next Button
        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            saveDataLocally()
            val intent = Intent(this, HVACActivity2::class.java)
            startActivity(intent)
        }

        // Restore Data if available
        restoreData()

        // Update IEQ Score on screen
        updateIEQScore2()
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

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()

        // Save Building Type
        if (buildingTypeSpinner.selectedItem.toString() == "Other") {
            editor.putString("buildingType2", buildingTypeOtherEditText.text.toString())
        } else {
            editor.putString("buildingType2", buildingTypeSpinner.selectedItem.toString())
        }

        // Save Type of Room
        if (typeOfRoomSpinner.selectedItem.toString() == "Other") {
            editor.putString("typeOfRoom2", typeOfRoomOtherEditText.text.toString())
        } else {
            editor.putString("typeOfRoom2", typeOfRoomSpinner.selectedItem.toString())
        }

        // Save other attributes
        editor.putString("squareFootage2", squareFootageSpinner.selectedItem.toString())
        editor.putString("gpsLocation2", gpsLocationEditText.text.toString())
        editor.putString("ageOfBuilding2", ageOfBuildingEditText.text.toString())
        editor.putString("date2", dateEditText.text.toString())
        editor.putString("timeOfDay2", timeOfDayEditText.text.toString())
        editor.putString("season2", seasonSpinner.selectedItem.toString())
        editor.apply()
    }

    private fun restoreData() {
        val buildingType = sharedPreferences.getString("buildingType2", "")
        val typeOfRoom = sharedPreferences.getString("typeOfRoom2", "")

        // Restore Building Type
        if (buildingType == "Other") {
            buildingTypeSpinner.setSelection(getSpinnerIndex(R.id.buildingTypeSpinner, "Other"))
            buildingTypeOtherEditText.setText(sharedPreferences.getString("buildingType2", ""))
            buildingTypeOtherEditText.visibility = View.VISIBLE
        } else {
            buildingTypeSpinner.setSelection(getSpinnerIndex(R.id.buildingTypeSpinner, buildingType!!))
            buildingTypeOtherEditText.visibility = View.GONE
        }

        // Restore Type of Room
        if (typeOfRoom == "Other") {
            typeOfRoomSpinner.setSelection(getSpinnerIndex(R.id.typeOfRoomSpinner, "Other"))
            typeOfRoomOtherEditText.setText(sharedPreferences.getString("typeOfRoom2", ""))
            typeOfRoomOtherEditText.visibility = View.VISIBLE
        } else {
            typeOfRoomSpinner.setSelection(getSpinnerIndex(R.id.typeOfRoomSpinner, typeOfRoom!!))
            typeOfRoomOtherEditText.visibility = View.GONE
        }

        // Restore other attributes
        squareFootageSpinner.setSelection(getSpinnerIndex(R.id.squareFootageSpinner, sharedPreferences.getString("squareFootage2", "")!!))
        gpsLocationEditText.setText(sharedPreferences.getString("gpsLocation2", ""))
        ageOfBuildingEditText.setText(sharedPreferences.getString("ageOfBuilding2", ""))
        dateEditText.setText(sharedPreferences.getString("date2", ""))
        timeOfDayEditText.setText(sharedPreferences.getString("timeOfDay2", ""))
        seasonSpinner.setSelection(getSpinnerIndex(R.id.seasonSpinner, sharedPreferences.getString("season2", "")!!))

        updateIEQScore2()
    }

    private fun getSpinnerIndex(spinnerId: Int, value: String): Int {
        val spinner = findViewById<Spinner>(spinnerId)
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
    }
    private fun updateIEQScore2() {
        ScoreUtils2.updateIEQScore2(this, sharedPreferences, ieqScoreTextView2)
    }

    override fun onResume() {
        super.onResume()
        updateIEQScore2()
    }

    override fun onDestroy() {
        super.onDestroy()
        // If you are using any resources that need to be cleaned up, do it here
    }
}
