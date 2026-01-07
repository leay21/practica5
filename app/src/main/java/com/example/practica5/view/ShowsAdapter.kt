package com.example.practica5.view

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.practica5.R
import com.example.practica5.model.ShowEntity

// Recibe una función 'onFavoriteClick' para saber qué hacer cuando pulsen el corazón
class ShowsAdapter(
    private val onFavoriteClick: (ShowEntity) -> Unit
) : ListAdapter<ShowEntity, ShowsAdapter.ShowViewHolder>(ShowDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_show, parent, false)
        return ShowViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShowViewHolder, position: Int) {
        val show = getItem(position)
        holder.bind(show, onFavoriteClick)
    }

    class ShowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSummary: TextView = itemView.findViewById(R.id.tvSummary)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)

        fun bind(show: ShowEntity, onFavoriteClick: (ShowEntity) -> Unit) {
            tvTitle.text = show.name

            // Limpiamos las etiquetas HTML que a veces trae la API (ej: <p>Resumen</p>)
            if (show.summary != null) {
                tvSummary.text = Html.fromHtml(show.summary, Html.FROM_HTML_MODE_COMPACT)
            } else {
                tvSummary.text = "Sin descripción disponible."
            }

            // Cargamos la imagen usando la librería Coil
            ivPoster.load(show.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery) // Imagen mientras carga
                error(android.R.drawable.stat_notify_error) // Imagen si falla
            }

            // Cambiamos el icono si es favorito o no
            if (show.isFavorite) {
                btnFavorite.setImageResource(android.R.drawable.btn_star_big_on)
            } else {
                btnFavorite.setImageResource(android.R.drawable.btn_star_big_off)
            }

            // Configurar el click del corazón
            btnFavorite.setOnClickListener {
                onFavoriteClick(show)
                // Truco visual: cambiar el icono inmediatamente para feedback rápido
                btnFavorite.setImageResource(android.R.drawable.btn_star_big_on)
            }
        }
    }

    // Clase auxiliar para calcular cambios en la lista eficientemente
    class ShowDiffCallback : DiffUtil.ItemCallback<ShowEntity>() {
        override fun areItemsTheSame(oldItem: ShowEntity, newItem: ShowEntity): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: ShowEntity, newItem: ShowEntity): Boolean {
            return oldItem == newItem
        }
    }
}