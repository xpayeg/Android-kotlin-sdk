package api


import model.PopularMovies
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.util.*

interface TmdbEndpoints {

    @GET("/3/movie/popular")
    fun getMovies(@Query("api_key") key: String): Call<ResponseBody>

}

interface Xpay {

    @GET("/users/{id}")
    fun userInfo(@Header("Authorization") authToken: String?, @Path("id") id: Int?): Call<ResponseBody>

}