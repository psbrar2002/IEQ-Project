package com.tst.ieqproject

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.tst.ieqproject.utils.FirebaseUtils
import com.tst.ieqproject.utils.ScoreUtils
import com.tst.ieqproject.utils.ScoreUtils2
import android.content.SharedPreferences
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat

class SubmissionActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var isPublicSurvey: Boolean = false
    private lateinit var ieqScoreTextView: TextView
    private lateinit var surveyIdentifierTextView: TextView
    private lateinit var surveyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submission)

        sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        isPublicSurvey = intent.getBooleanExtra("isPublicSurvey", false)
        ieqScoreTextView = findViewById(R.id.ieqScoreTextView)
        surveyIdentifierTextView = findViewById(R.id.surveyIdentifierTextView)

        // Instead of defaulting to "0000", generate (or retrieve) the proper survey ID.
        FirebaseUtils.generateAndSaveSurveyId(this, isPublicSurvey) { id ->
            surveyId = id
            surveyIdentifierTextView.text = "Survey ID: $id"
        }

        // Set up links and buttons.
        findViewById<TextView>(R.id.buildAirPurifierLink).setOnClickListener {
            openLink("https://www.commonhumanitycollective.org/purifier/buildyourown/")
        }
        findViewById<TextView>(R.id.writeLetterLink).setOnClickListener {
            openLink("https://oaklandtenantrights.org/tenant-rights/repairs/")
        }
        findViewById<TextView>(R.id.getSupportLink).setOnClickListener {
            openLink("https://baytanc.com/east-of-the-lake/")
        }
        findViewById<TextView>(R.id.housingRightsLink).setOnClickListener {
            openLink("https://ebclc.org/get-help/housing-services/")
        }
        findViewById<TextView>(R.id.writeComplaintLink).setOnClickListener {
            openLink("https://www.oaklandca.gov/services/report-a-property-complaint")
        }

        // Exit button: Clear all data except FAILED_SUBMISSIONS and navigate to MainActivity.
        findViewById<Button>(R.id.exitButton).setOnClickListener {
            clearAllData()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateIEQScore()
    }

    private fun updateIEQScore() {
        val sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val ieqScore: Float = if (isPublicSurvey) {
            ScoreUtils2.updateIEQScore2(this, sharedPreferences, ieqScoreTextView)
        } else {
            ScoreUtils.updateIEQScore(this, sharedPreferences, ieqScoreTextView)
        }
        Log.d("SubmissionActivity", "Calculated IEQ score: $ieqScore")
        val scoreColor = when {
            ieqScore >= 85 -> ContextCompat.getColor(this, R.color.colorA)
            ieqScore >= 75 -> ContextCompat.getColor(this, R.color.colorB)
            ieqScore >= 65 -> ContextCompat.getColor(this, R.color.colorC)
            ieqScore >= 55 -> ContextCompat.getColor(this, R.color.colorD)
            ieqScore >= 45 -> ContextCompat.getColor(this, R.color.colorE)
            ieqScore >= 35 -> ContextCompat.getColor(this, R.color.colorF)
            else -> ContextCompat.getColor(this, R.color.colorG)
        }
        val ieqScoreText = "IEQ Score: "
        val scoreText = "%.2f%%".format(ieqScore)
        val spannableString = SpannableString(ieqScoreText + scoreText).apply {
            setSpan(ForegroundColorSpan(scoreColor), ieqScoreText.length, ieqScoreText.length + scoreText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        ieqScoreTextView.text = spannableString
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun clearAllData() {
        val editor = sharedPreferences.edit()
        // Remove all keys except FAILED_SUBMISSIONS.
        for (key in sharedPreferences.all.keys) {
            if (key != "FAILED_SUBMISSIONS") {
                editor.remove(key)
            }
        }
        editor.apply()
    }
}
