package fr.greensky.lofimobile.PlaylistAdapter

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.greensky.lofimobile.Database.Singleton.stations
import fr.greensky.lofimobile.MainActivity
import fr.greensky.lofimobile.MusicDiffuser
import fr.greensky.lofimobile.R
import fr.greensky.lofimobile.fragments.HomeFragment
import fr.greensky.lofimobile.fragments.HomeFragment.Singleton.stationList
import fr.greensky.lofimobile.fragments.PlaylistPage
import fr.greensky.lofimobile.models.PlaylistModel

class PlaylistAdapter(public val context: MainActivity, private val list: List<PlaylistModel>, private val layoutId: Int): RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView? = view.findViewById(R.id.playlist_name)
        val img = view.findViewById<ImageView>(R.id.playlist_img)
        val playBtn = view.findViewById<ImageView>(R.id.playlist_icon_play)
        val shuffleBtn = view.findViewById<ImageView>(R.id.playlist_icon_shuffle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = list[position]
        holder.name?.text = "${current.name} (${current.ids.length()} music${if (current.ids.length() > 0) {"s"} else {""}})"

        if (current.ids.length() > 0) {
            val first = stations.find{ it.id == current.ids[0] }
            Glide.with(context).load(first?.img)?.into(holder.img)
        } else {
            val random = stations.random()
            Glide.with(context).load(random.img).into(holder.img)
        }

        holder.itemView.setOnClickListener {
            context.loadFragment(PlaylistPage(context, current))
        }

        if (current.ids.length() == 0) {
            holder.shuffleBtn.setColorFilter(ContextCompat.getColor(context, R.color.gray), PorterDuff.Mode.SRC_ATOP)
            holder.playBtn.setColorFilter(ContextCompat.getColor(context, R.color.gray), PorterDuff.Mode.SRC_ATOP)
        } else {
            holder.shuffleBtn.setOnClickListener {
                val player = MusicDiffuser(context)
                val songs: MutableList<String> = mutableListOf()

                for (i in 0 until current.ids.length()) {
                    songs.add(current.ids[0].toString())
                }

                songs.shuffle()
                player.start(songs[0], {
                    context.loadFragment(HomeFragment(context, stationList))
                })

                if (songs.size > 1) {
                    for (i in 1 until songs.size) {
                        player.playlistAddTrack(songs[i])
                    }
                }
            }
            holder.playBtn.setOnClickListener {
                val player = MusicDiffuser(context)
                player.clearTracks()
                player.start(current.ids[0].toString(), {
                    context.loadFragment(HomeFragment(context, stationList))
                })

                if (current.ids.length() > 1) {
                    for (i in 1 until current.ids.length()) {
                        player.addTrack(current.ids[i].toString())
                    }
                }
            }
        }
    }
}