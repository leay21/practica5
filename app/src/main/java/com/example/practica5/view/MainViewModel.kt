package com.example.practica5.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.practica5.data.ShowRepository
import com.example.practica5.model.ShowEntity
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ShowRepository) : ViewModel() {

    // UserId simulado (en una app real vendría del Login)
    var currentUserId = -1

    // 1. Lista de resultados de búsqueda (Observable)
    private val _searchResults = MutableLiveData<List<ShowEntity>>()
    val searchResults: LiveData<List<ShowEntity>> = _searchResults

    // 2. Lista de Favoritos (Viene directo de la BD Local)
    // Convertimos el Flow de Room a LiveData para la UI
    val favorites: LiveData<List<ShowEntity>> = repository.allFavorites.asLiveData()

    // Acción: Buscar serie
    fun search(query: String) {
        viewModelScope.launch {
            if (query.isNotEmpty()) {
                // 1. (NUEVO) Guardar en historial remoto
                if (currentUserId != -1) {
                    // Lanzamos una corrutina paralela para que no frene la búsqueda
                    launch { repository.saveSearchHistory(currentUserId, query) }
                }

                // 2. Buscar en API pública (Código existente)
                val results = repository.searchShows(query)
                _searchResults.postValue(results)
            }
        }
    }

    // Acción: Guardar favorito
    fun addToFavorites(show: ShowEntity) {
        if (currentUserId == -1) return // Protección
        viewModelScope.launch {
            repository.addFavorite(show, currentUserId)
        }
    }

    // Acción: Borrar favorito
    fun removeFromFavorites(showId: Int) {
        viewModelScope.launch {
            repository.removeFavorite(showId)
        }
    }

    // Agrega una variable para saber si estamos viendo recomendaciones
    val isRecommendationActive = MutableLiveData(false)

    fun loadRecommendations() {
        viewModelScope.launch {
            isRecommendationActive.postValue(true)

            // 1. Obtener lista actual de favoritos (usamos first() para obtener el valor actual del Flow)
            // Nota: Necesitas que 'repository.allFavorites' sea accesible
            // Como Flow es asíncrono, un truco rápido es guardar una copia local en memoria
            // O hacer una consulta simple. Para esta práctica, usaremos un truco:
            // Si el usuario no ha buscado nada, asumimos que quiere ver recomendaciones.

            // Vamos a simularlo pidiendo al repositorio buscar algo basado en un género popular
            // o idealmente, basado en el último favorito guardado si tuviéramos esa lista en memoria.

            // Estrategia Simple y Efectiva:
            // Buscamos una serie popular fija como "semilla" si no hay datos complejos
            val seedQuery = "Star Wars" // O "Marvel", "DC", etc.

            val recommendations = repository.searchShows(seedQuery)
            _searchResults.postValue(recommendations)
        }
    }
}

// Boilerplate: Fábrica para poder pasarle el Repository al ViewModel
class MainViewModelFactory(private val repository: ShowRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}