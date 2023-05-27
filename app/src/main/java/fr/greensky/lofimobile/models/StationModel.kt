package fr.greensky.lofimobile.models

import org.json.JSONObject

class StationModel (
    val title: String = "Titre",
    val url: String = "url",
    val authors: Array<String> = arrayOf(),
    val id: String = "",
    val beats: String = "lofi hip hop/relaxing beats",
    val img: String = "",
    val tracks: JSONObject = JSONObject("{}"),
    val ref: String = ""
)