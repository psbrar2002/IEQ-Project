package com.tst.ieqproject.utils

import android.content.Context
import android.content.SharedPreferences
import android.widget.TextView

object ScoreUtils {

    fun updateIEQScore(context: Context, sharedPreferences: SharedPreferences, ieqScoreTextView: TextView): Float {
        // Retrieve individual scores from SharedPreferences with fallback for incorrect types
        val iaqScore = sharedPreferences.getFloat("iaqScore", 0f).toDouble()
        val acousticComfortScore = sharedPreferences.getFloat("acousticComfortScore", 0f).toDouble()

        // Handle the case where hvacScore might have been stored as a String
        val hvacScore: Double = try {
            sharedPreferences.getFloat("hvacScore", 0f).toDouble()
        } catch (e: ClassCastException) {
            val hvacString = sharedPreferences.getString("hvacScore", "0")
            hvacString?.toDoubleOrNull() ?: 0.0
        }

        val thermalComfortScore = sharedPreferences.getFloat("thermalComfortScore", 0f).toDouble()

        // Calculate the total score
        val totalScore = iaqScore + acousticComfortScore + hvacScore + thermalComfortScore

        // Assuming the total possible score for these categories is 40
        val ieqScore = (totalScore / 40) * 100

        // Display the IEQ Score
        ieqScoreTextView.text = "IEQ Score: %.2f%%".format(ieqScore)
        sharedPreferences.edit().putFloat("ieqScore", ieqScore.toFloat()).apply()

        // Return the calculated score
        return ieqScore.toFloat()
    }
}
