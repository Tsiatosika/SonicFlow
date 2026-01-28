package com.example.sonicflow.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manager pour gérer la connexion au AudioPlayerService
 * Utilisez cette classe pour interagir avec le service audio depuis les repositories
 */
@UnstableApi
class AudioPlayerServiceManager(private val context: Context) {

    private var audioPlayerService: AudioPlayerService? = null
    private var isBound = false

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val serviceBinder = binder as? AudioPlayerService.AudioPlayerBinder
            audioPlayerService = serviceBinder?.getService()
            isBound = true
            _isServiceConnected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioPlayerService = null
            isBound = false
            _isServiceConnected.value = false
        }
    }

    fun bindService() {
        if (!isBound) {
            val intent = Intent(context, AudioPlayerService::class.java)
            context.startService(intent) // Start the service first
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
            _isServiceConnected.value = false
        }
    }

    fun getService(): AudioPlayerService? = audioPlayerService
}