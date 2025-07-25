package com.example.redoctorwebview

import android.graphics.Bitmap
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewScreen(onBackClick: () -> Unit) {
    var permissionGranted by remember { mutableStateOf(false) }

    RequestCameraPermission {
        permissionGranted = true
    }

    if (permissionGranted) {
        WebViewContent(onBackClick)
    }
}


@Composable
fun WebViewContent(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val receivedData = remember { mutableStateOf("") }

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
        onBackClick()
    }

    AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
}