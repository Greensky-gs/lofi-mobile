package fr.greensky.lofimobile.adapter

import android.app.AlertDialog
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.greensky.lofimobile.Database
import fr.greensky.lofimobile.MainActivity
import fr.greensky.lofimobile.MusicDiffuser
import fr.greensky.lofimobile.R
import fr.greensky.lofimobile.StationPopup
import fr.greensky.lofimobile.fragments.HomeFragment
import fr.greensky.lofimobile.fragments.PlaylistPage
import fr.greensky.lofimobile.models.StationModel

class PlaylistSongsAdapter(val context: MainActivity, private val list: List<StationModel>, private val layoutId: Int, private val playlistName: String) : RecyclerView.Adapter<PlaylistSongsAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.playing_img)
        val songName = view.findViewById<TextView>(R.id.playing_song_title)
        val play = view.findViewById<ImageView>(R.id.playing_play_icon)
        val remove = view.findViewById<ImageView>(R.id.playing_remove_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = list[position]
        val database = Database(context)
        val playIcon = holder.play

        Glide.with(context).load(Uri.parse(current.img)).into(holder.img)

        holder.songName.text = current.title

        holder.remove.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.confirmDeleteSongFromPlaylist)
            builder.setNegativeButton(R.string.confirmDeletePlaylistNo) {inter, _ ->
                inter.dismiss()
            }
            builder.setPositiveButton(R.string.confirmDeletePlaylistYes) {inter, _ ->
                inter.dismiss()
                database.removeFromPlaylist(playlistName, current.id)
                context.loadFragment(PlaylistPage(context, Database(context).getPlaylist(playlistName)!!))
            }

            val dialog = builder.create()
            dialog.show()
        }

        holder.itemView.setOnClickListener {
            StationPopup(context, current).show()
        }
        fun update() {
            if (MusicDiffuser.Singleton.currentSong != null && current.id == MusicDiffuser.Singleton.currentSong && MusicDiffuser.Singleton.diffuser?.isPlaying == true) {
                playIcon.setImageResource(R.drawable.ic_pause)
            } else {
                playIcon.setImageResource(R.drawable.ic_play)
            }
        }
        update()

        playIcon.setOnClickListener {
            update()
            if (MusicDiffuser.Singleton.currentSong == current.id && MusicDiffuser.Singleton.diffuser?.isPlaying == true) {
                MusicDiffuser(context).pause {
                    update()
                    context.loadFragment(HomeFragment(context, HomeFragment.Singleton.stationList))
                }
            } else if (MusicDiffuser.Singleton.currentSong == current.id && MusicDiffuser.Singleton.diffuser?.isPlaying == false) {
                MusicDiffuser(context).resume {
                    update()
                    context.loadFragment(HomeFragment(context, HomeFragment.Singleton.stationList))
                }
            } else {
                val songs = Database(context).getPlaylist(playlistName)!!.ids

                var found = false
                val toPush = mutableListOf<String>()

                for (i in 0 until songs.length()) {
                    if (songs[i] == current.id) {
                        found = true
                    }
                    if (found && songs[i] != current.id) {
                        toPush.add(0, songs[i] as String)
                    }
                }
                for (i in 0 until toPush.size) {
                    MusicDiffuser(context).playlistAddTrack(toPush[i])
                }

                MusicDiffuser(context).start(current.id, {
                    update()
                    context.loadFragment(HomeFragment(context, HomeFragment.Singleton.stationList))
                }, {
                    update()
                })
            }
        }
    }
}