package fr.greensky.lofimobile.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import fr.greensky.lofimobile.Database
import fr.greensky.lofimobile.MainActivity
import fr.greensky.lofimobile.R

class RegisterPlaylist(private val context: MainActivity) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val db = Database(context)
        val view = inflater?.inflate(if (db.getTheme() === "dark") { R.layout.register_playlist_fragment_dark } else {R.layout.register_playlist_fragment}, container, false)

        val button = view?.findViewById<Button>(R.id.register_button)
        val field = view?.findViewById<EditText>(R.id.register_name_field)

        val errorField = view?.findViewById<TextView>(R.id.register_error_view)

        field?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                errorField?.text = ""
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        button?.setOnClickListener {
            val name = field?.text?.toString()

            if (name.isNullOrBlank() || name.isNullOrEmpty()) {
                errorField?.text = getString(R.string.emptyName)
            } else {
                if (name === getString(R.string.recentlyPlaylist)) {
                    errorField?.text = getString(R.string.playlistUnallowedName)
                } else {
                    val result = Database(context).registerPlaylist(name)

                    if (result == false) {
                        errorField?.text = getString(R.string.alreadyExists)
                    } else {
                        context.loadFragment(PlaylistsFragment(context))
                    }
                }
            }
        }
        return view
    }
}