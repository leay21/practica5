package com.example.practica5.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.practica5.data.ShowRepository
import com.example.practica5.model.ShowEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ShowRepository) : ViewModel() {

    var currentUserId = -1
    private val _currentUserId = MutableLiveData<Int>()

    // --- ESTADO DE LA VISTA ---
    // true = Estamos viendo Favoritos
    // false = Estamos buscando o viendo recomendaciones
    private val _isShowingFavorites = MutableLiveData(false)
    val isShowingFavorites: LiveData<Boolean> = _isShowingFavorites // Solo lectura para la UI

    // --- DATOS ---
    private val _searchResults = MutableLiveData<List<ShowEntity>>()
    val searchResults: LiveData<List<ShowEntity>> = _searchResults

    val favorites: LiveData<List<ShowEntity>> = _currentUserId.switchMap { id ->
        repository.getFavorites(id).asLiveData()
    }

    fun setUserId(id: Int) {
        currentUserId = id
        _currentUserId.value = id
    }

    // --- ACCIONES ---

    // 1. Mostrar Favoritos
    fun showFavorites() {
        _isShowingFavorites.value = true
    }

    // 2. (NUEVO) Volver a Búsqueda/Recomendaciones
    fun showSearch() {
        _isShowingFavorites.value = false
    }

    fun search(query: String) {
        _isShowingFavorites.value = false
        viewModelScope.launch {
            if (query.isNotEmpty()) {
                if (currentUserId != -1) {
                    launch { repository.saveSearchHistory(currentUserId, query) }
                }
                // CAMBIO: Pasamos currentUserId
                val results = repository.searchShows(query, currentUserId)
                _searchResults.postValue(results)
            }
        }
    }

    fun loadRecommendations() {
        // Cambiamos el estado de la vista
        // NOTA: Usa la función showSearch() que creamos en el paso anterior si la tienes,
        // o modifica el LiveData directamente si lo cambiaste a MutableLiveData público.
        // Aquí asumo la corrección anterior:
        showSearch()

        viewModelScope.launch {
            if (currentUserId == -1) return@launch

            // 1. Obtener las 3 "semillas" aleatorias (Ej: "Batman", "Office", "Friends")
            val seeds = repository.getRecommendationSeeds(currentUserId)

            // 2. Lanzar 3 búsquedas en PARALELO (simultáneas)
            // Usamos 'async' para que no espere a una para empezar la otra
            val deferredResults = seeds.map { query ->
                async { repository.searchShows(query, currentUserId) }
            }

            // 3. Esperar a que terminen todas y juntar los resultados
            val resultsOfAll = deferredResults.awaitAll() // Espera a las 3
                .flatten() // Convierte lista de listas en una sola lista gigante
                .distinctBy { it.id } // Elimina series repetidas
                .shuffled() // ¡Mezcla todo para que se sienta como una recomendación real!

            _searchResults.postValue(resultsOfAll)
        }
    }

    fun addToFavorites(show: ShowEntity) {
        if (currentUserId == -1) return
        viewModelScope.launch { repository.addFavorite(show, currentUserId) }
    }
}

// Boilerplate Factory
class MainViewModelFactory(private val repository: ShowRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}