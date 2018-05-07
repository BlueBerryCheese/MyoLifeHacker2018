package blueberrycheese.myolifehacker.myo_music.activities.extensions

import android.content.Context
import android.content.Intent
import android.util.TypedValue

import blueberrycheese.myolifehacker.myo_music.activities.services.MusicService
import blueberrycheese.myolifehacker.R
import blueberrycheese.myolifehacker.myo_music.activities.helpers.*


fun Context.sendIntent(action: String) {
    Intent(this, MusicService::class.java).apply {
        this.action = action
        try {
            startService(this)
        } catch (ignored: Exception) {
        }
    }
}

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.dbHelper: DBHelper get() = DBHelper.newInstance(applicationContext)

fun Context.playlistChanged(newID: Int, callSetup: Boolean = true) {
    config.currentPlaylist = newID
    sendIntent(PAUSE)
    sendIntent(REFRESH_LIST)
    if (callSetup) {
        sendIntent(SETUP)
    }
}

fun Context.getActionBarHeight(): Int {
    val textSizeAttr = intArrayOf(R.attr.actionBarSize)
    val attrs = obtainStyledAttributes(TypedValue().data, textSizeAttr)
    val actionBarSize = attrs.getDimensionPixelSize(0, -1)
    attrs.recycle()
    return actionBarSize
}
