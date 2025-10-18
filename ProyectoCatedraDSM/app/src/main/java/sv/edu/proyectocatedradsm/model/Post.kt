package sv.edu.proyectocatedradsm.model

data class Post(
    val autorUid: String = "",
    val autorEmail: String = "",
    val texto: String = "",
    val fecha: com.google.firebase.Timestamp? = null
)
