package com.example.practica5.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practica5.R
import com.example.practica5.data.AppDatabase
import com.example.practica5.data.RetrofitClient
import com.example.practica5.data.ShowRepository

class MainActivity : AppCompatActivity() {

    // Referencias a la UI
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnViewFavorites: Button
    private lateinit var rvShows: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ShowsAdapter

    // Inicialización perezosa del ViewModel
    private val viewModel: MainViewModel by viewModels {
        // Construimos las dependencias manualmente (Inyección de Dependencias manual)
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ShowRepository(
            database.showDao(),
            RetrofitClient.myApi,
            RetrofitClient.tvMazeApi
        )
        MainViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Vincular Vistas
        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        btnViewFavorites = findViewById(R.id.btnViewFavorites)
        rvShows = findViewById(R.id.rvShows)
        progressBar = findViewById(R.id.progressBar)

        // 2. Configurar RecyclerView
        adapter = ShowsAdapter { showToAdd ->
            // Acción al dar click en favorito
            viewModel.addToFavorites(showToAdd)
            Toast.makeText(this, "${showToAdd.name} guardado y sincronizando...", Toast.LENGTH_SHORT).show()
        }
        rvShows.layoutManager = LinearLayoutManager(this)
        rvShows.adapter = adapter

        // 3. OBSERVAR los resultados de Búsqueda (API Pública)
        viewModel.searchResults.observe(this) { shows ->
            progressBar.visibility = View.GONE
            adapter.submitList(shows)
            if (shows.isEmpty()) {
                Toast.makeText(this, "No se encontraron resultados", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. OBSERVAR la lista de Favoritos (Base de Datos Local)
        // Nota: Esto se actualizará solo si decidimos mostrar favoritos
        viewModel.favorites.observe(this) { favoriteShows ->
            // Solo actualizamos la lista si el usuario pidió ver favoritos
            // (Para simplificar, usaremos un flag o lógica de botones simple)
        }

        // 5. Configurar Botón BUSCAR
        btnSearch.setOnClickListener {
            val query = etSearch.text.toString()
            if (query.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                // Dejamos de observar favoritos y observamos búsqueda
                viewModel.favorites.removeObservers(this)
                viewModel.searchResults.observe(this) { list ->
                    progressBar.visibility = View.GONE
                    adapter.submitList(list)
                }

                viewModel.search(query)
                // Ocultar teclado (opcional, buena práctica)
            }
        }

        // 6. Configurar Botón VER FAVORITOS
        btnViewFavorites.setOnClickListener {
            etSearch.text.clear()
            // Dejamos de observar búsqueda y observamos favoritos
            viewModel.searchResults.removeObservers(this)

            viewModel.favorites.observe(this) { localFavorites ->
                adapter.submitList(localFavorites)
                if (localFavorites.isEmpty()) {
                    Toast.makeText(this, "Aún no tienes favoritos guardados localmente", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}