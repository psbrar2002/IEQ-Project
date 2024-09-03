package com.tst.ieqproject.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

object FirebaseUtils {
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("surveyData")
    // New method to generate surveyId and save it early
    // Method to generate and save survey ID
    // Method to generate a unique surveyId and save it early
    fun generateAndSaveSurveyId(context: Context, isPublic: Boolean, callback: (String) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val existingSurveyId = sharedPreferences.getString("surveyId", null)

        if (existingSurveyId == null) {
            generateUniqueIdentifier(isPublic) { uniqueIdentifier ->
                sharedPreferences.edit().putString("surveyId", uniqueIdentifier).apply()
                Log.d("FirebaseUtils", "Generated and saved surveyId: $uniqueIdentifier")
                callback(uniqueIdentifier) // Pass the uniqueIdentifier to the callback
            }
        } else {
            Log.d("FirebaseUtils", "Existing surveyId found: $existingSurveyId")
            callback(existingSurveyId) // Pass the existingSurveyId to the callback
        }
    }

    private fun generateUniqueIdentifier(isPublic: Boolean, callback: (String) -> Unit) {
        val surveyTypePrefix = if (isPublic) "pub" else "pri"
        val randomFourDigitCode = (1000..9999).random().toString() // Generate a 4-digit code
        val potentialIdentifier = "$surveyTypePrefix-$randomFourDigitCode"

        // Check if the identifier already exists
        database.child(potentialIdentifier).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Identifier exists, generate a new one
                    Log.d("FirebaseUtils", "Identifier $potentialIdentifier exists, generating a new one.")
                    generateUniqueIdentifier(isPublic, callback)
                } else {
                    // Identifier is unique
                    Log.d("FirebaseUtils", "Identifier $potentialIdentifier is unique, using it.")
                    callback(potentialIdentifier)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseUtils", "Error checking identifier: ${error.message}")
                // Return the potentialIdentifier even on error, this should be handled better in production
                callback(potentialIdentifier)
            }
        })
    }

    fun submitSurveyDataToFirebase(
        context: Context,
        sharedPreferences: SharedPreferences,
        isPublic: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        val surveyId = sharedPreferences.getString("surveyId", null)
        if (surveyId == null) {
            Log.e("FirebaseUtils", "Survey ID is null, unable to submit data.")
            callback(false, "N/A")
            return
        }

            val surveyData: Map<String, Any>


            if (isPublic) {
                // Public survey data
                surveyData = mapOf(
                    "type" to "public",
                    "buildingAttributes" to mapOf(
                        "buildingType" to (sharedPreferences.getString("buildingType2", "N/A")
                            ?: "N/A"),
                        "typeOfRoom" to (sharedPreferences.getString("typeOfRoom2", "N/A")
                            ?: "N/A"),
                        "squareFootage" to (sharedPreferences.getString("squareFootage2", "N/A")
                            ?: "N/A"),
                        "gpsLocation" to (sharedPreferences.getString("gpsLocation2", "N/A")
                            ?: "N/A"),
                        "ageOfBuilding" to (sharedPreferences.getString("ageOfBuilding2", "N/A")
                            ?: "N/A"),
                        "date" to (sharedPreferences.getString("date2", "N/A") ?: "N/A"),
                        "timeOfDay" to (sharedPreferences.getString("timeOfDay2", "N/A") ?: "N/A"),
                        "season" to (sharedPreferences.getString("season2", "N/A") ?: "N/A")
                    ),
                    "hvacAttributes" to mapOf(
                        "airConditioning" to (sharedPreferences.getString("airConditioning", "N/A")
                            ?: "N/A"),
                        "typeOfAirConditioning" to (sharedPreferences.getString(
                            "typeOfAirConditioning",
                            "N/A"
                        ) ?: "N/A"),
                        "heatingType" to (sharedPreferences.getString("heatingType", "N/A")
                            ?: "N/A"),
                        "additionalAppliances" to (sharedPreferences.getString(
                            "additionalAppliances",
                            "N/A"
                        ) ?: "N/A"),
                        "portableAirFilter" to (sharedPreferences.getString(
                            "portableAirFilter",
                            "N/A"
                        ) ?: "N/A"),
                        "hvacScore" to sharedPreferences.getFloat("hvacScore2", 0f)
                    ),
                    "iaqAttributes" to mapOf(
                        "isKitchen" to (sharedPreferences.getString("isKitchen2", "N/A") ?: "N/A"),
                        "kitchenStoveType" to (sharedPreferences.getString(
                            "kitchenStoveType2",
                            "N/A"
                        ) ?: "N/A"),
                        "kitchenStoveFan" to (sharedPreferences.getString("kitchenStoveFan2", "N/A")
                            ?: "N/A"),
                        "isBathroom" to (sharedPreferences.getString("isBathroom2", "N/A")
                            ?: "N/A"),
                        "bathroomVentilationType" to (sharedPreferences.getString(
                            "bathroomVentilationType2",
                            "N/A"
                        ) ?: "N/A"),
                        "isMoldPresent" to (sharedPreferences.getString("isMoldPresent2", "N/A")
                            ?: "N/A"),
                        "pm25Value" to (sharedPreferences.getString("pm25Value2", "N/A") ?: "N/A"),
                        "indoorHumidity" to (sharedPreferences.getString("indoorHumidity2", "N/A")
                            ?: "N/A"),
                        "outdoorPM25" to (sharedPreferences.getString("outdoorPM252", "N/A")
                            ?: "N/A"),
                        "outdoorHumidity" to (sharedPreferences.getString("outdoorHumidity2", "N/A")
                            ?: "N/A"),
                        "iaqScore" to sharedPreferences.getFloat("iaqScore2", 0f)
                    ),
                    "thermalComfortAttributes" to mapOf(
                        "indoorTemp" to sharedPreferences.getFloat("indoorTemp2", 0f),
                        "outdoorTemp" to sharedPreferences.getFloat("outdoorTemp2", 0f),
                        "thermalComfortScore" to sharedPreferences.getFloat(
                            "thermalComfortScore2",
                            0f
                        )
                    ),
                    "acousticComfortAttributes" to mapOf(
                        "indoorDecibel" to (sharedPreferences.getString("indoorDecibel2", "N/A")
                            ?: "N/A"),
                        "outdoorDecibel" to (sharedPreferences.getString("outdoorDecibel2", "N/A")
                            ?: "N/A"),
                        "indoorNoiseSources" to (sharedPreferences.getString(
                            "indoorNoiseSources2",
                            "N/A"
                        ) ?: "N/A"),
                        "outdoorNoiseSources" to (sharedPreferences.getString(
                            "outdoorNoiseSources2",
                            "N/A"
                        ) ?: "N/A"),
                        "acousticComfortScore" to sharedPreferences.getFloat(
                            "acousticComfortScore2",
                            0f
                        )
                    ),
                    "ieqScore" to sharedPreferences.getFloat("ieqScore2", 0f)
                )
            } else {
                // Private survey data
                surveyData = mapOf(
                    "type" to "private",
                    "dwellingAttributes" to mapOf(
                        "homeType" to (sharedPreferences.getString("homeType", "N/A") ?: "N/A"),
                        "section8" to (sharedPreferences.getString("section8", "No") == "Yes"),
                        "oaklandHousing" to (sharedPreferences.getString(
                            "oaklandHousing",
                            "No"
                        ) == "Yes"),
                        "numPeople" to (sharedPreferences.getString("numPeople", "N/A") ?: "N/A"),
                        "squareFootage" to (sharedPreferences.getString("squareFootage", "N/A")
                            ?: "N/A"),
                        "date" to (sharedPreferences.getString("date", "N/A") ?: "N/A"),
                        "streetIntersection" to (sharedPreferences.getString(
                            "streetIntersection",
                            "N/A"
                        ) ?: "N/A"),
                        "buildingAge" to (sharedPreferences.getString("buildingAge", "N/A")
                            ?: "N/A")
                    ),
                    "hvacAttributes" to mapOf(
                        "hasAirConditioning" to sharedPreferences.getBoolean(
                            "hasAirConditioning",
                            false
                        ),
                        "airConditioningWorks" to sharedPreferences.getBoolean(
                            "airConditioningWorks",
                            false
                        ),
                        "airConditioningType" to (sharedPreferences.getString(
                            "airConditioningType",
                            "N/A"
                        ) ?: "N/A"),
                        "heatingType" to (sharedPreferences.getString("heatingType", "N/A")
                            ?: "N/A"),
                        "additionalAppliances" to (sharedPreferences.getString(
                            "additionalAppliances",
                            "N/A"
                        ) ?: "N/A"),
                        "hasPortableAirFilter" to sharedPreferences.getBoolean(
                            "hasPortableAirFilter",
                            false
                        ),
                        "hvacScore" to sharedPreferences.getFloat("hvacScore", 0f)
                    ),
                    "iaqAttributes" to mapOf(
                        "kitchenStoveType" to (sharedPreferences.getString(
                            "kitchenStoveType",
                            "N/A"
                        ) ?: "N/A"),
                        "kitchenStoveFan" to (sharedPreferences.getString("kitchenStoveFan", "N/A")
                            ?: "N/A"),
                        "bathroomVentilation" to (sharedPreferences.getString(
                            "bathroomVentilation",
                            "N/A"
                        ) ?: "N/A"),
                        "moldPresent" to (sharedPreferences.getString(
                            "moldPresent",
                            "No"
                        ) == "Yes"),
                        "livingRoomBeforeCooking" to (sharedPreferences.getString(
                            "livingRoomBeforeCooking",
                            "N/A"
                        ) ?: "N/A"),
                        "livingRoomAfterCooking" to (sharedPreferences.getString(
                            "livingRoomAfterCooking",
                            "N/A"
                        ) ?: "N/A"),
                        "livingRoomHumidity" to (sharedPreferences.getString(
                            "livingRoomHumidity",
                            "N/A"
                        ) ?: "N/A"),
                        "outdoorPM25" to (sharedPreferences.getString("outdoorPM25", "N/A")
                            ?: "N/A"),
                        "outdoorHumidity" to (sharedPreferences.getString("outdoorHumidity", "N/A")
                            ?: "N/A"),
                        "iaqScore" to sharedPreferences.getFloat("iaqScore", 0f)
                    ),
                    "thermalComfortAttributes" to mapOf(
                        "indoorTemp" to (sharedPreferences.getString("indoorTemperature", "N/A")
                            ?.toFloatOrNull() ?: 0f),
                        "outdoorTemp" to (sharedPreferences.getString("outdoorTemperature", "N/A")
                            ?.toFloatOrNull() ?: 0f),
                        "thermalComfortScore" to sharedPreferences.getFloat(
                            "thermalComfortScore",
                            0f
                        )
                    ),
                    "acousticComfortAttributes" to mapOf(
                        "indoorDecibel" to (sharedPreferences.getString("indoorDecibel", "N/A")
                            ?: "N/A"),
                        "outdoorDecibel" to (sharedPreferences.getString("outdoorDecibel", "N/A")
                            ?: "N/A"),
                        "indoorNoiseSources" to (sharedPreferences.getString(
                            "indoorNoiseSources",
                            "N/A"
                        ) ?: "N/A"),
                        "outdoorNoiseSources" to (sharedPreferences.getString(
                            "outdoorNoiseSources",
                            "N/A"
                        ) ?: "N/A"),
                        "acousticComfortScore" to sharedPreferences.getFloat(
                            "acousticComfortScore",
                            0f
                        )
                    ),
                    "demographicAttributes" to mapOf(
                        "multiracial" to sharedPreferences.getBoolean("multiracial", false),
                        "americanIndian" to sharedPreferences.getBoolean("americanIndian", false),
                        "asian" to sharedPreferences.getBoolean("asian", false),
                        "black" to sharedPreferences.getBoolean("black", false),
                        "hispanic" to sharedPreferences.getBoolean("hispanic", false),
                        "nativeHawaiian" to sharedPreferences.getBoolean("nativeHawaiian", false),
                        "white" to sharedPreferences.getBoolean("white", false),
                        "other" to sharedPreferences.getBoolean("other", false),
                        "otherEthnicity" to (sharedPreferences.getString("otherEthnicity", "N/A")
                            ?: "N/A")
                    ),
                    "ieqScore" to sharedPreferences.getFloat("ieqScore", 0f)
                )
            }
            Log.d("FirebaseUtils", "Submitting data: $surveyData")

            database.child(surveyId).setValue(surveyData)
                .addOnSuccessListener {
                    Log.d(
                        "FirebaseUtils",
                        "Data submitted successfully to surveyData/$surveyId"
                    )
                    callback(true, surveyId)
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseUtils", "Failed to submit data: ${exception.localizedMessage}")
                    callback(false, surveyId)
                }
        }
    }



