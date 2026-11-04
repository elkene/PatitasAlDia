package com.example.patitasaldia.ui.form

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.patitasaldia.R
import com.example.patitasaldia.databinding.ActivityFormMascotaBinding

/**
 * Formulario para agregar (sin id) o editar (con id) una mascota.
 *
 * Por ahora solo muestra la barra superior con el título correcto y la flecha
 * para regresar. Los campos, la validación y el guardado se hacen en la fase 3.
 */
class FormMascotaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormMascotaBinding

    /** Id de la mascota que se edita, o null si es una mascota nueva. */
    private var mascotaId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFormMascotaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        mascotaId = intent.getLongExtra(EXTRA_MASCOTA_ID, SIN_ID).takeIf { it != SIN_ID }

        binding.barraSuperior.setTitle(
            if (mascotaId == null) R.string.titulo_nueva_mascota else R.string.titulo_editar_mascota
        )
        binding.barraSuperior.setNavigationOnClickListener { finish() }
    }

    companion object {
        private const val EXTRA_MASCOTA_ID = "mascota_id"
        private const val SIN_ID = -1L

        /**
         * Crea el Intent para abrir el formulario. Sin mascotaId abre el modo
         * alta; con mascotaId abre el modo edición de esa mascota.
         */
        fun crearIntent(context: Context, mascotaId: Long? = null): Intent =
            Intent(context, FormMascotaActivity::class.java).apply {
                if (mascotaId != null) putExtra(EXTRA_MASCOTA_ID, mascotaId)
            }
    }
}
