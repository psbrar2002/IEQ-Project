package com.tst.ieqproject
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.preference.PreferenceManager
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.tst.ieqproject.utils.FirebaseUtils
import com.tst.ieqproject.utils.ScoreUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DwellingAttributesActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var streetIntersectionEditText: EditText
    private lateinit var suggestionsListView: ListView
    private lateinit var ieqScoreTextView: TextView
    private lateinit var surveyIdTextView: TextView
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var mapView: MapView
    private lateinit var locationSearchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private lateinit var currentPage: PdfRenderer.Page
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dwelling_attributes)
        // Load OSMDroid configuration
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        surveyIdTextView = findViewById(R.id.surveyIdTextView)



        val isPublicSurvey = false // Set this based on your logic


        // Set up CirclePageIndicator with the ViewPager

        // Use a callback to ensure the survey ID is ready
        FirebaseUtils.generateAndSaveSurveyId(this, isPublicSurvey) { surveyId ->
            // This code runs after the survey ID is generated
            surveyIdTextView.text = "Survey ID: $surveyId"
            Log.d("DwellingAttributesActivity", "Survey ID displayed: $surveyId")
        }
        // Initialize OSMDroid


        // Initialize UI components
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
//            }

        // Initialize UI components for Dwelling Attributes
        DwellingAttributes.setupSpinners(this, findViewById(android.R.id.content))

        // Initialize the EditTexts
        streetIntersectionEditText = findViewById(R.id.streetIntersectionEditText)

        ieqScoreTextView = findViewById(R.id.ieqScoreTextView)
        // Autopopulate the current date and time of day
        val dateEditText: EditText = findViewById(R.id.dateEditText)
        val timeOfDayEditText: EditText = findViewById(R.id.timeOfDayEditText)
        Log.d("DwellingAttributesActivity", "EditTexts initialized: dateEditText=$dateEditText, timeOfDayEditText=$timeOfDayEditText")
        // Get current date and time
        val calendar = Calendar.getInstance()

        // Format the date as mm/dd/yyyy
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        Log.d("DwellingAttributesActivity", "Current date: $currentDate")

        dateEditText.setText(currentDate)
        Log.d("DwellingAttributesActivity", "Date EditText value: ${dateEditText.text}")
        // Format the time as hh:mm AM/PM
        val timeFormat = DateFormat.getTimeFormat(this)
        val currentTime = timeFormat.format(calendar.time)
        Log.d("DwellingAttributesActivity", "Current time: $currentTime")
        timeOfDayEditText.setText(currentTime)
        Log.d("DwellingAttributesActivity", "Time of Day EditText value: ${timeOfDayEditText.text}")
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
        // For the Button with an icon
// Set up the Exit button
        val exitButton = findViewById<ImageButton>(R.id.exitButtonWithIcon)
        exitButton.setOnClickListener {
            // Handle the exit button click event
            showExitConfirmationDialog()
        }
        val openInstructionsLink = findViewById<TextView>(R.id.openInstructionsStoreLink)
        val content = "IEQ Survey Instructions"
        val spannableString = SpannableString(content)
        spannableString.setSpan(UnderlineSpan(), 0, content.length, 0)
        openInstructionsLink.text = spannableString

// Set it as a clickable link
        openInstructionsLink.setOnClickListener {
            val url = "https://drive.google.com/file/d/1PTKGWSZ3O_qd8TFKXs3WwYSBKgdVfHx0/view" // Replace with the new public link
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        val accessMapLink = findViewById<TextView>(R.id.accessMapLink)
        accessMapLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps"))
            startActivity(intent)
        }
        val infoButton: ImageButton = findViewById(R.id.infoButton)
//        TooltipCompat.setTooltipText(infoButton, "This is some information about the nearby street intersection.")
// Set the OnClickListener for click action

        infoButton.setOnClickListener {
            // Inflate the custom layout for the popup
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_info_layout, null)

            // Create the PopupWindow
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            // Measure the popup view dimensions
            popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val popupHeight = popupView.measuredHeight
            val popupWidth = popupView.measuredWidth

            // Get the location of the button on the screen
            val location = IntArray(2)
            infoButton.getLocationOnScreen(location)
            val buttonX = location[0]
            val buttonY = location[1]

            // Get screen dimensions
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val screenWidth = displayMetrics.widthPixels

            // Adjust xOffset to be centered over the button
            var xOffset = buttonX + (infoButton.width / 2) - (popupWidth / 2)
            var yOffset = buttonY - popupHeight - 10 // Always place above the button with 10 pixels margin

            // Ensure popup stays within screen boundaries horizontally
            if (xOffset < 0) {
                xOffset = 10 // Align with some margin from the left edge if it goes out of screen
            } else if (xOffset + popupWidth > screenWidth) {
                xOffset = screenWidth - popupWidth - 10 // Align with some margin from the right edge if it goes out of screen
            }

            // Ensure popup stays within screen boundaries vertically
            if (yOffset < 0) {
                // If there is not enough space above, place it below the button instead
                yOffset = buttonY + infoButton.height + 10 // 10 pixels margin below the button
            }

            // Show the PopupWindow at the calculated position
            popupWindow.showAtLocation(infoButton, Gravity.NO_GRAVITY, xOffset, yOffset)

            // Dismiss the popup when the user interacts with it
            popupView.setOnClickListener {
                popupWindow.dismiss()
            }
        }



    }
//        val content = "IEQ Survey Instructions"
//        val spannableString = SpannableString(content)
//        spannableString.setSpan(UnderlineSpan(), 0, content.length, 0)
//        openInstructionsLink.text = spannableString
//
//        openInstructionsLink.setOnClickListener {
//            openPdfFile()
//        }
//    }
//
//    private fun openPdfFile() {
//        val pdfFileName = "IEQ_Survey_Instructions.pdf"
//        val pdfFile = File(cacheDir, pdfFileName)
//
//        if (!pdfFile.exists()) {
//            try {
//                assets.open(pdfFileName).use { inputStream ->
//                    FileOutputStream(pdfFile).use { outputStream ->
//                        inputStream.copyTo(outputStream)
//                    }
//                }
//                Log.d("DwellingAttributesActivity", "PDF copied to cache directory successfully.")
//            } catch (e: IOException) {
//                e.printStackTrace()
//                Toast.makeText(this, "Error copying PDF to cache", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            Log.d("DwellingAttributesActivity", "PDF already exists in cache directory.")
//        }
//
//        // Log statement to verify if intent is started
//        Log.d("DwellingAttributesActivity", "Starting PdfViewerActivity.")
//
//        // Start PdfViewerActivity to display the PDF
//        val intent = Intent(this, PdfViewerActivity::class.java)
//        startActivity(intent)
//
//    }
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
//                    Toast.makeText(this@DwellingAttributesActivity, "Location not found", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(this@DwellingAttributesActivity, "Error searching location", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }



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
        editor.putString("timeOfDay", findViewById<EditText>(R.id.timeOfDayEditText).text.toString()) // New addition for Time of Day
        editor.putString("city", findViewById<EditText>(R.id.cityEditText).text.toString()) // New addition for City
        editor.putString("program",findViewById<EditText>(R.id.programEditText).text.toString()) // New addition for Program
        editor.putString("buildingAge", findViewById<EditText>(R.id.buildingAgeEditText).text.toString())
        editor.apply()
    }

    private fun restoreData() {
        findViewById<Spinner>(R.id.homeTypeSpinner).setSelection(getSpinnerIndex(R.id.homeTypeSpinner, sharedPreferences.getString("homeType", "")!!))
        findViewById<Spinner>(R.id.section8Spinner).setSelection(if (sharedPreferences.getString("section8", "") == "Yes") 1 else 0)
        findViewById<Spinner>(R.id.oaklandHousingSpinner).setSelection(if (sharedPreferences.getString("oaklandHousing", "") == "Yes") 1 else 0)
        findViewById<Spinner>(R.id.numberOfPeopleSpinner).setSelection(getSpinnerIndex(R.id.numberOfPeopleSpinner, sharedPreferences.getString("numPeople", "")!!))
        findViewById<Spinner>(R.id.squareFootageSpinner).setSelection(getSpinnerIndex(R.id.squareFootageSpinner, sharedPreferences.getString("squareFootage", "")!!))
        streetIntersectionEditText.setText(sharedPreferences.getString("streetIntersection", ""))
        findViewById<EditText>(R.id.cityEditText).setText(sharedPreferences.getString("city", "")) // Restore City
        findViewById<EditText>(R.id.buildingAgeEditText).setText(sharedPreferences.getString("buildingAge", ""))
        findViewById<EditText>(R.id.programEditText).setText(sharedPreferences.getString("program", ""))
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
