package com.tst.ieqproject.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.tst.ieqproject.R

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
        // Determine the color based on the score
        val scoreColor = when {
            ieqScore2 >= 85 -> ContextCompat.getColor(context, R.color.colorA)
            ieqScore2 >= 75 -> ContextCompat.getColor(context, R.color.colorB)
            ieqScore2 >= 65 -> ContextCompat.getColor(context, R.color.colorC)
            ieqScore2 >= 55 -> ContextCompat.getColor(context, R.color.colorD)
            ieqScore2 >= 45 -> ContextCompat.getColor(context, R.color.colorE)
            ieqScore2 >= 35 -> ContextCompat.getColor(context, R.color.colorF)
            else -> ContextCompat.getColor(context, R.color.colorG)
        }

        // Create a SpannableString with the color applied to the score part
        val ieqScoreText = "IEQ Score: "
        val scoreText = "%.2f%%".format(ieqScore2)
        val spannableString = SpannableString(ieqScoreText + scoreText).apply {
            setSpan(ForegroundColorSpan(scoreColor), ieqScoreText.length, ieqScoreText.length + scoreText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Display the colored IEQ Score
        ieqScoreTextView.text = spannableString

        // Return the calculated score
        return ieqScore2.toFloat()
    }
}
