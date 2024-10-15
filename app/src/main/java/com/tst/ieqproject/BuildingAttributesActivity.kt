package com.tst.ieqproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.SpannableString
import android.text.format.DateFormat
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    private lateinit var streetIntersectionEditText: EditText
    private lateinit var timeOfDayEditText: EditText
    private lateinit var seasonSpinner: Spinner
    private lateinit var ieqScoreTextView2: TextView
    private lateinit var surveyIdTextView: TextView
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_attributes)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        surveyIdTextView = findViewById(R.id.surveyIdTextView)

        val isPublicSurvey = true // Set this based on your logic

        // Use a callback to ensure the survey ID is ready
        FirebaseUtils.generateAndSaveSurveyId(this, isPublicSurvey) { surveyId ->
            // This code runs after the survey ID is generated
            surveyIdTextView.text = "Survey ID: $surveyId"
            Log.d("DwellingAttributesActivity", "Survey ID displayed: $surveyId")
        }

//        // Initialize UI components
//        mapView = findViewById(R.id.mapView)
//        mapView.setMultiTouchControls(true)
//        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
//
//
//        val mapController = mapView.controller
//        mapController.setZoom(15.0) // Setting initial zoom to 15.0 for city view
//        val startPoint = GeoPoint(37.8715, -122.2730) // UC Berkeley coordinates as an example
//        mapController.setCenter(startPoint)
//
//        val searchBar: EditText = findViewById(R.id.searchBar)
//        searchBar.setOnEditorActionListener { textView, actionId, keyEvent ->
//            val query = searchBar.text.toString()
//            if (query.isNotBlank()) {
//                searchLocation(query)
//            }
//            true
//        }
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize UI Components
        buildingTypeSpinner = findViewById(R.id.buildingTypeSpinner)
        buildingTypeOtherEditText = findViewById(R.id.buildingTypeOtherEditText)
        typeOfRoomSpinner = findViewById(R.id.typeOfRoomSpinner)
        typeOfRoomOtherEditText = findViewById(R.id.typeOfRoomOtherEditText)
        squareFootageSpinner = findViewById(R.id.squareFootageSpinner)
        gpsLocationEditText = findViewById(R.id.streetIntersectionEditText)
        ageOfBuildingEditText = findViewById(R.id.ageOfBuildingEditText)
        dateEditText = findViewById(R.id.dateEditText)
        timeOfDayEditText = findViewById(R.id.timeOfDayEditText)
        seasonSpinner = findViewById(R.id.seasonSpinner)
        ieqScoreTextView2 = findViewById(R.id.ieqScoreTextView2)
        val dateEditText: EditText = findViewById(R.id.dateEditText)
        val timeOfDayEditText: EditText = findViewById(R.id.timeOfDayEditText)

        // Get current date and time
        val calendar = Calendar.getInstance()

        // Format the date as mm/dd/yyyy
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)


        dateEditText.setText(currentDate)

        // Format the time as hh:mm AM/PM
        val timeFormat = DateFormat.getTimeFormat(this)
        val currentTime = timeFormat.format(calendar.time)

        timeOfDayEditText.setText(currentTime)

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
        val exitButton: ImageButton = findViewById(R.id.exitButtonWithIcon)
        exitButton.setOnClickListener {
            showExitConfirmationDialog()
        }
        val accessMapLink = findViewById<TextView>(R.id.accessMapLink)
        accessMapLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps"))
            startActivity(intent)
        }
        // Setup Next Button
        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            saveDataLocally()
            val intent = Intent(this, HVACActivity2::class.java)
            startActivity(intent)
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
//    private fun searchLocation(query: String) {
//        coroutineScope.launch {
//            try {
//                val results = NominatimAPI.search(query)
//                if (results.isNotEmpty()) {
//                    val firstResult = results[0]  // Assuming the first result is the most relevant
//                    val geoPoint = GeoPoint(firstResult.latitude, firstResult.longitude)
//                    mapView.controller.setZoom(18.0)
//                    mapView.controller.animateTo(geoPoint)
//                    mapView.invalidate()  // Refresh the map
//                } else {
//                    Toast.makeText(this@BuildingAttributesActivity, "Location not found", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(this@BuildingAttributesActivity, "Error searching location", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
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
        editor.putString("city", findViewById<EditText>(R.id.cityEditText).text.toString()) // New addition for City
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

        seasonSpinner.setSelection(getSpinnerIndex(R.id.seasonSpinner, sharedPreferences.getString("season2", "")!!))
        findViewById<EditText>(R.id.cityEditText).setText(sharedPreferences.getString("city", "")) // Restore City
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
