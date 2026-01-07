package com.example.practica5.data

import com.example.practica5.model.FavoriteRequest
import com.example.practica5.model.FavoriteResponse
import com.example.practica5.model.TvMazeResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// 1. Interfaz para TVMaze
interface TvMazeApi {
    @GET("search/shows")
    suspend fun searchShows(@Query("q") query: String): List<TvMazeResponse>
}

// 2. Interfaz para TU Backend (Node.js)
interface MyBackendApi {
    @POST("api/favoritos")
    suspend fun syncFavorite(@Body fav: FavoriteRequest): retrofit2.Response<Void>

    @GET("api/favoritos/{userId}")
    suspend fun getRemoteFavorites(@Path("userId") userId: Int): List<FavoriteResponse>
}

// 3. Objeto Singleton para crear las conexiones
object RetrofitClient {

    // Cliente para TVMaze
    val tvMazeApi: TvMazeApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.tvmaze.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TvMazeApi::class.java)
    }

    // Cliente para TU servidor Local
    // IMPORTANTE: En el emulador Android, "localhost" es "10.0.2.2"
    val myApi: MyBackendApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyBackendApi::class.java)
    }
}