package fr.greensky.lofimobile.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import fr.greensky.lofimobile.Database
import fr.greensky.lofimobile.Database.Singleton.currentToAdd
import fr.greensky.lofimobile.MainActivity
import fr.greensky.lofimobile.R
import fr.greensky.lofimobile.fragments.PlaylistsFragment

class AddToPlaylist(private val context: MainActivity) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val db = Database(context)
        val view = inflater?.inflate(if (db.getTheme() === "dark") { R.layout.add_to_playlist_fragment_dark } else {R.layout.add_to_playlist_fragment}, container, false)
        val back = context.findViewById<ImageView>(R.id.main_back_icon)
        back.visibility = View.VISIBLE

        back.setOnClickListener {
            currentToAdd = null
            context.loadFragment(PlaylistsFragment(context))
        }
        val error = view?.findViewById<TextView>(R.id.add_to_playlist_error)

        if (currentToAdd != null) {
            val playlists = Database(context).availablePlaylists(currentToAdd!!.id)
            val spinner = view?.findViewById<Spinner>(R.id.add_to_playlist_spinner)
            val btn = view?.findViewById<Button>(R.id.add_to_playlist_button)

            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, playlists.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
            spinner?.adapter = adapter

            btn?.setOnClickListener {
                val value = spinner?.selectedItem.toString()
                if (value.isNullOrBlank() || value.isNullOrEmpty()) {
                    error?.text = getString(R.string.addToPlaylistNoPlaylist)
                } else {
                    Database(context).addToPlaylist(value, currentToAdd!!.id, null)
                    currentToAdd = null
                    context.loadFragment(PlaylistsFragment(context))
                }
            }
        } else {
            error?.text = getString(R.string.addToPlaylistErrorNoStation)
        }

        return view
    }
}