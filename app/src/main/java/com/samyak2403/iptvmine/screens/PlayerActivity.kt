package com.samyak2403.iptvmine.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import com.samyak2403.iptvmine.R
import com.samyak2403.iptvmine.model.Channel

@UnstableApi
class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: androidx.media3.ui.PlayerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var imageViewFullScreen: ImageView
    private lateinit var imageViewLock: ImageView
    private lateinit var linearLayoutControlUp: LinearLayout
    private lateinit var linearLayoutControlBottom: LinearLayout
    private lateinit var channel: Channel
    private var playbackPosition = 0L
    private var isPlayerReady = false
    private var isFullScreen = false
    private var isLock = false
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackState = Player.STATE_IDLE

    companion object {
        private const val INCREMENT_MILLIS = 5000L

        fun start(context: Context, channel: Channel) {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra("channel", channel)
            }
            context.startActivity(intent)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Keep the screen on while playing video
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Retrieve channel from savedInstanceState or intent
        channel = savedInstanceState?.getParcelable("channel") ?: intent.getParcelableExtra("channel") ?: return

        setFindViewById()
        setupPlayer()
        setLockScreen()
        setFullScreen()
    }

    private fun setFindViewById() {
        playerView = findViewById(R.id.playerView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)
        imageViewFullScreen = findViewById(R.id.imageViewFullScreen)
        imageViewLock = findViewById(R.id.imageViewLock)
        linearLayoutControlUp = findViewById(R.id.linearLayoutControlUp)
        linearLayoutControlBottom = findViewById(R.id.linearLayoutControlBottom)
    }

    private fun setupPlayer() {
        // Create a factory for customizing the player with better configuration
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(15000) // 15 seconds
            .setReadTimeoutMs(15000)
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent("IPTVmine/1.0 (Android)")

        // Build the player with custom parameters
        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(INCREMENT_MILLIS)
            .setSeekForwardIncrementMs(INCREMENT_MILLIS)
            .setHandleAudioBecomingNoisy(true)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(this)
                    .setDataSourceFactory(httpDataSourceFactory)
            )
            .build()

        // Set up player view
        playerView.player = player
        playerView.controllerShowTimeoutMs = 3000 // Controls hide after 3 seconds
        playerView.controllerHideOnTouch = true
        playerView.setShowBuffering(androidx.media3.ui.PlayerView.SHOW_BUFFERING_ALWAYS)
        
        // Make sure the controller is shown initially
        playerView.showController()
        
        // Add listener for player events
        player.addListener(playerListener)
        
        // Try to play the stream
        playStream(channel.streamUrl)
    }

    private fun playStream(streamUrl: String) {
        try {
            Log.d("PlayerActivity", "Attempting to play: $streamUrl")
            
            // Detect stream type and create appropriate MediaItem
            val mediaItem = detectAndCreateMediaItem(streamUrl)
            
            // Set media item and prepare player
            player.setMediaItems(listOf(mediaItem), currentItem, playbackPosition)
            player.playWhenReady = playWhenReady
            player.prepare()
            
            // Update play/pause button state
            updatePlayPauseButton(player.isPlaying)
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error setting up media", e)
            // Create a source error exception
            val playbackException = PlaybackException(
                "Failed to setup media: ${e.message}",
                e,
                PlaybackException.ERROR_CODE_IO_UNSPECIFIED
            )
            handlePlayerError(playbackException)
        }
    }

    private fun detectAndCreateMediaItem(url: String): MediaItem {
        // Basic URL validation
        if (!url.startsWith("http") && !url.startsWith("rtmp")) {
            Log.w("PlayerActivity", "Invalid URL scheme: $url")
        }
        
        // Get file extension or identify format from URL
        val extension = url.substringAfterLast(".", "").lowercase()
        val containsFormat = getFormatIdentifierFromUrl(url)
        
        // Create a MediaItem with appropriate settings based on URL patterns
        val builder = MediaItem.Builder().setUri(Uri.parse(url))
        
        // Set appropriate MIME type based on format detection
        when {
            // HLS streams (.m3u8)
            extension == "m3u8" || containsFormat == "m3u8" || containsFormat == "hls" -> {
                Log.d("PlayerActivity", "Detected HLS stream")
                builder.setMimeType(MimeTypes.APPLICATION_M3U8)
            }
            
            // DASH streams (.mpd)
            extension == "mpd" || containsFormat == "mpd" || containsFormat == "dash" -> {
                Log.d("PlayerActivity", "Detected DASH stream")
                builder.setMimeType(MimeTypes.APPLICATION_MPD)
            }
            
            // Smooth Streaming (.ism, .isml)
            extension == "ism" || extension == "isml" || url.contains("manifest") -> {
                Log.d("PlayerActivity", "Detected Smooth Streaming")
                builder.setMimeType(MimeTypes.APPLICATION_SS)
            }
            
            // MP4 videos
            extension == "mp4" || containsFormat == "mp4" -> {
                Log.d("PlayerActivity", "Detected MP4 video")
                builder.setMimeType(MimeTypes.VIDEO_MP4)
            }
            
            // WebM videos
            extension == "webm" -> {
                Log.d("PlayerActivity", "Detected WebM video")
                builder.setMimeType(MimeTypes.VIDEO_WEBM)
            }
            
            // MKV videos
            extension == "mkv" -> {
                Log.d("PlayerActivity", "Detected MKV video")
                builder.setMimeType(MimeTypes.VIDEO_MATROSKA)
            }
            
            // MP3 audio
            extension == "mp3" -> {
                Log.d("PlayerActivity", "Detected MP3 audio")
                builder.setMimeType(MimeTypes.AUDIO_MPEG)
            }
            
            // AAC audio
            extension == "aac" -> {
                Log.d("PlayerActivity", "Detected AAC audio")
                builder.setMimeType(MimeTypes.AUDIO_AAC)
            }
            
            // MPEG-TS
            extension == "ts" -> {
                Log.d("PlayerActivity", "Detected MPEG-TS")
                builder.setMimeType(MimeTypes.VIDEO_MP2T)
            }
            
            // FLV videos
            extension == "flv" -> {
                Log.d("PlayerActivity", "Detected FLV video")
                builder.setMimeType(MimeTypes.VIDEO_FLV)
            }
            
            // Default case for other media types
            else -> {
                Log.d("PlayerActivity", "Using default media type detection")
                // Let Media3 handle automatic format detection
            }
        }
        
        // Configure for live streaming if detected
        val isLiveStream = url.contains("live") || url.contains("24x7") || 
                          url.contains("stream") || url.contains("real-time")
        
        if (isLiveStream) {
            Log.d("PlayerActivity", "Configuring for live streaming")
            builder.setLiveConfiguration(
                MediaItem.LiveConfiguration.Builder()
                    .setMaxPlaybackSpeed(1.02f)
                    .setTargetOffsetMs(5000)
                    .setMaxOffsetMs(10000)
                    .setMinOffsetMs(3000)
                    .setMinPlaybackSpeed(0.97f)
                    .build()
            )
        }
        
        return builder.build()
    }

    /**
     * Extracts format identifier from URL if present
     */
    private fun getFormatIdentifierFromUrl(url: String): String {
        // Common format identifiers in URLs
        val formatIdentifiers = listOf(
            "format=m3u8", "format=mpd", "format=hls", "format=dash", "format=mp4", 
            "type=m3u8", "type=mpd", "type=hls", "type=dash", "type=mp4",
            "playlist_type=m3u8", "stream_type=hls", 
            "/hls/", "/dash/", "/m3u8/", "/mpd/"
        )
        
        // Check each identifier
        for (identifier in formatIdentifiers) {
            if (url.contains(identifier, ignoreCase = true)) {
                // Extract the format from the identifier (after '=' or between '/')
                val format = if (identifier.contains("=")) {
                    identifier.substringAfter("=")
                } else {
                    identifier.trim('/')
                }
                return format
            }
        }
        
        return ""
    }

    // Try to convert HTTP URLs to HTTPS for better compatibility
    private fun tryHttpsUrl(httpUrl: String): String {
        if (!httpUrl.startsWith("http://")) {
            return httpUrl
        }
        
        return "https://" + httpUrl.substring(7)
    }

    // Create an alternative MediaItem with HTTPS URL when possible
    private fun createAlternativeMediaItem(url: String): MediaItem? {
        if (!url.startsWith("http://")) {
            return null
        }
        
        val httpsUrl = tryHttpsUrl(url)
        return detectAndCreateMediaItem(httpsUrl)
    }

    private fun handlePlayerError(error: PlaybackException) {
        Log.e("PlayerActivity", "Playback error: ${error.errorCodeName}", error)
        
        // Get the detailed error message for better debugging
        val errorCause = error.cause?.message ?: error.message ?: "Unknown error"
        Log.e("PlayerActivity", "Error details: $errorCause")
        
        // Check for specific error types
        val cleartextError = error.cause?.cause?.message?.contains("Cleartext HTTP traffic") == true
        val hlsParsingError = error.cause?.message?.contains("Input does not start with the #EXTM3U header") == true || 
                             error.cause?.message?.contains("contentIsMalformed=true") == true
        val formatError = error.cause?.message?.contains("Format") == true || 
                         error.cause?.message?.contains("No decoder") == true
        val sourceError = error.errorCode == PlaybackException.ERROR_CODE_IO_UNSPECIFIED ||
                         error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
        
        // Implement a cascade of fallback strategies
        if (cleartextError && channel.streamUrl.startsWith("http://")) {
            // Try HTTPS version if cleartext HTTP not allowed
            Log.d("PlayerActivity", "Attempting to use HTTPS instead of HTTP")
            val httpsUrl = tryHttpsUrl(channel.streamUrl)
            playStream(httpsUrl)
            return
        } else if (hlsParsingError) {
            // For HLS parsing errors, try as generic media
            if (channel.streamUrl.endsWith(".m3u8", ignoreCase = true)) {
                Log.d("PlayerActivity", "HLS parsing failed, trying direct media without format hint")
                
                // Create a generic MediaItem without MimeType
                val genericMediaItem = MediaItem.Builder()
                    .setUri(Uri.parse(channel.streamUrl))
                    .build()
                    
                player.clearMediaItems()
                player.setMediaItem(genericMediaItem)
                player.prepare()
                player.play()
                return
            } else {
                Log.d("PlayerActivity", "HLS parsing failed, trying raw URI")
                val nonHlsMediaItem = MediaItem.fromUri(Uri.parse(channel.streamUrl))
                player.clearMediaItems()
                player.setMediaItem(nonHlsMediaItem)
                player.prepare()
                player.play()
                return
            }
        } else if (formatError || sourceError) {
            // Try with different format detection
            Log.d("PlayerActivity", "Format/source error, trying alternative format")
            tryAlternativeFormat(channel.streamUrl)
            return
        }
        
        // If we reach here, we couldn't recover automatically
        // Show error message to the user
        errorTextView.visibility = View.VISIBLE
        playerView.visibility = View.GONE
        
        // Try fallback approach based on error type
        when {
            hlsParsingError -> {
                errorTextView.text = "Invalid stream format. This stream may not be available."
            }
            cleartextError -> {
                errorTextView.text = "Insecure connection not allowed. Try using an HTTPS stream or check app settings."
            }
            formatError -> {
                errorTextView.text = "This media format is not supported on your device."
            }
            error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
            error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
                errorTextView.text = "Network error. Please check your connection."
            }
            error.errorCode == PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED -> {
                errorTextView.text = "Audio format not supported by your device."
            }
            else -> {
                errorTextView.text = "Playback error: ${error.errorCodeName}"
            }
        }
    }

    /**
     * Try to play a stream with an alternative format detection
     */
    private fun tryAlternativeFormat(streamUrl: String) {
        // List of common MIME types to try
        val mimeTypesToTry = arrayOf(
            MimeTypes.APPLICATION_M3U8,
            MimeTypes.APPLICATION_MPD,
            MimeTypes.VIDEO_MP4,
            MimeTypes.VIDEO_MATROSKA,
            MimeTypes.VIDEO_MP2T,
            null // also try without MIME type
        )
        
        // Try each MIME type in sequence
        for ((index, mimeType) in mimeTypesToTry.withIndex()) {
            val builder = MediaItem.Builder().setUri(Uri.parse(streamUrl))
            if (mimeType != null) {
                builder.setMimeType(mimeType)
            }
            
            try {
                Log.d("PlayerActivity", "Trying format ${mimeType ?: "auto-detect"} (attempt ${index + 1})")
                val mediaItem = builder.build()
                player.clearMediaItems()
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
                
                // Add a delayed check to see if playback actually started
                Handler(Looper.getMainLooper()).postDelayed({
                    if (player.isPlaying) {
                        Log.d("PlayerActivity", "Successfully playing with format ${mimeType ?: "auto-detect"}")
                    } else if (index < mimeTypesToTry.size - 1) {
                        Log.d("PlayerActivity", "Playback failed with ${mimeType ?: "auto-detect"}, trying next format")
                        // Will try next format on next error
                    }
                }, 2000) // Check after 2 seconds
                
                return
            } catch (e: Exception) {
                Log.e("PlayerActivity", "Error with format ${mimeType ?: "auto-detect"}: ${e.message}")
                // Continue to next format
            }
        }
        
        // If all formats failed, show error
        errorTextView.visibility = View.VISIBLE
        playerView.visibility = View.GONE
        errorTextView.text = "Unable to play this stream. Format not supported."
    }

    private fun lockScreen(lock: Boolean) {
        linearLayoutControlUp.visibility = if (lock) View.INVISIBLE else View.VISIBLE
        linearLayoutControlBottom.visibility = if (lock) View.INVISIBLE else View.VISIBLE
    }

    private fun setLockScreen() {
        imageViewLock.setOnClickListener {
            isLock = !isLock
            imageViewLock.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    if (isLock) R.drawable.ic_baseline_lock else R.drawable.ic_baseline_lock_open
                )
            )
            lockScreen(isLock)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setFullScreen() {
        imageViewFullScreen.setOnClickListener {
            isFullScreen = !isFullScreen
            imageViewFullScreen.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    if (isFullScreen) R.drawable.ic_baseline_fullscreen_exit else R.drawable.ic_baseline_fullscreen
                )
            )
            requestedOrientation = if (isFullScreen) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUi()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            showSystemUi()
        }
    }
    
    private fun hideSystemUi() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        supportActionBar?.hide()
    }
    
    private fun showSystemUi() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        supportActionBar?.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("channel", channel)
        updateStartPosition()
    }
    
    private fun updateStartPosition() {
        playbackPosition = player.currentPosition
        currentItem = player.currentMediaItemIndex
        playWhenReady = player.playWhenReady
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || !isPlayerReady) {
            initializePlayer()
        }
    }
    
    private fun initializePlayer() {
        if (!::player.isInitialized) {
            setupPlayer()
        } else {
            playerView.player = player
            val mediaItem = detectAndCreateMediaItem(channel.streamUrl)
            player.setMediaItems(listOf(mediaItem), currentItem, playbackPosition)
            player.playWhenReady = playWhenReady
            player.prepare()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            updateStartPosition()
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            updateStartPosition()
            releasePlayer()
        }
    }
    
    private fun releasePlayer() {
        if (::player.isInitialized) {
            updateStartPosition()
            player.removeListener(playerListener)
            player.clearMediaItems()
            player.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.release()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isLock) return
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageViewFullScreen.performClick()
        } else super.onBackPressed()
    }

    // Create a reusable player listener
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            playbackState = state
            when (state) {
                Player.STATE_READY -> {
                    isPlayerReady = true
                    progressBar.visibility = View.GONE
                }
                Player.STATE_BUFFERING -> {
                    progressBar.visibility = View.VISIBLE
                }
                Player.STATE_ENDED -> {
                    // Attempt to restart playback for live streams
                    if (player.isCurrentMediaItemLive) {
                        player.seekToDefaultPosition()
                        player.prepare()
                    }
                }
                Player.STATE_IDLE -> {
                    // Player is idle, could handle differently
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            // Update play/pause button visibility based on playback state
            updatePlayPauseButton(isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            handlePlayerError(error)
        }
        
        override fun onIsLoadingChanged(isLoading: Boolean) {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    /**
     * Updates the play/pause button visibility based on player state
     */
    private fun updatePlayPauseButton(isPlaying: Boolean) {
        try {
            val playButton = playerView.findViewById<View>(androidx.media3.ui.R.id.exo_play)
            val pauseButton = playerView.findViewById<View>(androidx.media3.ui.R.id.exo_pause)
            
            if (isPlaying) {
                playButton?.visibility = View.GONE
                pauseButton?.visibility = View.VISIBLE
            } else {
                playButton?.visibility = View.VISIBLE
                pauseButton?.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error updating play/pause button: ${e.message}")
        }
    }
}
