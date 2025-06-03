package com.tst.ieqproject.utils

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object FailedSubmissionsManager {
    private const val PREF_KEY = "FAILED_SUBMISSIONS"
    private const val MAX_FAILED_SUBMISSIONS = 15

    fun addFailedSubmission(context: Context, surveyData: Map<String, Any>) {
        val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        // Get the current queue
        val jsonStr = prefs.getString(PREF_KEY, "[]")
        val jsonArray = JSONArray(jsonStr)

        // If we've reached capacity, optionally reject new ones or remove oldest.
        if (jsonArray.length() >= MAX_FAILED_SUBMISSIONS) {
            Log.w("FailedSubmissionsManager", "Capacity reached. Removing oldest submission.")
            // Remove the first (oldest) element.
            val newArray = JSONArray()
            for (i in 1 until jsonArray.length()) {
                newArray.put(jsonArray.getJSONObject(i))
            }
            // Replace the array.
            prefs.edit().putString(PREF_KEY, newArray.toString()).apply()
        }

        // Add the new submission.
        val jsonObject = JSONObject(surveyData)
        jsonArray.put(jsonObject)
        prefs.edit().putString(PREF_KEY, jsonArray.toString()).apply()
        Log.d("FailedSubmissionsManager", "Added submission: ${jsonObject.toString()}")
    }

    fun getFailedSubmissions(context: Context): MutableList<String> {
        val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(PREF_KEY, "[]")
        Log.d("FailedSubmissionsManager", "Current stored submissions: $jsonStr")
        val jsonArray = JSONArray(jsonStr)
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getJSONObject(i).toString())
        }
        return list
    }

    fun removeFailedSubmission(context: Context, index: Int) {
        val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(PREF_KEY, "[]")
        val jsonArray = JSONArray(jsonStr)
        val newArray = JSONArray()
        for (i in 0 until jsonArray.length()) {
            if (i != index) {
                newArray.put(jsonArray.getJSONObject(i))
            }
        }
        prefs.edit().putString(PREF_KEY, newArray.toString()).apply()
        Log.d("FailedSubmissionsManager", "Removed submission at index: $index. New queue: ${newArray.toString()}")
    }

    fun getCapacityRemaining(context: Context): Int {
        val currentCount = getFailedSubmissions(context).size
        return MAX_FAILED_SUBMISSIONS - currentCount
    }
}
