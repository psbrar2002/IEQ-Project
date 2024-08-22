package com.tst.ieqproject

import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.IOException

object NominatimAPI {
    private val client = OkHttpClient()
    private const val NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?"

    data class Place(
        @SerializedName("display_name") val displayName: String
    )

    @Throws(IOException::class)
    fun search(query: String): List<Place> {
        val url = "${NOMINATIM_URL}q=${query}&format=json&addressdetails=1"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val body = response.body?.string() ?: return emptyList()
            return Gson().fromJson(body, Array<Place>::class.java).toList()
        }
    }
}
