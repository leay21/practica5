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

    // 3. Buscar Serie
    fun search(query: String) {
        _isShowingFavorites.value = false // Cambiamos a modo búsqueda

        viewModelScope.launch {
            if (query.isNotEmpty()) {
                if (currentUserId != -1) {
                    launch { repository.saveSearchHistory(currentUserId, query) }
                }
                val results = repository.searchShows(query)
                _searchResults.postValue(results)
            }
        }
    }

    // 4. Recomendaciones
    fun loadRecommendations() {
        _isShowingFavorites.value = false // Cambiamos a modo búsqueda

        viewModelScope.launch {
            if (currentUserId == -1) return@launch

            val favorite = repository.getAnyFavorite(currentUserId)
            val seedQuery = favorite?.name ?: "Star Wars"

            val results = repository.searchShows(seedQuery)
            _searchResults.postValue(results)
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