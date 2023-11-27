package fr.greensky.lofimobile.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import fr.greensky.lofimobile.Database
import fr.greensky.lofimobile.Database.Singleton.stations
import fr.greensky.lofimobile.MainActivity
import fr.greensky.lofimobile.R
import fr.greensky.lofimobile.adapter.ItemDecoration
import fr.greensky.lofimobile.adapter.PlaylistSongsAdapter
import fr.greensky.lofimobile.models.PlaylistModel
import fr.greensky.lofimobile.models.StationModel

class PlaylistPage(private val context: MainActivity, private val playlist: PlaylistModel) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val back = context.findViewById<ImageView>(R.id.main_back_icon)
        back.visibility = View.VISIBLE

        back.setOnClickListener {
            context.loadFragment(PlaylistsFragment(context))
        }
        val db = Database(context)
        val view = inflater?.inflate(if (db.getTheme() === "dark") { R.layout.playlist_page_dark } else {R.layout.playlist_page}, container, false)

        view?.findViewById<TextView>(R.id.playlist_page_name)?.text = playlist.name
        val recycler = view?.findViewById<RecyclerView>(R.id.playlist_page_recycler)

        var stationsList = mutableListOf<StationModel>()
        for (i in 0 until playlist.ids.length()) {
            stationsList.add(stations.find{ it.id == playlist.ids[i] }!!)
        }
        recycler?.adapter = PlaylistSongsAdapter(context, stationsList, R.layout.playlist_recycling, playlist.name)
        recycler?.addItemDecoration(ItemDecoration(10))

        setBtn(view)
        return view
    }

    private fun setBtn(view: View?) {
        val button = view?.findViewById<ImageView>(R.id.playlist_page_delete)

        button?.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(getString(R.string.confirmDeletePlaylist))

            builder.setPositiveButton(getString(R.string.confirmDeletePlaylistYes)) {inter, _ ->
                inter.dismiss()
                Database(context).deletePlaylist(playlist.name)
                context.loadFragment(HomeFragment(context))
            }
            builder.setNegativeButton(getString(R.string.confirmDeletePlaylistNo)) { inter, _ ->
                inter.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }
}