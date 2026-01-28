package com.example.sonicflow.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.media3.common.util.UnstableApi
import com.example.sonicflow.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class AudioPlayerServiceConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var service: AudioPlayerService? = null
    private var isBound = false

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            android.util.Log.d("ServiceConnection", "Service connected")
            service = (binder as? AudioPlayerService.AudioPlayerBinder)?.getService()
            isBound = true
            _isConnected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            android.util.Log.d("ServiceConnection", "Service disconnected")
            service = null
            isBound = false
            _isConnected.value = false
        }
    }

    fun bind() {
        if (!isBound) {
            android.util.Log.d("ServiceConnection", "Binding to service")
            val intent = Intent(context, AudioPlayerService::class.java)
            context.startService(intent)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        if (isBound) {
            android.util.Log.d("ServiceConnection", "Unbinding from service")
            context.unbindService(connection)
            isBound = false
            _isConnected.value = false
        }
    }

    // Méthodes déléguées au service
    fun playTrack(track: Track) {
        if (service != null) {
            service?.playTrack(track)
        } else {
            android.util.Log.e("ServiceConnection", "Service not available - cannot play track")
        }
    }

    fun pause() {
        service?.pause()
    }

    fun resume() {
        service?.resume()
    }

    fun seekTo(position: Long) {
        service?.seekTo(position)
    }

    fun skipToNext() {
        service?.skipToNext()
    }

    fun skipToPrevious() {
        service?.skipToPrevious()
    }

    fun setShuffleModeEnabled(enabled: Boolean) {
        service?.setShuffleModeEnabled(enabled)
    }

    fun setRepeatMode(mode: Int) {
        service?.setRepeatMode(mode)
    }

    fun getPlaybackState(): Flow<AudioPlayerService.PlaybackState> {
        return service?.playbackState ?: flow {
            emit(AudioPlayerService.PlaybackState())
        }
    }

    fun getCurrentPlayingTrack(): Flow<Track?> {
        return service?.currentPlayingTrack ?: flow {
            emit(null)
        }
    }
}