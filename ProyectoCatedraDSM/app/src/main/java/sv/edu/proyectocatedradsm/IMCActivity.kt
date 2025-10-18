package sv.edu.proyectocatedradsm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class IMCActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imc)

        val etPeso = findViewById<EditText>(R.id.etPeso)
        val etAltura = findViewById<EditText>(R.id.etAltura)
        val btnCalcular = findViewById<Button>(R.id.btnCalcular)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)

        btnCalcular.setOnClickListener {
            val peso = etPeso.text.toString().toFloatOrNull()
            val altura = etAltura.text.toString().toFloatOrNull()

            if (peso == null || altura == null) {
                Toast.makeText(this, "Ingresa valores válidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imc = peso / (altura * altura)
            val resultado = when {
                imc < 18.5 -> "Bajo peso"
                imc < 24.9 -> "Normal"
                imc < 29.9 -> "Sobrepeso"
                else -> "Obesidad"
            }

            tvResultado.text = "Tu IMC es %.2f\n$resultado".format(imc)
        }

        // --- Menú inferior ---
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_imc

        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_imc -> true // ya estamos aquí
                R.id.nav_config -> {
                    Toast.makeText(this, "Configuración (en desarrollo)", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}