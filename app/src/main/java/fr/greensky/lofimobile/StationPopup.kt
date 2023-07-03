package fr.greensky.lofimobile

import android.app.Dialog
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.greensky.lofimobile.Database.Singleton.currentToAdd
import fr.greensky.lofimobile.MusicsAdapter.MusicsAdapter
import fr.greensky.lofimobile.adapter.AddToPlaylist
import fr.greensky.lofimobile.adapter.ItemDecoration
import fr.greensky.lofimobile.adapter.RecyclerAdapter
import fr.greensky.lofimobile.fragments.HomeFragment
import fr.greensky.lofimobile.models.StationModel

class StationPopup(private val context: MainActivity, private val station: StationModel): Dialog(context) {
    val database = Database(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.station_popup)

        setContent()
        setImage()
        setRecycler()
        setupCloseButton()
        setPlayButton()
        setAddButton()
    }

    private fun setAddButton() {
        val btn = findViewById<ImageView>(R.id.playlist_icon)
        val database = Database(context)
        if (!database.hasAvailablePlaylists(station.id)) {
            btn.setColorFilter(ContextCompat.getColor(context, R.color.gray), PorterDuff.Mode.SRC_ATOP)
        } else {
            btn.setColorFilter(ContextCompat.getColor(context, R.color.black), PorterDuff.Mode.SRC_ATOP)
            btn.setOnClickListener {
                dismiss()
                currentToAdd = station
                context.loadFragment(AddToPlaylist(context))
            }
        }
    }

    private fun setPlayButton() {
        val btn = findViewById<ImageView>(R.id.play_icon)
        btn.setColorFilter(ContextCompat.getColor(context, R.color.black), PorterDuff.Mode.SRC_ATOP)

        fun update() {
            if (MusicDiffuser.Singleton.currentSong != null && station.id == MusicDiffuser.Singleton.currentSong && MusicDiffuser.Singleton.diffuser?.isPlaying == true) {
                btn.setImageResource(R.drawable.ic_pause)
            } else {
                btn.setImageResource(R.drawable.ic_play)
            }
        }
        update()

        btn.setOnClickListener {
            update()
            if (MusicDiffuser.Singleton.currentSong == station.id && MusicDiffuser.Singleton.diffuser?.isPlaying == true) {
                MusicDiffuser(context).pause {
                    update()
                    context.loadFragment(HomeFragment(context, HomeFragment.Singleton.stationList))
                }
            } else if (MusicDiffuser.Singleton.currentSong == station.id && MusicDiffuser.Singleton.diffuser?.isPlaying == false) {
                MusicDiffuser(context).resume {
                    update()
                    context.loadFragment(HomeFragment(context, HomeFragment.Singleton.stationList))
                }
            } else {
                MusicDiffuser(context).start(station.id, {
                    update()
                    context.loadFragment(HomeFragment(context, HomeFragment.Singleton.stationList))
                }, {
                    update()
                })
            }
        }
    }

    private fun setupCloseButton() {
        val btn = findViewById<ImageView>(R.id.popup_close_button)
        btn.setOnClickListener {
            dismiss()
        }
    }

    private fun setRecycler() {
        val recycler = findViewById<RecyclerView>(R.id.popup_musics_recycler)
        if (station.tracks.length() == 0) {
            recycler.adapter = MusicsAdapter(context, listOf("Musiques introuvables"), R.layout.music_view_holder)
        } else {
            val keys = station.tracks.keys()
            var tracks: MutableList<String> = mutableListOf()
            for (key in keys) {
                tracks.add(station.tracks.get(key) as String)
            }
            recycler.adapter = MusicsAdapter(context, tracks, R.layout.music_view_holder)
            recycler.addItemDecoration(ItemDecoration(2))
        }

    }

    private fun setImage() {
        val img = findViewById<ImageView>(R.id.popup_img)
        Glide.with(context).load(Uri.parse(station.img)).into(img)
    }

    private fun setContent() {
        val title = findViewById<TextView>(R.id.popup_song_name)
        title.text = station.title

        val authors = findViewById<TextView>(R.id.popup_authors_content)
        authors.text = if (station.authors.isEmpty()) {
            "Lofi Girl"
        } else {
            station.authors.joinToString(separator = " x ") + ""
        }
    }
}