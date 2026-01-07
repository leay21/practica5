package com.example.practica5.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.practica5.model.ShowEntity
import kotlinx.coroutines.flow.Flow

// 1. El DAO (Data Access Object): Las instrucciones SQL
@Dao
interface ShowDao {
    // Obtener solo los favoritos (Flow actualiza la UI automáticamente si algo cambia)
    @Query("SELECT * FROM shows_table WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<ShowEntity>>

    // Insertar o Actualizar una serie
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShow(show: ShowEntity)

    // Borrar favorito (opcional)
    @Query("DELETE FROM shows_table WHERE id = :showId")
    suspend fun deleteFavorite(showId: Int)

    // Verificar si ya existe (útil para pintar el corazón de 'me gusta')
    @Query("SELECT EXISTS(SELECT * FROM shows_table WHERE id = :id AND isFavorite = 1)")
    suspend fun isFavorite(id: Int): Boolean
}

// 2. La Base de Datos
@Database(entities = [ShowEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tv_shows_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}