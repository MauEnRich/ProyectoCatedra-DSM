package sv.edu.proyectocatedradsm

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import sv.edu.proyectocatedradsm.model.Post

class CreatePostActivity : AppCompatActivity() {

    private lateinit var etContenido: EditText
    private lateinit var btnPublicar: Button
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        etContenido = findViewById(R.id.etContenido)
        btnPublicar = findViewById(R.id.btnPublicar)

        btnPublicar.setOnClickListener {
            val texto = etContenido.text.toString().trim()
            if (texto.isEmpty()) {
                Toast.makeText(this, "Escribe algo antes de publicar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuario = FirebaseAuth.getInstance().currentUser
            if (usuario == null) {
                Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val post = Post(
                autorUid = usuario.uid,
                autorEmail = usuario.email ?: "Desconocido",
                texto = texto,
                fecha = Timestamp.now()
            )

            firestore.collection("posts")
                .add(post)
                .addOnSuccessListener {
                    Toast.makeText(this, "Post creado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show() }
        }
    }
}
