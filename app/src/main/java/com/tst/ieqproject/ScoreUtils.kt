package com.tst1.ieqproject.utils

import android.content.Context
import android.content.SharedPreferences
import android.widget.TextView

object ScoreUtils {

    fun updateIEQScore(context: Context, sharedPreferences: SharedPreferences, ieqScoreTextView: TextView) {
        // Retrieve individual scores from SharedPreferences
        val iaqScore = sharedPreferences.getFloat("iaqScore", 0f).toDouble()
        val acousticComfortScore = sharedPreferences.getFloat("acousticComfortScore", 0f).toDouble()
        val hvacScore = sharedPreferences.getFloat("hvacScore", 0f).toDouble()
        val thermalComfortScore = sharedPreferences.getFloat("thermalComfortScore", 0f).toDouble()

        // Calculate the total score including the thermal comfort score
        val totalScore = iaqScore + acousticComfortScore + hvacScore + thermalComfortScore

        // Assuming the total possible score for these categories is 40
        val ieqScore = (totalScore / 40) * 100

        // Display the IEQ Score
        ieqScoreTextView.text = "IEQ Score: %.2f%%".format(ieqScore)
        sharedPreferences.edit().putFloat("ieqScore", ieqScore.toFloat()).apply()
    }
}
