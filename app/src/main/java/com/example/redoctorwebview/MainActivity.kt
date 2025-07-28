package com.example.redoctorwebview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.redoctorwebview.ui.theme.ReDoctorWebViewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReDoctorWebViewTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "main") {
                    composable("main") {

                        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                        val vitals = savedStateHandle
                            ?.getLiveData<List<VitalsResult>>("vitals_data")

                        MainScreen(
                            vitals = vitals?.value ?: emptyList(),
                            onOpenWebView = {
                                navController.navigate("webview")
                            }
                        )
                    }
                    composable("webview") {
                        WebViewScreen(
                            onBackClick = { vitalsList ->
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("vitals_data", vitalsList)
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(vitals: List<VitalsResult>, onOpenWebView: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(top = 30.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onOpenWebView,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Open WebView")
        }

        if (vitals.isEmpty()) {
            Text("There is no data yet", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(vitals) { item ->
                    VitalsCard(item)
                }
            }
        }
    }
}







