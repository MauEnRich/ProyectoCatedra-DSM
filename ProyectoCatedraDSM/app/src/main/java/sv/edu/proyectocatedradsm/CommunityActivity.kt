package sv.edu.proyectocatedradsm

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import sv.edu.proyectocatedradsm.adapter.PostAdapter
import sv.edu.proyectocatedradsm.adapter.PostActionListener
import sv.edu.proyectocatedradsm.model.Post

class CommunityActivity : AppCompatActivity(), PostActionListener {

    private lateinit var rvPosts: RecyclerView
    private lateinit var btnNuevoPost: Button
    private val firestore = FirebaseFirestore.getInstance()
    private val postsList = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        rvPosts = findViewById(R.id.rvPosts)
        btnNuevoPost = findViewById(R.id.btnNuevoPost)


        adapter = PostAdapter(postsList, this)
        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = adapter

        listenPosts()


        btnNuevoPost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_community

        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            return@setOnItemSelectedListener when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_imc -> {
                    startActivity(Intent(this, IMCActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_community -> true
                R.id.nav_logout -> {
                    showLogoutConfirmation()
                    false
                }
                else -> false
            }
        }
    }

    // --- Implementación de la Interfaz con Diálogo de Confirmación para Borrar ---
    override fun onDeletePost(postId: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar este post de la comunidad?")
            .setPositiveButton("Sí, Eliminar") { dialog, which ->
                deletePostFromFirestore(postId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun deletePostFromFirestore(postId: String) {
        firestore.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Post eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar post.", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar la sesión actual?")
            .setPositiveButton("Sí, Cerrar") { dialog, which ->
                logout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show()
    }

    private fun listenPosts() {
        firestore.collection("posts")
            .orderBy("fecha")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error al conectar con Firestore", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    postsList.clear()
                    for (doc in snapshots.documents) {
                        val post = doc.toObject(Post::class.java)
                        if (post != null) postsList.add(post)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}