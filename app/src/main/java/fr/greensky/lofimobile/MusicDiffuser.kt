package fr.greensky.lofimobile

import android.media.MediaPlayer
import com.google.firebase.storage.FirebaseStorage
import fr.greensky.lofimobile.Database.Singleton.stations
import fr.greensky.lofimobile.MusicDiffuser.Singleton.currentSong
import fr.greensky.lofimobile.MusicDiffuser.Singleton.diffuser
import fr.greensky.lofimobile.MusicDiffuser.Singleton.tracks
import fr.greensky.lofimobile.fragments.HomeFragment
import fr.greensky.lofimobile.fragments.HomeFragment.Singleton.stationList

class MusicDiffuser(private val context: MainActivity) {
    object Singleton {
        var diffuser: MediaPlayer? = null
        var tracks: MutableList<String> = mutableListOf()
        var currentSong: String? = null
    }

    fun init() {
    }
    fun start(id: String, callback: (() -> Unit)? = null, callbackOnEnd: (() -> Unit)? = null) {
        diffuser = MediaPlayer()

        FirebaseStorage.getInstance().getReference("${id}.mp3").downloadUrl.addOnSuccessListener {
            diffuser?.setDataSource(it.toString())
            diffuser?.prepareAsync()

            diffuser?.setOnPreparedListener { player ->
                player.start()
                currentSong = id
                if (callback != null) {
                    callback()
                }
                diffuser?.setOnCompletionListener {
                    if (callbackOnEnd != null) {
                        callbackOnEnd()
                    }
                    skip()
                }
            }
        }
    }
    fun clearTracks() {
        tracks.clear()
    }
    fun skip() {
        val next = if (tracks.size == 0) {
            stations.random().id
        } else {
            stations.find { it.id == tracks[0] }!!.id
        }

        if (tracks.size > 0) {
            tracks.removeFirst()
        }
        start(next, {
            context.loadFragment(HomeFragment(context, stationList))
        })
    }
    fun pause(callback: (() -> Unit)? = null) {
        if (diffuser != null && diffuser?.isPlaying == true) {
            diffuser?.pause()
            if (callback != null) {
                callback()
            }
        }
    }
    fun resume(callback: (() -> Unit)? = null) {
        if (diffuser != null && diffuser?.isPlaying == false) {
            diffuser?.start()
            if (callback != null) {
                callback()
            }
        }
    }
    fun addTrack(id: String) {
        tracks.add(id)
    }
    fun removeTrack(id: String) {
        tracks = tracks.filter{ it != id } as MutableList<String>
    }
    fun playingId(): String? {
        return currentSong
    }
}