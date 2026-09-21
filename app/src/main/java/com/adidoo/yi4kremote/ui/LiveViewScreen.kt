package com.adidoo.yi4kremote.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.ui.PlayerView
import com.adidoo.yi4kremote.CameraUiState
import com.adidoo.yi4kremote.formatDuration
import kotlinx.coroutines.delay

@Composable
fun LiveViewScreen(
    uiState: CameraUiState,
    rtspUrl: String?,
    onTakePhoto: () -> Unit,
    onToggleRecording: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenSettings: () -> Unit,
    onStatusMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    LaunchedEffect(rtspUrl) {
        if (rtspUrl != null) {
            val mediaSource = RtspMediaSource.Factory()
                .createMediaSource(MediaItem.fromUri(rtspUrl))
            player.setMediaSource(mediaSource)
            player.prepare()
            player.playWhenReady = true
        } else {
            player.stop()
        }
    }

    // The video fills the raw screen bounds edge-to-edge; only the overlaid controls below
    // get inset padding, so they stay clear of the status bar, notch and nav bar/gesture pill.
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PlayerView(it).apply {
                    useController = false
                    this.player = player
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatusChip(
                    icon = Icons.Filled.BatteryFull,
                    label = uiState.batteryPercent?.let { "$it%" } ?: "--",
                )
                uiState.videoResolution?.let { StatusChip(icon = null, label = it) }
                uiState.estimatedRemainingSeconds?.let {
                    StatusChip(icon = Icons.Filled.Timer, label = formatDuration(it))
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Réglages avancés", tint = Color.White)
                }
                IconButton(onClick = onDisconnect) {
                    Icon(Icons.Filled.LinkOff, contentDescription = "Déconnecter", tint = Color.White)
                }
            }
            uiState.recordingElapsedSeconds?.let { elapsed ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.FiberManualRecord,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        formatDuration(elapsed),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }

        if (isLandscape) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .safeDrawingPadding()
                    .padding(20.dp)
                    .align(Alignment.CenterEnd),
                verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PhotoButton(onTakePhoto)
                RecordButton(uiState.isRecording, onToggleRecording)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding()
                    .padding(24.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                PhotoButton(onTakePhoto)
                RecordButton(uiState.isRecording, onToggleRecording)
            }
        }

        uiState.statusMessage?.let { message ->
            LaunchedEffect(message) {
                delay(3000)
                onStatusMessageShown()
            }
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding()
                    .padding(16.dp),
            ) {
                Text(message)
            }
        }
    }
}

@Composable
private fun PhotoButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            Icons.Filled.Camera,
            contentDescription = "Photo",
            tint = Color.White,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun RecordButton(isRecording: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            if (isRecording) Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
            contentDescription = if (isRecording) "Arrêter l'enregistrement" else "Démarrer l'enregistrement",
            tint = if (isRecording) Color.Red else Color.White,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun StatusChip(icon: ImageVector?, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon?.let {
            Icon(it, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}
