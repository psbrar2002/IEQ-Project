package com.example.ieqproject

data class DemographicAttributes(
    val multiracial: Boolean,
    val americanIndian: List<String>,
    val asian: Boolean,
    val black: Boolean,
    val hispanic: Boolean,
    val nativeHawaiian: Boolean,
    val white: Boolean,
    val other: Boolean,
    val otherEthnicity: String
)
