package com.tst.ieqproject

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner

data class HVACAttributes(
    val hasAirConditioning: Boolean,
    val airConditioningWorks: Boolean?,
    val airConditioningType: String?,
    val heatingType: String,
    val additionalAppliances: String?,
    val hasPortableAirFilter: Boolean,
    val hvacScore: Double
) {
    companion object {
        fun setupSpinners(context: Context, rootView: View) {
            setupYesNoSpinner(context, rootView.findViewById(R.id.hasAirConditioningSpinner))
            setupYesNoSpinner(context, rootView.findViewById(R.id.airConditioningWorksSpinner))
            setupAirConditioningTypeSpinner(context, rootView.findViewById(R.id.airConditioningTypeSpinner))
            setupHeatingTypeSpinner(context, rootView.findViewById(R.id.heatingTypeSpinner))
            setupYesNoSpinner(context, rootView.findViewById(R.id.hasPortableAirFilterSpinner))
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

        private fun setupAirConditioningTypeSpinner(context: Context, spinner: Spinner) {
            ArrayAdapter.createFromResource(
                context,
                R.array.air_conditioning_type_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }

        private fun setupHeatingTypeSpinner(context: Context, spinner: Spinner) {
            ArrayAdapter.createFromResource(
                context,
                R.array.heating_type_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }
    }
}
