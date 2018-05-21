package blueberrycheese.myolifehacker.myo_music.activities.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import blueberrycheese.myolifehacker.R
import com.simplemobiletools.commons.extensions.setupDialogStuff
import blueberrycheese.myolifehacker.myo_music.activities.models.Playlist
import kotlinx.android.synthetic.main.music_dialog_remove_playlist.view.*

class RemovePlaylistDialog(val activity: Activity, val playlist: Playlist? = null, val callback: (deleteFiles: Boolean) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.music_dialog_remove_playlist, null).apply {
            remove_playlist_description.text = getDescriptionText()
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialog, which -> callback(view.remove_playlist_checkbox.isChecked) })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            activity.setupDialogStuff(view, this, R.string.remove_playlist)
        }
    }

    private fun getDescriptionText(): String {
        return if (playlist == null) {
            activity.getString(R.string.remove_playlist_description)
        } else
            String.format(activity.resources.getString(R.string.remove_playlist_description_placeholder), playlist.title)
    }
}
