package blueberrycheese.myolifehacker.myo_music.activities.activitys

import android.app.Activity
import android.app.PendingIntent.getActivity
import android.app.SearchManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.net.Uri
import android.nfc.Tag
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import blueberrycheese.myolifehacker.*
import blueberrycheese.myolifehacker.R.id.lottie_music_lock
import blueberrycheese.myolifehacker.R.id.lottie_music_unlock
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.interfaces.RecyclerScrollCallback
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.commons.models.Release
import com.simplemobiletools.commons.views.MyLinearLayoutManager
import blueberrycheese.myolifehacker.BuildConfig
import blueberrycheese.myolifehacker.FontConfig
import blueberrycheese.myolifehacker.R
import blueberrycheese.myolifehacker.R.string.playpause
import blueberrycheese.myolifehacker.SystemControl.GestureDetectMethod_System
import blueberrycheese.myolifehacker.SystemControl.GestureDetectModel_System
import blueberrycheese.myolifehacker.Toasty
import blueberrycheese.myolifehacker.events.ServiceEvent
import blueberrycheese.myolifehacker.myo_manage.*
import blueberrycheese.myolifehacker.myo_music.Gesture.GestureDetectMethod_Music
import blueberrycheese.myolifehacker.myo_music.Gesture.GestureDetectModel_Music
import blueberrycheese.myolifehacker.myo_music.Gesture.GestureDetectSendResultAction_Music
import blueberrycheese.myolifehacker.myo_music.Gesture.MusicEvent
import blueberrycheese.myolifehacker.myo_music.activities.adapters.SongAdapter
import blueberrycheese.myolifehacker.myo_music.activities.dialogs.ChangeSortingDialog
import blueberrycheese.myolifehacker.myo_music.activities.dialogs.NewPlaylistDialog
import blueberrycheese.myolifehacker.myo_music.activities.dialogs.RemovePlaylistDialog
import blueberrycheese.myolifehacker.myo_music.activities.extensions.*
import blueberrycheese.myolifehacker.myo_music.activities.helpers.*
import blueberrycheese.myolifehacker.myo_music.activities.inlines.indexOfFirstOrNull
import blueberrycheese.myolifehacker.myo_music.activities.interfaces.SongListListener
import blueberrycheese.myolifehacker.myo_music.activities.models.Events
import blueberrycheese.myolifehacker.myo_music.activities.models.Playlist
import blueberrycheese.myolifehacker.myo_music.activities.models.Song
import blueberrycheese.myolifehacker.myo_music.activities.services.MusicService
import com.airbnb.lottie.LottieAnimationView
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_tab_fragment1.*
import kotlinx.android.synthetic.main.music_activity_main.*
import kotlinx.android.synthetic.main.music_item_navigation.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.cert.TrustAnchor
import java.util.*
import java.util.logging.Level.OFF

class MainActivity : SimpleActivity(), SongListListener{
    private var isThirdPartyIntent = false
    private var songs = ArrayList<Song>()
    private var searchMenuItem: MenuItem? = null
    private var isSearchOpen = false
    private var artView: ViewGroup? = null
    private var oldCover: Drawable? = null

    private var icon_1: Drawable? = null
    private var icon_2:Drawable? = null
    private var icon_3:Drawable? = null
    private var icon_4:Drawable? = null
    private var icon_5:Drawable? = null
    private var icon_6:Drawable? = null
    private var actionbarSize = 0
    private var topArtHeight = 0

    private var myoApp: MyoApp? = null
    private var storedTextColor = 0
    private var storedShowAlbumCover = true

    //Detect용 가져옴
    /** Device Scanning Time (ms)  */
    private val SCAN_PERIOD: Long = 5000

    /** Intent code for requesting Bluetooth enable  */
    private val REQUEST_ENABLE_BT = 1

    private var myoConnection: Boolean?  = null
    private val TAG = "BLE_Myo"
    private var bluetoothDevice: BluetoothDevice? = null
    private val mHandler: Handler? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var gestureText: TextView? = null
    private var mMyoCallback: MyoGattCallback? = null
    private val commandList = MyoCommandList()
    private var deviceName: String? = null
    internal var gestureString = arrayOf("WiFi On, Off", "Sound Mode Chnage ", "Volume Up", "Volume Down", "Brightness Up", "Brightness Down")
    internal var animationView_music_lock: LottieAnimationView? = null
    internal var animationView_music_unlock: LottieAnimationView? = null
    private var first = true
    private val saveModel: GestureSaveModel? = null
    private var saveMethod: GestureSaveMethod? = null
    private var detectModel: GestureDetectModel_Music? = null
    private var detectMethod: GestureDetectMethod_Music? = null
    private var gestureNum = -1
    internal var dttButton: Button?=null
    internal var smoothcount = IntArray(6)
    lateinit var bus: Bus

    private val VIBRATION_A = 1
    private val VIBRATION_B = 2
    private val VIBRATION_C = 3
    private val ADDITIONAL_DELAY = 0
    private val CURRENT_ACTIVITY = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_activity_main)

        FontConfig.setGlobalFont(this, this!!.getWindow().getDecorView())
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)  //윈도우 가장위에 배터리,wifi뜨는 부분 제거
        icon_1 = resources.getDrawable(R.drawable.gesture_1_w)
        icon_2 = resources.getDrawable(R.drawable.gesture_2_w)
        icon_3 = resources.getDrawable(R.drawable.gesture_3_w)
        icon_4 = resources.getDrawable(R.drawable.gesture_4_w)
        icon_5 = resources.getDrawable(R.drawable.gesture_5_w)
        icon_6 = resources.getDrawable(R.drawable.gesture_6_w)
      //  playlist = findViewById(R.id.playlists_list) as TextView
        appLaunched()
        isThirdPartyIntent = intent.action == Intent.ACTION_VIEW

        /*
        View view=inflater.inflate(R.layout.music_activity_playlists, parent,false)
        Util.setGlobalFont(context, view)
        MyViewHolder holder=new MyViewHolder(view)
        return holder

        */


        //animationView_system.setVisibility(View.INVISIBLE)


       // lottie_music.setVisibility(View.INVISIBLE)


        //lottie_music.visibility(INVISIABLE)

       // lottie_music!!.setVisibility(View.VISIBLE)

        //========================================================================
        lottie_music_lock.setVisibility(View.INVISIBLE)
        lottie_music_unlock.setVisibility(View.INVISIBLE)

        //lottie_music.playAnimation()
        //lottie_music.loop(true)

        /*
        animationView_music = findViewById<View>(R.id.lottie_music) as LottieAnimationView
        animationView_music!!.playAnimation()
        animationView_music.loop(true)
  */
        bus = BusProvider.instance
        bus.register(this)
        initSeekbarChangeListener()

        actionbarSize = getActionBarHeight()
        artView = layoutInflater.inflate(R.layout.music_item_transparent, null) as ViewGroup
        setTopArtHeight()
        songs_fastscroller.measureItemIndex = LIST_HEADERS_COUNT

        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                initializePlayer()
            } else {
                toast(R.string.no_storage_permissions)
            }
        }

        shuffle_btn.setOnClickListener { toggleShuffle() }
        previous_btn.setOnClickListener { sendIntent(PREVIOUS) }
        play_pause_btn.setOnClickListener { sendIntent(PLAYPAUSE) }
        next_btn.setOnClickListener { sendIntent(NEXT) }
        repeat_btn.setOnClickListener { toggleSongRepetition() }
        song_progress_current.setOnClickListener { sendIntent(SKIP_BACKWARD) }
        song_progress_max.setOnClickListener { sendIntent(SKIP_FORWARD) }

        songs_playlist_empty_add_folder.setOnClickListener { addFolderToPlaylist() }
        volumeControlStream = AudioManager.STREAM_MUSIC
        //뮤직 플레이어 실행하고 메시지 뜨는 곳
        //checkWhatsNewDialog()
        storeStateVariables()

        songs_list.recyclerScrollCallback = object : RecyclerScrollCallback {
            override fun onScrolled(scrollY: Int) {
                top_navigation.beVisibleIf(scrollY > topArtHeight && !isSearchOpen)
                val minOverlayTransitionY = actionbarSize - topArtHeight
                art_holder.translationY = Math.min(0, Math.max(minOverlayTransitionY, -scrollY / 2)).toFloat()
                song_list_background.translationY = Math.max(0, -scrollY + topArtHeight).toFloat()
            }
        }

        if (savedInstanceState != null) {
            songs_list.onGlobalLayout {
                songs_list.scrollToPosition(0)
            }
        }

        //Detect용
        //gestureText = findViewById(R.id.MusicActivityGesture) as TextView

//        startNopModel()
//
//        val intent = getIntent()
//
//        if (intent != null) {
//            Log.d(TAG, "44444")
//            bluetoothDevice = intent.extras.getParcelable("bluetoothDevice")
//            if (bluetoothDevice != null) {
//                Log.d(TAG, "55555")
//                deviceName = bluetoothDevice?.getName()
//                Log.d(TAG, "66666")
//                val views = HashMap<String, View>()
//                mMyoCallback = MyoGattCallback(mHandler)
//                mBluetoothGatt = bluetoothDevice?.connectGatt(this, false, mMyoCallback)
//                Log.d(TAG, "77777")
//                mMyoCallback?.setBluetoothGatt(mBluetoothGatt)
//                Log.d(TAG, "bluetoothDevice is $deviceName")
//                val mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//                mBluetoothAdapter = mBluetoothManager.adapter
//            }
//
//        }
//        //dttButton = findViewById(R.id.dttButton_music) as Butto
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        //Post event to notify that user's watching the activity.
        EventBus.getDefault().postSticky(ServiceEvent.currentActivity_Event(CURRENT_ACTIVITY))

        if (storedTextColor != config.textColor) {
            updateAlbumCover()
        }

        if (storedShowAlbumCover != config.showAlbumCover) {
            setTopArtHeight()
            songs_list.adapter?.notifyDataSetChanged()
            if (config.showAlbumCover) {
                updateAlbumCover()
            } else {
                art_image.setImageDrawable(null)
            }
        }

        setupIconColors()
        markCurrentSong()
        updateTextColors(main_holder)
        getSongsAdapter()?.updateColors()
        songs_playlist_empty_add_folder.setTextColor(getAdjustedPrimaryColor())
        songs_playlist_empty_add_folder.paintFlags = songs_playlist_empty_add_folder.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        songs_fastscroller.allowBubbleDisplay = config.showInfoBubble
        songs_fastscroller.updateBubbleColors()
        art_holder.background = ColorDrawable(config.backgroundColor)
        song_list_background.background = ColorDrawable(config.backgroundColor)
        top_navigation.background = ColorDrawable(config.backgroundColor)
    }

    override fun onPause() {
        super.onPause()
        storeStateVariables()
    }

    /*override fun onStop() {

        super.onStop()
        searchMenuItem?.collapseActionView()
    }*/

    override fun onDestroy() {
        super.onDestroy()
        bus.unregister(this)

        if (isThirdPartyIntent && !isChangingConfigurations) {
            sendIntent(FINISH)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        setupSearch(menu)

        val autoplay = menu.findItem(R.id.toggle_autoplay)
        autoplay.title = getString(if (config.autoplay) R.string.disable_autoplay else R.string.enable_autoplay)

        val isSongSelected = MusicService.mCurrSong != null
        menu.apply {
            findItem(R.id.sort).isVisible = !isThirdPartyIntent
            findItem(R.id.toggle_autoplay).isVisible = !isThirdPartyIntent
            findItem(R.id.sort).isVisible = !isThirdPartyIntent
            findItem(R.id.open_playlist).isVisible = !isThirdPartyIntent
            findItem(R.id.remove_current).isVisible = !isThirdPartyIntent && isSongSelected
            findItem(R.id.delete_current).isVisible = !isThirdPartyIntent && isSongSelected
            findItem(R.id.add_folder_to_playlist).isVisible = !isThirdPartyIntent
            findItem(R.id.add_file_to_playlist).isVisible = !isThirdPartyIntent
            findItem(R.id.remove_playlist).isVisible = !isThirdPartyIntent
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort -> showSortingDialog()
            R.id.remove_current -> getSongsAdapter()?.removeCurrentSongFromPlaylist()
            R.id.delete_current -> getSongsAdapter()?.deleteCurrentSong()
            R.id.open_playlist -> openPlaylist()
            R.id.toggle_autoplay -> toggleAutoplay()
            R.id.add_folder_to_playlist -> addFolderToPlaylist()
            R.id.add_file_to_playlist -> addFileToPlaylist()
            R.id.create_playlist_from_folder -> createPlaylistFromFolder()
            R.id.remove_playlist -> removePlaylist()
            R.id.settings -> launchSettings()
            R.id.about -> launchAbout()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun storeStateVariables() {
        config.apply {
            storedTextColor = textColor
            storedShowAlbumCover = showAlbumCover
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_VIEW) {
            setIntent(intent)
            initThirdPartyIntent()
        }
    }

    private fun setTopArtHeight() {
        topArtHeight = if (config.showAlbumCover) resources.getDimensionPixelSize(R.dimen.top_art_height) else 0
        artView!!.setPadding(0, topArtHeight, 0, 0)
    }

    private fun setupSearch(menu: Menu) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.search)
        (searchMenuItem!!.actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isSubmitButtonEnabled = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    if (isSearchOpen) {
                        searchQueryChanged(newText)
                    }
                    return true
                }
            })
        }

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                songs_playlist_empty.text = getString(R.string.no_items_found)
                songs_playlist_empty_add_folder.beGone()
                art_holder.beGone()
                getSongsAdapter()?.searchOpened()
                top_navigation.beGone()
                isSearchOpen = true
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                songs_playlist_empty.text = getString(R.string.playlist_empty)
                songs_playlist_empty_add_folder.beVisibleIf(songs.isEmpty())
                art_holder.beVisibleIf(songs.isNotEmpty())
                if (isSearchOpen) {
                    searchQueryChanged("")
                    getSongsAdapter()?.searchClosed()
                }
                isSearchOpen = false
                return true
            }
        })
    }

    private fun launchSettings() {
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        val faqItems = arrayListOf(
                FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons),
                FAQItem(R.string.faq_4_title_commons, R.string.faq_4_text_commons),
                FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons)
        )

        startAboutActivity(R.string.app_name, LICENSE_OTTO or LICENSE_MULTISELECT or LICENSE_STETHO, BuildConfig.VERSION_NAME, faqItems)
    }

    private fun showSortingDialog() {
        ChangeSortingDialog(this) {
            sendIntent(REFRESH_LIST)
        }
    }

    private fun toggleShuffle() {
        val isShuffleEnabled = !config.isShuffleEnabled
        config.isShuffleEnabled = isShuffleEnabled
        shuffle_btn.applyColorFilter(if (isShuffleEnabled) getAdjustedPrimaryColor() else config.textColor)
        shuffle_btn.alpha = if (isShuffleEnabled) 1f else LOWER_ALPHA
        getSongsAdapter()?.updateShuffle(isShuffleEnabled)
        toast(if (isShuffleEnabled) R.string.shuffle_enabled else R.string.shuffle_disabled)
    }

    private fun toggleSongRepetition() {
        val repeatSong = !config.repeatSong
        config.repeatSong = repeatSong
        repeat_btn.applyColorFilter(if (repeatSong) getAdjustedPrimaryColor() else config.textColor)
        repeat_btn.alpha = if (repeatSong) 1f else LOWER_ALPHA
        getSongsAdapter()?.updateRepeatSong(repeatSong)
        toast(if (repeatSong) R.string.song_repetition_enabled else R.string.song_repetition_disabled)
    }

    private fun toggleAutoplay() {
        config.autoplay = !config.autoplay
        invalidateOptionsMenu()
        toast(if (config.autoplay) R.string.autoplay_enabled else R.string.autoplay_disabled)
    }

    private fun removePlaylist() {
        if (config.currentPlaylist == DBHelper.ALL_SONGS_ID) {
            toast(R.string.all_songs_cannot_be_deleted)
        } else {
            val playlist = dbHelper.getPlaylistWithId(config.currentPlaylist)
            RemovePlaylistDialog(this, playlist) {
                if (it) {
                    val paths = dbHelper.getPlaylistSongPaths(config.currentPlaylist)
                    val files = paths.map { FileDirItem(it, it.getFilenameFromPath()) } as ArrayList<FileDirItem>
                    dbHelper.removeSongsFromPlaylist(paths, -1)
                    deleteFiles(files) { }
                }
                dbHelper.removePlaylist(config.currentPlaylist)
            }
        }
    }

    private fun openPlaylist() {
        dbHelper.getPlaylists {
            val items = arrayListOf<RadioItem>()
            it.mapTo(items) { RadioItem(it.id, it.title) }
            items.add(RadioItem(-1, getString(R.string.create_playlist)))

            RadioGroupDialog(this, items, config.currentPlaylist) {
                if (it == -1) {
                    NewPlaylistDialog(this) {
                        MusicService.mCurrSong = null
                        playlistChanged(it, false)
                        invalidateOptionsMenu()
                    }
                } else {
                    playlistChanged(it as Int)
                    invalidateOptionsMenu()
                }
            }
        }
    }

    private fun addFolderToPlaylist() {
        FilePickerDialog(this, getFilePickerInitialPath(), pickFile = false) {
            toast(R.string.fetching_songs)
            Thread {
                val songs = getFolderSongs(File(it))
                dbHelper.addSongsToPlaylist(songs)
                sendIntent(REFRESH_LIST)
            }.start()
        }
    }

    private fun getFolderSongs(folder: File): ArrayList<String> {
        val songFiles = ArrayList<String>()
        val files = folder.listFiles() ?: return songFiles
        files.forEach {
            if (it.isDirectory) {
                songFiles.addAll(getFolderSongs(it))
            } else if (it.isAudioFast()) {
                songFiles.add(it.absolutePath)
            }
        }
        return songFiles
    }

    private fun addFileToPlaylist() {
        FilePickerDialog(this, getFilePickerInitialPath()) {
            if (it.isAudioFast()) {
                dbHelper.addSongToPlaylist(it)
                sendIntent(REFRESH_LIST)
            } else {
                toast(R.string.invalid_file_format)
            }
        }
    }

    private fun createPlaylistFromFolder() {
        FilePickerDialog(this, getFilePickerInitialPath(), pickFile = false) {
            Thread {
                createPlaylistFrom(it)
            }.start()
        }
    }

    private fun createPlaylistFrom(path: String) {
        val songs = getFolderSongs(File(path))
        if (songs.isEmpty()) {
            toast(R.string.folder_contains_no_audio)
            return
        }

        val folderName = path.getFilenameFromPath()
        var playlistName = folderName
        var curIndex = 1
        val playlistIdWithTitle = dbHelper.getPlaylistIdWithTitle(folderName)
        if (playlistIdWithTitle != -1) {
            while (true) {
                playlistName = "${folderName}_$curIndex"
                if (dbHelper.getPlaylistIdWithTitle(playlistName) == -1) {
                    break
                }

                curIndex++
            }
        }

        val playlist = Playlist(0, playlistName)
        val newPlaylistId = dbHelper.insertPlaylist(playlist)
        dbHelper.addSongsToPlaylist(songs, newPlaylistId)
        playlistChanged(newPlaylistId)
    }

    private fun getFilePickerInitialPath() = if (songs.isEmpty()) Environment.getExternalStorageDirectory().toString() else songs[0].path

    private fun initializePlayer() {
        if (isThirdPartyIntent) {
            initThirdPartyIntent()
        } else {
            sendIntent(INIT)
        }
    }

    private fun initThirdPartyIntent() {
        val realPath = intent.getStringExtra(REAL_FILE_PATH) ?: ""
        var fileUri = intent.data
        if (realPath.isNotEmpty()) {
            fileUri = Uri.fromFile(File(realPath))
        }

        Intent(this, MusicService::class.java).apply {
            data = fileUri
            action = INIT_PATH
            startService(this)
        }
    }



    private fun setupIconColors() {
        val textColor = config.textColor
        previous_btn.applyColorFilter(textColor)
        play_pause_btn.applyColorFilter(textColor)
        next_btn.applyColorFilter(textColor)

        shuffle_btn.applyColorFilter(if (config.isShuffleEnabled) getAdjustedPrimaryColor() else config.textColor)
        shuffle_btn.alpha = if (config.isShuffleEnabled) 1f else LOWER_ALPHA

        repeat_btn.applyColorFilter(if (config.repeatSong) getAdjustedPrimaryColor() else config.textColor)
        repeat_btn.alpha = if (config.repeatSong) 1f else LOWER_ALPHA

        getSongsAdapter()?.updateTextColor(textColor)
        songs_fastscroller.updatePrimaryColor()
    }

    private fun songPicked(pos: Int) {
        setupIconColors()
        Intent(this, MusicService::class.java).apply {
            putExtra(SONG_POS, pos)
            action = PLAYPOS
            startService(this)
        }
    }

    private fun updateSongInfo(song: Song?) {
        song_info_title.text = song?.title ?: ""
        song_info_artist.text = song?.artist ?: ""
        song_progressbar.max = song?.duration ?: 0
        song_progressbar.progress = 0

        getSongsAdapter()?.updateSong(song)
        if (songs.isEmpty() && !isThirdPartyIntent) {
            toast(R.string.empty_playlist)
        }
    }

    private fun fillSongsListView(songs: ArrayList<Song>) {
        this.songs = songs
        val currAdapter = songs_list.adapter
        songs_fastscroller.setViews(songs_list) {
            val item = getSongsAdapter()?.songs?.getOrNull(it)
            songs_fastscroller.updateBubbleText(item?.getBubbleText() ?: "")
        }

        if (currAdapter == null) {
            SongAdapter(this@MainActivity, songs, this, artView!!, songs_list, songs_fastscroller) {
                songPicked(getSongIndex(it as Song))
            }.apply {
                isThirdPartyIntent = this@MainActivity.isThirdPartyIntent
                addVerticalDividers(true)
                songs_list.adapter = this
            }
        } else {
            val state = (songs_list.layoutManager as MyLinearLayoutManager).onSaveInstanceState()
            (currAdapter as SongAdapter).apply {
                isThirdPartyIntent = this@MainActivity.isThirdPartyIntent
                updateSongs(songs)
            }
            (songs_list.layoutManager as MyLinearLayoutManager).onRestoreInstanceState(state)
        }
        markCurrentSong()

        songs_list.beVisibleIf(songs.isNotEmpty())
        art_holder.beVisibleIf(songs_list.isVisible())
        songs_playlist_empty.beVisibleIf(songs.isEmpty())
        songs_playlist_empty_add_folder.beVisibleIf(songs.isEmpty())
    }

    private fun getSongIndex(song: Song) = songs.indexOfFirstOrNull { it == song } ?: 0

    private fun getSongsAdapter() = songs_list.adapter as? SongAdapter

    @Subscribe
    fun songChangedEvent(event: Events.SongChanged) {
        val song = event.song
        updateSongInfo(song)
        markCurrentSong()
        updateAlbumCover()
    }

    @Subscribe
    fun songStateChanged(event: Events.SongStateChanged) {
        val isPlaying = event.isPlaying
        play_pause_btn.setImageDrawable(resources.getDrawable(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play))
        getSongsAdapter()?.updateSongState(isPlaying)
    }


    @Subscribe
    fun playlistUpdated(event: Events.PlaylistUpdated) {
        fillSongsListView(event.songs)
    }

    @Subscribe
    fun progressUpdated(event: Events.ProgressUpdated) {
        val progress = event.progress
        song_progressbar.progress = progress
        getSongsAdapter()?.updateSongProgress(progress)
    }

    @Subscribe
    fun noStoragePermission(event: Events.NoStoragePermission) {
        toast(R.string.no_storage_permissions)
    }

    private fun markCurrentSong() {
        val newSong = MusicService.mCurrSong
        val cnt = songs.size - 1
        val songIndex = (0..cnt).firstOrNull { songs[it] == newSong } ?: -1
        getSongsAdapter()?.updateCurrentSongIndex(songIndex)
    }

    private fun updateAlbumCover() {
        if (!config.showAlbumCover) {
            return
        }

        try {
            val options = RequestOptions().placeholder(oldCover)
            Glide.with(this).clear(art_image)
            Glide.with(this)
                    .load(MusicService.mCurrSongCover)
                    .apply(options)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean) = false

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            oldCover = resource?.constantState?.newDrawable()
                            return false
                        }
                    })
                    .into(art_image)
        } catch (ignored: Exception) {
        }
    }


    // 마요 잠기면 애니메이션 재생
    @org.greenrobot.eventbus.Subscribe
    fun getMyoDevice(event: ServiceEvent.myoLock_Event) {
        myoConnection = event.lock
        if (myoConnection as Boolean) {
            //  animationView_main.cancelAnimation();
            //  animationView_main.clearAnimation();
            //  animationView_main.setAnimation("lock.json");
            lottie_music_lock.playAnimation()
            lottie_music_lock.loop(true)
            lottie_music_lock.setVisibility(View.VISIBLE)
            lottie_music_unlock.setVisibility(View.INVISIBLE)
        } else {
            //  animationView_main.cancelAnimation();
            // animationView_main.clearAnimation();
            //animationView_main_unlock.setAnimation("material_wave_loading.json");
            lottie_music_unlock.playAnimation()
            lottie_music_unlock.loop(true)
            lottie_music_unlock.setVisibility(View.VISIBLE)
            lottie_music_lock.setVisibility(View.INVISIBLE)
        }
    }

    // 마요 연결되어 있으면 애니메이션 재생
    @org.greenrobot.eventbus.Subscribe(sticky = true)
    fun getMyoDevice(event: ServiceEvent.myoConnected_Event) {
        myoConnection = event.connection
        myoApp = application.applicationContext as MyoApp
        if (myoConnection as Boolean) {
            if (first && !myoApp!!.isUnlocked()) {
                lottie_music_lock.playAnimation()
                lottie_music_lock.loop(true)
                lottie_music_lock.setVisibility(View.VISIBLE)
                first = false
            } else if (first && myoApp!!.isUnlocked()) {
                lottie_music_unlock.playAnimation()
                lottie_music_unlock.loop(true)
                lottie_music_unlock.setVisibility(View.VISIBLE)
                first = false
            }
        } else {
            lottie_music_lock.cancelAnimation()
            lottie_music_unlock.cancelAnimation()
            lottie_music_lock.setVisibility(View.INVISIBLE)
            lottie_music_unlock.setVisibility(View.INVISIBLE)
        }
    }

    private fun searchQueryChanged(text: String) {
        val filtered = songs.filter { it.artist.contains(text, true) || it.title.contains(text, true) } as ArrayList
        filtered.sortBy { !(it.artist.startsWith(text, true) || it.title.startsWith(text, true)) }
        songs_playlist_empty.beVisibleIf(filtered.isEmpty())
        getSongsAdapter()?.updateSongs(filtered)
    }

    private fun initSeekbarChangeListener() {
        song_progressbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val duration = song_progressbar.max.getFormattedDuration()
                val formattedProgress = progress.getFormattedDuration()
                song_progress_current.text = formattedProgress
                song_progress_max.text = duration
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Intent(this@MainActivity, MusicService::class.java).apply {
                    putExtra(PROGRESS, seekBar.progress)
                    action = SET_PROGRESS
                    startService(this)
                }
            }
        })
    }

    override fun refreshItems() {
        sendIntent(REFRESH_LIST)
    }

    override fun listToggleShuffle() {
        toggleShuffle()
    }

    override fun listToggleSongRepetition() {
        toggleSongRepetition()
    }
//처음나오는 메시지
//    private fun checkWhatsNewDialog() {
//        arrayListOf<Release>().apply {
//            add(Release(25, R.string.release_25))
//            add(Release(27, R.string.release_27))
//            add(Release(28, R.string.release_28))
//            add(Release(37, R.string.release_37))
//            checkWhatsNew(this, BuildConfig.VERSION_CODE)
//        }
//    }


    override fun onStop() {
        //Post event to notify that user's leaving the activity.
        EventBus.getDefault().postSticky(ServiceEvent.currentActivity_Event(-1))

        EventBus.getDefault().unregister(this)
        super.onStop()
        searchMenuItem?.collapseActionView()
        //this.closeBLEGatt()

    }

    @org.greenrobot.eventbus.Subscribe(threadMode = ThreadMode.MAIN)
    fun onGestureEvent(event: ServiceEvent.GestureEvent) {
        gestureNum = event.gestureNumber

        if (gestureNum != 4) {
            EventBus.getDefault().post(ServiceEvent.GestureEvent_forMusic(gestureNum))

            Log.d(TAG, "Music activity sending gesture : " + gestureNum + " to MusicService")
        } else if(gestureNum == 4){
            smoothcount[gestureNum]++
            if (smoothcount[gestureNum] > 1) {
                //Send Vibration Event
                EventBus.getDefault().post(ServiceEvent.VibrateEvent(VIBRATION_A))
                //Restart lock Timer so user can use gesture continuously
                EventBus.getDefault().post(ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY))
                finish()

                resetSmoothCount()
//                    Toasty.normal(baseContext, "Capture Photo", Toast.LENGTH_SHORT, icon_1).show()
            }
        }
    }

//    @org.greenrobot.eventbus.Subscribe(threadMode = ThreadMode.MAIN)
//    fun onMessageEvent(event: ServiceEvent.GestureEvent) {
//
//        gestureNum = event.gestureNumber
//
//        when (gestureNum) {
//            0 -> {
//
////                if (smoothcount[gestureNum] > 1) {
////                    //intent로 Musicservice에 가서 기능 가져오기
////                    // 멈춤,재생
//////                    Intent(this, MusicService::class.java).apply {
//////
//////                        action = PLAYPAUSE
//////                        startService(this)
//////                    }
////                    sendIntent(PLAYPAUSE)
////
////                    //Send Vibration Event
////                    EventBus.getDefault().post(ServiceEvent.VibrateEvent(VIBRATION_A))
////                    //Restart lock Timer so user can use gesture continuously
////                    EventBus.getDefault().post(ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY))
////
////                    Toasty.normal(this!!, "Play/Pause", Toast.LENGTH_SHORT, icon_1).show()
////                    smoothcount[gestureNum] = -1
////                    resetSmoothCount()
////                }
////
////                smoothcount[gestureNum]++
//            }
//
//            1 -> {
//
////                if (smoothcount[gestureNum] > 1){
////                    //이전
//////                    Intent(this, MusicService::class.java).apply {
//////
//////                        action = PREVIOUS
//////                        startService(this)
//////                    }
////                    sendIntent(PREVIOUS)
////
////                    //Send Vibration Event
////                    EventBus.getDefault().post(ServiceEvent.VibrateEvent(VIBRATION_A))
////                    //Restart lock Timer so user can use gesture continuously
////                    EventBus.getDefault().post(ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY))
////
////                    Toasty.normal(this!!, "Previous", Toast.LENGTH_SHORT, icon_2).show()
////                    smoothcount[gestureNum] = -1
////                    resetSmoothCount()
////                }
////                smoothcount[gestureNum]++
//            }
//
//            2 -> {
////                if (smoothcount[gestureNum] > 1) {
//////                    Intent(this, MusicService::class.java).apply {
//////
//////                        action = NEXT
//////                        startService(this)
//////                    }
////
////                    //앞으로
////                    sendIntent(NEXT)
////
////                    //Send Vibration Event
////                    EventBus.getDefault().post(ServiceEvent.VibrateEvent(VIBRATION_A))
////                    //Restart lock Timer so user can use gesture continuously
////                    EventBus.getDefault().post(ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY))
////
////                    Toasty.normal(this!!, "Next", Toast.LENGTH_SHORT, icon_3).show()
////                    smoothcount[gestureNum] = -1
////                    resetSmoothCount()
////                }
////                smoothcount[gestureNum]++
//            }
//
//            3 -> {
////                if (smoothcount[gestureNum] > 1) {
////
////
////
////                    smoothcount[gestureNum] = -1
////                    resetSmoothCount()
////                }
////                smoothcount[gestureNum]++
//            }
//            4 -> {
//                smoothcount[gestureNum]++
//                if (smoothcount[gestureNum] > 2) {
//                    //Send Vibration Event
//                    EventBus.getDefault().post(ServiceEvent.VibrateEvent(VIBRATION_A))
//                    //Restart lock Timer so user can use gesture continuously
//                    EventBus.getDefault().post(ServiceEvent.restartLockTimerEvent(ADDITIONAL_DELAY))
//                    finish()
//
//                    resetSmoothCount()
////                    Toasty.normal(baseContext, "Capture Photo", Toast.LENGTH_SHORT, icon_1).show()
//                }
//            }
//            else -> {
//            }
//        }
//    }

    fun resetSmoothCount() {

        for (i in smoothcount.indices) {
            smoothcount[i] = 0
        }
    }
}
