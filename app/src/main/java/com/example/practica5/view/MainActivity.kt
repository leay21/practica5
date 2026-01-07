package com.example.practica5.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.example.practica5.data.SessionManager
import com.example.practica5.data.ShowRepository

class MainActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnViewFavorites: Button
    private lateinit var rvShows: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ShowsAdapter

    private val viewModel: MainViewModel by viewModels {
        val db = AppDatabase.getDatabase(applicationContext)
        val repo = ShowRepository(db.showDao(), RetrofitClient.myApi, RetrofitClient.tvMazeApi)
        MainViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Chequeo de Sesión
        val session = SessionManager(this)
        if (!session.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        viewModel.setUserId(session.getUserId())
        supportActionBar?.title = "Hola, ${session.getUserName()}"

        setContentView(R.layout.activity_main)

        // 2. Setup UI
        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        btnViewFavorites = findViewById(R.id.btnViewFavorites)
        rvShows = findViewById(R.id.rvShows)
        progressBar = findViewById(R.id.progressBar)

        adapter = ShowsAdapter { show ->
            viewModel.addToFavorites(show)
            Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show()
        }
        rvShows.layoutManager = LinearLayoutManager(this)
        rvShows.adapter = adapter

        // --- OBSERVADORES ---

        // A. Observar cambios en Búsqueda/Recomendaciones
        viewModel.searchResults.observe(this) { shows ->
            if (viewModel.isShowingFavorites.value == false) {
                progressBar.visibility = View.GONE
                adapter.submitList(shows)
                if (shows.isEmpty()) Toast.makeText(this, "Sin resultados", Toast.LENGTH_SHORT).show()
            }
        }

        // B. Observar cambios en Favoritos (BD Local)
        viewModel.favorites.observe(this) { favs ->
            if (viewModel.isShowingFavorites.value == true) {
                progressBar.visibility = View.GONE
                adapter.submitList(favs)
            }
        }

        // C. Observar el MODO (Búsqueda vs Favoritos)
        viewModel.isShowingFavorites.observe(this) { isFavMode ->
            if (isFavMode) {
                btnViewFavorites.text = "Volver a Buscar"
                // Si hay favoritos, mostrarlos. Si no, lista vacía.
                val currentFavs = viewModel.favorites.value ?: emptyList()
                adapter.submitList(currentFavs)
            } else {
                btnViewFavorites.text = "Ver Mis Favoritos"
                // Si hay búsqueda previa, mostrarla. Si no, lista vacía.
                val currentSearch = viewModel.searchResults.value ?: emptyList()
                adapter.submitList(currentSearch)
            }
        }

        // --- BOTONES ---

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString()
            if (query.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                viewModel.search(query) // Pone isShowingFavorites = false automáticamente
            }
        }

        btnViewFavorites.setOnClickListener {
            if (viewModel.isShowingFavorites.value == true) {
                // CORREGIDO: Usamos la función del ViewModel en vez de postValue directo
                viewModel.showSearch()
            } else {
                etSearch.text.clear()
                viewModel.showFavorites()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val session = SessionManager(this)
        menu?.add(0, 1, 0, "Cerrar Sesión")

        // Menú dinámico según rol
        if (session.getUserRole() == "admin") {
            menu?.add(0, 2, 0, "Ver Historial Global (Admin)")
        } else {
            menu?.add(0, 2, 0, "Ver Mi Historial")
        }

        menu?.add(0, 3, 0, "🌟 Ver Recomendaciones")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                SessionManager(this).logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            2 -> startActivity(Intent(this, HistoryActivity::class.java))
            3 -> {
                Toast.makeText(this, "Cargando sugerencias...", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.VISIBLE
                etSearch.text.clear()
                viewModel.loadRecommendations() // Pone isShowingFavorites = false automáticamente
            }
        }
        return super.onOptionsItemSelected(item)
    }
}