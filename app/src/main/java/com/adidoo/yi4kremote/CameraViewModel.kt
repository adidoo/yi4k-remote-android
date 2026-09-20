package com.adidoo.yi4kremote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adidoo.yi4k.sdk.CameraConnectionState
import com.adidoo.yi4k.sdk.CameraEvent
import com.adidoo.yi4k.sdk.YiCameraController
import com.adidoo.yi4k.sdk.YiProtocol
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CameraUiState(
    val connection: CameraConnectionState = CameraConnectionState.Disconnected,
    val isRecording: Boolean = false,
    val batteryPercent: Int? = null,
    val videoResolution: String? = null,
    val videoQuality: String? = null,
    val freeStorageBytes: Long? = null,
    val recordingElapsedSeconds: Long? = null,
    val estimatedRemainingSeconds: Long? = null,
    val statusMessage: String? = null,
)

private const val STORAGE_POLL_INTERVAL_IDLE_MS = 5_000L
private const val STORAGE_POLL_INTERVAL_RECORDING_MS = 2_000L

// Minimum time into a recording before the observed SD-card drain rate is trusted: too short
// a window and rounding/filesystem flush timing makes the rate noisy.
private const val MIN_CALIBRATION_SECONDS = 5L

class CameraViewModel(
    private val controller: YiCameraController = YiCameraController(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _rtspUrl = MutableStateFlow<String?>(null)
    val rtspUrl: StateFlow<String?> = _rtspUrl.asStateFlow()

    private var storagePollJob: Job? = null
    private var recordingTimerJob: Job? = null

    // Free space measured right when the current (or most recent) recording started, and the
    // resulting drain rate — this is how "time remaining" is estimated, since the camera
    // protocol doesn't expose its own bitrate tables. It's recalibrated on every recording.
    private var recordingBaselineBytes: Long? = null
    private var observedBytesPerSecond: Double? = null

    init {
        viewModelScope.launch {
            controller.connectionState.collect { state ->
                val settings = (state as? CameraConnectionState.Connected)?.settings
                _uiState.update {
                    it.copy(
                        connection = state,
                        videoResolution = settings?.get(YiProtocol.KEY_VIDEO_RESOLUTION),
                        videoQuality = settings?.get(YiProtocol.KEY_VIDEO_QUALITY),
                    )
                }
                if (state is CameraConnectionState.Connected) {
                    startLiveView()
                    startStoragePolling()
                } else {
                    stopStoragePolling()
                    observedBytesPerSecond = null
                }
            }
        }
        viewModelScope.launch {
            controller.isRecording.collect { recording ->
                _uiState.update { it.copy(isRecording = recording) }
                // Reset the baseline either way: refreshStorage() (re)captures it lazily from
                // a fresh read, whether recording just started or was already running when we
                // (re)connected — never from a possibly-stale cached free-space value.
                recordingBaselineBytes = null
                if (recording) startRecordingTimer() else stopRecordingTimer()
                restartStoragePolling()
                refreshStorage()
            }
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

    private fun startStoragePolling() {
        storagePollJob?.cancel()
        storagePollJob = viewModelScope.launch {
            while (isActive) {
                val interval = if (_uiState.value.isRecording) {
                    STORAGE_POLL_INTERVAL_RECORDING_MS
                } else {
                    STORAGE_POLL_INTERVAL_IDLE_MS
                }
                delay(interval)
                refreshStorage()
            }
        }
    }

    /** Switches polling cadence immediately when a recording starts or stops. */
    private fun restartStoragePolling() {
        if (storagePollJob != null) startStoragePolling()
    }

    private fun stopStoragePolling() {
        storagePollJob?.cancel()
        storagePollJob = null
        _uiState.update { it.copy(freeStorageBytes = null, estimatedRemainingSeconds = null) }
    }

    private suspend fun refreshStorage() {
        val free = runCatching { controller.getFreeStorageBytes() }.getOrNull()?.takeIf { it >= 0 } ?: return

        // Lazily (re)capture the calibration baseline from this fresh read the first time we
        // see it while recording — covers both a recording we just started, and one the camera
        // was already running when we (re)connected.
        if (_uiState.value.isRecording && recordingBaselineBytes == null) {
            recordingBaselineBytes = free
        }

        val baseline = recordingBaselineBytes
        val elapsed = _uiState.value.recordingElapsedSeconds
        if (baseline != null && elapsed != null && elapsed >= MIN_CALIBRATION_SECONDS) {
            val consumedBytes = baseline - free
            if (consumedBytes > 0) {
                observedBytesPerSecond = consumedBytes.toDouble() / elapsed
            }
        }

        val remaining = observedBytesPerSecond?.takeIf { it > 0 }?.let { rate -> (free / rate).toLong() }
        _uiState.update { it.copy(freeStorageBytes = free, estimatedRemainingSeconds = remaining) }
    }

    private fun startRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            var elapsed = 0L
            _uiState.update { it.copy(recordingElapsedSeconds = elapsed) }
            while (isActive) {
                delay(1_000)
                elapsed += 1
                _uiState.update { it.copy(recordingElapsedSeconds = elapsed) }
            }
        }
    }

    private fun stopRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        _uiState.update { it.copy(recordingElapsedSeconds = null) }
    }

    private fun setStatus(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    override fun onCleared() {
        storagePollJob?.cancel()
        recordingTimerJob?.cancel()
        controller.disconnect()
        super.onCleared()
    }
}
