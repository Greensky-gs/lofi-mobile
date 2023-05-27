package fr.greensky.lofimobile.MusicsAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.greensky.lofimobile.MainActivity
import fr.greensky.lofimobile.R

class MusicsAdapter(public val context: MainActivity, private val list: List<String>, private val layoutId: Int): RecyclerView.Adapter<MusicsAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val content: TextView? = view.findViewById(R.id.music_recycling_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = list[position]
        holder.content?.text = current
    }
}