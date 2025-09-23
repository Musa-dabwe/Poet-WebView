package com.example.musicplayer

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch

class FloatingBubbleService : Service() {

    private lateinit var windowManager: WindowManager

    // Views
    private lateinit var bubbleView: View
    private lateinit var playerView: View
    private lateinit var playlistView: View

    // Layout Params
    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var playerParams: WindowManager.LayoutParams
    private lateinit var playlistParams: WindowManager.LayoutParams

    // State
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isPlayerViewVisible = false
    private var isPlaylistViewVisible = false
    private var isShuffled = false

    // Player
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var originalSongs: List<Song>
    private lateinit var displayedSongs: MutableList<Song>

    // Player UI
    private lateinit var textTitle: TextView
    private lateinit var textArtist: TextView
    private lateinit var buttonPlayPause: ImageView
    private lateinit var buttonNext: ImageView
    private lateinit var buttonPrevious: ImageView
    private lateinit var buttonShuffle: ImageView
    private lateinit var buttonMenu: ImageView

    // Playlist UI
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var shuffleSwitch: MaterialSwitch


    override fun onBind(intent: Intent): IBinder? = null

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Inflate Views
        bubbleView = LayoutInflater.from(this).inflate(R.layout.layout_bubble, null)
        playerView = LayoutInflater.from(this).inflate(R.layout.layout_player, null)
        playlistView = LayoutInflater.from(this).inflate(R.layout.layout_playlist, null)

        // Bubble View Setup
        bubbleParams = createLayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, layoutFlag)
        bubbleParams.gravity = Gravity.TOP or Gravity.START
        bubbleParams.x = 0
        bubbleParams.y = 100
        windowManager.addView(bubbleView, bubbleParams)

        // Player View Setup
        playerParams = createLayoutParams(340.toDp(), WindowManager.LayoutParams.WRAP_CONTENT, layoutFlag)
        playerParams.gravity = Gravity.TOP or Gravity.START
        playerView.visibility = View.GONE
        windowManager.addView(playerView, playerParams)

        // Playlist View Setup
        playlistParams = createLayoutParams(325.toDp(), 325.toDp(), layoutFlag)
        playlistParams.gravity = Gravity.CENTER
        playlistView.visibility = View.GONE
        windowManager.addView(playlistView, playlistParams)


        startForegroundService()
        bindPlayerViews()
        bindPlaylistViews()
        setupExoPlayer()
        setupPlayerControls()
        setupBubbleTouchListener()
    }

    private fun createLayoutParams(width: Int, height: Int, layoutFlag: Int): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(width, height, layoutFlag, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)
    }

    private fun Int.toDp(): Int = (this * resources.displayMetrics.density).toInt()


    private fun bindPlayerViews() {
        textTitle = playerView.findViewById(R.id.text_title)
        textArtist = playerView.findViewById(R.id.text_artist)
        buttonPlayPause = playerView.findViewById(R.id.button_play_pause)
        buttonNext = playerView.findViewById(R.id.button_next)
        buttonPrevious = playerView.findViewById(R.id.button_previous)
        buttonShuffle = playerView.findViewById(R.id.button_shuffle)
        buttonMenu = playerView.findViewById(R.id.button_menu)
    }

    private fun bindPlaylistViews() {
        val recyclerView: RecyclerView = playlistView.findViewById(R.id.recycler_view_playlist)
        recyclerView.layoutManager = LinearLayoutManager(this)
        playlistAdapter = PlaylistAdapter(mutableListOf()) { position ->
            exoPlayer.seekTo(position, 0)
            exoPlayer.play()
            togglePlaylistView() // Hide playlist on song selection
        }
        recyclerView.adapter = playlistAdapter

        // Sorting and Shuffle controls
        playlistView.findViewById<Button>(R.id.button_sort_az).setOnClickListener { sortPlaylist(SortType.AZ) }
        playlistView.findViewById<Button>(R.id.button_sort_za).setOnClickListener { sortPlaylist(SortType.ZA) }
        playlistView.findViewById<Button>(R.id.button_sort_artist).setOnClickListener { sortPlaylist(SortType.ARTIST) }
        shuffleSwitch = playlistView.findViewById(R.id.switch_shuffle)
        shuffleSwitch.setOnCheckedChangeListener { _, isChecked ->
            setShuffle(isChecked)
        }
        playlistView.findViewById<View>(R.id.button_close_playlist).setOnClickListener { togglePlaylistView() }
    }

    private fun setupExoPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        val musicScanner = MusicScanner(this)
        originalSongs = musicScanner.scanForMusic()
        displayedSongs = originalSongs.toMutableList()
        updateExoPlayerPlaylist()
        playlistAdapter.updateSongs(displayedSongs)


        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                buttonPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateTrackInfo()
            }
        })
    }

    private fun setupPlayerControls() {
        buttonPlayPause.setOnClickListener { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() }
        buttonNext.setOnClickListener { exoPlayer.seekToNextMediaItem() }
        buttonPrevious.setOnClickListener { exoPlayer.seekToPreviousMediaItem() }
        buttonShuffle.setOnClickListener { setShuffle(!isShuffled) }
        buttonMenu.setOnClickListener { togglePlaylistView() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBubbleTouchListener() {
        bubbleView.setOnTouchListener { _, event ->
            handleTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP && (Math.abs(event.rawX - initialTouchX) < 10) && (Math.abs(event.rawY - initialTouchY) < 10)) {
                togglePlayerView()
            }
            true
        }
    }


    // --- View Toggling ---
    private fun togglePlayerView() {
        if (isPlayerViewVisible) {
            playerView.visibility = View.GONE
            if(isPlaylistViewVisible) togglePlaylistView()
        } else {
            playerParams.y = bubbleParams.y + bubbleView.height + 20
            playerParams.x = 0
            windowManager.updateViewLayout(playerView, playerParams)
            playerView.visibility = View.VISIBLE
            updateTrackInfo()
        }
        isPlayerViewVisible = !isPlayerViewVisible
    }

    private fun togglePlaylistView() {
        playlistView.visibility = if (isPlaylistViewVisible) View.GONE else View.VISIBLE
        isPlaylistViewVisible = !isPlaylistViewVisible
    }


    // --- Drag Logic ---
    private fun handleTouchEvent(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = bubbleParams.x
                initialY = bubbleParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                bubbleParams.x = initialX + (event.rawX - initialTouchX).toInt()
                bubbleParams.y = initialY + (event.rawY - initialTouchY).toInt()
                windowManager.updateViewLayout(bubbleView, bubbleParams)

                if (isPlayerViewVisible) {
                    playerParams.y = bubbleParams.y + bubbleView.height + 20
                    windowManager.updateViewLayout(playerView, playerParams)
                }
            }
        }
    }


    // --- Playlist Logic ---
    private var lastSortType = SortType.AZ
    private fun sortPlaylist(sortType: SortType) {
        setShuffle(false)
        when (sortType) {
            SortType.AZ -> displayedSongs.sortBy { it.title }
            SortType.ZA -> displayedSongs.sortByDescending { it.title }
            SortType.ARTIST -> displayedSongs.sortBy { it.artist }
        }
        lastSortType = sortType
        playlistAdapter.updateSongs(displayedSongs)
        updateExoPlayerPlaylist()
    }

    private fun setShuffle(shuffle: Boolean) {
        isShuffled = shuffle
        shuffleSwitch.isChecked = shuffle
        updateShuffleButtonTint()

        if (shuffle) {
            displayedSongs.shuffle()
        } else {
            sortPlaylist(lastSortType)
        }
        playlistAdapter.updateSongs(displayedSongs)
        updateExoPlayerPlaylist()
    }

    private fun updateShuffleButtonTint() {
        val color = if (isShuffled) Color.GREEN else Color.WHITE
        DrawableCompat.setTint(buttonShuffle.drawable, color)
    }

    private fun updateExoPlayerPlaylist() {
        val currentMediaItemIndex = exoPlayer.currentMediaItemIndex
        val currentSong = if (currentMediaItemIndex != -1) originalSongs.find { it.contentUri.toString() == exoPlayer.getMediaItemAt(currentMediaItemIndex).mediaId } else null
        val mediaItems = displayedSongs.map { MediaItem.Builder().setUri(it.contentUri).setMediaId(it.contentUri.toString()).build() }
        exoPlayer.setMediaItems(mediaItems, false)
        val newIndex = if (currentSong != null) displayedSongs.indexOfFirst { it.id == currentSong.id } else -1
        if (newIndex != -1) {
            exoPlayer.seekTo(newIndex, exoPlayer.currentPosition)
        }
    }


    // --- UI Updates & Service Lifecycle ---
    private fun updateTrackInfo() {
        val currentSong = displayedSongs.getOrNull(exoPlayer.currentMediaItemIndex)
        if (currentSong != null) {
            textTitle.text = currentSong.title
            textArtist.text = currentSong.artist
        } else {
            textTitle.text = "Music Player"
            textArtist.text = "No song selected"
        }
    }

    private fun startForegroundService() {
        val channelId = "floating_bubble_channel"
        val channelName = "Floating Bubble Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Music Player")
            .setContentText("Floating bubble is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        windowManager.removeView(bubbleView)
        windowManager.removeView(playerView)
        windowManager.removeView(playlistView)
    }

    private enum class SortType { AZ, ZA, ARTIST }
}
