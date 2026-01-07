package com.example.practica5.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.practica5.model.FavoriteRequest
import com.example.practica5.model.FavoriteResponse
import com.example.practica5.model.HistoryRequest
import com.example.practica5.model.TvMazeResponse
import com.example.practica5.model.LoginResponse
import com.example.practica5.model.LoginRequest
import com.example.practica5.model.RegisterRequest
import com.example.practica5.model.RegisterResponse

interface TvMazeApi {
    @GET("search/shows")
    suspend fun searchShows(@Query("q") query: String): List<TvMazeResponse>
}

interface MyBackendApi {
    @POST("api/favoritos")
    suspend fun syncFavorite(@Body fav: FavoriteRequest): retrofit2.Response<Void>

    @GET("api/favoritos/{userId}")
    suspend fun getRemoteFavorites(@Path("userId") userId: Int): List<FavoriteResponse>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): retrofit2.Response<LoginResponse>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): retrofit2.Response<RegisterResponse>

    @POST("api/historial")
    suspend fun addToHistory(@Body request: HistoryRequest): retrofit2.Response<Void>

    @GET("api/historial/{userId}")
    suspend fun getHistory(@Path("userId") userId: Int): List<com.example.practica5.model.HistoryItem>
}

object RetrofitClient {

    // 1. CREAMOS EL "ESPÍA" (INTERCEPTOR)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Veremos todo: Cabeceras y Datos
    }

    // 2. CREAMOS UN CLIENTE HTTP QUE USE ESE ESPÍA
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val tvMazeApi: TvMazeApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.tvmaze.com/")
            .client(client) // <-- Le enchufamos el cliente con logs
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TvMazeApi::class.java)
    }

    val myApi: MyBackendApi by lazy {
        Retrofit.Builder()
            // REVISA: Asegúrate que esta IP sea la de tu PC (ipconfig)
            .baseUrl("http://192.168.100.147:3000/")
            .client(client) // <-- Le enchufamos el cliente con logs aquí también
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyBackendApi::class.java)
    }
}