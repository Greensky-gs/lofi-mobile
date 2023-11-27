package fr.greensky.lofimobile.fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import fr.greensky.lofimobile.Database
import fr.greensky.lofimobile.MainActivity
import fr.greensky.lofimobile.R

class WaitFragment(private val context: MainActivity) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val db = Database(context)
        val theme = if (db.getTheme() === "dark") { R.layout.wait_fragment_dark } else { R.layout.wait_fragment }

        return inflater?.inflate(theme, container, false)
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val imageView = view?.findViewById<ImageView>(R.id.load_img)
        imageView?.alpha = 0f

        val fadeInAnimation = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f)
        fadeInAnimation.duration = 2000

        fadeInAnimation.start()
    }
}