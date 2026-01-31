package com.example.sonicflow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Build
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@UnstableApi
class AudioPlayerService : MediaSessionService() {

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "music_channel"
        private const val POSITION_UPDATE_INTERVAL = 500L
    }

    private lateinit var mediaSession: MediaSession
    private lateinit var player: ExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager

    private val binder = AudioPlayerBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var positionUpdateJob: Job? = null

    // Liste des tracks pour retrouver le track actuel
    private var currentTrackList: List<Track> = emptyList()

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

    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(15000)
            .setSeekForwardIncrementMs(30000)
            .build()

        setupPlayerListeners()
        createNotificationChannel()
        setupNotificationManager()

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())
            .build()

        startForegroundService()
        startPositionUpdate()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onDestroy() {
        positionUpdateJob?.cancel()
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

    private fun startPositionUpdate() {
        positionUpdateJob = serviceScope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    updatePlaybackState()
                }
                delay(POSITION_UPDATE_INTERVAL)
            }
        }
    }

    private fun updatePlaybackState() {
        _playbackState.value = _playbackState.value.copy(
            currentPosition = player.currentPosition,
            duration = if (player.duration > 0) player.duration else _currentPlayingTrack.value?.duration ?: 0L,
            bufferedPosition = player.bufferedPosition,
            isPlaying = player.isPlaying
        )
    }

    private fun updateCurrentTrack() {
        // Retrouver le track actuel dans la liste
        val currentIndex = player.currentMediaItemIndex
        if (currentIndex >= 0 && currentIndex < currentTrackList.size) {
            val track = currentTrackList[currentIndex]
            android.util.Log.d("AudioPlayerService", "Updating current track to: ${track.title} (index: $currentIndex)")
            _currentPlayingTrack.value = track
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlaybackState()
            _playbackState.value = _playbackState.value.copy(playbackState = playbackState)
            android.util.Log.d("AudioPlayerService", "Playback state changed: $playbackState")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlaybackState()
            android.util.Log.d("AudioPlayerService", "Is playing: $isPlaying")
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updatePlaybackState()
            // Le morceau a changé (Next/Previous)
            if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex) {
                android.util.Log.d("AudioPlayerService", "Media item changed from ${oldPosition.mediaItemIndex} to ${newPosition.mediaItemIndex}")
                updateCurrentTrack()
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            updatePlaybackState()
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            // Gérer les changements de vitesse si nécessaire
        }

        override fun onPlayerError(error: PlaybackException) {
            error.printStackTrace()
            android.util.Log.e("AudioPlayerService", "Playback error", error)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // Quand on passe automatiquement au morceau suivant
            android.util.Log.d("AudioPlayerService", "Media item transition: ${mediaItem?.mediaMetadata?.title}, reason: $reason")
            updateCurrentTrack()
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

    fun playTrack(track: Track) {
        android.util.Log.d("AudioPlayerService", "Playing track: ${track.title}, Duration: ${track.duration}, URI: ${track.uri}")

        // Sauvegarder le track dans la liste
        currentTrackList = listOf(track)

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
        android.util.Log.d("AudioPlayerService", "Playing track list: ${tracks.size} tracks, starting at index $startIndex")

        // Sauvegarder la liste complète des tracks
        currentTrackList = tracks

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

        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.playWhenReady = true

        // Mettre à jour le track actuel
        _currentPlayingTrack.value = tracks.getOrNull(startIndex)
        android.util.Log.d("AudioPlayerService", "Initial track: ${_currentPlayingTrack.value?.title}")
    }

    fun pause() {
        player.pause()
        updatePlaybackState()
    }

    fun resume() {
        player.play()
        updatePlaybackState()
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
        updatePlaybackState()
    }

    fun skipToNext() {
        android.util.Log.d("AudioPlayerService", "Skip to next called, current index: ${player.currentMediaItemIndex}, has next: ${player.hasNextMediaItem()}")
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            player.play()
            updateCurrentTrack()
            updatePlaybackState()
        } else {
            android.util.Log.w("AudioPlayerService", "No next item available")
        }
    }

    fun skipToPrevious() {
        android.util.Log.d("AudioPlayerService", "Skip to previous called, current index: ${player.currentMediaItemIndex}, has previous: ${player.hasPreviousMediaItem()}")
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            player.play()
            updateCurrentTrack()
            updatePlaybackState()
        } else {
            android.util.Log.w("AudioPlayerService", "No previous item available")
        }
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