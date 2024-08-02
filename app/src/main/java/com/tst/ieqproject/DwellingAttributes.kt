package com.tst.ieqproject

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner

data class DwellingAttributes(
    val homeType: String,
    val isSection8: Boolean,
    val isOaklandHousing: Boolean,
    val numPeople: String,
    val squareFootage: String,
    val date: String,
    val streetIntersection: String,
    val buildingAge: String?
) {
    companion object {
        fun setupSpinners(context: Context, rootView: View) {
            setupHomeTypeSpinner(context, rootView.findViewById(R.id.homeTypeSpinner))
            setupSection8Spinner(context, rootView.findViewById(R.id.section8Spinner))
            setupOaklandHousingSpinner(context, rootView.findViewById(R.id.oaklandHousingSpinner))
            setupNumberOfPeopleSpinner(context, rootView.findViewById(R.id.numberOfPeopleSpinner))
            setupSquareFootageSpinner(context, rootView.findViewById(R.id.squareFootageSpinner))
        }

        private fun setupHomeTypeSpinner(context: Context, spinner: Spinner) {
            ArrayAdapter.createFromResource(
                context,
                R.array.home_type_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }

        private fun setupSection8Spinner(context: Context, spinner: Spinner) {
            ArrayAdapter.createFromResource(
                context,
                R.array.yes_no_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }

        private fun setupOaklandHousingSpinner(context: Context, spinner: Spinner) {
            ArrayAdapter.createFromResource(
                context,
                R.array.yes_no_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }

        private fun setupNumberOfPeopleSpinner(context: Context, spinner: Spinner) {
            ArrayAdapter.createFromResource(
                context,
                R.array.number_of_people_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }

        private fun setupSquareFootageSpinner(context: Context, spinner: Spinner) {
            ArrayAdapter.createFromResource(
                context,
                R.array.square_footage_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }
    }
}
