package fr.greensky.lofimobile

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import fr.greensky.lofimobile.Database.Singleton.currentPlaylist
import fr.greensky.lofimobile.Database.Singleton.databaseRef
import fr.greensky.lofimobile.Database.Singleton.homeCurrentList
import fr.greensky.lofimobile.Database.Singleton.stations
import fr.greensky.lofimobile.Database.Singleton.storageRef
import fr.greensky.lofimobile.models.DatabaseStationModel
import fr.greensky.lofimobile.models.PlaylistModel
import fr.greensky.lofimobile.models.StationModel
import org.json.JSONArray
import org.json.JSONObject

val configs = Config()
class Database(public val context: MainActivity) {
    object Singleton {
        val database = FirebaseDatabase.getInstance(configs.databaseURL)
        val databaseRef = database.getReference(configs.databaseReference)

        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(configs.storageRef)
        val stations = arrayListOf<StationModel>()

        var currentToAdd: StationModel? = null
        var homeCurrentList: MutableList<StationModel> = mutableListOf()

        var currentPlaylist: PlaylistModel? = null
    }

    fun launch(callback: () -> Unit) {
        databaseRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                stations.clear()
                for (sn in snapshot.children) {
                    val station = sn.getValue(DatabaseStationModel::class.java)

                    val authorsJsonArray = JSONArray(station?.authors)
                    val authorsArray = Array(authorsJsonArray.length()) { i -> authorsJsonArray.getString(i) }

                    val tracks = JSONObject(station?.tracks)

                    stations.add(
                        StationModel(
                            station?.title!!,
                            station.url,
                            authorsArray,
                            station.id,
                            station.beats,
                            station.img,
                            tracks,
                            station.ref
                        )
                    )
                }
                callback()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Database Error", error.toString())
            }

        })
    }
    fun getTheme(): String {
        val themes = context.getSharedPreferences("configs", 0x0000);
        val data = themes.getBoolean("theme", false)

        return if (data) { "dark" } else { "light" }
    }
    fun swapTheme(): Boolean {
        val themes = context.getSharedPreferences("configs", 0x0000)
        val data = getTheme();

        val theme = if (data == "dark") { true } else { false };
        val editor = themes.edit();

        editor.putBoolean("theme", !theme);
        editor.apply()
        return !theme;
    }
    fun getPlaylist(name: String): PlaylistModel? {
        val preferences = context.getSharedPreferences("playlist", 0x0000)

        val data = preferences.getString(name, null)
        if (data.isNullOrBlank()) return null

        val json = JSONObject(data)
        return PlaylistModel(
            json.getString("name"),
            json.getJSONArray("ids")
        )
    }
    private fun isPlaylist(name: String): Boolean {
        val preferences = context.getSharedPreferences("playlist", 0x0000)

        return !preferences.getString(name, null).isNullOrBlank()
    }
    fun addToPlaylist(playlist: String, id: String, max: Int?): Boolean {
        if (!isPlaylist(playlist)) {
            createPlaylist(playlist)
        }

        val play = getPlaylist(playlist)!!
        var isItemExists = false

        for (i in 0 until play.ids.length()) {
            val currentItem = play.ids.optString(i)
            if (currentItem == id) {
                isItemExists = true
                break
            }
        }

        if (isItemExists) return false

        if (max !== null && play.ids.length() >= max) {
            var i = play.ids.length() - 1;
            while (i >= max) {
                play.ids.remove(i)
                i-=1
            }
        }

        play.ids.put(id)

        val preferences = context.getSharedPreferences("playlist", 0x0000)
        val editor = preferences.edit()

        fun rep(str: String): String {
            return str.replace("\"", "\\\"")
        }
        fun map(): String {
            var content = mutableListOf<String>()
            for (i in 0 until play.ids.length()) {
                content.add("\"${play.ids[i]}\"")
            }

            return content.joinToString(",")
        }
        editor.putString(playlist, "{ \"name\":\"${rep(play.name)}\", \"ids\": [${map()}] }")

        editor.apply()
        return true
    }
    fun removeFromPlaylist(playlist: String, id: String): Boolean {
        if (!isPlaylist(playlist)) return false

        val play = getPlaylist(playlist)!!
        var ids = JSONArray("[]")

        for (i in 0 until play.ids.length()) {
            if (play.ids[i] != id) {
                ids.put(play.ids[i])
            }
        }
        val preferences = context.getSharedPreferences("playlist", 0x0000)
        val editor = preferences.edit()

        fun rep(str: String): String {
            return str.replace("\"", "\\\"")
        }
        fun map(): String {
            var content = mutableListOf<String>()
            for (i in 0 until ids.length()) {
                content.add("\"${ids[i]}\"")
            }

            return content.joinToString(",")
        }
        editor.putString(playlist, "{ \"name\":\"${rep(play.name)}\", \"ids\": [${map()}] }")
        editor.apply()
        return true
    }
    fun createPlaylist(name: String): Boolean {
        if (isPlaylist(name)) return false
        val preferences = context.getSharedPreferences("playlist", 0x0000)
        val editor = preferences.edit()

        editor.putString(name, "{\"name\": \"${name}\", \"ids\":[]")
        editor.apply()
        return true
    }
    fun deletePlaylist(name: String){
        if (isPlaylist(name)) {
            val preferences = context.getSharedPreferences("playlist", 0x0000)
            val editor = preferences.edit()

            editor.remove(name)
            editor.apply()
        }
    }
    fun playlists(systems: Boolean? = false): MutableList<PlaylistModel> {
        val preferences = context.getSharedPreferences("playlist", 0x0000)
        val playlists = mutableListOf<PlaylistModel>()

        for ((key, value) in preferences.all) {
            if (getPlaylist(key) != null) {
                playlists.add(getPlaylist(key)!!)
            }
        }

        val unallowed = listOf(context.getString(R.string.recentlyPlaylist))

        return playlists.filter {
            if (!systems!!) { unallowed.indexOf(it.name) == -1 } else {true}
        }.toMutableList()
    }
    fun registerPlaylist(name: String): Boolean {
        if (isPlaylist(name)) return false

        val preferences = context.getSharedPreferences("playlist", 0x0000)
        val editor = preferences.edit()

        editor.putString(name, "{\"name\":\"${name.replace("\"".toRegex(), "\\\"")}\", \"ids\": []}")
        editor.apply()

        return true
    }
    fun getMusicAudio(id: String): StorageReference {
        return storageRef.child("${id}.mp3")
    }
    fun availablePlaylists(id: String): List<PlaylistModel> {
        fun includedIn(arr: JSONArray): Boolean {
            var included = false

            for (i in 0 until arr.length()) {
                if (arr[i] == id) included = true
            }
            return included
        }
        return playlists().filter { !includedIn(it.ids) }
    }
    fun hasAvailablePlaylists(id: String): Boolean {
        return availablePlaylists(id).isNotEmpty()
    }
    fun currentPlaylist(): PlaylistModel? {
        return currentPlaylist
    }
    fun setCurrentPlaylist(playlist: PlaylistModel): PlaylistModel {
        currentPlaylist = playlist
        return playlist
    }
    fun getCurrentHomeList(): MutableList<StationModel> {
        return homeCurrentList
    }
    fun setCurrentHomeList(list: MutableList<StationModel>): MutableList<StationModel> {
        homeCurrentList = list
        return list
    }
}