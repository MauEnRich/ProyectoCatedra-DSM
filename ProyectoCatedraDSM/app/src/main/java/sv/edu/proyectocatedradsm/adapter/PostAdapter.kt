package sv.edu.proyectocatedradsm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import sv.edu.proyectocatedradsm.model.Post
import sv.edu.proyectocatedradsm.R
import java.text.SimpleDateFormat
import java.util.Locale


interface PostActionListener {
    fun onDeletePost(postId: String)
}

class PostAdapter(
    private val posts: List<Post>,
    private val listener: PostActionListener // <--- NUEVO: Recibimos el listener
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Obtener UID actual

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAutor: TextView = itemView.findViewById(R.id.tvAutor)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvContenido: TextView = itemView.findViewById(R.id.tvContenido)
        val btnDeletePost: ImageButton = itemView.findViewById(R.id.btnDeletePost) // <--- NUEVO
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

        val date = post.fecha?.toDate()?.let {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
        } ?: "Fecha no disponible"
        holder.tvFecha.text = date

        
        if (post.autorUid == currentUserId) {
            holder.btnDeletePost.visibility = View.VISIBLE
            holder.btnDeletePost.setOnClickListener {
                listener.onDeletePost(post.postId) // Llama a la actividad para borrar
            }
        } else {
            holder.btnDeletePost.visibility = View.GONE
            holder.btnDeletePost.setOnClickListener(null) // Limpiar el listener
        }
    }

    override fun getItemCount(): Int = posts.size
}