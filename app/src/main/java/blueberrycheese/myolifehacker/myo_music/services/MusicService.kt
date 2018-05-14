package blueberrycheese.myolifehacker.myo_music.activities.services

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.AudioManager.*
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Handler
import android.os.PowerManager
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.commons.helpers.isOreoPlus
import blueberrycheese.myolifehacker.R
import blueberrycheese.myolifehacker.events.ServiceEvent
import blueberrycheese.myolifehacker.myo_music.activities.activitys.MainActivity
import blueberrycheese.myolifehacker.myo_music.activities.extensions.config
import blueberrycheese.myolifehacker.myo_music.activities.extensions.dbHelper
import blueberrycheese.myolifehacker.myo_music.activities.helpers.*
import blueberrycheese.myolifehacker.myo_music.activities.models.Events
import blueberrycheese.myolifehacker.myo_music.activities.models.Song
import blueberrycheese.myolifehacker.myo_music.activities.receivers.ControlActionsListener
import blueberrycheese.myolifehacker.myo_music.activities.receivers.HeadsetPlugReceiver
import blueberrycheese.myolifehacker.myo_music.activities.receivers.RemoteControlReceiver
import com.squareup.otto.Bus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.IOException
import java.util.*
import android.content.Context
import blueberrycheese.myolifehacker.myo_music.activities.extensions.sendIntent

class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
    companion object {
        private val TAG = MusicService::class.java.simpleName
        private const val MIN_INITIAL_DURATION = 30
        private const val PROGRESS_UPDATE_INTERVAL = 1000
        private const val MIN_SKIP_LENGTH = 2000
        private const val NOTIFICATION_ID = 78    // just a random number

        var mCurrSong: Song? = null
        var mCurrSongCover: Bitmap? = null
        var mEqualizer: Equalizer? = null
        private var mHeadsetPlugReceiver: HeadsetPlugReceiver? = null
        private var mPlayer: MediaPlayer? = null
        private var mPlayedSongIndexes = ArrayList<Int>()
        private var mBus: Bus? = null
        private var mProgressHandler: Handler? = null
        private var mSongs = ArrayList<Song>()
        private var mAudioManager: AudioManager? = null
        private var mCoverArtHeight = 0
        private var mOreoFocusHandler: OreoAudioFocusHandler? = null

        private var mWasPlayingAtFocusLost = false
        private var mPlayOnPrepare = true
        private var mIsThirdPartyIntent = false
        private var intentUri: Uri? = null
        private var isServiceInitialized = false
        private var prevAudioFocusState = 0

        private var gestureNum = -1
        internal var smoothcount = IntArray(6)
        private val VIBRATION_A = 1
        private val VIBRATION_B = 2
        private val VIBRATION_C = 3
        private val ADDITIONAL_DELAY = 0
        fun getIsPlaying() = mPlayer?.isPlaying == true
    }

    override fun onCreate() {
        super.onCreate()

        if (mBus == null) {
            mBus = BusProvider.instance
            mBus!!.register(this)
        }

        mCoverArtHeight = resources.getDimension(R.dimen.top_art_height).toInt()
        mProgressHandler = Handler()
        val remoteControlComponent = ComponentName(packageName, RemoteControlReceiver::class.java.name)
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager!!.registerMediaButtonEventReceiver(remoteControlComponent)
        if (isOreoPlus()) {
            mOreoFocusHandler = OreoAudioFocusHandler(applicationContext)
        }

        if (hasPermission(PERMISSION_WRITE_STORAGE)) {
            initService()
        } else {
            mBus!!.post(Events.NoStoragePermission())
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
            Log.e(TAG,"EventBus registered")
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
        destroyPlayer()
    }

    private fun initService() {
        mSongs.clear()
        mPlayedSongIndexes = ArrayList()
        mCurrSong = null
        if (mIsThirdPartyIntent && intentUri != null) {
            val path = getRealPathFromURI(intentUri!!) ?: ""
            val song = dbHelper.getSongFromPath(path)
            if (song != null) {
                mSongs.add(song)
            }
        } else {
            getSortedSongs()
        }

        mHeadsetPlugReceiver = HeadsetPlugReceiver()
        mWasPlayingAtFocusLost = false
        initMediaPlayerIfNeeded()
        setupNotification()
        isServiceInitialized = true
    }
//뮤직 서비스 얻어오는 곳
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (!hasPermission(PERMISSION_WRITE_STORAGE)) {
            return START_NOT_STICKY
        }

        when (intent.action) {
            INIT -> {
                mIsThirdPartyIntent = false
                if (!isServiceInitialized) {
                    initService()
                }
                initSongs()
            }
            INIT_PATH -> {
                mIsThirdPartyIntent = true
                if (intentUri != intent.data) {
                    intentUri = intent.data
                    initService()
                    initSongs()
                } else {
                    updateUI()
                }
            }
            SETUP -> {
                mPlayOnPrepare = true
                setupNextSong()
            }
            PREVIOUS -> {
                mPlayOnPrepare = true
                playPreviousSong()
            }
            PAUSE -> pauseSong()
            PLAYPAUSE -> {
                mPlayOnPrepare = true
                if (getIsPlaying()) {
                    pauseSong()
                } else {
                    resumeSong()
                }
            }
            NEXT -> {
                mPlayOnPrepare = true
                setupNextSong()
            }
            PLAYPOS -> playSong(intent)
            EDIT -> {
                mCurrSong = intent.getSerializableExtra(EDITED_SONG) as Song
                songChanged(mCurrSong)
                setupNotification()
            }
            FINISH -> {
                mBus!!.post(Events.ProgressUpdated(0))
                destroyPlayer()
            }
            REFRESH_LIST -> {
                getSortedSongs()
                mBus!!.post(Events.PlaylistUpdated(mSongs))
            }
            SET_PROGRESS -> {
                if (mPlayer != null) {
                    val progress = intent.getIntExtra(PROGRESS, mPlayer!!.currentPosition / 1000)
                    updateProgress(progress)
                }
            }
            SET_EQUALIZER -> {
                if (intent.extras?.containsKey(EQUALIZER) == true) {
                    val presetID = intent.extras.getInt(EQUALIZER)
                    if (mEqualizer != null) {
                        setPreset(presetID)
                    }
                }
            }
            SKIP_BACKWARD -> skipBackward()
            SKIP_FORWARD -> skipForward()
        }

        return START_NOT_STICKY
    }

    private fun setupSong() {
        if (mIsThirdPartyIntent) {
            initMediaPlayerIfNeeded()

            try {
                mPlayer!!.apply {
                    reset()
                    setDataSource(applicationContext, intentUri)
                    setOnPreparedListener(null)
                    prepare()
                    start()
                    requestAudioFocus()
                }

                val song = mSongs.first()
                mSongs.clear()
                mSongs.add(song)
                mCurrSong = song
                updateUI()
            } catch (e: Exception) {
                Log.e(TAG, "setupSong Exception $e")
            }
        } else {
            mPlayOnPrepare = false
            setupNextSong()
        }
    }

    private fun initSongs() {
        updateUI()
        if (mCurrSong == null) {
            setupSong()
        } else {
            val secs = mPlayer!!.currentPosition / 1000
            mBus!!.post(Events.ProgressUpdated(secs))
        }
    }

    private fun updateUI() {
        mBus!!.post(Events.PlaylistUpdated(mSongs))
        songChanged(mCurrSong)
        songStateChanged(getIsPlaying())
    }

    private fun initMediaPlayerIfNeeded() {
        if (mPlayer != null)
            return

        mPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener(this@MusicService)
            setOnCompletionListener(this@MusicService)
            setOnErrorListener(this@MusicService)
        }
        setupEqualizer()
    }

    private fun getAllDeviceSongs() {
        val ignoredPaths = config.ignoredPaths
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val columns = arrayOf(MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA)

        var cursor: Cursor? = null
        val paths = ArrayList<String>()

        try {
            cursor = contentResolver.query(uri, columns, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val duration = cursor.getIntValue(MediaStore.Audio.Media.DURATION) / 1000
                    if (duration > MIN_INITIAL_DURATION) {
                        val path = cursor.getStringValue(MediaStore.Audio.Media.DATA)
                        if (!ignoredPaths.contains(path)) {
                            paths.add(path)
                        }
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }

        dbHelper.addSongsToPlaylist(paths)
    }

    private fun getSortedSongs() {
        if (config.currentPlaylist == DBHelper.ALL_SONGS_ID) {
            getAllDeviceSongs()
        }

        mSongs = dbHelper.getSongs()
        Song.sorting = config.sorting
        mSongs.sort()
    }

    private fun setupEqualizer() {
        mEqualizer = Equalizer(1, mPlayer!!.audioSessionId)
        mEqualizer?.enabled = true
        setPreset(config.equalizer)
    }

    private fun setPreset(id: Int) {
        try {
            mEqualizer?.usePreset(id.toShort())
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "setPreset $e")
        }
    }

    @SuppressLint("NewApi")
    private fun setupNotification() {
        val title = mCurrSong?.title ?: ""
        val artist = mCurrSong?.artist ?: ""
        val playPauseButtonPosition = 1
        val nextButtonPosition = 2
        val playPauseIcon = if (getIsPlaying()) R.drawable.ic_pause else R.drawable.ic_play

        var notifWhen = 0L
        var showWhen = false
        var usesChronometer = false
        var ongoing = false
        if (getIsPlaying()) {
            notifWhen = System.currentTimeMillis() - mPlayer!!.currentPosition
            showWhen = true
            usesChronometer = true
            ongoing = true
        }

        val channelId = "music_player_channel"
        if (isOreoPlus()) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = resources.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            NotificationChannel(channelId, name, importance).apply {
                enableLights(false)
                enableVibration(false)
                notificationManager.createNotificationChannel(this)
            }
        }

        if (mCurrSongCover?.isRecycled == true) {
            mCurrSongCover = resources.getColoredBitmap(R.drawable.ic_headset, config.textColor)
        }

        val notification = NotificationCompat.Builder(this)
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(playPauseButtonPosition, nextButtonPosition))
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.ic_headset_small)
                .setLargeIcon(mCurrSongCover)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_MAX)
                .setWhen(notifWhen)
                .setShowWhen(showWhen)
                .setUsesChronometer(usesChronometer)
                .setContentIntent(getContentIntent())
                .setOngoing(ongoing)
                .setChannelId(channelId)
                .addAction(R.drawable.ic_previous, getString(R.string.previous), getIntent(PREVIOUS))
                .addAction(playPauseIcon, getString(R.string.playpause), getIntent(PLAYPAUSE))
                .addAction(R.drawable.ic_next, getString(R.string.next), getIntent(NEXT))

        startForeground(NOTIFICATION_ID, notification.build())

        if (!getIsPlaying()) {
            Handler().postDelayed({ stopForeground(false) }, 500)
        }
    }

    private fun getContentIntent(): PendingIntent {
        val contentIntent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(this, 0, contentIntent, 0)
    }

    private fun getIntent(action: String): PendingIntent {
        val intent = Intent(this, ControlActionsListener::class.java)
        intent.action = action
        return PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
    }

    private fun getNewSongId(): Int {
        return if (config.isShuffleEnabled) {
            val cnt = mSongs.size
            when (cnt) {
                0 -> -1
                1 -> 0
                else -> {
                    val random = Random()
                    var newSongIndex = random.nextInt(cnt)
                    while (mPlayedSongIndexes.contains(newSongIndex)) {
                        newSongIndex = random.nextInt(cnt)
                    }
                    newSongIndex
                }
            }
        } else {
            if (mPlayedSongIndexes.isEmpty()) {
                return 0
            }

            val lastIndex = mPlayedSongIndexes[mPlayedSongIndexes.size - 1]
            (lastIndex + 1) % Math.max(mSongs.size, 1)
        }
    }

    private fun playPreviousSong() {
        if (mSongs.isEmpty()) {
            handleEmptyPlaylist()
            return
        }

        initMediaPlayerIfNeeded()

        // play the previous song if we are less than 5 secs into the song, else restart
        // remove the latest song from the list
        if (mPlayedSongIndexes.size > 1 && mPlayer!!.currentPosition < 5000) {
            mPlayedSongIndexes.removeAt(mPlayedSongIndexes.size - 1)
            setSong(mPlayedSongIndexes[mPlayedSongIndexes.size - 1], false)
        } else {
            restartSong()
        }
    }

    private fun pauseSong() {
        if (mSongs.isEmpty())
            return

        initMediaPlayerIfNeeded()

        mPlayer!!.pause()
        songStateChanged(false)
    }

    private fun resumeSong() {
        if (mSongs.isEmpty()) {
            handleEmptyPlaylist()
            return
        }

        initMediaPlayerIfNeeded()

        if (mCurrSong == null) {
            setupNextSong()
        } else {
            mPlayer!!.start()
            requestAudioFocus()
        }

        songStateChanged(true)
    }

    private fun setupNextSong() {
        if (mIsThirdPartyIntent) {
            setupSong()
        } else {
            setSong(getNewSongId(), true)
        }
    }

    private fun restartSong() {
        val newSongIndex = if (mPlayedSongIndexes.isEmpty()) 0 else mPlayedSongIndexes[mPlayedSongIndexes.size - 1]
        setSong(newSongIndex, false)
    }

    private fun playSong(intent: Intent) {
        if (mIsThirdPartyIntent) {
            setupSong()
        } else {
            mPlayOnPrepare = true
            val pos = intent.getIntExtra(SONG_POS, 0)
            setSong(pos, true)
        }
    }

    private fun setSong(songIndex: Int, addNewSongToHistory: Boolean) {
        if (mSongs.isEmpty()) {
            handleEmptyPlaylist()
            return
        }

        initMediaPlayerIfNeeded()

        mPlayer!!.reset()
        if (addNewSongToHistory) {
            mPlayedSongIndexes.add(songIndex)
            if (mPlayedSongIndexes.size >= mSongs.size) {
                mPlayedSongIndexes.clear()
            }
        }

        mCurrSong = mSongs[Math.min(songIndex, mSongs.size - 1)]

        try {
            val trackUri = if (mCurrSong!!.id == 0L) {
                Uri.fromFile(File(mCurrSong!!.path))
            } else {
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCurrSong!!.id)
            }
            mPlayer!!.setDataSource(applicationContext, trackUri)
            mPlayer!!.prepareAsync()
            songChanged(mCurrSong)
        } catch (e: IOException) {
            Log.e(TAG, "setSong IOException $e")
        }
    }

    private fun handleEmptyPlaylist() {
        mPlayer!!.pause()
        abandonAudioFocus()
        mCurrSong = null
        songChanged(null)
        songStateChanged(false)
    }

    override fun onBind(intent: Intent) = null

    override fun onCompletion(mp: MediaPlayer) {
        if (!config.autoplay) {
            return
        }

        if (config.repeatSong) {
            restartSong()
        } else if (mPlayer!!.currentPosition > 0) {
            mPlayer!!.reset()
            setupNextSong()
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mPlayer!!.reset()
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        if (mPlayOnPrepare) {
            mp.start()
            requestAudioFocus()
        }
        songStateChanged(getIsPlaying())
        setupNotification()
    }

    private fun songChanged(song: Song?) {
        mCurrSongCover = getAlbumImage(song)
        mBus!!.post(Events.SongChanged(song))
    }

    private fun getAlbumImage(song: Song?): Bitmap {
        if (File(song?.path ?: "").exists()) {
            try {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(song!!.path)
                val rawArt = mediaMetadataRetriever.embeddedPicture
                if (rawArt != null) {
                    val options = BitmapFactory.Options()
                    val bitmap = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, options)
                    if (bitmap != null) {
                        return if (bitmap.height > mCoverArtHeight * 2) {
                            val ratio = bitmap.width / bitmap.height.toFloat()
                            Bitmap.createScaledBitmap(bitmap, (mCoverArtHeight * ratio * 2).toInt(), mCoverArtHeight * 2, false)
                        } else {
                            bitmap
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }

        return resources.getColoredBitmap(R.drawable.ic_headset, config.textColor)
    }

    private fun destroyPlayer() {
        mPlayer?.stop()
        mPlayer?.release()
        mPlayer = null

        if (mBus != null) {
            songStateChanged(false)
            songChanged(null)
            mBus!!.unregister(this)
        }

        mEqualizer?.release()

        stopForeground(true)
        stopSelf()
        mIsThirdPartyIntent = false
        isServiceInitialized = false

        val remoteControlComponent = ComponentName(packageName, RemoteControlReceiver::class.java.name)
        mAudioManager!!.unregisterMediaButtonEventReceiver(remoteControlComponent)
        abandonAudioFocus()
    }

    private fun requestAudioFocus() {
        if (isOreoPlus()) {
            mOreoFocusHandler?.requestAudioFocus(this)
        } else {
            mAudioManager?.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    private fun abandonAudioFocus() {
        if (isOreoPlus()) {
            mOreoFocusHandler?.abandonAudioFocus()
        } else {
            mAudioManager?.abandonAudioFocus(this)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AUDIOFOCUS_GAIN -> audioFocusGained()
            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> duckAudio()
            AUDIOFOCUS_LOSS, AUDIOFOCUS_LOSS_TRANSIENT -> audioFocusLost()
        }
        prevAudioFocusState = focusChange
    }

    private fun audioFocusLost() {
        if (getIsPlaying()) {
            mWasPlayingAtFocusLost = true
            pauseSong()
        } else {
            mWasPlayingAtFocusLost = false
        }
    }

    private fun audioFocusGained() {
        if (mWasPlayingAtFocusLost) {
            if (prevAudioFocusState == AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                unduckAudio()
            } else {
                resumeSong()
            }
        }

        mWasPlayingAtFocusLost = false
    }

    private fun duckAudio() {
        mPlayer?.setVolume(0.3f, 0.3f)
        mWasPlayingAtFocusLost = getIsPlaying()
    }

    private fun unduckAudio() {
        mPlayer?.setVolume(1f, 1f)
    }

    private fun updateProgress(progress: Int) {
        mPlayer!!.seekTo(progress * 1000)
        resumeSong()
    }

    private fun songStateChanged(isPlaying: Boolean) {
        handleProgressHandler(isPlaying)
        setupNotification()
        mBus!!.post(Events.SongStateChanged(isPlaying))

        if (isPlaying) {
            val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
            filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            registerReceiver(mHeadsetPlugReceiver, filter)
        } else {
            try {
                unregisterReceiver(mHeadsetPlugReceiver)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "IllegalArgumentException $e")
            }
        }
    }

    private fun handleProgressHandler(isPlaying: Boolean) {
        if (isPlaying) {
            mProgressHandler!!.post(object : Runnable {
                override fun run() {
                    val secs = mPlayer!!.currentPosition / 1000
                    mBus!!.post(Events.ProgressUpdated(secs))
                    mProgressHandler!!.removeCallbacksAndMessages(null)
                    mProgressHandler!!.postDelayed(this, PROGRESS_UPDATE_INTERVAL.toLong())
                }
            })
        } else {
            mProgressHandler!!.removeCallbacksAndMessages(null)
        }
    }

    private fun skipBackward() {
        skip(false)
    }

    private fun skipForward() {
        skip(true)
    }

    private fun skip(forward: Boolean) {
        val curr = mPlayer!!.currentPosition
        val twoPercents = Math.max(mPlayer!!.duration / 50, MIN_SKIP_LENGTH)
        val newProgress = if (forward) curr + twoPercents else curr - twoPercents
        mPlayer!!.seekTo(newProgress)
        resumeSong()
    }

    //제스처에 따라 기능 얻어오는 곳

    @org.greenrobot.eventbus.Subscribe(threadMode = ThreadMode.MAIN)
    fun onGestureEvent(event: ServiceEvent.GestureEvent) {

        gestureNum = event.gestureNumber
        Log.d("MusicEvent", "MusicEvent Gesture num : " + event.gestureNumber)

        when (gestureNum) {
            0 -> {
                smoothcount[gestureNum]++
                if (smoothcount[gestureNum] > 2) {
                    //intent로 Musicservice에 가서 기능 가져오기
                    // 멈춤,재생
//                    Intent(this, MusicService::class.java).apply {
//
//                        action = PLAYPAUSE
//                        startService(this)
//                    }

                    this.sendIntent(blueberrycheese.myolifehacker.myo_music.activities.helpers.PLAYPAUSE)

                    //Send Vibration Event
                    EventBus.getDefault().post(ServiceEvent.VibrateEvent(VIBRATION_A))
                    //Restart lock Timer so user can use gesture continuously
                    EventBus.getDefault().post(ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY))

//                    Toasty.normal(this!!, "Play/Pause", Toast.LENGTH_SHORT, icon_1).show()
//                    smoothcount[gestureNum] = -1
                    resetSmoothCount()
                }


            }

            1 -> {
                smoothcount[gestureNum]++
                if (smoothcount[gestureNum] > 2){
                    //이전
//                    Intent(this, MusicService::class.java).apply {
//
//                        action = PREVIOUS
//                        startService(this)
//                    }

                    this.sendIntent(blueberrycheese.myolifehacker.myo_music.activities.helpers.PREVIOUS);
//                    sendIntent(PREVIOUS)

                    //Send Vibration Event
                    EventBus.getDefault().post(ServiceEvent.VibrateEvent(VIBRATION_A))
                    //Restart lock Timer so user can use gesture continuously
                    EventBus.getDefault().post(ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY))

//                    Toasty.normal(this!!, "Previous", Toast.LENGTH_SHORT, icon_2).show()
//                    smoothcount[gestureNum] = -1
                    resetSmoothCount()
                }

            }

            2 -> {
                smoothcount[gestureNum]++
                if (smoothcount[gestureNum] > 2) {
//                    Intent(this, MusicService::class.java).apply {
//
//                        action = NEXT
//                        startService(this)
//                    }

                    //앞으로
                    this.sendIntent(blueberrycheese.myolifehacker.myo_music.activities.helpers.NEXT)
//                    sendIntent(NEXT)

                    //Send Vibration Event
                    EventBus.getDefault().post(ServiceEvent.VibrateEvent(VIBRATION_A))
                    //Restart lock Timer so user can use gesture continuously
                    EventBus.getDefault().post(ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY))

//                    Toasty.normal(this!!, "Next", Toast.LENGTH_SHORT, icon_3).show()
//                    smoothcount[gestureNum] = -1
                    resetSmoothCount()
                }

            }

            3 -> {
                smoothcount[gestureNum]++
                if (smoothcount[gestureNum] > 2) {



//                    smoothcount[gestureNum] = -1
                    resetSmoothCount()
                }

            }

            else -> {
            }
        }
    }

    fun resetSmoothCount() {

        for (i in smoothcount.indices) {
            smoothcount[i] = 0
        }
    }

}
