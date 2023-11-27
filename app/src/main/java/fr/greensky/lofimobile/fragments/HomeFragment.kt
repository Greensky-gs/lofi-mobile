package fr.greensky.lofimobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.greensky.lofimobile.Database
import fr.greensky.lofimobile.Database.Singleton.stations
import fr.greensky.lofimobile.MainActivity
import fr.greensky.lofimobile.MusicDiffuser
import fr.greensky.lofimobile.MusicDiffuser.Singleton.currentSong
import fr.greensky.lofimobile.MusicDiffuser.Singleton.diffuser
import fr.greensky.lofimobile.R
import fr.greensky.lofimobile.StationPopup
import fr.greensky.lofimobile.adapter.AddToPlaylist
import fr.greensky.lofimobile.models.StationModel
import fr.greensky.lofimobile.adapter.ItemDecoration
import fr.greensky.lofimobile.adapter.RecyclerAdapter
import fr.greensky.lofimobile.fragments.HomeFragment.Singleton.stationList
import java.util.Random

class HomeFragment(private val context: MainActivity, private val songList: MutableList<StationModel>? = mutableListOf()): Fragment() {
    object Singleton {
        var stationList = mutableListOf<StationModel>()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val db = Database(context)
        val isPlaying = diffuser !== null && currentSong !== null
        val view = inflater?.inflate(if (isPlaying) {
            if (db.getTheme() === "dark") {
                R.layout.home_fragment_playing_dark
            } else {
                R.layout.home_fragment_playing
            }
        } else {
            if (db.getTheme() === "dark") {
                R.layout.home_fragment_dark
            } else {
                R.layout.home_fragment
            }
        }, container, false)

        if (isPlaying) {
            val song = stations.find { it.id == currentSong }!!
            val title = view?.findViewById<TextView>(R.id.playing_song_title)
            val img = view?.findViewById<ImageView>(R.id.playing_img)
            val playIcon = view?.findViewById<ImageView>(R.id.playing_play_icon)
            val skipIcon = view?.findViewById<ImageView>(R.id.playing_remove_icon)
            val addToPlaylistIcon = view?.findViewById<ImageView>(R.id.playing_playlist_icon)
            val card = view?.findViewById<CardView>(R.id.playing_station_info)

            Glide.with(context).load(song.img).into(img!!)
            title?.text = song.title

            card?.setOnClickListener {
                StationPopup(context, song).show()
            }

            if (diffuser?.isPlaying == true) {
                playIcon?.setImageResource(R.drawable.ic_pause)
            } else {
                playIcon?.setImageResource(R.drawable.ic_play)
            }
            fun update() {
                if (currentSong != null && song.id == currentSong && diffuser?.isPlaying == true) {
                    playIcon?.setImageResource(R.drawable.ic_pause)
                } else {
                    playIcon?.setImageResource(R.drawable.ic_play)
                }
            }
            fun setAdd() {
                val db = Database(context)
                if (!db.hasAvailablePlaylists(song.id)) {
                    addToPlaylistIcon?.visibility = View.GONE
                } else {
                    addToPlaylistIcon?.setOnClickListener {
                        Database.Singleton.currentToAdd = song
                        context.loadFragment(AddToPlaylist(context))
                    }
                }
            }
            setAdd()
            update()

            playIcon?.setOnClickListener {
                update()
                if (currentSong == song.id && diffuser?.isPlaying() == true) {
                    MusicDiffuser(context).pause() {
                        update()
                        context.loadFragment(HomeFragment(context, stationList))
                    }
                } else if (currentSong == song.id && diffuser?.isPlaying() == false) {
                    MusicDiffuser(context).resume() {
                        update()
                        context.loadFragment(HomeFragment(context, stationList))
                    }
                } else {
                    MusicDiffuser(context).start(song.id, {
                        update()
                        context.loadFragment(HomeFragment(context, stationList))
                    }, {
                        update()
                    })
                }
            }

            skipIcon?.setOnClickListener {
                MusicDiffuser(context).skip()
            }
        }

        val recycler = view?.findViewById<RecyclerView>(R.id.recycler)
        var songs: MutableList<StationModel> = if (songList != null && songList!!.size > 0) {
            songList!!
        } else {
            mutableListOf()
        }
        val random = Random()

        while (songs.size < 25 && stations.size > 0) {
            val randomIndex = random.nextInt(stations.size)
            val randomSong = stations[randomIndex]

            if (!songs.contains(randomSong)) {
                songs.add(randomSong)
            }
        }

        stationList = songs
        recycler?.adapter = RecyclerAdapter(context, songs, R.layout.vertical_item, true)
        recycler?.addItemDecoration(ItemDecoration(40))

        return view
    }
}