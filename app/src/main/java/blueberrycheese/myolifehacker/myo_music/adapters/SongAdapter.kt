package blueberrycheese.myolifehacker.myo_music.activities.adapters

import android.content.Intent
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.bignerdranch.android.multiselector.MultiSelector
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.PropertiesDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.views.FastScroller
import com.simplemobiletools.commons.views.MyRecyclerView
import blueberrycheese.myolifehacker.BuildConfig
import blueberrycheese.myolifehacker.R
import blueberrycheese.myolifehacker.myo_music.activities.activitys.SimpleActivity
import blueberrycheese.myolifehacker.myo_music.activities.dialogs.EditDialog
import blueberrycheese.myolifehacker.myo_music.activities.extensions.config
import blueberrycheese.myolifehacker.myo_music.activities.extensions.dbHelper
import blueberrycheese.myolifehacker.myo_music.activities.extensions.sendIntent
import blueberrycheese.myolifehacker.myo_music.activities.helpers.*
import blueberrycheese.myolifehacker.myo_music.activities.interfaces.SongListListener
import blueberrycheese.myolifehacker.myo_music.activities.models.Song
import blueberrycheese.myolifehacker.myo_music.activities.services.MusicService
import kotlinx.android.synthetic.main.music_item_navigation.view.*
import kotlinx.android.synthetic.main.music_item_song.view.*

class SongAdapter(activity: SimpleActivity, var songs: ArrayList<Song>, val listener: SongListListener, val transparentView: View,
                  recyclerView: MyRecyclerView, fastScroller: FastScroller, itemClick: (Any) -> Unit) : MyRecyclerViewAdapter(activity, recyclerView, fastScroller, itemClick) {

    private val VIEW_TYPE_TRANSPARENT = 0
    private val VIEW_TYPE_NAVIGATION = 1
    private val VIEW_TYPE_ITEM = 2

    private var currentSongIndex = 0
    private var songsHashCode = songs.hashCode()
    private var currentSong: Song? = null
    private var initialProgress = 0
    private var initialIsPlaying = false

    private var transparentViewHolder: TransparentViewHolder? = null
    private var transparentViewHeight = 0

    private var navigationView: ViewGroup? = null
    private var navigationViewHolder: NavigationViewHolder? = null
    private var navigationViewHeight = 0

    var isThirdPartyIntent = false

    init {
        setupDragListener(true)
        positionOffset = LIST_HEADERS_COUNT
    }

    override fun getActionMenuId() = R.menu.cab

    override fun prepareItemSelection(view: View) {}

    override fun markItemSelection(select: Boolean, view: View?) {
        view?.song_frame?.isSelected = select
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TRANSPARENT -> getTransparentViewHolder()
            VIEW_TYPE_NAVIGATION -> getNavigationViewHolder()
            else -> createViewHolder(R.layout.music_item_song, parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_TRANSPARENT
            1 -> VIEW_TYPE_NAVIGATION
            else -> VIEW_TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder !is TransparentViewHolder && holder !is NavigationViewHolder) {
            val song = songs[position - LIST_HEADERS_COUNT]
            val view = holder.bindView(song, !isThirdPartyIntent) { itemView, layoutPosition ->
                setupView(itemView, song, layoutPosition)
            }
            bindViewHolder(holder, position - LIST_HEADERS_COUNT, view)
        } else {
            holder.itemView.tag = holder
        }
    }

    override fun getItemCount() = songs.size + LIST_HEADERS_COUNT

    override fun prepareActionMode(menu: Menu) {
        menu.apply {
            findItem(R.id.cab_rename).isVisible = selectedPositions.size == 1
        }
    }

    override fun actionItemPressed(id: Int) {
        when (id) {
            R.id.cab_properties -> showProperties()
            R.id.cab_rename -> displayEditDialog()
            R.id.cab_share -> shareItems()
            R.id.cab_select_all -> selectAll()
            R.id.cab_remove_from_playlist -> removeFromPlaylist()
            R.id.cab_delete -> askConfirmDelete()
        }
    }

    override fun getSelectableItemCount() = songs.size

    fun searchOpened() {
        transparentViewHeight = transparentView.height
        transparentView.layoutParams?.height = 0

        navigationViewHeight = navigationView?.height ?: 0
        navigationView?.layoutParams?.height = 0
    }

    fun searchClosed() {
        transparentView.layoutParams?.height = transparentViewHeight
        navigationView?.layoutParams?.height = navigationViewHeight
        notifyDataSetChanged()
    }

    private fun getTransparentViewHolder(): TransparentViewHolder {
        if (transparentViewHolder == null) {
            transparentViewHolder = TransparentViewHolder(transparentView)
        }

        return transparentViewHolder!!
    }

    private fun getNavigationViewHolder(): NavigationViewHolder {
        if (navigationView == null) {
            navigationView = activity.layoutInflater.inflate(R.layout.music_item_navigation, null) as ViewGroup
            initNavigationView()
        }

        if (navigationViewHolder == null) {
            navigationViewHolder = NavigationViewHolder(navigationView!!)
        }

        return navigationViewHolder!!
    }

    private fun showProperties() {
        if (selectedPositions.size <= 1) {
            PropertiesDialog(activity, songs[selectedPositions.first()].path)
        } else {
            val paths = ArrayList<String>()
            selectedPositions.forEach { paths.add(songs[it].path) }
            PropertiesDialog(activity, paths)
        }
    }

    private fun displayEditDialog() {
        if (selectedPositions.size == 1) {
            EditDialog(activity, songs[selectedPositions.first()]) {
                if (it == MusicService.mCurrSong) {
                    Intent(activity, MusicService::class.java).apply {
                        putExtra(EDITED_SONG, it)
                        action = EDIT
                        activity.startService(this)
                    }
                }

                activity.sendIntent(REFRESH_LIST)
                activity.runOnUiThread { finishActMode() }
            }
        }
    }

    private fun shareItems() {
        val paths = ArrayList<String>()
        selectedPositions.forEach {
            paths.add(songs[it].path)
        }
        activity.sharePathsIntent(paths, BuildConfig.APPLICATION_ID)
    }

    private fun askConfirmDelete() {
        ConfirmationDialog(activity) {
            deleteSongs()
            finishActMode()
        }
    }

    private fun deleteSongs() {
        if (selectedPositions.isEmpty()) {
            return
        }

        val paths = ArrayList<String>(selectedPositions.size)
        val fileDirItems = ArrayList<FileDirItem>(selectedPositions.size)
        val removeSongs = ArrayList<Song>(selectedPositions.size)

        val SAFPath = songs[selectedPositions.first()].path
        activity.handleSAFDialog(SAFPath) {
            selectedPositions.sortedDescending().forEach {
                val song = songs[it]
                paths.add(song.path)
                fileDirItems.add(FileDirItem(song.path, song.path.getFilenameFromPath()))
                removeSongs.add(song)
                if (song == MusicService.mCurrSong) {
                    activity.sendIntent(NEXT)
                }
            }

            songs.removeAll(removeSongs)
            activity.dbHelper.removeSongsFromPlaylist(paths, -1)
            activity.deleteFiles(fileDirItems) { }
            removeSelectedItems()

            if (songs.isEmpty()) {
                listener.refreshItems()
            }
        }
    }

    private fun removeFromPlaylist() {
        if (selectedPositions.isEmpty()) {
            return
        }

        val paths = ArrayList<String>(selectedPositions.size)
        val removeSongs = ArrayList<Song>(selectedPositions.size)

        selectedPositions.sortedDescending().forEach {
            val song = songs[it]
            paths.add(song.path)
            removeSongs.add(song)
            if (song == MusicService.mCurrSong) {
                activity.sendIntent(NEXT)
            }
        }

        activity.config.addIgnoredPaths(paths)
        songs.removeAll(removeSongs)
        activity.dbHelper.removeSongsFromPlaylist(paths)
        removeSelectedItems()

        if (songs.isEmpty()) {
            listener.refreshItems()
        }
    }

    fun updateSongs(newSongs: ArrayList<Song>) {
        val newHashCode = newSongs.hashCode()
        if (newHashCode != songsHashCode) {
            songsHashCode = newHashCode
            songs = newSongs
            currentSongIndex = -1
            notifyDataSetChanged()
        }
    }

    fun updateCurrentSongIndex(index: Int) {
        val correctIndex = index + LIST_HEADERS_COUNT
        val prevIndex = currentSongIndex
        currentSongIndex = -1
        notifyItemChanged(prevIndex)

        currentSongIndex = correctIndex
        if (index >= 0) {
            notifyItemChanged(correctIndex)
        }
    }

    fun updateSong(song: Song?) {
        currentSong = song
        navigationView?.apply {
            song_info_title.text = song?.title ?: ""
            song_info_artist.text = song?.artist ?: ""
            song_progressbar.max = song?.duration ?: 0
            song_progressbar.progress = 0
        }
    }

    fun removeCurrentSongFromPlaylist() {
        selectedPositions.clear()
        selectedPositions.add(currentSongIndex - positionOffset)
        removeFromPlaylist()
        selectedPositions.clear()
    }

    fun deleteCurrentSong() {
        ConfirmationDialog(activity) {
            selectedPositions.clear()
            if (songs.isNotEmpty()) {
                selectedPositions.add(currentSongIndex - positionOffset)
                deleteSongs()
                selectedPositions.clear()
            }
        }
    }

    private fun initNavigationView() {
        navigationView?.apply {
            shuffle_btn.setOnClickListener { listener.listToggleShuffle() }
            previous_btn.setOnClickListener { activity.sendIntent(PREVIOUS) }
            play_pause_btn.setOnClickListener { activity.sendIntent(PLAYPAUSE) }
            next_btn.setOnClickListener { activity.sendIntent(NEXT) }
            repeat_btn.setOnClickListener { listener.listToggleSongRepetition() }
            song_progress_current.setOnClickListener { activity.sendIntent(SKIP_BACKWARD) }
            song_progress_max.setOnClickListener { activity.sendIntent(SKIP_FORWARD) }

            updateColors()

            song_progressbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val duration = song_progressbar.max.getFormattedDuration()
                    val formattedProgress = progress.getFormattedDuration()
                    song_progress_current.text = formattedProgress
                    song_progress_max.text = duration
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    Intent(activity, MusicService::class.java).apply {
                        putExtra(PROGRESS, seekBar.progress)
                        action = SET_PROGRESS
                        activity.startService(this)
                    }
                }
            })

            updateSong(currentSong)
            updateSongProgress(initialProgress)
            updateSongState(initialIsPlaying)
        }
    }

    fun updateColors() {
        val config = activity.config
        textColor = config.textColor
        primaryColor = config.primaryColor
        backgroundColor = config.backgroundColor
        navigationView?.apply {
            previous_btn.applyColorFilter(textColor)
            play_pause_btn.applyColorFilter(textColor)
            next_btn.applyColorFilter(textColor)
            repeat_btn.applyColorFilter(textColor)

            shuffle_btn.applyColorFilter(if (config.isShuffleEnabled) activity.getAdjustedPrimaryColor() else textColor)
            shuffle_btn.alpha = if (config.isShuffleEnabled) 1f else LOWER_ALPHA

            repeat_btn.applyColorFilter(if (config.repeatSong) activity.getAdjustedPrimaryColor() else textColor)
            repeat_btn.alpha = if (config.repeatSong) 1f else LOWER_ALPHA

            song_info_title.setTextColor(textColor)
            song_info_artist.setTextColor(textColor)
            song_progress_current.setTextColor(textColor)
            song_progress_max.setTextColor(textColor)
            song_progressbar.setColors(textColor, activity.getAdjustedPrimaryColor(), backgroundColor)
        }
    }

    fun updateSongState(isPlaying: Boolean) {
        if (navigationView == null) {
            initialIsPlaying = isPlaying
        }
        navigationView?.play_pause_btn?.setImageDrawable(resources.getDrawable(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play))
    }

    fun updateSongProgress(progress: Int) {
        if (navigationView == null) {
            initialProgress = progress
        }
        navigationView?.song_progressbar?.progress = progress
    }

    fun updateShuffle(enable: Boolean) {
        navigationView?.apply {
            shuffle_btn.applyColorFilter(if (enable) activity.getAdjustedPrimaryColor() else textColor)
            shuffle_btn.alpha = if (enable) 1f else LOWER_ALPHA
        }
    }

    fun updateRepeatSong(repeat: Boolean) {
        navigationView?.apply {
            repeat_btn.applyColorFilter(if (repeat) activity.getAdjustedPrimaryColor() else textColor)
            repeat_btn.alpha = if (repeat) 1f else LOWER_ALPHA
        }
    }

    class TransparentViewHolder(view: View) : ViewHolder(view, multiSelector = MultiSelector())

    class NavigationViewHolder(view: ViewGroup) : ViewHolder(view, multiSelector = MultiSelector())

    private fun setupView(view: View, song: Song, layoutPosition: Int) {
        view.apply {
            song_title.text = song.title
            song_title.setTextColor(textColor)

            song_artist.text = song.artist
            song_artist.setTextColor(textColor)

            song_note_image.beInvisibleIf(currentSongIndex != layoutPosition)
            if (currentSongIndex == layoutPosition) {
                song_note_image.applyColorFilter(textColor)
            }
        }
    }
}
