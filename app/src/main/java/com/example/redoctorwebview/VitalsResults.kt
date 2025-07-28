package com.example.redoctorwebview

data class VitalsResult(
    val timestamp: Long,
    val result: ResultData
)

data class ResultData(
    val basicVitals: BasicVitals,
    val glucose: Glucose,
    val riskLevel: Int
)

data class BasicVitals(
    val bloodOxygen: Int,
    val heartRate: Int,
    val respirationRate: Int,
    val coreBodyTemperature: Double,
    val systolicBloodPressure: Int,
    val diastolicBloodPressure: Int,
    val pulsePressure: Double,
    val stress: Int,
    val reflectionIndex: Int,
    val lasi: Int,
    val hrv: Int
)

data class Glucose(
    val glucoseMin: Int,
    val glucoseMax: Int
)
