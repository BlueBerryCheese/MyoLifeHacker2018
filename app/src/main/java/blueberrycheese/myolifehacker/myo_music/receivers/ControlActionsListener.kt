package blueberrycheese.myolifehacker.myo_music.activities.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import blueberrycheese.myolifehacker.myo_music.activities.extensions.sendIntent
import blueberrycheese.myolifehacker.myo_music.activities.helpers.FINISH
import blueberrycheese.myolifehacker.myo_music.activities.helpers.NEXT
import blueberrycheese.myolifehacker.myo_music.activities.helpers.PLAYPAUSE
import blueberrycheese.myolifehacker.myo_music.activities.helpers.PREVIOUS

class ControlActionsListener : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        when (action) {
            PREVIOUS, PLAYPAUSE, NEXT, FINISH -> context.sendIntent(action)
        }
    }
}
