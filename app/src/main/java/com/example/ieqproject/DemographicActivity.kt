package com.example.ieqproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ieqproject.utils.FirebaseUtils
import com.example.ieqproject.utils.ScoreUtils

class DemographicActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var ieqScoreTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demographic)

        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)

        // Find the IEQ score TextView
        ieqScoreTextView = findViewById(R.id.ieqScoreTextView)

        // Restore saved data
        restoreData()

        // Update IEQ Score on screen
        ScoreUtils.updateIEQScore(this, sharedPreferences, ieqScoreTextView)

        val backButton: Button = findViewById(R.id.backButton)
        val submitButton: Button = findViewById(R.id.submitButton)

        backButton.setOnClickListener {
            saveDataLocally()
            finish()  // Navigate back to the previous activity
        }

        submitButton.setOnClickListener {
            saveDataLocally()
            FirebaseUtils.submitDataToFirebase(this, sharedPreferences) { success ->
                if (success) {
                    clearAllData()
                    Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show()
                    // Navigate back to the start
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Failed to submit data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("multiracial", findViewById<CheckBox>(R.id.multiracialCheckBox).isChecked)
        editor.putBoolean("asian", findViewById<CheckBox>(R.id.asianCheckBox).isChecked)
        editor.putBoolean("black", findViewById<CheckBox>(R.id.blackCheckBox).isChecked)
        editor.putBoolean("hispanic", findViewById<CheckBox>(R.id.hispanicCheckBox).isChecked)
        editor.putBoolean("nativeHawaiian", findViewById<CheckBox>(R.id.nativeHawaiianCheckBox).isChecked)
        editor.putBoolean("white", findViewById<CheckBox>(R.id.whiteCheckBox).isChecked)
        editor.putBoolean("other", findViewById<CheckBox>(R.id.otherCheckBox).isChecked)
        editor.putString("otherEthnicity", findViewById<EditText>(R.id.otherEthnicityEditText).text.toString())
        editor.apply()
    }

    private fun restoreData() {
        findViewById<CheckBox>(R.id.multiracialCheckBox).isChecked = sharedPreferences.getBoolean("multiracial", false)
        findViewById<CheckBox>(R.id.asianCheckBox).isChecked = sharedPreferences.getBoolean("asian", false)
        findViewById<CheckBox>(R.id.blackCheckBox).isChecked = sharedPreferences.getBoolean("black", false)
        findViewById<CheckBox>(R.id.hispanicCheckBox).isChecked = sharedPreferences.getBoolean("hispanic", false)
        findViewById<CheckBox>(R.id.nativeHawaiianCheckBox).isChecked = sharedPreferences.getBoolean("nativeHawaiian", false)
        findViewById<CheckBox>(R.id.whiteCheckBox).isChecked = sharedPreferences.getBoolean("white", false)
        findViewById<CheckBox>(R.id.otherCheckBox).isChecked = sharedPreferences.getBoolean("other", false)
        findViewById<EditText>(R.id.otherEthnicityEditText).setText(sharedPreferences.getString("otherEthnicity", ""))
    }

    private fun clearAllData() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        restoreData()
    }
}
