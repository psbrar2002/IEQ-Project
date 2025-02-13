package com.tst.ieqproject.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import org.json.JSONObject

object FirebaseUtils {
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("surveyData")

    /**
     * Generates and saves a survey ID.
     * If offline, a 4-digit survey ID is generated in the same format as online IDs.
     */
    fun generateAndSaveSurveyId(context: Context, isPublic: Boolean, callback: (String) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val existingSurveyId = sharedPreferences.getString("surveyId", null)
        if (existingSurveyId != null) {
            Log.d("FirebaseUtils", "Existing surveyId found: $existingSurveyId")
            callback(existingSurveyId)
            return
        }

        // If no network is available, generate a normal 4-digit offline ID.
        if (!isNetworkAvailable(context)) {
            val offlineId = if (isPublic)
                "OFF-PUB-${(1000..9999).random()}"
            else
                "OFF-PRI-${(1000..9999).random()}"
            sharedPreferences.edit().putString("surveyId", offlineId).apply()
            Log.d("FirebaseUtils", "No network available. Generated offline surveyId: $offlineId")
            callback(offlineId)
            return
        }

        // Otherwise, generate a unique ID from Firebase.
        generateUniqueIdentifier(isPublic) { uniqueIdentifier ->
            sharedPreferences.edit().putString("surveyId", uniqueIdentifier).apply()
            Log.d("FirebaseUtils", "Generated and saved surveyId: $uniqueIdentifier")
            callback(uniqueIdentifier)
        }
    }

    private fun generateUniqueIdentifier(isPublic: Boolean, callback: (String) -> Unit) {
        val surveyTypePrefix = if (isPublic) "PUB" else "PRI"
        val randomFourDigitCode = (1000..9999).random().toString()
        val potentialIdentifier = "$surveyTypePrefix-$randomFourDigitCode"
        database.child(potentialIdentifier).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d("FirebaseUtils", "Identifier $potentialIdentifier exists, generating a new one.")
                    generateUniqueIdentifier(isPublic, callback)
                } else {
                    Log.d("FirebaseUtils", "Identifier $potentialIdentifier is unique, using it.")
                    callback(potentialIdentifier)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseUtils", "Error checking identifier: ${error.message}")
                callback(potentialIdentifier)
            }
        })
    }

    /**
     * Attempts to submit survey data to Firebase.
     * If no network is available, it stores the data locally in the failed submissions queue.
     */
    fun submitSurveyDataToFirebase(
        context: Context,
        sharedPreferences: android.content.SharedPreferences,
        isPublic: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        val surveyId = sharedPreferences.getString("surveyId", null)
        if (surveyId == null) {
            Log.e("FirebaseUtils", "Survey ID is null, unable to submit data.")
            callback(false, "N/A")
            return
        }
        val surveyData: Map<String, Any> = if (isPublic) {
            mapOf(
                "type" to "public",
                "buildingAttributes" to mapOf(
                    "buildingType" to (sharedPreferences.getString("buildingType2", "N/A") ?: "N/A"),
                    "typeOfRoom" to (sharedPreferences.getString("typeOfRoom2", "N/A") ?: "N/A"),
                    "nameofroom" to (sharedPreferences.getString("nameofroom", "N/A") ?: "N/A"),
                    "squareFootage" to (sharedPreferences.getString("squareFootage2", "N/A") ?: "N/A"),
                    "gpsLocation" to (sharedPreferences.getString("gpsLocation2", "N/A") ?: "N/A"),
                    "ageOfBuilding" to (sharedPreferences.getString("ageOfBuilding2", "N/A") ?: "N/A"),
                    "date" to (sharedPreferences.getString("date2", "N/A") ?: "N/A"),
                    "timeOfDay" to (sharedPreferences.getString("timeOfDay2", "N/A") ?: "N/A"),
                    "program" to (sharedPreferences.getString("program", "N/A") ?: "N/A"),
                    "city" to (sharedPreferences.getString("city", "N/A") ?: "N/A"),
                    "season" to (sharedPreferences.getString("season2", "N/A") ?: "N/A")
                ),
                "hvacAttributes" to mapOf(
                    "airConditioning" to (sharedPreferences.getString("airConditioning", "N/A") ?: "N/A"),
                    "typeOfAirConditioning" to (sharedPreferences.getString("typeOfAirConditioning", "N/A") ?: "N/A"),
                    "heatingType" to (sharedPreferences.getString("heatingType", "N/A") ?: "N/A"),
                    "additionalAppliances" to (sharedPreferences.getString("additionalAppliances", "N/A") ?: "N/A"),
                    "portableAirFilter" to (sharedPreferences.getString("portableAirFilter", "N/A") ?: "N/A"),
                    "hvacScore" to sharedPreferences.getFloat("hvacScore2", 0f)
                ),
                "iaqAttributes" to mapOf(
                    "isKitchen" to (sharedPreferences.getString("isKitchen2", "N/A") ?: "N/A"),
                    "kitchenStoveType" to (sharedPreferences.getString("kitchenStoveType2", "N/A") ?: "N/A"),
                    "kitchenStoveFan" to (sharedPreferences.getString("kitchenStoveFan2", "N/A") ?: "N/A"),
                    "isBathroom" to (sharedPreferences.getString("isBathroom2", "N/A") ?: "N/A"),
                    "bathroomVentilationType" to (sharedPreferences.getString("bathroomVentilationType2", "N/A") ?: "N/A"),
                    "isMoldPresent" to (sharedPreferences.getString("isMoldPresent2", "N/A") ?: "N/A"),
                    "pm25Value" to (sharedPreferences.getString("pm25Value2", "N/A") ?: "N/A"),
                    "indoorHumidity" to (sharedPreferences.getString("indoorHumidity2", "N/A") ?: "N/A"),
                    "outdoorPM25" to (sharedPreferences.getString("outdoorPM252", "N/A") ?: "N/A"),
                    "outdoorHumidity" to (sharedPreferences.getString("outdoorHumidity2", "N/A") ?: "N/A"),
                    "iaqScore" to sharedPreferences.getFloat("iaqScore2", 0f)
                ),
                "thermalComfortAttributes" to mapOf(
                    "indoorTemp" to sharedPreferences.getFloat("indoorTemp2", 0f),
                    "outdoorTemp" to sharedPreferences.getFloat("outdoorTemp2", 0f),
                    "thermalComfortScore" to sharedPreferences.getFloat("thermalComfortScore2", 0f)
                ),
                "acousticComfortAttributes" to mapOf(
                    "indoorDecibel" to (sharedPreferences.getString("indoorDecibel2", "N/A") ?: "N/A"),
                    "outdoorDecibel" to (sharedPreferences.getString("outdoorDecibel2", "N/A") ?: "N/A"),
                    "indoorNoiseSources" to (sharedPreferences.getString("indoorNoiseSources2", "N/A") ?: "N/A"),
                    "outdoorNoiseSources" to (sharedPreferences.getString("outdoorNoiseSources2", "N/A") ?: "N/A"),
                    "acousticComfortScore" to sharedPreferences.getFloat("acousticComfortScore2", 0f)
                ),
                "ieqScore" to sharedPreferences.getFloat("ieqScore2", 0f)
            )
        } else {
            mapOf(
                "type" to "private",
                "dwellingAttributes" to mapOf(
                    "homeType" to (sharedPreferences.getString("homeType", "N/A") ?: "N/A"),
                    "section8" to (sharedPreferences.getString("section8", "No") == "Yes"),
                    "oaklandHousing" to (sharedPreferences.getString("oaklandHousing", "No") == "Yes"),
                    "numPeople" to (sharedPreferences.getString("numPeople", "N/A") ?: "N/A"),
                    "squareFootage" to (sharedPreferences.getString("squareFootage", "N/A") ?: "N/A"),
                    "date" to (sharedPreferences.getString("date", "N/A") ?: "N/A"),
                    "timeOfDay" to (sharedPreferences.getString("timeOfDay", "N/A") ?: "N/A"),
                    "program" to (sharedPreferences.getString("program", "N/A") ?: "N/A"),
                    "city" to (sharedPreferences.getString("city", "N/A") ?: "N/A"),
                    "streetIntersection" to (sharedPreferences.getString("streetIntersection", "N/A") ?: "N/A"),
                    "buildingAge" to (sharedPreferences.getString("buildingAge", "N/A") ?: "N/A")
                ),
                "hvacAttributes" to mapOf(
                    "hasAirConditioning" to sharedPreferences.getBoolean("hasAirConditioning", false),
                    "airConditioningWorks" to sharedPreferences.getBoolean("airConditioningWorks", false),
                    "airConditioningType" to (sharedPreferences.getString("airConditioningType", "N/A") ?: "N/A"),
                    "heatingType" to (sharedPreferences.getString("heatingType", "N/A") ?: "N/A"),
                    "additionalAppliances" to (sharedPreferences.getString("additionalAppliances", "N/A") ?: "N/A"),
                    "hasPortableAirFilter" to sharedPreferences.getBoolean("hasPortableAirFilter", false),
                    "hvacScore" to sharedPreferences.getFloat("hvacScore", 0f)
                ),
                "iaqAttributes" to mapOf(
                    "kitchenStoveType" to (sharedPreferences.getString("kitchenStoveType", "N/A") ?: "N/A"),
                    "kitchenStoveFan" to (sharedPreferences.getString("kitchenStoveFan", "N/A") ?: "N/A"),
                    "bathroomVentilation" to (sharedPreferences.getString("bathroomVentilation", "N/A") ?: "N/A"),
                    "moldPresent" to (sharedPreferences.getString("moldPresent", "No") == "Yes"),
                    "livingRoomBeforeCooking" to (sharedPreferences.getString("livingRoomBeforeCooking", "N/A") ?: "N/A"),
                    "livingRoomAfterCooking" to (sharedPreferences.getString("livingRoomAfterCooking", "N/A") ?: "N/A"),
                    "livingRoomHumidity" to (sharedPreferences.getString("livingRoomHumidity", "N/A") ?: "N/A"),
                    "outdoorPM25" to (sharedPreferences.getString("outdoorPM25", "N/A") ?: "N/A"),
                    "outdoorHumidity" to (sharedPreferences.getString("outdoorHumidity", "N/A") ?: "N/A"),
                    "iaqScore" to sharedPreferences.getFloat("iaqScore", 0f)
                ),
                "thermalComfortAttributes" to mapOf(
                    "indoorTemp" to (sharedPreferences.getString("indoorTemperature", "N/A")?.toFloatOrNull() ?: 0f),
                    "outdoorTemp" to (sharedPreferences.getString("outdoorTemperature", "N/A")?.toFloatOrNull() ?: 0f),
                    "thermalComfortScore" to sharedPreferences.getFloat("thermalComfortScore", 0f)
                ),
                "acousticComfortAttributes" to mapOf(
                    "indoorDecibel" to (sharedPreferences.getString("indoorDecibel", "N/A") ?: "N/A"),
                    "outdoorDecibel" to (sharedPreferences.getString("outdoorDecibel", "N/A") ?: "N/A"),
                    "indoorNoiseSources" to (sharedPreferences.getString("indoorNoiseSources", "N/A") ?: "N/A"),
                    "outdoorNoiseSources" to (sharedPreferences.getString("outdoorNoiseSources", "N/A") ?: "N/A"),
                    "acousticComfortScore" to sharedPreferences.getFloat("acousticComfortScore", 0f)
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
                    "otherEthnicity" to (sharedPreferences.getString("otherEthnicity", "N/A") ?: "N/A")
                ),
                "ieqScore" to sharedPreferences.getFloat("ieqScore", 0f)
            )
        }

        Log.d("FirebaseUtils", "Submitting data: $surveyData")

        // If no network is available, store the submission locally.
        if (!isNetworkAvailable(context)) {
            Log.d("FirebaseUtils", "No network available. Storing failed submission locally. SurveyID: $surveyId")
            storeFailedSubmission(context, surveyData, surveyId)
            Toast.makeText(context, "No network. Survey saved to resubmit queue.", Toast.LENGTH_SHORT).show()
            callback(false, surveyId)
            return
        }

        // Otherwise, attempt to submit to Firebase.
        database.child(surveyId).setValue(surveyData)
            .addOnSuccessListener {
                Log.d("FirebaseUtils", "Data submitted successfully to surveyData/$surveyId")
                callback(true, surveyId)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseUtils", "Failed to submit data: ${exception.localizedMessage}")
                storeFailedSubmission(context, surveyData, surveyId)
                callback(false, surveyId)
            }
    }

    private fun storeFailedSubmission(context: Context, surveyData: Map<String, Any>, surveyId: String) {
        val surveyDataWithId = surveyData.toMutableMap()
        surveyDataWithId["surveyId"] = surveyId
        // Add a timestamp for display if not already present.
        if (!surveyDataWithId.containsKey("timestamp")) {
            surveyDataWithId["timestamp"] = System.currentTimeMillis()
        }
        FailedSubmissionsManager.addFailedSubmission(context, surveyDataWithId)
        Log.d("FirebaseUtils", "Survey $surveyId stored locally in the resubmit queue.")
        // Clear the survey ID so that the next survey gets a new one.
        val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        prefs.edit().remove("surveyId").apply()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }
    }
}
