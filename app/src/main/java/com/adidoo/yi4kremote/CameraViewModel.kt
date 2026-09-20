package com.adidoo.yi4kremote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adidoo.yi4k.sdk.CameraConnectionState
import com.adidoo.yi4k.sdk.CameraEvent
import com.adidoo.yi4k.sdk.YiCameraController
import com.adidoo.yi4k.sdk.YiProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CameraUiState(
    val connection: CameraConnectionState = CameraConnectionState.Disconnected,
    val isRecording: Boolean = false,
    val batteryPercent: Int? = null,
    val videoResolution: String? = null,
    val statusMessage: String? = null,
)

class CameraViewModel(
    private val controller: YiCameraController = YiCameraController(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _rtspUrl = MutableStateFlow<String?>(null)
    val rtspUrl: StateFlow<String?> = _rtspUrl.asStateFlow()

    init {
        viewModelScope.launch {
            controller.connectionState.collect { state ->
                _uiState.update {
                    it.copy(
                        connection = state,
                        videoResolution = (state as? CameraConnectionState.Connected)
                            ?.settings?.get(YiProtocol.KEY_VIDEO_RESOLUTION),
                    )
                }
                if (state is CameraConnectionState.Connected) startLiveView()
            }
        }
        viewModelScope.launch {
            controller.isRecording.collect { recording -> _uiState.update { it.copy(isRecording = recording) } }
        }
        viewModelScope.launch {
            controller.batteryPercent.collect { battery -> _uiState.update { it.copy(batteryPercent = battery) } }
        }
        viewModelScope.launch {
            controller.events.collect { event ->
                val message = when (event) {
                    is CameraEvent.PhotoTaken -> "Photo enregistrée : ${event.path.substringAfterLast('/')}"
                    CameraEvent.RecordingStarted -> "Enregistrement démarré"
                    CameraEvent.RecordingStopped -> "Enregistrement arrêté"
                    is CameraEvent.Unknown -> null
                }
                if (message != null) setStatus(message)
            }
        }
    }

    fun connect() {
        viewModelScope.launch { controller.connect() }
    }

    fun disconnect() {
        viewModelScope.launch { runCatching { controller.stopLiveView() } }
        controller.disconnect()
        _rtspUrl.value = null
    }

    fun takePhoto() {
        viewModelScope.launch {
            runCatching { controller.takePhoto() }
                .onFailure { setStatus("Échec de la photo : ${it.message}") }
        }
    }

    fun toggleRecording() {
        viewModelScope.launch {
            runCatching {
                if (_uiState.value.isRecording) controller.stopRecording() else controller.startRecording()
            }.onFailure { setStatus("Échec de l'enregistrement : ${it.message}") }
        }
    }

    fun consumeStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private fun startLiveView() {
        viewModelScope.launch {
            runCatching { controller.startLiveView() }
                .onSuccess { _rtspUrl.value = it }
                .onFailure { setStatus("Flux vidéo indisponible : ${it.message}") }
        }
    }

    private fun setStatus(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    override fun onCleared() {
        controller.disconnect()
        super.onCleared()
    }
}
