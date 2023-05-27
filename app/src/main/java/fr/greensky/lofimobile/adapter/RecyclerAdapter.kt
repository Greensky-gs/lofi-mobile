package fr.greensky.lofimobile.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.greensky.lofimobile.Database
import fr.greensky.lofimobile.Database.Singleton.currentToAdd
import fr.greensky.lofimobile.MainActivity
import fr.greensky.lofimobile.MusicDiffuser
import fr.greensky.lofimobile.MusicDiffuser.Singleton.currentSong
import fr.greensky.lofimobile.MusicDiffuser.Singleton.diffuser
import fr.greensky.lofimobile.R
import fr.greensky.lofimobile.models.StationModel
import fr.greensky.lofimobile.StationPopup
import fr.greensky.lofimobile.fragments.HomeFragment
import fr.greensky.lofimobile.fragments.HomeFragment.Singleton.stationList

class RecyclerAdapter(public val context: MainActivity, private val list: List<StationModel>, private val layoutId: Int, private val displayButtons: Boolean = false): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.song_image)
        val songName: TextView? = view.findViewById(R.id.song_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = list[position]
        Glide.with(context).load(Uri.parse(current.img)).into(holder.image)

        holder.songName?.text = if (current.authors.isEmpty()) {
            current.title
        } else {
            current.authors.joinToString(" x ") + " - " + current.title
        }

        if (displayButtons) {
            setButtons(holder.itemView, current)
        }
        holder.itemView.setOnClickListener {
            StationPopup(context, current).show()
        }
    }

    private fun setButtons(view: View, current: StationModel) {
        val addToPlaylistBtn = view.findViewById<ImageView>(R.id.add_to_playlist_icon)
        val playlistBackground = view.findViewById<CardView>(R.id.add_to_playlist_background)

        val playIcon = view.findViewById<ImageView>(R.id.play_icon)
        fun update() {
            if (currentSong != null && current.id == currentSong && diffuser?.isPlaying == true) {
                playIcon.setImageResource(R.drawable.ic_pause)
            } else {
                playIcon.setImageResource(R.drawable.ic_play)
            }
        }
        fun setAdd() {
            val db = Database(context)
            if (!db.hasAvailablePlaylists(current.id)) {
                addToPlaylistBtn.visibility = View.GONE
                playlistBackground.visibility = View.GONE
            } else {
                addToPlaylistBtn.setOnClickListener {
                    currentToAdd = current
                    context.loadFragment(AddToPlaylist(context))
                }
            }
        }
        setAdd()
        update()

        playIcon.setOnClickListener {
            update()
            if (currentSong == current.id && diffuser?.isPlaying == true) {
                MusicDiffuser(context).pause {
                    update()
                    context.loadFragment(HomeFragment(context, stationList))
                }
            } else if (currentSong == current.id && diffuser?.isPlaying == false) {
                MusicDiffuser(context).resume {
                    update()
                    context.loadFragment(HomeFragment(context, stationList))
                }
            } else {
                MusicDiffuser(context).start(current.id, {
                    update()
                    context.loadFragment(HomeFragment(context, stationList))
                }, {
                    update()
                })
            }
        }
    }
}