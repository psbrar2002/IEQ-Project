package com.tst.ieqproject.utils

import android.content.Context
import android.content.SharedPreferences
import android.widget.TextView

object ScoreUtils2 {

    fun updateIEQScore2(context: Context, sharedPreferences: SharedPreferences, ieqScoreTextView: TextView): Float {
        // Retrieve individual scores from SharedPreferences
        val iaqScore2 = sharedPreferences.getFloat("iaqScore2", 0f).toDouble()
        val acousticComfortScore2 = sharedPreferences.getFloat("acousticComfortScore2", 0f).toDouble()
        val hvacScore2 = sharedPreferences.getFloat("hvacScore2", 0f).toDouble()
        val thermalComfortScore2 = sharedPreferences.getFloat("thermalComfortScore2", 0f).toDouble()

        // Calculate the total score including the thermal comfort score
        val totalScore2 = iaqScore2 + acousticComfortScore2 + hvacScore2 + thermalComfortScore2

        // Assuming the total possible score for these categories is 40
        val ieqScore2 = (totalScore2 / 40) * 100

        // Display the IEQ Score
        ieqScoreTextView.text = "IEQ Score: %.2f%%".format(ieqScore2)
        sharedPreferences.edit().putFloat("ieqScore2", ieqScore2.toFloat()).apply()

        // Return the calculated score
        return ieqScore2.toFloat()
    }
}
