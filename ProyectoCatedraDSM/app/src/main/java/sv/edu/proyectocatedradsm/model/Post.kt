package sv.edu.proyectocatedradsm.model

import com.google.firebase.firestore.DocumentId

data class Post(
    @DocumentId
    var postId: String = "",
    val autorUid: String = "",
    val autorEmail: String = "",
    val texto: String = "",
    val fecha: com.google.firebase.Timestamp? = null
)