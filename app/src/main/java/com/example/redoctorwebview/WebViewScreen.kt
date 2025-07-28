package com.example.redoctorwebview

import android.graphics.Bitmap
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun WebViewScreen(onBackClick: (List<VitalsResult>) -> Unit) {
    var permissionGranted by remember { mutableStateOf(false) }

    RequestCameraPermission {
        permissionGranted = true
    }

    if (permissionGranted) {
        WebViewContent(onBackClick)
    }
}


@Composable
fun WebViewContent(onBackClick: (List<VitalsResult>) -> Unit) {
    val context = LocalContext.current
    val receivedData = remember { mutableStateOf("") }
    var waitingForData by remember { mutableStateOf(false) }

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            addJavascriptInterface(
                JSBridge(
                    onData = { receivedData.value = it },
                ), "AndroidBridge"
            )

            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.grant(request.resources)
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    val userData = """
                        localStorage.setItem("userData", JSON.stringify({
                            height: 1.8,
                            weight: 75,
                            age: 41,
                            gender: 1,
                            restHeartRate: 72
                        }));
                    """.trimIndent()

                    view?.evaluateJavascript(userData, null)

                }
            }
            loadUrl("https://bp2.re.doctor/")
        }
    }

    BackHandler {
        val getVitalsResults = """
                (function() {
                    const data = localStorage.getItem("redoctor/vitals-results");
                    if (data) {
                        AndroidBridge.sendData(data);
                    }
                })();
            """.trimIndent()


        webView.evaluateJavascript(getVitalsResults, null)
        waitingForData = true
    }

    LaunchedEffect(waitingForData, receivedData.value) {
        if (waitingForData && receivedData.value.isNotBlank()) {
            val type = object : TypeToken<List<VitalsResult>>() {}.type
            val vitalsList = runCatching {
                Gson().fromJson<List<VitalsResult>>(receivedData.value, type)
            }.getOrDefault(emptyList())

            onBackClick(vitalsList)
            waitingForData = false
        }
    }

    AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
}

