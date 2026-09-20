package com.adidoo.yi4kremote.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.adidoo.yi4k.sdk.CameraConnectionState

@Composable
fun ConnectScreen(
    connection: CameraConnectionState,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("Yi4kRemote", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Text(
            "1. Allume la caméra et active son Wi-Fi.\n" +
                "2. Connecte le Wi-Fi de ce téléphone au point d'accès de la caméra " +
                "(nom commençant généralement par \"YDXJ\").\n" +
                "3. Reviens ici et appuie sur Connecter.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(24.dp))

        OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }) {
            Icon(Icons.Filled.Wifi, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ouvrir les paramètres Wi-Fi")
        }

        Spacer(Modifier.height(16.dp))

        when (connection) {
            is CameraConnectionState.Connecting -> CircularProgressIndicator()
            is CameraConnectionState.Failed -> {
                Text(
                    connection.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onConnect) { Text("Réessayer") }
            }
            else -> Button(onClick = onConnect) { Text("Connecter") }
        }
    }
}
