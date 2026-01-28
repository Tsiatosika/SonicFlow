package com.example.sonicflow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.example.sonicflow.MainActivity
import com.example.sonicflow.R
import com.example.sonicflow.domain.model.Track
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@UnstableApi
class AudioPlayerService : MediaSessionService() {

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "music_channel"
    }

    private lateinit var mediaSession: MediaSession
    private lateinit var player: ExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager

    private val binder = AudioPlayerBinder()

    // Les StateFlows pour exposer l'état
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState = _playbackState.asStateFlow()

    private val _currentPlayingTrack = MutableStateFlow<Track?>(null)
    val currentPlayingTrack = _currentPlayingTrack.asStateFlow()

    data class PlaybackState(
        val isPlaying: Boolean = false,
        val currentPosition: Long = 0L,
        val duration: Long = 0L,
        val bufferedPosition: Long = 0L,
        val playbackState: Int = Player.STATE_IDLE
    )

    // Binder pour permettre la connexion au service
    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        // Initialiser le player
        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(15000)
            .setSeekForwardIncrementMs(30000)
            .build()

        // Configurer les listeners du player
        setupPlayerListeners()

        // Créer le canal de notification
        createNotificationChannel()

        // Configurer le gestionnaire de notifications
        setupNotificationManager()

        // Créer la MediaSession
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())
            .build()

        // Démarrer le service en foreground
        startForegroundService()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onDestroy() {
        player.removeListener(playerListener)
        notificationManager.setPlayer(null)
        mediaSession.release()
        player.release()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SonicFlow Music",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupNotificationManager() {
        notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setChannelNameResourceId(R.string.notification_channel_name)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
            .setMediaDescriptionAdapter(
                object : PlayerNotificationManager.MediaDescriptionAdapter {
                    override fun getCurrentContentTitle(player: Player): CharSequence {
                        return _currentPlayingTrack.value?.title ?: "SonicFlow"
                    }

                    override fun createCurrentContentIntent(player: Player): PendingIntent? {
                        val intent = Intent(this@AudioPlayerService, MainActivity::class.java)
                        return PendingIntent.getActivity(
                            this@AudioPlayerService,
                            0,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    }

                    override fun getCurrentContentText(player: Player): CharSequence {
                        return _currentPlayingTrack.value?.artist ?: "No track playing"
                    }

                    override fun getCurrentLargeIcon(
                        player: Player,
                        callback: PlayerNotificationManager.BitmapCallback
                    ) = null
                }
            )
            .setNotificationListener(notificationListener)
            .build()

        notificationManager.setPlayer(player)
    }

    private fun setupPlayerListeners() {
        player.addListener(playerListener)
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            _playbackState.value = _playbackState.value.copy(
                playbackState = playbackState,
                isPlaying = playbackState == Player.STATE_READY && player.isPlaying,
                currentPosition = player.currentPosition,
                duration = player.duration,
                bufferedPosition = player.bufferedPosition
            )
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            _playbackState.value = _playbackState.value.copy(
                currentPosition = player.currentPosition
            )
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            _playbackState.value = _playbackState.value.copy(isPlaying = playWhenReady)
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            // Gérer les changements de vitesse si nécessaire
        }

        override fun onPlayerError(error: PlaybackException) {
            error.printStackTrace()
            android.util.Log.e("AudioPlayerService", "Playback error", error)
        }
    }

    private val notificationListener = object : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing) {
                startForeground(notificationId, notification)
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            if (dismissedByUser) {
                stopSelf()
            }
        }
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SonicFlow")
            .setContentText("Music player")
            .setSmallIcon(R.drawable.ic_music_note)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    // Méthodes publiques pour contrôler la lecture
    fun playTrack(track: Track) {
        android.util.Log.d("AudioPlayerService", "Playing track: ${track.title}")

        val mediaItem = MediaItem.Builder()
            .setUri(track.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setDisplayTitle(track.title)
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        _currentPlayingTrack.value = track
        _playbackState.value = _playbackState.value.copy(
            isPlaying = true,
            duration = track.duration
        )
    }

    fun playTrackList(tracks: List<Track>, startIndex: Int = 0) {
        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setUri(track.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setDisplayTitle(track.title)
                        .build()
                )
                .build()
        }

        player.setMediaItems(mediaItems)
        player.prepare()
        player.seekTo(startIndex, 0L)
        player.playWhenReady = true

        _currentPlayingTrack.value = tracks.getOrNull(startIndex)
    }

    fun pause() {
        player.pause()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
    }

    fun resume() {
        player.play()
        _playbackState.value = _playbackState.value.copy(isPlaying = true)
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
        _playbackState.value = _playbackState.value.copy(currentPosition = position)
    }

    fun skipToNext() {
        player.seekToNext()
    }

    fun skipToPrevious() {
        player.seekToPrevious()
    }

    fun setShuffleModeEnabled(enabled: Boolean) {
        player.shuffleModeEnabled = enabled
    }

    fun setRepeatMode(mode: Int) {
        player.repeatMode = mode
    }

    fun getCurrentPosition(): Long = player.currentPosition
    fun getDuration(): Long = player.duration
    fun isPlaying(): Boolean = player.isPlaying

    inner class MediaSessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedItems = mediaItems.map { mediaItem ->
                mediaItem.buildUpon()
                    .setUri(mediaItem.requestMetadata.mediaUri)
                    .build()
            }.toMutableList()

            return Futures.immediateFuture(updatedItems)
        }
    }
}