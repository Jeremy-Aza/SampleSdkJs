package com.example.samplesdkjs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 1
    private var permissionRequest: PermissionRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Configura el WebView para habilitar Javascript
        val webView: WebView = findViewById(R.id.webview)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true
        webSettings.allowUniversalAccessFromFileURLs = true
        webSettings.mediaPlaybackRequiresUserGesture = false

        /* webView.evaluateJavascript("""sessionStorage.getItem("imageCapture")"""){
             result -> println("Imagen desde session storage: $result")
         }*/

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                consoleMessage?.let { println("WebView Console: ${it.message()} (Line: ${it.lineNumber()}, Source: ${it.sourceId()})") }
                return super.onConsoleMessage(consoleMessage)
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                if (request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    //Solicitar el permiso de cámara
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST_CODE
                    )

                    //Almacenar la solicitud para concederla más tarde
                    permissionRequest = request
                }
            }
        }

        // Configura el WebViewClient para manejar los eventos de la web
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                return false
            }
        }

        // Carga el archivo HTML desde los assets
        webView.loadUrl("file:///android_asset/www/veridoc.html")
    }

    override fun onRequestPermissionsResult(
        requesCode: Int, permissions: Array<String>, grandResults: IntArray
    ) {
        super.onRequestPermissionsResult(requesCode, permissions, grandResults)

        if (requesCode === CAMERA_PERMISSION_REQUEST_CODE) {
            if (grandResults.isNotEmpty() && grandResults[0] === PackageManager.PERMISSION_GRANTED) {
                println("Permiso concedido")
                permissionRequest?.grant(permissionRequest?.resources)
                permissionRequest = null
            } else {
                println("Permiso denegado")
                Toast.makeText(
                    this, "Se necesita permiso de cámara para continuar", Toast.LENGTH_SHORT
                ).show()
                permissionRequest?.deny()
                permissionRequest = null
            }
        }
    }
}