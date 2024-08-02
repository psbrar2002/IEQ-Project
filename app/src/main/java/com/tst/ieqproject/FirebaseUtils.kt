package com.tst1.ieqproject.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tst.ieqproject.AcousticComfortAttributes
import com.tst.ieqproject.IAQAttributes
import com.tst.ieqproject.DemographicAttributes
import com.tst.ieqproject.DwellingAttributes
import com.tst.ieqproject.HVACAttributes
import com.tst.ieqproject.ThermalComfortAttributes
import com.tst.ieqproject.UserSurveyData
import java.util.*

object FirebaseUtils {
    private lateinit var database: DatabaseReference

    fun submitDataToFirebase(context: Context, sharedPreferences: SharedPreferences, callback: (Boolean) -> Unit) {
        database = FirebaseDatabase.getInstance().reference

        try {
            // HVAC Data
            val hvacAttributes = HVACAttributes(
                sharedPreferences.getBoolean("hasAirConditioning", false),
                sharedPreferences.getBoolean("airConditioningWorks", false),
                sharedPreferences.getString("airConditioningType", "") ?: "",
                sharedPreferences.getString("heatingType", "") ?: "",
                sharedPreferences.getString("additionalAppliances", "") ?: "",
                sharedPreferences.getBoolean("hasPortableAirFilter", false),
                sharedPreferences.getFloat("hvacScore", 0f).toDouble()
            )

            // IAQ Data
            val iaqAttributes = IAQAttributes(
                sharedPreferences.getString("kitchenStoveType", "") ?: "",
                sharedPreferences.getString("kitchenStoveFan", "") ?: "",
                sharedPreferences.getString("livingRoomBeforeCooking", "") ?: "",
                sharedPreferences.getString("livingRoomAfterCooking", "") ?: "",
                sharedPreferences.getString("livingRoomHumidity", "") ?: "",
                sharedPreferences.getString("outdoorPM25", "") ?: "",
                sharedPreferences.getString("outdoorHumidity", "") ?: "",
                sharedPreferences.getString("moldPresent", "") ?: "",
                sharedPreferences.getFloat("iaqScore", 0f).toDouble()
            )

            // Dwelling Data
            val dwellingAttributes = DwellingAttributes(
                sharedPreferences.getString("homeType", "") ?: "",
                sharedPreferences.getString("section8", "No") == "Yes",
                sharedPreferences.getString("oaklandHousing", "No") == "Yes",
                sharedPreferences.getString("numPeople", "") ?: "",
                sharedPreferences.getString("squareFootage", "") ?: "",
                sharedPreferences.getString("date", "") ?: "",
                sharedPreferences.getString("streetIntersection", "") ?: "",
                sharedPreferences.getString("buildingAge", "") ?: ""
            )

            // Demographic Data
            val demographicAttributes = DemographicAttributes(
                sharedPreferences.getBoolean("isMultiracial", false),
                sharedPreferences.getBoolean("americanIndian", false),
                sharedPreferences.getBoolean("asian", false),
                sharedPreferences.getBoolean("black", false),
                sharedPreferences.getBoolean("hispanic", false),
                sharedPreferences.getBoolean("nativeHawaiian", false),
                sharedPreferences.getBoolean("white", false),
                sharedPreferences.getBoolean("other", false),
                sharedPreferences.getString("otherEthnicity", "") ?: ""
            )

            // Acoustic Comfort Data
            val acousticComfortAttributes = AcousticComfortAttributes(
                sharedPreferences.getString("indoorDecibel", "") ?: "",
                sharedPreferences.getString("outdoorDecibel", "") ?: "",
                sharedPreferences.getString("indoorNoiseSources", "") ?: "",
                sharedPreferences.getString("outdoorNoiseSources", "") ?: "",
                sharedPreferences.getFloat("acousticComfortScore", 0f).toDouble()
            )

            // Thermal Comfort Data
            val thermalComfortAttributes = ThermalComfortAttributes(
                sharedPreferences.getString("indoorTemperature", "") ?: "",
                sharedPreferences.getString("outdoorTemperature", "") ?: "",
                sharedPreferences.getFloat("thermalComfortScore", 0f).toDouble()
            )

            // Overall IEQ Score
            val ieqScore = sharedPreferences.getFloat("ieqScore", 0f).toDouble()

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

            // Generate a unique ID for the submission
            val userId = UUID.randomUUID().toString()

            // Push data to Firebase under the unique ID
            database.child("surveyData").child(userId).setValue(userSurveyData)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false)
        }
    }
}
