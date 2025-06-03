package com.tst.ieqproject

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.tst.ieqproject.Secrets.Companion.PASSWORD
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.database.*
import com.tst.ieqproject.utils.FailedSubmissionsManager
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var dataSizeTextView: TextView
    private lateinit var badgeTextView: TextView
    private lateinit var resubmitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference
//        dataSizeTextView = findViewById(R.id.dataSizeTextView)
        badgeTextView = findViewById(R.id.badgeTextView)
        resubmitButton = findViewById(R.id.resubmitButton)

        val startSurveyButton: Button = findViewById(R.id.startSurveyButton)
        val exportButton: Button = findViewById(R.id.exportButton)
        val startSurveyButton2: Button = findViewById(R.id.startSurveyButton2)
        // First TextView: Color "IEQ" green
        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        val welcomeText = "Welcome to IEQ Survey"
        val spannableWelcome = SpannableString(welcomeText)

        // Set color for "IEQ" (index 11-14)
        spannableWelcome.setSpan(
            ForegroundColorSpan(Color.parseColor("#004225")), // Dark green color
            11, 14, // Indexes for "IEQ"
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        welcomeTextView.text = spannableWelcome

        // Second TextView: Make all text green
        val environmentalQualityTextView = findViewById<TextView>(R.id.environmentalQualityTextView)
        environmentalQualityTextView.setTextColor(Color.parseColor("#004225")) // Green color
        resubmitButton.setOnClickListener {
            val intent = Intent(this, ResubmitActivity::class.java)
            startActivityForResult(intent, 1) // Expect result to update badge
        }

        startSurveyButton.setOnClickListener {
            startActivity(Intent(this, DwellingAttributesActivity::class.java))
        }

        startSurveyButton2.setOnClickListener {
            startActivity(Intent(this, BuildingAttributesActivity::class.java))
        }

        setupDataSizeListener()
        exportButton.setOnClickListener {
            showPasswordDialog()
        }

        // Update badge count when MainActivity starts
        updateBadgeCount(FailedSubmissionsManager.getFailedSubmissions(this).size)
    }

    private fun updateBadgeCount(failedCount: Int) {
        if (failedCount > 0) {
            badgeTextView.text = failedCount.toString()
            badgeTextView.visibility = View.VISIBLE
        } else {
            badgeTextView.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val failedCount = data?.getIntExtra("failedCount", 0) ?: 0
            updateBadgeCount(failedCount) // Update the badge dynamically
        }
    }

    private fun setupDataSizeListener() {
        database.child("surveyData").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var totalSizeBytes = 0L
                dataSnapshot.children.forEach { childSnapshot ->
                    totalSizeBytes += childSnapshot.value.toString().toByteArray().size.toLong()
                }
                val dataSizeReadable = formatSize(totalSizeBytes)
//                dataSizeTextView.text = "Data Size: $dataSizeReadable"
            }

            override fun onCancelled(databaseError: DatabaseError) {
//                dataSizeTextView.text = "Data Size: Error"
            }
        })
    }

    private fun formatSize(sizeInBytes: Long): String {
        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            sizeInBytes < kb -> "$sizeInBytes Bytes"
            sizeInBytes < mb -> String.format("%.2f KB", sizeInBytes / kb)
            sizeInBytes < gb -> String.format("%.2f MB", sizeInBytes / mb)
            else -> String.format("%.2f GB", sizeInBytes / gb)
        }
    }

    private fun retrieveData() {
        database.child("surveyData").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val privateData = dataSnapshot.children.filter {
                    it.child("type").value == "private"
                }
                val publicData = dataSnapshot.children.filter {
                    it.child("type").value == "public"
                }

                val privateFile = exportDataAsCsv(privateData, isPublic = false)
                val publicFile = exportDataAsCsv(publicData, isPublic = true)

                sendEmailWithAttachment(listOf(privateFile, publicFile))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle cancellation
            }
        })
    }
    private fun showPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Password")

        val viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_password, findViewById(android.R.id.content), false)
        val passwordInput = viewInflated.findViewById<EditText>(R.id.passwordEditText)

        builder.setView(viewInflated)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            val password = passwordInput.text.toString()
            if (password == PASSWORD) {
                dialog.dismiss()
                retrieveData() // Proceed with data export
            } else {
                dialog.dismiss()
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

        builder.show()
    }
    private fun exportDataAsCsv(data: Iterable<DataSnapshot>, isPublic: Boolean): File {
        val fileName = if (isPublic) "public_survey_data.csv" else "private_survey_data.csv"
        val file = File(getExternalFilesDir(null), fileName)
        FileWriter(file).use { writer ->
            CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(*getHeaders(isPublic))).use { csvPrinter ->
                data.forEach { snapshot ->
                    val rowData = extractRowData(snapshot, isPublic)
                    csvPrinter.printRecord(*rowData)
                }
            }
        }
        return file
    }

    private fun getHeaders(isPublic: Boolean): Array<String> {
        return if (isPublic) {
            arrayOf(
                "ID", "IEQ Score", "Building Type", "Type of Room", "Name of Room","Square Footage", "GPS Location",
                "Age of Building", "Date", "Time of Day", "Program","City","Season", "HVAC Score", "Air Conditioning",
                "Type of Air Conditioning", "Heating Type", "Additional Appliances", "Portable Air Filter",
                "IAQ Score", "Kitchen Presence", "Kitchen Stove Type", "Kitchen Stove Fan", "Bathroom Presence",
                "Bathroom Ventilation Type", "Mold Presence", "PM2.5 Value", "Indoor Humidity", "Outdoor PM2.5",
                "Outdoor Humidity", "Thermal Comfort Score", "Indoor Temp", "Outdoor Temp", "Acoustic Comfort Score",
                "Indoor Decibel", "Outdoor Decibel","Indoor Noise Sources", "Outdoor Noise Sources"
            )
        } else {
            arrayOf(
                "ID", "IEQ Score", "Home Type", "Section 8", "Oakland Housing", "Number of People",
                "Square Footage", "Date", "Time of Day","Program", "City","Street Intersection", "Building Age", "HVAC Score", "Has Air Conditioning",
                "Air Conditioning Works", "Type of Air Conditioning", "Heating Type", "Additional Appliances",
                "Has Portable Air Filter", "IAQ Score", "Kitchen Stove Type", "Kitchen Stove Fan",
                "Bathroom Ventilation", "Mold Present", "Living Room Before Cooking", "Living Room After Cooking",
                "Living Room Humidity", "Outdoor PM2.5", "Outdoor Humidity", "Thermal Comfort Score",
                "Indoor Temperature", "Outdoor Temperature", "Acoustic Comfort Score", "Indoor Decibel",
                "Outdoor Decibel", "Indoor Noise Sources", "Outdoor Noise Sources", "Multiracial",
                "American Indian", "Asian", "Black", "Hispanic", "Native Hawaiian", "White", "Other",
                "Other Ethnicity"
            )
        }
    }

    private fun extractRowData(snapshot: DataSnapshot, isPublic: Boolean): Array<String> {
        return if (isPublic) {
            arrayOf(
                snapshot.key ?: "N/A",
                snapshot.child("ieqScore").value.toString(),
                snapshot.child("buildingAttributes/buildingType").value.toString(),
                snapshot.child("buildingAttributes/typeOfRoom").value.toString(),
                snapshot.child("buildingAttributes/nameofroom").value.toString(),
                snapshot.child("buildingAttributes/squareFootage").value.toString(),
                snapshot.child("buildingAttributes/gpsLocation").value.toString(),
                snapshot.child("buildingAttributes/ageOfBuilding").value.toString(),
                snapshot.child("buildingAttributes/date").value.toString(),
                snapshot.child("buildingAttributes/timeOfDay").value.toString(),
                snapshot.child("buildingAttributes/program").value.toString(),
                snapshot.child("buildingAttributes/city").value.toString(), // Added City
                snapshot.child("buildingAttributes/season").value.toString(),
                snapshot.child("hvacAttributes/hvacScore").value.toString(),
                snapshot.child("hvacAttributes/airConditioning").value.toString(),
                snapshot.child("hvacAttributes/typeOfAirConditioning").value.toString(),
                snapshot.child("hvacAttributes/heatingType").value.toString(),
                snapshot.child("hvacAttributes/additionalAppliances").value.toString(),
                snapshot.child("hvacAttributes/portableAirFilter").value.toString(),
                snapshot.child("iaqAttributes/iaqScore").value.toString(),
                snapshot.child("iaqAttributes/isKitchen").value.toString(),
                snapshot.child("iaqAttributes/kitchenStoveType").value.toString(),
                snapshot.child("iaqAttributes/kitchenStoveFan").value.toString(),
                snapshot.child("iaqAttributes/isBathroom").value.toString(),
                snapshot.child("iaqAttributes/bathroomVentilationType").value.toString(),
                snapshot.child("iaqAttributes/isMoldPresent").value.toString(),
                snapshot.child("iaqAttributes/pm25Value").value.toString(),
                snapshot.child("iaqAttributes/indoorHumidity").value.toString(),
                snapshot.child("iaqAttributes/outdoorPM25").value.toString(),
                snapshot.child("iaqAttributes/outdoorHumidity").value.toString(),
                snapshot.child("thermalComfortAttributes/thermalComfortScore").value.toString(),
                snapshot.child("thermalComfortAttributes/indoorTemp").value.toString(),
                snapshot.child("thermalComfortAttributes/outdoorTemp").value.toString(),
                snapshot.child("acousticComfortAttributes/acousticComfortScore").value.toString(),
                snapshot.child("acousticComfortAttributes/indoorDecibel").value.toString(),
                snapshot.child("acousticComfortAttributes/outdoorDecibel").value.toString(),
                snapshot.child("acousticComfortAttributes/indoorNoiseSources").value.toString(),
                snapshot.child("acousticComfortAttributes/outdoorNoiseSources").value.toString()
            )
        } else {
            arrayOf(
                snapshot.key ?: "N/A",
                snapshot.child("ieqScore").value.toString(),
                snapshot.child("dwellingAttributes/homeType").value.toString(),
                snapshot.child("dwellingAttributes/section8").value.toString(),
                snapshot.child("dwellingAttributes/oaklandHousing").value.toString(),
                snapshot.child("dwellingAttributes/numPeople").value.toString(),
                snapshot.child("dwellingAttributes/squareFootage").value.toString(),
                snapshot.child("dwellingAttributes/date").value.toString(),
                snapshot.child("dwellingAttributes/timeOfDay").value.toString(), // Added Time of Day
                snapshot.child("dwellingAttributes/program").value.toString(),
                snapshot.child("dwellingAttributes/city").value.toString(), // Added City
                snapshot.child("dwellingAttributes/streetIntersection").value.toString(),
                snapshot.child("dwellingAttributes/buildingAge").value.toString(),
                snapshot.child("hvacAttributes/hvacScore").value.toString(),
                snapshot.child("hvacAttributes/hasAirConditioning").value.toString(),
                snapshot.child("hvacAttributes/airConditioningWorks").value.toString(),
                snapshot.child("hvacAttributes/airConditioningType").value.toString(),
                snapshot.child("hvacAttributes/heatingType").value.toString(),
                snapshot.child("hvacAttributes/additionalAppliances").value.toString(),
                snapshot.child("hvacAttributes/hasPortableAirFilter").value.toString(),
                snapshot.child("iaqAttributes/iaqScore").value.toString(),
                snapshot.child("iaqAttributes/kitchenStoveType").value.toString(),
                snapshot.child("iaqAttributes/kitchenStoveFan").value.toString(),
                snapshot.child("iaqAttributes/bathroomVentilation").value.toString(),
                snapshot.child("iaqAttributes/moldPresent").value.toString(),
                snapshot.child("iaqAttributes/livingRoomBeforeCooking").value.toString(),
                snapshot.child("iaqAttributes/livingRoomAfterCooking").value.toString(),
                snapshot.child("iaqAttributes/livingRoomHumidity").value.toString(),
                snapshot.child("iaqAttributes/outdoorPM25").value.toString(),
                snapshot.child("iaqAttributes/outdoorHumidity").value.toString(),
                snapshot.child("thermalComfortAttributes/thermalComfortScore").value.toString(),
                snapshot.child("thermalComfortAttributes/indoorTemp").value.toString(),
                snapshot.child("thermalComfortAttributes/outdoorTemp").value.toString(),
                snapshot.child("acousticComfortAttributes/acousticComfortScore").value.toString(),
                snapshot.child("acousticComfortAttributes/indoorDecibel").value.toString(),
                snapshot.child("acousticComfortAttributes/outdoorDecibel").value.toString(),
                snapshot.child("acousticComfortAttributes/indoorNoiseSources").value.toString(),
                snapshot.child("acousticComfortAttributes/outdoorNoiseSources").value.toString(),
                snapshot.child("demographicAttributes/multiracial").value.toString(),
                snapshot.child("demographicAttributes/americanIndian").value.toString(),
                snapshot.child("demographicAttributes/asian").value.toString(),
                snapshot.child("demographicAttributes/black").value.toString(),
                snapshot.child("demographicAttributes/hispanic").value.toString(),
                snapshot.child("demographicAttributes/nativeHawaiian").value.toString(),
                snapshot.child("demographicAttributes/white").value.toString(),
                snapshot.child("demographicAttributes/other").value.toString(),
                snapshot.child("demographicAttributes/otherEthnicity").value.toString()
            )
        }
    }

    private fun sendEmailWithAttachment(files: List<File>) {
        val uris = files.map { file ->
            FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        }

        val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "text/csv"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            putExtra(Intent.EXTRA_EMAIL, arrayOf("ieq.tool@gmail.com")) // Replace with actual email address
            putExtra(Intent.EXTRA_SUBJECT, "Firebase Data Export")
            putExtra(Intent.EXTRA_TEXT, "Please find the attached CSV files with the exported data.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }
}
