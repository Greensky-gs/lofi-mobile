package fr.greensky.lofimobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import fr.greensky.lofimobile.fragments.HomeFragment
import fr.greensky.lofimobile.fragments.PlaylistsFragment
import fr.greensky.lofimobile.fragments.SearchFragment
import fr.greensky.lofimobile.fragments.WaitFragment

class MainActivity : AppCompatActivity() {
    private var hasBeenLoaded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(WaitFragment(this))
        val db = Database(this)
        db.launch {
            hasBeenLoaded = true
            handleNavigation()

            loadFragment(HomeFragment(this))
        }
    }

    private fun handleNavigation() {
        val playlist = findViewById<ImageView>(R.id.mainPlaylistPageNav)
        val home = findViewById<ImageView>(R.id.mainHomePageNav)
        val search = findViewById<ImageView>(R.id.mainSearchPageNav)

        playlist.setOnClickListener {
            loadFragment(PlaylistsFragment(this))
        }
        home.setOnClickListener {
            loadFragment(HomeFragment(this))
        }
        search.setOnClickListener {
            loadFragment(SearchFragment(this))
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

    override fun onResume() {
        if (hasBeenLoaded) {
            super.onResume()
            loadFragment(HomeFragment(this))
        } else {
            super.onResume()
        }
    }
}