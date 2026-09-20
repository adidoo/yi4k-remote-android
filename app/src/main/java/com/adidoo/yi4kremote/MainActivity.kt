package com.adidoo.yi4kremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
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
        setContent {
            Yi4kRemoteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val uiState by viewModel.uiState.collectAsState()
                    val rtspUrl by viewModel.rtspUrl.collectAsState()

                    if (uiState.connection is CameraConnectionState.Connected) {
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
