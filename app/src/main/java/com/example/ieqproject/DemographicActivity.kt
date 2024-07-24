package com.example.ieqproject

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DemographicActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demographic)

        sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference

        // Restore saved data
        restoreData()

        val backButton: Button = findViewById(R.id.backButton)
        val submitButton: Button = findViewById(R.id.submitButton)

        backButton.setOnClickListener {
            saveDataLocally()
            finish()  // Navigate back to the previous activity
        }

        submitButton.setOnClickListener {
            saveDataLocally()
            FirebaseUtils.submitDataToFirebase(this, sharedPreferences)
        }
    }

    private fun saveDataLocally() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("multiracial", findViewById<CheckBox>(R.id.multiracialCheckBox).isChecked)
        editor.putBoolean("americanIndian", findViewById<CheckBox>(R.id.americanIndianCheckBox).isChecked)
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
        findViewById<CheckBox>(R.id.americanIndianCheckBox).isChecked = sharedPreferences.getBoolean("americanIndian", false)
        findViewById<CheckBox>(R.id.asianCheckBox).isChecked = sharedPreferences.getBoolean("asian", false)
        findViewById<CheckBox>(R.id.blackCheckBox).isChecked = sharedPreferences.getBoolean("black", false)
        findViewById<CheckBox>(R.id.hispanicCheckBox).isChecked = sharedPreferences.getBoolean("hispanic", false)
        findViewById<CheckBox>(R.id.nativeHawaiianCheckBox).isChecked = sharedPreferences.getBoolean("nativeHawaiian", false)
        findViewById<CheckBox>(R.id.whiteCheckBox).isChecked = sharedPreferences.getBoolean("white", false)
        findViewById<CheckBox>(R.id.otherCheckBox).isChecked = sharedPreferences.getBoolean("other", false)
        findViewById<EditText>(R.id.otherEthnicityEditText).setText(sharedPreferences.getString("otherEthnicity", ""))
    }

//    private fun submitDataToFirebase() {
//        val multiracial = sharedPreferences.getBoolean("multiracial", false)
//        val americanIndian = sharedPreferences.getBoolean("americanIndian", false)
//        val asian = sharedPreferences.getBoolean("asian", false)
//        val black = sharedPreferences.getBoolean("black", false)
//        val hispanic = sharedPreferences.getBoolean("hispanic", false)
//        val nativeHawaiian = sharedPreferences.getBoolean("nativeHawaiian", false)
//        val white = sharedPreferences.getBoolean("white", false)
//        val other = sharedPreferences.getBoolean("other", false)
//        val otherEthnicity = sharedPreferences.getString("otherEthnicity", "")
//
//        val demographicAttributes = DemographicAttributes(
//            multiracial,
//            americanIndian,
//            asian,
//            black,
//            hispanic,
//            nativeHawaiian,
//            white,
//            other,
//            otherEthnicity ?: ""
//        )
//
//        database.child("demographicAttributes").push().setValue(demographicAttributes)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Failed to submit data", Toast.LENGTH_SHORT).show()
//            }
//    }
}
