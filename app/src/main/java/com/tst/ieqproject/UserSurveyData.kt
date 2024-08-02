package com.tst.ieqproject

data class UserSurveyData(
    val hvacAttributes: HVACAttributes,
    val iaqAttributes: IAQAttributes,
    val dwellingAttributes: DwellingAttributes,
    val demographicAttributes: DemographicAttributes,
    val acousticComfortAttributes: AcousticComfortAttributes,
    val thermalComfortAttributes: ThermalComfortAttributes,
    val ieqScore: Double
)