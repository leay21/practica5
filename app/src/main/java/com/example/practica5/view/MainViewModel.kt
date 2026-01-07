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
                val results = repository.searchShows(query)
                // Verificamos cuáles de estos resultados YA son favoritos en nuestra BD
                // Esto es un detalle de calidad (pintar el corazón si ya existe)
                // Nota: Para hacerlo perfecto, deberíamos cruzar datos con la BD local,
                // por ahora mostraremos los resultados tal cual vienen de la API.
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