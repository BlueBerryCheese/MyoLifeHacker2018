package blueberrycheese.myolifehacker.myo_music.activities.models

import android.provider.MediaStore
import com.simplemobiletools.commons.extensions.getFormattedDuration
import com.simplemobiletools.commons.helpers.SORT_BY_ARTIST
import com.simplemobiletools.commons.helpers.SORT_BY_PATH
import com.simplemobiletools.commons.helpers.SORT_BY_TITLE
import com.simplemobiletools.commons.helpers.SORT_DESCENDING
import java.io.Serializable

data class Song(val id: Long, var title: String, var artist: String, var path: String, val duration: Int, val album: String) : Serializable, Comparable<Song> {
    companion object {
        private const val serialVersionUID = 6717978793256842245L
        var sorting = 0
    }

    override fun compareTo(other: Song): Int {
        var res = when {
            sorting and SORT_BY_TITLE != 0 -> {
                if (title == MediaStore.UNKNOWN_STRING && other.title != MediaStore.UNKNOWN_STRING) {
                    1
                } else if (title != MediaStore.UNKNOWN_STRING && other.title == MediaStore.UNKNOWN_STRING) {
                    -1
                } else {
                    title.toLowerCase().compareTo(other.title.toLowerCase())
                }
            }
            sorting and SORT_BY_ARTIST != 0 -> {
                if (artist == MediaStore.UNKNOWN_STRING && other.artist != MediaStore.UNKNOWN_STRING) {
                    1
                } else if (artist != MediaStore.UNKNOWN_STRING && other.artist == MediaStore.UNKNOWN_STRING) {
                    -1
                } else {
                    artist.toLowerCase().compareTo(other.artist.toLowerCase())
                }
            }
            sorting and SORT_BY_PATH != 0 -> path.toLowerCase().compareTo(other.path.toLowerCase())
            else -> when {
                duration == other.duration -> 0
                duration > other.duration -> 1
                else -> -1
            }
        }

        if (sorting and SORT_DESCENDING != 0) {
            res *= -1
        }

        return res
    }

    fun getBubbleText() = when {
        sorting and SORT_BY_TITLE != 0 -> title
        sorting and SORT_BY_ARTIST != 0 -> artist
        sorting and SORT_BY_PATH != 0 -> path
        else -> duration.getFormattedDuration()
    }
}
