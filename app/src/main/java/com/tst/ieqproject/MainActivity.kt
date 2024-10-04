package com.tst.ieqproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.database.*
import com.tst.ieqproject.Secrets.Companion.PASSWORD
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.osmdroid.util.GeoPoint
import java.io.File
import java.io.FileWriter

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var dataSizeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Initialize the TextView for displaying data size
        dataSizeTextView = findViewById(R.id.dataSizeTextView)

        val startSurveyButton: Button = findViewById(R.id.startSurveyButton)
        val exportButton: Button = findViewById(R.id.exportButton)
        val startSurveyButton2: Button = findViewById(R.id.startSurveyButton2)


        // Set up the start survey button click listener
        startSurveyButton.setOnClickListener {
            startActivity(Intent(this, DwellingAttributesActivity::class.java))
        }

        startSurveyButton2.setOnClickListener {
            startActivity(Intent(this, BuildingAttributesActivity::class.java))
        }

        // Listen for changes in the data size
        setupDataSizeListener()

        // Set up the export button click listener
        exportButton.setOnClickListener {
            showPasswordDialog()
        }
    }

    private fun setupDataSizeListener() {
        database.child("surveyData").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Calculate the size of the data
                var totalSizeBytes = 0L

                dataSnapshot.children.forEach { childSnapshot ->
                    totalSizeBytes += getSizeOfDataSnapshot(childSnapshot)
                }

                // Convert the size to a human-readable format
                val dataSizeReadable = formatSize(totalSizeBytes)
                dataSizeTextView.text = "Data Size: $dataSizeReadable"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle cancellation
                dataSizeTextView.text = "Data Size: Error"
            }
        })
    }

    private fun getSizeOfDataSnapshot(dataSnapshot: DataSnapshot): Long {
        var size = 0L

        // Calculate the size of this snapshot
        if (dataSnapshot.value is String) {
            size += (dataSnapshot.value as String).toByteArray().size.toLong()
        } else if (dataSnapshot.value is Number) {
            size += 8L // Assuming 8 bytes for a number (Long, Double, etc.)
        } else if (dataSnapshot.value is Boolean) {
            size += 1L // Assuming 1 byte for a boolean
        }

        // Recursively calculate the size of child snapshots
        dataSnapshot.children.forEach { child ->
            size += getSizeOfDataSnapshot(child)
        }

        return size
    }

    private fun formatSize(sizeInBytes: Long): String {
        val kilobyte = 1024.0
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024

        return when {
            sizeInBytes < kilobyte -> "$sizeInBytes Bytes"
            sizeInBytes < megabyte -> String.format("%.2f KB", sizeInBytes / kilobyte)
            sizeInBytes < gigabyte -> String.format("%.2f MB", sizeInBytes / megabyte)
            sizeInBytes < terabyte -> String.format("%.2f GB", sizeInBytes / gigabyte)
            else -> String.format("%.2f TB", sizeInBytes / terabyte)
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
                "ID", "IEQ Score", "Building Type", "Type of Room", "Square Footage", "GPS Location",
                "Age of Building", "Date", "Time of Day", "City", "Season", "HVAC Score", "Air Conditioning",
                "Type of Air Conditioning", "Heating Type", "Additional Appliances", "Portable Air Filter",
                "IAQ Score", "Kitchen Presence", "Kitchen Stove Type", "Kitchen Stove Fan", "Bathroom Presence",
                "Bathroom Ventilation Type", "Mold Presence", "PM2.5 Value", "Indoor Humidity", "Outdoor PM2.5",
                "Outdoor Humidity", "Thermal Comfort Score", "Indoor Temp", "Outdoor Temp", "Acoustic Comfort Score",
                "Indoor Decibel", "Outdoor Decibel","Indoor Noise Sources", "Outdoor Noise Sources"
            )
        } else {
            arrayOf(
                "ID", "IEQ Score", "Home Type", "Section 8", "Oakland Housing", "Number of People",
                "Square Footage", "Date", "Time of Day", "City","Street Intersection", "Building Age", "HVAC Score", "Has Air Conditioning",
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
                snapshot.child("buildingAttributes/squareFootage").value.toString(),
                snapshot.child("buildingAttributes/gpsLocation").value.toString(),
                snapshot.child("buildingAttributes/ageOfBuilding").value.toString(),
                snapshot.child("buildingAttributes/date").value.toString(),
                snapshot.child("buildingAttributes/timeOfDay").value.toString(),
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
