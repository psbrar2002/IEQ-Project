package com.example.ieqproject

data class AcousticComfortAttributes(
    val indoorDecibel: String,
    val outdoorDecibel: String,
    val indoorNoiseSources: String,
    val outdoorNoiseSources: String,
    val acousticComfortScore: Double,
//    val ieqScore: Double // Add this field to store the IEQ score
)

