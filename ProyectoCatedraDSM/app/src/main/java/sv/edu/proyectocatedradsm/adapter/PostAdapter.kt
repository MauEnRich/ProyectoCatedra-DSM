package sv.edu.proyectocatedradsm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sv.edu.proyectocatedradsm.model.Post
import sv.edu.proyectocatedradsm.R
import java.text.SimpleDateFormat
import java.util.Locale

class PostAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAutor: TextView = itemView.findViewById(R.id.tvAutor)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvContenido: TextView = itemView.findViewById(R.id.tvContenido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.tvAutor.text = post.autorEmail
        holder.tvContenido.text = post.texto

        // Convertir Timestamp a fecha legible
        val date = post.fecha?.toDate()?.let {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
        } ?: "Fecha no disponible"

        holder.tvFecha.text = date
    }

    override fun getItemCount(): Int = posts.size
}
