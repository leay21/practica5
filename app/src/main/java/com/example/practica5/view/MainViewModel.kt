package com.example.practica5.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap // Importante para que funcione el switchMap
import androidx.lifecycle.viewModelScope
import com.example.practica5.data.ShowRepository
import com.example.practica5.model.ShowEntity
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ShowRepository) : ViewModel() {

    // UserId actual (se llena desde MainActivity al iniciar)
    var currentUserId = -1

    // 1. LiveData para controlar si estamos viendo recomendaciones o búsquedas
    val isRecommendationActive = MutableLiveData(false)

    // 2. Lista de resultados de búsqueda (Observable)
    private val _searchResults = MutableLiveData<List<ShowEntity>>()
    val searchResults: LiveData<List<ShowEntity>> = _searchResults

    // 3. Gestión reactiva del usuario y sus favoritos
    private val _currentUserId = MutableLiveData<Int>()

    // Esta "magia" hace que cuando _currentUserId cambie, se actualice la consulta a Room
    // automáticamente con el ID del nuevo usuario.
    val favorites: LiveData<List<ShowEntity>> = _currentUserId.switchMap { id ->
        repository.getFavorites(id).asLiveData()
    }

    // Método para setear el usuario al iniciar la Activity
    fun setUserId(id: Int) {
        currentUserId = id
        _currentUserId.value = id
    }

    // --- ACCIONES ---

    // A. BUSCAR SERIE
    fun search(query: String) {
        // Marcamos que NO es recomendación, es una búsqueda explícita
        isRecommendationActive.postValue(false)

        viewModelScope.launch {
            if (query.isNotEmpty()) {
                // 1. Guardar en historial remoto (si hay usuario)
                if (currentUserId != -1) {
                    launch { repository.saveSearchHistory(currentUserId, query) }
                }

                // 2. Buscar en API pública
                val results = repository.searchShows(query)
                _searchResults.postValue(results)
            }
        }
    }

    // B. CARGAR RECOMENDACIONES (Lógica Inteligente)
    fun loadRecommendations() {
        // Marcamos que SÍ es recomendación
        isRecommendationActive.postValue(true)

        viewModelScope.launch {
            if (currentUserId == -1) return@launch

            // 1. Semilla por defecto (si el usuario es nuevo y no tiene nada)
            var seedQuery = "Star Wars"

            // 2. Consultamos a la BD local si tiene ALGÚN favorito
            val favorite = repository.getAnyFavorite(currentUserId)

            if (favorite != null) {
                // ¡Tiene favoritos! Usamos el nombre de uno para buscar similares
                seedQuery = favorite.name
            }

            // 3. Buscamos en la API usando esa semilla
            val results = repository.searchShows(seedQuery)

            // 4. (Opcional) Podríamos filtrar los que ya tiene en favoritos aquí
            // val misFavs = favorites.value ?: emptyList()
            // val filtered = results.filter { r -> misFavs.none { f -> f.id == r.id } }

            _searchResults.postValue(results)
        }
    }

    // C. AGREGAR FAVORITO
    fun addToFavorites(show: ShowEntity) {
        if (currentUserId == -1) return
        viewModelScope.launch {
            repository.addFavorite(show, currentUserId)
        }
    }

    // D. BORRAR FAVORITO
    fun removeFromFavorites(showId: Int) {
        if (currentUserId == -1) return
        viewModelScope.launch {
            // CORRECCIÓN: Ahora pasamos también el userId al repositorio
            repository.removeFavorite(showId, currentUserId)
        }
    }
}

// Boilerplate: Fábrica del ViewModel
class MainViewModelFactory(private val repository: ShowRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}