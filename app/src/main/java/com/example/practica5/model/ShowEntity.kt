package com.example.practica5.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. Esta es la tabla para la base de datos local (Room)
// La clave primaria ahora es la combinación de (id, userId)
@Entity(tableName = "shows_table", primaryKeys = ["id", "userId"])
data class ShowEntity(
    val id: Int, // ID de la serie
    val userId: Int, // ID del usuario dueño de este favorito <--- NUEVO
    val name: String,
    val imageUrl: String?,
    val summary: String?,
    var isFavorite: Boolean = false
)

// 2. Clases auxiliares para leer la respuesta de TVMaze (JSON)
// La API devuelve: [ { "show": { "id": 1, "name": "...", "image": {...} } } ]

data class TvMazeResponse(
    val show: TvMazeShow
)

data class TvMazeShow(
    val id: Int,
    val name: String,
    val summary: String?,
    val image: TvMazeImage?
)

data class TvMazeImage(
    val medium: String?,
    val original: String?
)

// 3. Clase para enviar datos a TU API (Node.js)
data class FavoriteRequest(
    val userId: Int,
    val showId: Int,
    val title: String,
    val image: String?
)

data class FavoriteResponse(
    val userId: Int,
    val showId: Int,
    val title: String
)