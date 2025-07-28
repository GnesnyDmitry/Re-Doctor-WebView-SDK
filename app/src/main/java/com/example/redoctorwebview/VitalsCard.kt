package com.example.redoctorwebview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VitalsCard(item: VitalsResult) {
    val formatter = remember {
        java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss", java.util.Locale.getDefault())
    }
    val dateString = formatter.format(java.util.Date(item.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Measurement time: $dateString", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))

            with(item.result.basicVitals) {
                Text("Heart rate: $heartRate bpm")
                Text("Respiration rate: $respirationRate breaths/min")
                Text("Blood oxygen: $bloodOxygen%")
                Text("Temperature: ${"%.1f".format(coreBodyTemperature)}°C")
                Text("Blood pressure: $systolicBloodPressure / $diastolicBloodPressure mmHg")
                Text("Pulse pressure: ${"%.1f".format(pulsePressure)} mmHg")
                Text("Stress level: $stress")
                Text("HRV: $hrv")
                Text("Reflection Index: $reflectionIndex")
                Text("LASI: $lasi")
            }
            with(item.result.glucose) {
                Text("Glucose: $glucoseMin – $glucoseMax mg/dL")
            }
            Text("Risk level: ${item.result.riskLevel}")

        }
    }
}