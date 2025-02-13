package com.tst.ieqproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.tst.ieqproject.utils.FailedSubmissionsManager
import com.tst.ieqproject.utils.FirebaseUtils
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class FailedSubmission(
    val surveyId: String,
    val timestamp: Long,
    val json: String
) {
    override fun toString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateString = formatter.format(Date(timestamp))
        return "Survey ID: $surveyId\nSubmitted on: $dateString"
    }
}

class ResubmitActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var capacityTextView: TextView
    private lateinit var submitAllButton: Button
    private lateinit var exitButton: ImageButton
    private lateinit var failedSubmissions: MutableList<FailedSubmission>
    private lateinit var adapter: ArrayAdapter<FailedSubmission>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resubmit)

        capacityTextView = findViewById(R.id.capacityTextView)
        listView = findViewById(R.id.failedSubmissionsListView)
        submitAllButton = findViewById(R.id.submitAllButton)
        exitButton = findViewById(R.id.exitButtonWithIcon)
        exitButton.setOnClickListener {
            showExitConfirmationDialog()
        }

        loadFailedSubmissions()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, failedSubmissions)
        listView.adapter = adapter
        updateCapacityDisplay()

        // Short click: resubmit individual submission.
        listView.setOnItemClickListener { _, _, position, _ ->
            val failedSubmission = failedSubmissions[position]
            AlertDialog.Builder(this)
                .setTitle("Resubmit Survey")
                .setMessage("Do you want to resubmit survey ${failedSubmission.surveyId}?")
                .setPositiveButton("Resubmit") { _, _ ->
                    resubmitSurvey(failedSubmission)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Long click: delete a submission.
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val failedSubmission = failedSubmissions[position]
            AlertDialog.Builder(this)
                .setTitle("Delete Submission")
                .setMessage("Do you want to delete survey ${failedSubmission.surveyId} from the queue?")
                .setPositiveButton("Delete") { _, _ ->
                    FailedSubmissionsManager.removeFailedSubmission(this, position)
                    failedSubmissions.removeAt(position)
                    adapter.notifyDataSetChanged()
                    updateCapacityDisplay()
                    Toast.makeText(this, "Submission deleted.", Toast.LENGTH_SHORT).show()
                    if (failedSubmissions.isEmpty()) {
                        finish() // Close activity if the queue is empty.
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }

        // "Submit All" button: iterate over a copy of the list and resubmit each.
        submitAllButton.setOnClickListener {
            if (!FirebaseUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "No internet connection. Please try again later.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Create a copy to iterate over safely.
            val submissionsCopy = failedSubmissions.toList()
            for (submission in submissionsCopy) {
                resubmitSurvey(submission)
            }
            // Schedule a refresh after a delay.
            listView.postDelayed({
                loadFailedSubmissions()
                adapter.clear()
                adapter.addAll(failedSubmissions)
                adapter.notifyDataSetChanged()
                updateCapacityDisplay()
                if (failedSubmissions.isEmpty()) {
                    finish()
                }
            }, 3000)
        }
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit Survey")
        builder.setMessage("Are you sure you want to exit? Your failed submissions will remain available.")
        builder.setPositiveButton("Yes") { dialog, _ ->
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun loadFailedSubmissions() {
        val jsonList = FailedSubmissionsManager.getFailedSubmissions(this)
        failedSubmissions = mutableListOf()
        for (jsonStr in jsonList) {
            try {
                val jsonObject = JSONObject(jsonStr)
                val surveyId = jsonObject.optString("surveyId", "N/A")
                val timestamp = jsonObject.optLong("timestamp", 0L)
                failedSubmissions.add(FailedSubmission(surveyId, timestamp, jsonStr))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (failedSubmissions.isEmpty()) {
            Toast.makeText(this, "No failed submissions found.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Loaded ${failedSubmissions.size} failed submission(s).", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCapacityDisplay() {
        val remaining = FailedSubmissionsManager.getCapacityRemaining(this)
        capacityTextView.text = "Capacity remaining: $remaining"
    }

    private fun resubmitSurvey(failedSubmission: FailedSubmission) {
        if (!FirebaseUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection. Please try again later.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val jsonObject = JSONObject(failedSubmission.json)
            val surveyId = jsonObject.optString("surveyId", "")
            if (surveyId.isEmpty()) {
                Toast.makeText(this, "Invalid survey data. Cannot resubmit.", Toast.LENGTH_SHORT).show()
                return
            }
            val surveyData: Map<String, Any?> = jsonToMap(jsonObject)
            FirebaseDatabase.getInstance().getReference("surveyData").child(surveyId)
                .setValue(surveyData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Survey $surveyId resubmitted successfully.", Toast.LENGTH_SHORT).show()
                    // Remove the item using its surveyId (to avoid index issues).
                    val index = failedSubmissions.indexOfFirst { it.surveyId == surveyId }
                    if (index != -1) {
                        FailedSubmissionsManager.removeFailedSubmission(this, index)
                        failedSubmissions.removeAt(index)
                        adapter.notifyDataSetChanged()
                        updateCapacityDisplay()
                    }
                    // Navigate to SubmissionActivity so the user sees their score.
                    val intent = Intent(this, SubmissionActivity::class.java)
                    intent.putExtra("isPublicSurvey", surveyData["type"] == "public")
//                    startActivity(intent)
                    finish() // Close ResubmitActivity.
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Resubmission failed for survey $surveyId: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error processing survey data.", Toast.LENGTH_SHORT).show()
        }
    }

    // Recursive helper to convert JSONObject to Map<String, Any?>
    private fun jsonToMap(json: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)
            when (value) {
                is JSONObject -> map[key] = jsonToMap(value)
                is org.json.JSONArray -> map[key] = jsonToList(value)
                JSONObject.NULL -> map[key] = null
                else -> map[key] = value
            }
        }
        return map
    }

    // Recursive helper to convert JSONArray to List<Any?>
    private fun jsonToList(array: org.json.JSONArray): List<Any?> {
        val list = mutableListOf<Any?>()
        for (i in 0 until array.length()) {
            val value = array.get(i)
            when (value) {
                is JSONObject -> list.add(jsonToMap(value))
                is org.json.JSONArray -> list.add(jsonToList(value))
                JSONObject.NULL -> list.add(null)
                else -> list.add(value)
            }
        }
        return list
    }
}
