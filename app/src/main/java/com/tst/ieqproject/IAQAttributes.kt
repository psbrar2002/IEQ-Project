package com.tst.ieqproject

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.tst.ieqproject.R

data class IAQAttributes(
    val kitchenStoveType: String,
    val kitchenStoveFan: String,
    val livingRoomBeforeCooking: String,
    val livingRoomAfterCooking: String,
    val livingRoomHumidity: String,
    val outdoorPM25: String,
    val outdoorHumidity: String,
    val moldPresent: String,
    val iaqScore: Double
) {
    companion object {
        fun setupSpinners(context: Context, rootView: View) {
            setupKitchenStoveTypeSpinner(context, rootView.findViewById(R.id.kitchenStoveTypeSpinner))
            setupKitchenStoveFanSpinner(context, rootView.findViewById(R.id.kitchenStoveFanSpinner))
            setupYesNoSpinner(context, rootView.findViewById(R.id.moldPresentSpinner))
        }

        private fun setupKitchenStoveTypeSpinner(context: Context, spinner: Spinner) {
            ArrayAdapter.createFromResource(
                context,
                R.array.kitchen_stove_type_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }

        private fun setupKitchenStoveFanSpinner(context: Context, spinner: Spinner) {
            ArrayAdapter.createFromResource(
                context,
                R.array.kitchen_stove_fan_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }

        private fun setupYesNoSpinner(context: Context, spinner: Spinner) {
            ArrayAdapter.createFromResource(
                context,
                R.array.yes_no_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }
    }
}
