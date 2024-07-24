package com.example.ieqproject

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

object FirebaseUtils {
    private lateinit var database: DatabaseReference

    fun submitDataToFirebase(context: Context, sharedPreferences: SharedPreferences) {
        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        try {
            // HVAC Data
            val hasAirConditioning = sharedPreferences.getBoolean("hasAirConditioning", false)
            val airConditioningWorks = sharedPreferences.getBoolean("airConditioningWorks", false)
            val airConditioningType = sharedPreferences.getString("airConditioningType", "") ?: ""
            val heatingType = sharedPreferences.getString("heatingType", "") ?: ""
            val additionalAppliances = sharedPreferences.getString("additionalAppliances", "") ?: ""
            val hasPortableAirFilter = sharedPreferences.getBoolean("hasPortableAirFilter", false)
            val hvacScore = sharedPreferences.getFloat("hvacScore", 0f).toDouble()

            val hvacAttributes = HVACAttributes(
                hasAirConditioning,
                airConditioningWorks,
                airConditioningType,
                heatingType,
                additionalAppliances,
                hasPortableAirFilter,
                hvacScore
            )

            // IAQ Data
            val kitchenStoveType = sharedPreferences.getString("kitchenStoveType", "") ?: ""
            val kitchenStoveFan = sharedPreferences.getString("kitchenStoveFan", "") ?: ""
            val livingRoomBeforeCooking = sharedPreferences.getString("livingRoomBeforeCooking", "") ?: ""
            val livingRoomAfterCooking = sharedPreferences.getString("livingRoomAfterCooking", "") ?: ""
            val livingRoomHumidity = sharedPreferences.getString("livingRoomHumidity", "") ?: ""
            val outdoorPM25 = sharedPreferences.getString("outdoorPM25", "") ?: ""
            val outdoorHumidity = sharedPreferences.getString("outdoorHumidity", "") ?: ""
            val moldPresent = sharedPreferences.getString("moldPresent", "") ?: ""
            val iaqScore = sharedPreferences.getFloat("iaqScore", 0f).toDouble()

            val iaqAttributes = IAQAttributes(
                kitchenStoveType,
                kitchenStoveFan,
                livingRoomBeforeCooking,
                livingRoomAfterCooking,
                livingRoomHumidity,
                outdoorPM25,
                outdoorHumidity,
                moldPresent,
                iaqScore
            )

            // Dwelling Data
            val homeType = sharedPreferences.getString("homeType", "") ?: ""
            val section8 = sharedPreferences.getBoolean("section8", false)
            val oaklandHousing = sharedPreferences.getBoolean("oaklandHousing", false)
            val numPeople = sharedPreferences.getString("numPeople", "") ?: ""
            val squareFootage = sharedPreferences.getString("squareFootage", "") ?: ""
            val date = sharedPreferences.getString("date", "") ?: ""
            val streetIntersection = sharedPreferences.getString("streetIntersection", "") ?: ""
            val buildingAge = sharedPreferences.getString("buildingAge", "") ?: ""

            val dwellingAttributes = DwellingAttributes(
                homeType,
                section8,
                oaklandHousing,
                numPeople,
                squareFootage,
                date,
                streetIntersection,
                buildingAge
            )

            // Demographic Data
            val multiracial = sharedPreferences.getBoolean("isMultiracial", false)
            val americanIndian = listOf<String>() // Add logic to retrieve this if applicable
            val asian = sharedPreferences.getBoolean("asian", false)
            val black = sharedPreferences.getBoolean("black", false)
            val hispanic = sharedPreferences.getBoolean("hispanic", false)
            val nativeHawaiian = sharedPreferences.getBoolean("nativeHawaiian", false)
            val white = sharedPreferences.getBoolean("white", false)
            val other = sharedPreferences.getBoolean("other", false)
            val otherEthnicity = sharedPreferences.getString("otherEthnicity", "") ?: ""

            val demographicAttributes = DemographicAttributes(
                multiracial,
                americanIndian,
                asian,
                black,
                hispanic,
                nativeHawaiian,
                white,
                other,
                otherEthnicity
            )

            // Acoustic Comfort Data
            val indoorDecibel = sharedPreferences.getString("indoorDecibel", "") ?: ""
            val outdoorDecibel = sharedPreferences.getString("outdoorDecibel", "") ?: ""
            val indoorNoiseSources = sharedPreferences.getString("indoorNoiseSources", "") ?: ""
            val outdoorNoiseSources = sharedPreferences.getString("outdoorNoiseSources", "") ?: ""
            val acousticComfortScore = sharedPreferences.getFloat("acousticComfortScore", 0f).toDouble()

            val acousticComfortAttributes = AcousticComfortAttributes(
                indoorDecibel,
                outdoorDecibel,
                indoorNoiseSources,
                outdoorNoiseSources,
                acousticComfortScore
            )

            // Thermal Comfort Data
            val indoorTemperature = sharedPreferences.getString("indoorTemperature", "") ?: ""
            val outdoorTemperature = sharedPreferences.getString("outdoorTemperature", "") ?: ""
            val thermalComfortScore = sharedPreferences.getFloat("thermalComfortScore", 0f).toDouble()

            val thermalComfortAttributes = ThermalComfortAttributes(
                indoorTemperature,
                outdoorTemperature,
                thermalComfortScore
            )

            // Overall IEQ Score calculation
            val totalPossibleScore = 100.0  // Assuming 100 is the total possible score; adjust as needed
            val combinedScore = hvacScore + iaqScore + acousticComfortScore + thermalComfortScore
            val ieqScore = (combinedScore / totalPossibleScore) * 100

            // User Survey Data
            val userSurveyData = UserSurveyData(
                hvacAttributes,
                iaqAttributes,
                dwellingAttributes,
                demographicAttributes,
                acousticComfortAttributes,
                thermalComfortAttributes,
                ieqScore
            )

            // Generate a unique ID for each submission using the current timestamp
            val userId = generateUniqueID()

            // Push data to Firebase under the generated unique ID
            database.child("surveyData").child(userId).setValue(userSurveyData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Data submitted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to submit data", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            e.printStackTrace()  // Print stack trace for debugging
            Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun generateUniqueID(): String {
        // Generate a timestamp-based unique ID
        val sdf = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US)
        return sdf.format(Date())
    }
}
