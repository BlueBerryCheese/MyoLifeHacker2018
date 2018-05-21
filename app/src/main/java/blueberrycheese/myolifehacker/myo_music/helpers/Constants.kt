package blueberrycheese.myolifehacker.myo_music.activities.helpers

const val SONG_POS = "song_position"
const val PROGRESS = "progress"
const val EDITED_SONG = "edited_song"

private const val PATH = "com.simplemobiletools.musicplayer.action."

const val INIT = PATH + "INIT"
const val INIT_PATH = PATH + "INIT_PATH"
const val SETUP = PATH + "SETUP"
const val FINISH = PATH + "FINISH"
const val PREVIOUS = PATH + "PREVIOUS"
const val PAUSE = PATH + "PAUSE"
const val PLAYPAUSE = PATH + "PLAYPAUSE"
const val NEXT = PATH + "NEXT"
const val EDIT = PATH + "EDIT"
const val PLAYPOS = PATH + "PLAYPOS"
const val REFRESH_LIST = PATH + "REFRESH_LIST"
const val SET_PROGRESS = PATH + "SET_PROGRESS"
const val SET_EQUALIZER = PATH + "SET_EQUALIZER"
const val SKIP_BACKWARD = PATH + "SKIP_BACKWARD"
const val SKIP_FORWARD = PATH + "SKIP_FORWARD"

// shared preferences
const val SHUFFLE = "shuffle"
const val EQUALIZER = "equalizer"
const val REPEAT_SONG = "repeat_song"
const val AUTOPLAY = "autoplay"
const val IGNORED_PATHS = "ignored_paths"
const val CURRENT_PLAYLIST = "current_playlist"
const val SHOW_FILENAME = "show_filename"
const val SHOW_ALBUM_COVER = "show_album_cover"

const val LIST_HEADERS_COUNT = 2
const val LOWER_ALPHA = 0.5f

const val SHOW_FILENAME_NEVER = 1
const val SHOW_FILENAME_IF_UNAVAILABLE = 2
const val SHOW_FILENAME_ALWAYS = 3
