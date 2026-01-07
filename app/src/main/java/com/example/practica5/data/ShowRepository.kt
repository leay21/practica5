package com.example.practica5.data

import android.util.Log
import com.example.practica5.model.FavoriteRequest
import com.example.practica5.model.HistoryRequest
import com.example.practica5.model.ShowEntity
import kotlinx.coroutines.flow.Flow

class ShowRepository(
    private val showDao: ShowDao,
    private val myApi: MyBackendApi,
    private val tvApi: TvMazeApi
) {

    // --- PARTE 1: DATOS LOCALES (Fuente de la verdad) ---
    // La UI observará esto. Si algo cambia aquí, la pantalla se actualiza sola.
    val allFavorites: Flow<List<ShowEntity>> = showDao.getFavorites()

    // --- PARTE 2: BÚSQUEDA EN TVMAZE (API PÚBLICA) ---
    suspend fun searchShows(query: String): List<ShowEntity> {
        return try {
            val response = tvApi.searchShows(query)
            // Convertimos la respuesta de la API a nuestra entidad ShowEntity
            response.mapNotNull { item ->
                // Filtramos resultados sin imagen o nombre para que se vea bonito
                if (item.show.image?.medium != null) {
                    ShowEntity(
                        id = item.show.id,
                        name = item.show.name,
                        imageUrl = item.show.image.medium,
                        summary = item.show.summary,
                        isFavorite = false // Por defecto no es favorito al buscar
                    )
                } else null
            }
        } catch (e: Exception) {
            Log.e("REPO", "Error buscando en API: ${e.message}")
            emptyList() // Si no hay internet, devolvemos lista vacía (o podrías buscar en local)
        }
    }

    // --- PARTE 3: SINCRONIZACIÓN (OFFLINE FIRST) ---
    suspend fun addFavorite(show: ShowEntity, userId: Int) {
        // 1. Guardar en LOCAL (Room) inmediatamente.
        // Esto garantiza que la app funcione RÁPIDO y SIN INTERNET.
        val favoriteShow = show.copy(isFavorite = true)
        showDao.insertShow(favoriteShow)

        // 2. Intentar enviar a TU SERVIDOR (Node.js)
        try {
            val request = FavoriteRequest(
                userId = userId,
                showId = show.id,
                title = show.name,
                image = show.imageUrl
            )
            val response = myApi.syncFavorite(request)
            if (response.isSuccessful) {
                Log.d("SYNC", "Sincronizado con la nube exitosamente")
            } else {
                Log.d("SYNC", "Error en servidor: ${response.code()}")
            }
        } catch (e: Exception) {
            // Si falla (ej. sin internet), no pasa nada grave.
            // El dato ya está guardado en local (paso 1).
            Log.e("SYNC", "Sin conexión al servidor. Se sincronizará luego (lógica pendiente).")
        }
    }

    suspend fun removeFavorite(showId: Int) {
        showDao.deleteFavorite(showId)
        // Opcional: Llamar a API para borrar en remoto también
    }
    suspend fun saveSearchHistory(userId: Int, query: String) {
        try {
            // "Fire and forget": Lo enviamos y no nos preocupa mucho si falla o no,
            // para no detener la búsqueda del usuario.
            myApi.addToHistory(HistoryRequest(userId, query))
            Log.d("REPO", "Historial guardado: $query")
        } catch (e: Exception) {
            Log.e("REPO", "No se pudo guardar historial: ${e.message}")
        }
    }

    suspend fun fetchHistory(userId: Int): List<com.example.practica5.model.HistoryItem> {
        return try {
            myApi.getHistory(userId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}