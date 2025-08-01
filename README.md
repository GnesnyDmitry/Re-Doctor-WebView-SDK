# RE‑DOCTOR Web SDK

This is an Android application built with Jetpack Compose, implementing a WebView interface with support for:

- Camera access (via CAMERA permission)
- Embedding user data into WebView **(localStorage)**
- Two-way communication between JS and Android via **JavascriptInterface**

It works with: `https://bp2.re.doctor/`

---

## Requirements

- Compose UI + WebView + Java 17+

---

## Project Structure

 -  MainActivity.kt
 -  WebViewScreen.kt
 -  JSBridge.kt
 -  CameraPermission.kt

---

## JSBridge

A bridge between JavaScript and Android (WebView → Kotlin):

```kotlin
class JSBridge(
    private val onData: (String) -> Unit,
) {
    @JavascriptInterface
    fun sendData(json: String) {
        println("VitalsResults: $json")
        onData(json)
    }
}
```
Notes on **@JavascriptInterface**

 - Required for all methods called from JS.
 - Without it, WebView ignores the method.
 - Only safe when used with trusted websites.

How it works
1. Attach this class to the WebView:

```kotlin
webView.addJavascriptInterface(JSBridge(...), "AndroidBridge")
```
2. Inside the webpage, JavaScript can call:

```kotlin
AndroidBridge.sendData(JSON.stringify({ bpm: 72, spo2: 98 }));
```
3. This triggers the Android method:

```kotlin
fun sendData(json: String)
```
The JSON string is passed to the external onData(...) callback, where you can:

- Save data to a ViewModel
- Display it on screen
- Log it or send it to a server
- Close the screen, etc.

 ---

## WebViewScreen
`RequestCameraPermission` - Android camera permission request

`WebViewContent` - WebView interface implementation with full logic

```koltin
settings.javaScriptEnabled = true
settings.domStorageEnabled = true
```
**javaScriptEnabled = true** enables JavaScript execution in WebView. Needed for client-side scripting, event handling, localStorage access, calling Android interfaces, etc.

**domStorageEnabled = true** enables DOM storage (localStorage, sessionStorage). Required for data passing.

---

## Permissions for WebView
WebView uses **WebChromeClient** to handle permissions for camera and other multimedia features.

The **onPermissionRequest method** automatically grants access to requested resources, which is essential for WebRTC functionality (e.g., camera for video analysis):

```kotlin
webChromeClient = object : WebChromeClient() {
    override fun onPermissionRequest(request: PermissionRequest?) {
        request?.grant(request.resources)
    }
}
```

`request.resources` contains the list of requested resources

`request.grant(...)` allows usage of those resources

---

## Adding the JavascriptInterface named AndroidBridge (WebView → Kotlin)
It allows transferring data from localStorage in JavaScript to Android, where it is stored.

1. *JSBridge* is passed into the WebView.
2. After executing the JavaScript code, the *receivedData* state is updated.
*receivedData* is a *MutableState<String>* stored in Compose.

```kotlin
addJavascriptInterface(
    JSBridge(
        onData = { receivedData.value = it },
    ), "AndroidBridge"
)
```
## Automatic injection of userData into **localStorage** on page start
You must save data into **localStorage** inside the **onPageStarted** callback so that it’s available before page rendering begins.
If you set data later (e.g., in **onPageFinished**), the web page might require a reload to show the injected values.

`Use view?.evaluateJavascript(userData, null)` - to execute JS code inside the WebView:

```kotlin
fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
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
```

---

## Retrieving data from JS (redoctor/vitals-results) on page exit
 - `BackHandler` - handler for system back button press.
 - `getVitalsResults` is a JavaScript snippet for retrieving data from localStorage.
 - Use `webView.evaluateJavascript` to run JavaScript code inside the WebView.

1. When the back button is pressed, the following JavaScript is executed in the WebView:
   It reads data from *localStorage* and calls *AndroidBridge.sendData(...)*.

```kotlin
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
```

2. The waitingForData flag is set to true, indicating that data is expected from JavaScript.

3. The data listener is implemented using LaunchedEffect, which watches changes to *waitingForData* and *receivedData.value*.
When data is received and not blank, it triggers parsing:

```kotlin
LaunchedEffect(waitingForData, receivedData.value) {
    if (waitingForData && receivedData.value.isNotBlank()) {
        ...
    }
}
```

4. The received JSON string is parsed into a *List<VitalsResult>*:

```kotlin
val type = object : TypeToken<List<VitalsResult>>() {}.type
val vitalsList = runCatching {
    Gson().fromJson<List<VitalsResult>>(receivedData.value, type)
}.getOrDefault(emptyList())
```

5. Then *onBackClick(vitalsList)* is called to pass the parsed result back to the previous screen.

6. Finally, the *waitingForData* flag is reset to *false*.
---