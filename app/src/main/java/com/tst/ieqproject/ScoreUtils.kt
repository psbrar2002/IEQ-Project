package com.tst.ieqproject.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.tst.ieqproject.R

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

        // Save the IEQ Score in SharedPreferences
        sharedPreferences.edit().putFloat("ieqScore", ieqScore.toFloat()).apply()

        // Determine the color based on the score
        val scoreColor = when {
            ieqScore >= 85 -> ContextCompat.getColor(context, R.color.colorA)
            ieqScore >= 75 -> ContextCompat.getColor(context, R.color.colorB)
            ieqScore >= 65 -> ContextCompat.getColor(context, R.color.colorC)
            ieqScore >= 55 -> ContextCompat.getColor(context, R.color.colorD)
            ieqScore >= 45 -> ContextCompat.getColor(context, R.color.colorE)
            ieqScore >= 35 -> ContextCompat.getColor(context, R.color.colorF)
            else -> ContextCompat.getColor(context, R.color.colorG)
        }

        // Create a SpannableString with the color applied to the score part
        val ieqScoreText = "IEQ Score: "
        val scoreText = "%.2f%%".format(ieqScore)
        val spannableString = SpannableString(ieqScoreText + scoreText).apply {
            setSpan(ForegroundColorSpan(scoreColor), ieqScoreText.length, ieqScoreText.length + scoreText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Display the colored IEQ Score
        ieqScoreTextView.text = spannableString

        // Return the calculated score
        return ieqScore.toFloat()
    }
}
