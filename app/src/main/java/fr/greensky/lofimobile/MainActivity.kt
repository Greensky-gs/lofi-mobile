package fr.greensky.lofimobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import fr.greensky.lofimobile.adapter.AddToPlaylist
import fr.greensky.lofimobile.fragments.HomeFragment
import fr.greensky.lofimobile.fragments.PlaylistPage
import fr.greensky.lofimobile.fragments.PlaylistsFragment
import fr.greensky.lofimobile.fragments.SearchFragment
import fr.greensky.lofimobile.fragments.WaitFragment

class MainActivity : AppCompatActivity() {
    private var hasBeenLoaded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Database(this)

        //setContentView(if (db.getTheme() === "dark") { R.layout.activity_main_dark } else { R.layout.activity_main })
        setContentView(R.layout.activity_main)

        updateTheme(db.getTheme())
        loadFragment(WaitFragment(this))

        db.launch {
            hasBeenLoaded = true
            handleNavigation(db)

            loadFragment(HomeFragment(this))
        }
    }

    private fun handleNavigation(db: Database) {
        val playlist = findViewById<ImageView>(R.id.mainPlaylistPageNav)
        val home = findViewById<ImageView>(R.id.mainHomePageNav)
        val search = findViewById<ImageView>(R.id.mainSearchPageNav)
        val theme = findViewById<ImageView>(R.id.switchTheme)

        playlist.setOnClickListener {
            loadFragment(PlaylistsFragment(this))
        }
        home.setOnClickListener {
            loadFragment(HomeFragment(this))
        }
        search.setOnClickListener {
            loadFragment(SearchFragment(this))
        }
        theme.setOnClickListener {
            handleTheme(db)
        }
    }

    fun loadFragment(fragment: Fragment) {
        clearBtns()
        try {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)

            transaction.commit()
        } catch (err: Throwable) {
            Log.e("Error", err.toString())
        }
    }
    private fun clearBtns() {
        val plus = findViewById<ImageView>(R.id.main_plus_icon)
        val back = findViewById<ImageView>(R.id.main_back_icon)

        plus.visibility = View.GONE
        back.visibility = View.GONE

        plus.setOnClickListener(null)
        back.setOnClickListener(null)
    }
    private fun handleTheme(db: Database) {
        val fragment =
            this.supportFragmentManager.findFragmentById(R.id.fragment_container) ?: return

        if (fragment === null) {
            Log.i("Theme: ", "null")
            return
        }
        val swap = db.swapTheme()
        val current = db.getTheme()

        updateTheme(current)

        alternate(fragment.toString())
    }
    private fun updateTheme(current: String) {
        val button = findViewById<ImageView>(R.id.switchTheme)
        button.setImageResource(if (current === "dark") { R.drawable.ic_light } else { R.drawable.ic_dark })
    }
    private fun alternate(input: String) {
        val fragmentFunctions = listOf(
            fun () {
                loadFragment(HomeFragment(this))
            },
            fun () {
                loadFragment(SearchFragment(this))
            },
            fun() {
                loadFragment(PlaylistsFragment(this))
            },
            fun() {
                val db = Database(this)
                loadFragment(PlaylistPage(this, db.currentPlaylist()!!))
            },
            fun () {
                loadFragment(AddToPlaylist(this))
            }
        )
            val fragmentnames = listOf<String>(
                "HomeFragment",
                "SearchFragment",
                "PlaylistsFragment",
                "PlaylistPage",
                "AddToPlaylist",
            )
        val fragmentName = input.split('{').first()
        Log.i("Name", fragmentName.length.toString())
        val index = fragmentnames.indexOf(fragmentName)
        if (index == -1) return

        (fragmentFunctions[index])()
    }

    override fun onResume() {
        if (hasBeenLoaded) {
            super.onResume()
            loadFragment(HomeFragment(this))
        } else {
            super.onResume()
        }
    }
}