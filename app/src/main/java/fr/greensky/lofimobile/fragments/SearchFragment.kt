package fr.greensky.lofimobile.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import fr.greensky.lofimobile.Database.Singleton.stations
import fr.greensky.lofimobile.MainActivity
import fr.greensky.lofimobile.R
import fr.greensky.lofimobile.adapter.ItemDecoration
import fr.greensky.lofimobile.adapter.RecyclerAdapter

class SearchFragment(private val context: MainActivity) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater?.inflate(R.layout.search_page, container, false)
        val textInput = view?.findViewById<EditText>(R.id.search_input)

        val recycler = view?.findViewById<RecyclerView>(R.id.search_results_recycler)
        recycler?.addItemDecoration(ItemDecoration(10))

        textInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) return
                val search = s.toString().lowercase()

                val searched = stations.filter {
                    search in it.title.lowercase() || it.title.lowercase() in search || ((it.authors.joinToString("").lowercase() in search) && ( !it.authors.isEmpty() )) || search in it.authors.joinToString("").lowercase()
                }

                //TODO mettre un fond blanc derrière le bouton de démarrage de musique
                recycler?.adapter = RecyclerAdapter(context, searched, R.layout.vertical_item, true)
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        return view
    }
}