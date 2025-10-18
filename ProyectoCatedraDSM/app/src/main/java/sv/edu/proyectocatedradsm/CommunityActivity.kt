package sv.edu.proyectocatedradsm

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import sv.edu.proyectocatedradsm.adapter.PostAdapter
import sv.edu.proyectocatedradsm.model.Post

class CommunityActivity : AppCompatActivity() {

    private lateinit var rvPosts: RecyclerView
    private lateinit var etPost: EditText
    private lateinit var btnPublicar: Button
    private val firestore = FirebaseFirestore.getInstance()
    private val postsList = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        // Verificar usuario autenticado
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.d("CommunityActivity", "Usuario logueado: UID=${user.uid}, Email=${user.email}")
        } else {
            Log.d("CommunityActivity", "No hay usuario logueado")
        }

        // Inicializar vistas
        rvPosts = findViewById(R.id.rvPosts)
        etPost = findViewById(R.id.etPost)
        btnPublicar = findViewById(R.id.btnPublicar)

        // Configurar RecyclerView
        adapter = PostAdapter(postsList)
        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = adapter

        // Escuchar cambios en Firestore
        listenPosts()

        // Botón para crear post
        btnPublicar.setOnClickListener {
            val texto = etPost.text.toString().trim()
            if (texto.isEmpty()) {
                Toast.makeText(this, "Escribe algo antes de publicar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuario = FirebaseAuth.getInstance().currentUser
            if (usuario == null) {
                Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevoPost = Post(
                autorUid = usuario.uid,
                autorEmail = usuario.email ?: "Desconocido",
                texto = texto,
                fecha = Timestamp.now()
            )

            firestore.collection("posts")
                .add(nuevoPost)
                .addOnSuccessListener {
                    Toast.makeText(this, "Post publicado", Toast.LENGTH_SHORT).show()
                    etPost.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al publicar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun listenPosts() {
        firestore.collection("posts")
            .orderBy("fecha") // opcional, ordenar por fecha
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("CommunityActivity", "Error al leer Firestore: ${e.message}")
                    Toast.makeText(
                        this,
                        "Error al conectar con Firestore: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    Log.d("CommunityActivity", "Se recibieron ${snapshots.size()} documentos")
                    postsList.clear()

                    for (doc in snapshots.documents) {
                        val post = doc.toObject(Post::class.java)
                        if (post != null) {
                            postsList.add(post)
                            Log.d("CommunityActivity", "Documento: ${doc.id} -> $post")
                        } else {
                            Log.w("CommunityActivity", "Documento ${doc.id} es nulo o mal formateado")
                        }
                    }

                    adapter.notifyDataSetChanged()
                }
            }
    }
}
