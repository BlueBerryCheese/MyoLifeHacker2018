package blueberrycheese.myolifehacker.myo_music.activities.activitys

import android.content.Intent
import android.os.Bundle
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.models.RadioItem
import blueberrycheese.myolifehacker.R
import blueberrycheese.myolifehacker.myo_music.activities.extensions.config
import blueberrycheese.myolifehacker.myo_music.activities.extensions.sendIntent
import blueberrycheese.myolifehacker.myo_music.activities.helpers.REFRESH_LIST
import blueberrycheese.myolifehacker.myo_music.activities.helpers.SHOW_FILENAME_ALWAYS
import blueberrycheese.myolifehacker.myo_music.activities.helpers.SHOW_FILENAME_IF_UNAVAILABLE
import blueberrycheese.myolifehacker.myo_music.activities.helpers.SHOW_FILENAME_NEVER
import blueberrycheese.myolifehacker.myo_music.activities.services.MusicService
import kotlinx.android.synthetic.main.music_activity_settings.*
import java.util.*

class SettingsActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_activity_settings)
    }

    override fun onResume() {
        super.onResume()

        setupCustomizeColors()
        setupManagePlaylists()
        setupUseEnglish()
        setupAvoidWhatsNew()
        setupShowInfoBubble()
        setupShowAlbumCover()
        setupEqualizer()
        setupReplaceTitle()
        updateTextColors(settings_scrollview)
    }

    private fun setupCustomizeColors() {
        settings_customize_colors_holder.setOnClickListener {
            startCustomizationActivity()
        }
    }

    private fun setupUseEnglish() {
        settings_use_english_holder.beVisibleIf(config.wasUseEnglishToggled || Locale.getDefault().language != "en")
        settings_use_english.isChecked = config.useEnglish
        settings_use_english_holder.setOnClickListener {
            settings_use_english.toggle()
            config.useEnglish = settings_use_english.isChecked
            System.exit(0)
        }
    }

    private fun setupAvoidWhatsNew() {
        settings_avoid_whats_new.isChecked = config.avoidWhatsNew
        settings_avoid_whats_new_holder.setOnClickListener {
            settings_avoid_whats_new.toggle()
            config.avoidWhatsNew = settings_avoid_whats_new.isChecked
        }
    }

    private fun setupManagePlaylists() {
        settings_manage_playlists_holder.setOnClickListener {
            startActivity(Intent(this, PlaylistsActivity::class.java))
        }
    }

    private fun setupShowInfoBubble() {
        settings_show_info_bubble.isChecked = config.showInfoBubble
        settings_show_info_bubble_holder.setOnClickListener {
            settings_show_info_bubble.toggle()
            config.showInfoBubble = settings_show_info_bubble.isChecked
        }
    }

    private fun setupShowAlbumCover() {
        settings_show_album_cover.isChecked = config.showAlbumCover
        settings_show_album_cover_holder.setOnClickListener {
            settings_show_album_cover.toggle()
            config.showAlbumCover = settings_show_album_cover.isChecked
        }
    }

    private fun setupEqualizer() {
        val equalizer = MusicService.mEqualizer ?: return
        val items = arrayListOf<RadioItem>()
        (0 until equalizer.numberOfPresets).mapTo(items) { RadioItem(it, equalizer.getPresetName(it.toShort())) }

        settings_equalizer.text = items[config.equalizer].title
        settings_equalizer_holder.setOnClickListener {
            RadioGroupDialog(this@SettingsActivity, items, config.equalizer) {
                config.equalizer = it as Int
                settings_equalizer.text = items[it].title
            }
        }
    }

    private fun setupReplaceTitle() {
        settings_show_filename.text = getShowFilenameText()
        settings_show_filename_holder.setOnClickListener {
            val items = arrayListOf(
                    RadioItem(SHOW_FILENAME_NEVER, getString(R.string.never)),
                    RadioItem(SHOW_FILENAME_IF_UNAVAILABLE, getString(R.string.title_is_not_available)),
                    RadioItem(SHOW_FILENAME_ALWAYS, getString(R.string.always)))

            RadioGroupDialog(this@SettingsActivity, items, config.showFilename) {
                config.showFilename = it as Int
                settings_show_filename.text = getShowFilenameText()
                sendIntent(REFRESH_LIST)
            }
        }
    }

    private fun getShowFilenameText() = getString(when (config.showFilename) {
        SHOW_FILENAME_NEVER -> R.string.never
        SHOW_FILENAME_IF_UNAVAILABLE -> R.string.title_is_not_available
        else -> R.string.always
    })
}
