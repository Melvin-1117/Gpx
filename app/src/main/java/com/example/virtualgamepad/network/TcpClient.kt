package com.example.virtualgamepad

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket

class TcpClient {
    @Volatile
    private var socket: Socket? = null
    private var writer: BufferedWriter? = null

    suspend fun connect(host: String, port: Int, timeoutMs: Int = 3000): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                close()
                val s = Socket()
                s.connect(InetSocketAddress(host, port), timeoutMs)
                socket = s
                writer = BufferedWriter(OutputStreamWriter(s.getOutputStream()))
                true
            } catch (e: Exception) {
                e.printStackTrace()
                close()
                false
            }
        }
    }

    suspend fun send(message: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val w = writer ?: return@withContext false
                w.write(message)
                w.newLine()
                w.flush()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun isConnected(): Boolean {
        return socket?.isConnected == true && socket?.isClosed == false
    }

    suspend fun close() {
        withContext(Dispatchers.IO) {
            try {
                writer?.close()
            } catch (_: Exception) {}
            try {
                socket?.close()
            } catch (_: Exception) {}
            writer = null
            socket = null
        }
    }
}
