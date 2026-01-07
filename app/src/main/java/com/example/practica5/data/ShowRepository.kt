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

    // CAMBIO: Ahora pedimos el userId para filtrar la lista.
    // Esto soluciona que todos vean los mismos favoritos.
    fun getFavorites(userId: Int): Flow<List<ShowEntity>> {
        return showDao.getFavorites(userId)
    }

    // NUEVO: Método auxiliar para el sistema de recomendaciones.
    // Obtiene 1 favorito cualquiera de este usuario para usarlo como semilla.
    suspend fun getAnyFavorite(userId: Int): ShowEntity? {
        return showDao.getOneFavorite(userId)
    }

    // --- PARTE 2: BÚSQUEDA EN TVMAZE (API PÚBLICA) ---
    suspend fun searchShows(query: String, userId: Int): List<ShowEntity> {
        return try {
            // 1. Obtenemos los IDs de los favoritos que ya tenemos guardados
            val myFavoriteIds = showDao.getFavoriteIds(userId)

            // 2. Buscamos en la API
            val response = tvApi.searchShows(query)

            // 3. Cruzamos la información
            response.mapNotNull { item ->
                if (item.show.image?.medium != null) {
                    // ¿Esta serie de internet está en mis favoritos locales?
                    val isAlreadyFav = myFavoriteIds.contains(item.show.id)

                    ShowEntity(
                        id = item.show.id,
                        // Si ya es favorito, mantenemos el userId real. Si no, 0.
                        userId = if (isAlreadyFav) userId else 0,
                        name = item.show.name,
                        imageUrl = item.show.image.medium,
                        summary = item.show.summary,
                        // ¡AQUÍ ESTÁ LA MAGIA! Si está en la lista, marcamos true
                        isFavorite = isAlreadyFav
                    )
                } else null
            }
        } catch (e: Exception) {
            Log.e("REPO", "Error buscando: ${e.message}")
            emptyList()
        }
    }

    // --- PARTE 3: SINCRONIZACIÓN (OFFLINE FIRST) ---
    suspend fun addFavorite(show: ShowEntity, userId: Int) {
        // 1. Guardar en LOCAL (Room)
        // CAMBIO: Es vital asignar el userId AQUÍ antes de guardar.
        // Usamos .copy() para crear una versión nueva del objeto con el ID y flag correctos.
        val favoriteShow = show.copy(
            isFavorite = true,
            userId = userId
        )
        showDao.insertShow(favoriteShow)

        // 2. Sincronizar con Nube (Node.js)
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
            Log.e("SYNC", "Sin conexión. Guardado solo localmente.")
        }
    }

    // CAMBIO: Pedimos userId para borrar solo el favorito de ESTE usuario
    suspend fun removeFavorite(showId: Int, userId: Int) {
        showDao.deleteFavorite(showId, userId)
    }

    // --- HISTORIAL ---
    suspend fun saveSearchHistory(userId: Int, query: String) {
        try {
            myApi.addToHistory(HistoryRequest(userId, query))
            Log.d("REPO", "Historial guardado: $query")
        } catch (e: Exception) {
            Log.e("REPO", "No se pudo guardar historial: ${e.message}")
        }
    }

    // CAMBIO: Pedimos userId para que el servidor decida qué devolver (Admin vs User)
    suspend fun fetchHistory(userId: Int): List<com.example.practica5.model.HistoryItem> {
        return try {
            myApi.getHistory(userId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}