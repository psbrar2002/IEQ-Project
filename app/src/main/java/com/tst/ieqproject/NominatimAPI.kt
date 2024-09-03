// NominatimAPI.kt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.osmdroid.util.GeoPoint
import java.net.URL

object NominatimAPI {
    suspend fun search(query: String): List<GeoPoint> {
        return withContext(Dispatchers.IO) {
            val url = "https://nominatim.openstreetmap.org/search?q=${query}&format=json"
            val response = URL(url).readText()
            val jsonArray = JSONArray(response)

            val results = mutableListOf<GeoPoint>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val lat = jsonObject.getDouble("lat")
                val lon = jsonObject.getDouble("lon")
                results.add(GeoPoint(lat, lon))
            }
            results
        }
    }
}
