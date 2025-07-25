package com.example.redoctorwebview

import android.webkit.JavascriptInterface
import org.json.JSONArray

class JSBridge(
    private val onData: (String) -> Unit,
) {
    @JavascriptInterface
    fun sendData(json: String) {
        println("VitalsResults: $json")
        onData(json)
    }
}
