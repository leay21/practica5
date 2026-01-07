package com.example.practica5.view

import android.content.Intent // Nuevo import
import android.os.Bundle
import android.view.Menu // Nuevo import
import android.view.MenuItem // Nuevo import
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
import com.example.practica5.data.SessionManager // Nuevo import
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

        // -----------------------------------------------------------
        // 1. (NUEVO) VERIFICAR SESIÓN ANTES DE CARGAR LA VISTA
        // -----------------------------------------------------------
        val session = SessionManager(this)
        if (!session.isLoggedIn()) {
            // Si no está logueado, mandar al Login y cerrar esta pantalla
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 2. (NUEVO) CONFIGURAR EL USUARIO EN EL VIEWMODEL
        // Recuperamos el ID guardado y se lo pasamos a la lógica
        viewModel.currentUserId = session.getUserId()

        // Opcional: Poner el nombre del usuario en la barra de arriba
        supportActionBar?.title = "Hola, ${session.getUserName()}"
        // -----------------------------------------------------------

        setContentView(R.layout.activity_main)

        // Vincular Vistas
        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        btnViewFavorites = findViewById(R.id.btnViewFavorites)
        rvShows = findViewById(R.id.rvShows)
        progressBar = findViewById(R.id.progressBar)

        // Configurar RecyclerView
        adapter = ShowsAdapter { showToAdd ->
            // Acción al dar click en favorito
            viewModel.addToFavorites(showToAdd)
            Toast.makeText(this, "${showToAdd.name} guardado y sincronizando...", Toast.LENGTH_SHORT).show()
        }
        rvShows.layoutManager = LinearLayoutManager(this)
        rvShows.adapter = adapter

        // OBSERVAR resultados de Búsqueda
        viewModel.searchResults.observe(this) { shows ->
            progressBar.visibility = View.GONE
            adapter.submitList(shows)
            if (shows.isEmpty()) {
                Toast.makeText(this, "No se encontraron resultados", Toast.LENGTH_SHORT).show()
            }
        }

        // OBSERVAR lista de Favoritos
        viewModel.favorites.observe(this) { favoriteShows ->
            // La lista se actualiza automáticamente gracias a Room
        }

        // Configurar Botón BUSCAR
        btnSearch.setOnClickListener {
            val query = etSearch.text.toString()
            if (query.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                viewModel.favorites.removeObservers(this)
                viewModel.searchResults.observe(this) { list ->
                    progressBar.visibility = View.GONE
                    adapter.submitList(list)
                }
                viewModel.search(query)
            }
        }

        // Configurar Botón VER FAVORITOS
        btnViewFavorites.setOnClickListener {
            etSearch.text.clear()
            viewModel.searchResults.removeObservers(this)
            viewModel.favorites.observe(this) { localFavorites ->
                adapter.submitList(localFavorites)
                if (localFavorites.isEmpty()) {
                    Toast.makeText(this, "Aún no tienes favoritos guardados localmente", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // -----------------------------------------------------------
    // 3. (NUEVO) MENU PARA CERRAR SESIÓN (LOGOUT)
    // -----------------------------------------------------------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Agregamos un botón "Salir" programáticamente a la barra superior
        menu?.add(0, 1, 0, "Cerrar Sesión")?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu?.add(0, 2, 0, "Ver Historial (Admin)")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) { // Si pulsaron nuestro botón "Cerrar Sesión"
            val session = SessionManager(this)
            session.logout() // Borrar datos

            // Volver al login
            val intent = Intent(this, LoginActivity::class.java)
            // Limpiar la pila de actividades para que no puedan volver atrás
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return true
        }
        if (item.itemId == 2) {
            startActivity(Intent(this, HistoryActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}