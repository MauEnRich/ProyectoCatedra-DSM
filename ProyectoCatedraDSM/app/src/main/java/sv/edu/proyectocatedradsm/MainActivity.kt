package sv.edu.proyectocatedradsm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sv.edu.proyectocatedradsm.api.ProductResponse
import sv.edu.proyectocatedradsm.api.RetrofitInstance
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var btnSearchFood: Button
    private lateinit var etFoodName: EditText
    private lateinit var tvResult: TextView
    private lateinit var ivPreview: ImageView
    private lateinit var previewView: PreviewView
    private lateinit var btnTakePhoto: Button

    private var imageCapture: ImageCapture? = null
    private val CAMERA_PERMISSION_CODE = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_imc -> {
                    startActivity(android.content.Intent(this, IMCActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_community -> {
                    startActivity(Intent(this, CommunityActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_logout -> { // CASO DE LOGOUT
                    showLogoutConfirmation()
                    false
                }
                else -> false
            }
        }

        btnSearchFood = findViewById(R.id.btnSearchFood)
        etFoodName = findViewById(R.id.etFoodName)
        tvResult = findViewById(R.id.tvResult)
        ivPreview = findViewById(R.id.ivPreview)
        previewView = findViewById(R.id.previewView)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            startCamera()
        }

        btnSearchFood.setOnClickListener {
            val name = etFoodName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Escribe un alimento", Toast.LENGTH_SHORT).show()
            } else {
                searchFood(name)
            }
        }

        btnTakePhoto.setOnClickListener {
            takePhoto()
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


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }


    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(cacheDir, "temp_photo.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver,
                        output.savedUri ?: return
                    )
                    ivPreview.setImageBitmap(bitmap)
                    analyzeBitmap(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    tvResult.text = "Error al tomar la foto: ${exception.message}"
                }
            }
        )
    }


    private fun analyzeBitmap(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        labeler.process(image)
            .addOnSuccessListener { labels: List<ImageLabel> ->
                if (labels.isNotEmpty()) {
                    val topLabel = labels.maxByOrNull { it.confidence }
                    val detectedFood = topLabel?.text
                    if (!detectedFood.isNullOrEmpty()) {
                        tvResult.text = "Detectado: $detectedFood\nBuscando información..."
                        searchFood(detectedFood)
                    } else {
                        tvResult.text = "No se detectó ningún alimento"
                    }
                } else {
                    tvResult.text = "No se detectó ningún alimento"
                }
            }
            .addOnFailureListener {
                tvResult.text = "Error al analizar la imagen"
            }
    }


    private fun searchFood(name: String) {
        val call: Call<ProductResponse> = RetrofitInstance.api.searchFood(name, size = 10)
        call.enqueue(object : Callback<ProductResponse> {
            override fun onResponse(
                call: Call<ProductResponse>,
                response: Response<ProductResponse>
            ) {
                if (response.isSuccessful) {
                    val product = response.body()?.products?.firstOrNull {
                        it.product_name?.contains(name, ignoreCase = true) == true
                    }

                    if (product != null) {
                        val kcal = product.nutriments?.energy_kcal_100g ?: 0f
                        val sugar = product.nutriments?.sugars_100g ?: 0f
                        val fat = product.nutriments?.fat_100g ?: 0f

                        val saludable = when {
                            kcal < 150 && sugar < 5 && fat < 5 -> "✅ Saludable"
                            kcal > 400 || sugar > 10 || fat > 15 -> "⚠️ No muy saludable"
                            else -> "⚖️ Moderado"
                        }

                        tvResult.text = """
                    🥗 ${product.product_name}

                   Energía: ${kcal} kcal
                   Azúcar: ${sugar} g
                   Grasas: ${fat} g

                  Resultado: $saludable
                  """.trimIndent()

                        Glide.with(this@MainActivity)
                            .load(product.image_url)
                            .into(ivPreview)
                    } else {
                        tvResult.text = "No se encontró información del alimento."
                    }
                } else {
                    tvResult.text = "Error en la respuesta de la API."
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                tvResult.text = "Error al conectar con la API: ${t.message}"
            }
        })
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}