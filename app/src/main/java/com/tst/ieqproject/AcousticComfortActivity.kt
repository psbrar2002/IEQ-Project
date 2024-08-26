package com.tst.ieqproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tst.ieqproject.utils.FirebaseUtils
import com.tst.ieqproject.utils.ScoreUtils

class AcousticComfortActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var acousticComfortScoreTextView: TextView
    private lateinit var ieqScoreTextView: TextView
    private lateinit var database: DatabaseReference
    private var acousticComfortScore: Double = 0.0
    private lateinit var surveyIdTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acoustic_comfort)

        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        surveyIdTextView = findViewById(R.id.surveyIdTextView)

        val isPublicSurvey = false // Set this based on your logic

        // Use a callback to ensure the survey ID is ready
        FirebaseUtils.generateAndSaveSurveyId(this, isPublicSurvey) { surveyId ->
            // This code runs after the survey ID is generated
            surveyIdTextView.text = "Survey ID: $surveyId"
            Log.d("DwellingAttributesActivity", "Survey ID displayed: $surveyId")
        }
        database = FirebaseDatabase.getInstance().reference

        acousticComfortScoreTextView = findViewById(R.id.acousticComfortScoreTextView)
        ieqScoreTextView = findViewById(R.id.ieqScoreTextView)
        val openInstructionsLink = findViewById<TextView>(R.id.openInstructionsStoreLink)
        val content = "IEQ Survey Instructions"
        val spannableString = SpannableString(content)
        spannableString.setSpan(UnderlineSpan(), 0, content.length, 0)
        openInstructionsLink.text = spannableString

// If you want to set it as a clickable link, you can use:
        openInstructionsLink.setOnClickListener {
            val url = "https://drive.google.com/file/d/1XI4uJaBIzbrHDUG1hekpgHTL1s_HHA9A/view"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        // Restore saved data
        restoreData()
        setupAcousticComfortListener()

        // Listeners for real-time Acoustic Comfort score update
        setupListeners()

        // Setup navigation and submit buttons
        setupNavigationButtons()
    }

    private fun setupListeners() {
        val indoorDecibelEditText: EditText = findViewById(R.id.indoorDecibelEditText)
        val outdoorDecibelEditText: EditText = findViewById(R.id.outdoorDecibelEditText)
        val indoorNoiseSourcesEditText: EditText = findViewById(R.id.indoorNoiseSourcesEditText)
        val outdoorNoiseSourcesEditText: EditText = findViewById(R.id.outdoorNoiseSourcesEditText)

        val listener = TextWatcherAdapter { updateAcousticComfortScore() }

        indoorDecibelEditText.addTextChangedListener(listener)
        outdoorDecibelEditText.addTextChangedListener(listener)
        indoorNoiseSourcesEditText.addTextChangedListener(listener)
        outdoorNoiseSourcesEditText.addTextChangedListener(listener)
    }

    private fun setupAcousticComfortListener() {
        val indoorDecibelEditText = findViewById<EditText>(R.id.indoorDecibelEditText)
        indoorDecibelEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateAcousticComfortScore()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit Survey")
        builder.setMessage("Are you sure you want to exit the survey? Your progress will not be saved.")

        builder.setPositiveButton("Yes") { dialog, which ->
            // Clear all data before exiting
            clearAllData()

            // Navigate back to the main screen
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun clearAllData() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        restoreData()
    }

    private fun updateAcousticComfortScore() {
        acousticComfortScore = 0.0 // Initialize score to 0

        val indoorDecibel = findViewById<EditText>(R.id.indoorDecibelEditText).text.toString().toDoubleOrNull()

        // Only score if the input is valid and within the desired range
        if (indoorDecibel != null && indoorDecibel < 46) {
            acousticComfortScore += 10.0
        }

        // Display the Acoustic Comfort Score
        acousticComfortScoreTextView.text = "Acoustic Comfort Score: $acousticComfortScore"
        sharedPreferences.edit().putFloat("acousticComfortScore", acousticComfortScore.toFloat()).apply()
        ScoreUtils.updateIEQScore(this, sharedPreferences, ieqScoreTextView)
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putString("indoorDecibel", findViewById<EditText>(R.id.indoorDecibelEditText).text.toString())
        editor.putString("outdoorDecibel", findViewById<EditText>(R.id.outdoorDecibelEditText).text.toString())
        editor.putString("indoorNoiseSources", findViewById<EditText>(R.id.indoorNoiseSourcesEditText).text.toString())
        editor.putString("outdoorNoiseSources", findViewById<EditText>(R.id.outdoorNoiseSourcesEditText).text.toString())
        editor.apply()
    }

    private fun restoreData() {
        findViewById<EditText>(R.id.indoorDecibelEditText).setText(sharedPreferences.getString("indoorDecibel", ""))
        findViewById<EditText>(R.id.outdoorDecibelEditText).setText(sharedPreferences.getString("outdoorDecibel", ""))
        findViewById<EditText>(R.id.indoorNoiseSourcesEditText).setText(sharedPreferences.getString("indoorNoiseSources", ""))
        findViewById<EditText>(R.id.outdoorNoiseSourcesEditText).setText(sharedPreferences.getString("outdoorNoiseSources", ""))
        updateAcousticComfortScore()
    }

    private fun setupNavigationButtons() {
        val backButton: Button = findViewById(R.id.backButton)
        val nextButton: Button = findViewById(R.id.nextButton)

        backButton.setOnClickListener {
            saveDataLocally()
            finish()  // Navigate back to the previous activity
        }

        nextButton.setOnClickListener {
            saveDataLocally()
            val intent = Intent(this, DemographicActivity::class.java)
            startActivity(intent)
        }
        val exitButton: Button = findViewById(R.id.exitButton)
        exitButton.setOnClickListener {
            showExitConfirmationDialog()
        }

    }
}

// Helper class to simplify text watcher implementation
class TextWatcherAdapter(private val onTextChanged: () -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        onTextChanged()
    }
    override fun afterTextChanged(s: Editable?) {}
}
