package fr.greensky.lofimobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import fr.greensky.lofimobile.Database
import fr.greensky.lofimobile.MainActivity
import fr.greensky.lofimobile.MusicDiffuser
import fr.greensky.lofimobile.PlaylistAdapter.PlaylistAdapter
import fr.greensky.lofimobile.R
import fr.greensky.lofimobile.adapter.ItemDecoration

class PlaylistsFragment(private val context: MainActivity): Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val plus = context.findViewById<ImageView>(R.id.main_plus_icon)
        plus.visibility = View.VISIBLE
        plus.setOnClickListener {
            context.loadFragment(RegisterPlaylist(context))
        }

        val view = inflater?.inflate(R.layout.playlist_fragment, container, false)
        val recycler = view?.findViewById<RecyclerView>(R.id.playlist_recycler!!)

        val list = Database(context).playlists()
        recycler?.adapter = PlaylistAdapter(context, list, R.layout.playlist_view_recycling)
        recycler?.addItemDecoration(ItemDecoration(30))

        return view
    }
}