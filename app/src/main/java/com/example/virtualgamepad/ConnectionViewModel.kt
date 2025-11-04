package com.example.virtualgamepad

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConnectionViewModel : ViewModel() {
    private val tcpClient = TcpClient()
    private val TAG = "ConnectionVM"

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    // Hardcoded address/port (change as needed)
    private val host = "192.168.1.50"
    private val port = 8080

    // Monitoring / heartbeat
    private var monitorJob: Job? = null
    private val heartbeatMs = 5000L
    private val initialReconnectDelayMs = 2000L
    private val maxReconnectDelayMs = 30000L

    /**
     * Start monitoring connection: will attempt to connect and send periodic heartbeats.
     */
    fun connect() {
        if (monitorJob != null) return // already running

        monitorJob = viewModelScope.launch {
            var backoff = initialReconnectDelayMs
            while (true) {
                try {
                    if (!tcpClient.isConnected()) {
                        Log.d(TAG, "Attempting connect to $host:$port")
                        val ok = tcpClient.connect(host, port)
                        _connected.value = ok
                        if (ok) {
                            Log.d(TAG, "Connected")
                            backoff = initialReconnectDelayMs
                        } else {
                            Log.d(TAG, "Connect failed, will retry in $backoff ms")
                            delay(backoff)
                            backoff = (backoff * 2).coerceAtMost(maxReconnectDelayMs)
                            continue
                        }
                    }

                    // while connected, send heartbeat periodically
                    val sent = tcpClient.send("HEARTBEAT")
                    if (!sent) {
                        Log.d(TAG, "Heartbeat failed, marking disconnected")
                        _connected.value = false
                        tcpClient.close()
                        delay(backoff)
                        backoff = (backoff * 2).coerceAtMost(maxReconnectDelayMs)
                        continue
                    } else {
                        _connected.value = true
                    }

                    delay(heartbeatMs)
                } catch (e: Exception) {
                    Log.w(TAG, "Monitor loop exception: ${e.message}")
                    _connected.value = false
                    try { tcpClient.close() } catch (_: Exception) {}
                    delay(initialReconnectDelayMs)
                }
            }
        }
    }

    /**
     * Stop monitoring and close connection.
     */
    fun disconnect() {
        monitorJob?.cancel()
        monitorJob = null
        viewModelScope.launch {
            try {
                tcpClient.close()
            } catch (_: Exception) {}
            _connected.value = false
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            try {
                val ok = tcpClient.send(message)
                if (!ok) {
                    Log.d(TAG, "sendMessage failed, marking disconnected")
                    _connected.value = false
                }
            } catch (e: Exception) {
                Log.w(TAG, "sendMessage exception: ${e.message}")
                _connected.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
