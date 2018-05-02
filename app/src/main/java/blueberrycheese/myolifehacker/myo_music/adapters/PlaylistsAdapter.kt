package blueberrycheese.myolifehacker.myo_music.activities.adapters

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.views.MyRecyclerView
import blueberrycheese.myolifehacker.R
import blueberrycheese.myolifehacker.myo_music.activities.activitys.SimpleActivity
import blueberrycheese.myolifehacker.myo_music.activities.dialogs.NewPlaylistDialog
import blueberrycheese.myolifehacker.myo_music.activities.dialogs.RemovePlaylistDialog
import blueberrycheese.myolifehacker.myo_music.activities.extensions.config
import blueberrycheese.myolifehacker.myo_music.activities.extensions.dbHelper
import blueberrycheese.myolifehacker.myo_music.activities.helpers.DBHelper
import blueberrycheese.myolifehacker.myo_music.activities.interfaces.RefreshPlaylistsListener
import blueberrycheese.myolifehacker.myo_music.activities.models.Playlist
import kotlinx.android.synthetic.main.music_item_playlist.view.*
import java.util.*

class PlaylistsAdapter(activity: SimpleActivity, val playlists: ArrayList<Playlist>, val listener: RefreshPlaylistsListener?, recyclerView: MyRecyclerView,
                       itemClick: (Any) -> Unit) : MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab_playlists

    override fun prepareItemSelection(view: View) {}

    override fun markItemSelection(select: Boolean, view: View?) {
        view?.playlist_frame?.isSelected = select
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.music_item_playlist, parent)

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val playlist = playlists[position]
        val view = holder.bindView(playlist) { itemView, layoutPosition ->
            setupView(itemView, playlist)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = playlists.size

    override fun prepareActionMode(menu: Menu) {
        menu.apply {
            findItem(R.id.cab_rename).isVisible = selectedPositions.size == 1
        }
    }

    override fun actionItemPressed(id: Int) {
        when (id) {
            R.id.cab_delete -> askConfirmDelete()
            R.id.cab_rename -> showRenameDialog()
        }
    }

    override fun getSelectableItemCount() = playlists.size

    private fun askConfirmDelete() {
        RemovePlaylistDialog(activity) {
            val ids = selectedPositions.map { playlists[it].id } as ArrayList<Int>
            if (it) {
                deletePlaylistSongs(ids) {
                    removePlaylists(ids)
                }
            } else {
                removePlaylists(ids)
            }
            finishActMode()
        }
    }

    private fun deletePlaylistSongs(ids: ArrayList<Int>, callback: () -> Unit) {
        var cnt = ids.size
        ids.map { activity.dbHelper.getPlaylistSongPaths(it).map { FileDirItem(it, it.getFilenameFromPath()) } as ArrayList<FileDirItem> }
                .forEach {
                    activity.deleteFiles(it) {
                        if (--cnt <= 0) {
                            callback()
                        }
                    }
                }
    }

    private fun removePlaylists(ids: ArrayList<Int>) {
        val isDeletingCurrentPlaylist = ids.contains(activity.config.currentPlaylist)
        val playlistsToDelete = ArrayList<Playlist>(selectedPositions.size)

        for (pos in selectedPositions) {
            if (playlists[pos].id == DBHelper.ALL_SONGS_ID) {
                activity.toast(R.string.all_songs_cannot_be_deleted)
                selectedPositions.remove(pos)
                toggleItemSelection(false, pos)
                break
            }
        }

        selectedPositions.sortedDescending().forEach {
            val playlist = playlists[it]
            playlistsToDelete.add(playlist)
        }
        playlists.removeAll(playlistsToDelete)
        activity.dbHelper.removePlaylists(ids)

        if (isDeletingCurrentPlaylist) {
            reloadList()
        } else {
            removeSelectedItems()
        }
    }

    private fun showRenameDialog() {
        NewPlaylistDialog(activity, playlists[selectedPositions.first()]) {
            reloadList()
        }
    }

    private fun reloadList() {
        finishActMode()
        listener?.refreshItems()
    }

    private fun setupView(view: View, playlist: Playlist) {
        view.apply {
            playlist_title.text = playlist.title
            playlist_title.setTextColor(textColor)
            playlist_icon.applyColorFilter(textColor)
            playlist_icon.beInvisibleIf(playlist.id != context.config.currentPlaylist)
        }
    }
}
