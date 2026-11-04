package com.example.patitasaldia.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.patitasaldia.databinding.ActivityMainBinding
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.ui.form.FormMascotaActivity

/**
 * Pantalla principal. Contiene ListaMascotasFragment y el botón flotante
 * para agregar una mascota.
 */
class MainActivity : AppCompatActivity(), ListaMascotasFragment.AlSeleccionarMascota {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Deja espacio para las barras del sistema (estado y navegación)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        binding.fabAgregar.setOnClickListener {
            startActivity(FormMascotaActivity.crearIntent(this))
        }
    }

    override fun onMascotaSeleccionada(mascota: Mascota) {
        // Fase 4: abrir DetalleMascotaActivity en teléfono.
        // Fase 5: mostrar DetalleMascotaFragment en el panel derecho en tableta.
    }
}
