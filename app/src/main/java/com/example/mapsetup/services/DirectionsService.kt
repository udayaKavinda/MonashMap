import com.example.mapsetup.models.DirectionsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsService {
    @GET("maps/api/directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String,
        @Query("alternatives") alternatives: Boolean = true,
        @Query("mode") mode: String = "walking" // Optional, e.g., driving, walking, bicycling
    ): Call<DirectionsResponse>
}
