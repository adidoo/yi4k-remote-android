package com.adidoo.yi4kremote

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.adidoo.yi4k.sdk.CameraConnectionState
import com.adidoo.yi4kremote.ui.ConnectScreen
import com.adidoo.yi4kremote.ui.LiveViewScreen
import com.adidoo.yi4kremote.ui.theme.Yi4kRemoteTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Yi4kRemoteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val uiState by viewModel.uiState.collectAsState()
                    val rtspUrl by viewModel.rtspUrl.collectAsState()
                    val isConnected = uiState.connection is CameraConnectionState.Connected

                    // Lock to portrait while pairing; once live, let the phone rotate freely
                    // (respecting the user's own rotation-lock setting) for the landscape view.
                    LaunchedEffect(isConnected) {
                        requestedOrientation = if (isConnected) {
                            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        } else {
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                    }

                    if (isConnected) {
                        LiveViewScreen(
                            uiState = uiState,
                            rtspUrl = rtspUrl,
                            onTakePhoto = viewModel::takePhoto,
                            onToggleRecording = viewModel::toggleRecording,
                            onDisconnect = viewModel::disconnect,
                            onStatusMessageShown = viewModel::consumeStatusMessage,
                        )
                    } else {
                        ConnectScreen(
                            connection = uiState.connection,
                            onConnect = viewModel::connect,
                        )
                    }
                }
            }
        }
    }
}
