package blueberrycheese.myolifehacker.myo_music.activities.activitys

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import blueberrycheese.myolifehacker.R
import blueberrycheese.myolifehacker.myo_music.activities.adapters.PlaylistsAdapter
import blueberrycheese.myolifehacker.myo_music.activities.dialogs.NewPlaylistDialog
import blueberrycheese.myolifehacker.myo_music.activities.extensions.dbHelper
import blueberrycheese.myolifehacker.myo_music.activities.extensions.playlistChanged
import blueberrycheese.myolifehacker.myo_music.activities.interfaces.RefreshPlaylistsListener
import blueberrycheese.myolifehacker.myo_music.activities.models.Playlist
import kotlinx.android.synthetic.main.music_activity_playlists.*

class PlaylistsActivity : SimpleActivity(), RefreshPlaylistsListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_activity_playlists)
        getPlaylists()
    }

    private fun getPlaylists() {
        dbHelper.getPlaylists {
            runOnUiThread {
                PlaylistsAdapter(this@PlaylistsActivity,it , this@PlaylistsActivity, playlists_list) {
                    getPlaylists()
                    playlistChanged((it as Playlist).id)
                }.apply {
                    playlists_list.adapter = this
                }
            }
        }
    }

    override fun refreshItems() {
        getPlaylists()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_playlists, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.create_playlist -> showCreatePlaylistFolder()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showCreatePlaylistFolder() {
        NewPlaylistDialog(this) {
            getPlaylists()
        }
    }
}
