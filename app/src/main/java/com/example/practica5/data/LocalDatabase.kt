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
    @Query("SELECT * FROM shows_table WHERE isFavorite = 1 AND userId = :userId")
    fun getFavorites(userId: Int): Flow<List<ShowEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShow(show: ShowEntity)

    @Query("DELETE FROM shows_table WHERE id = :showId AND userId = :userId")
    suspend fun deleteFavorite(showId: Int, userId: Int)

    @Query("SELECT * FROM shows_table WHERE isFavorite = 1 AND userId = :userId LIMIT 1")
    suspend fun getOneFavorite(userId: Int): ShowEntity?

    @Query("SELECT EXISTS(SELECT * FROM shows_table WHERE id = :id AND userId = :userId AND isFavorite = 1)")
    suspend fun isFavorite(id: Int, userId: Int): Boolean
    // NUEVO: Obtener solo los IDs para hacer cruce de información rápido
    @Query("SELECT id FROM shows_table WHERE isFavorite = 1 AND userId = :userId")
    suspend fun getFavoriteIds(userId: Int): List<Int>
    // NUEVO: Obtener solo los nombres de los favoritos para usarlos como palabras clave
    @Query("SELECT name FROM shows_table WHERE isFavorite = 1 AND userId = :userId")
    suspend fun getFavoriteNames(userId: Int): List<String>
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