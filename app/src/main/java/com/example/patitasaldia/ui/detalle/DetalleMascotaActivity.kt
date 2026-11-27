package com.example.patitasaldia.ui.detalle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.patitasaldia.R
import com.example.patitasaldia.databinding.ActivityDetalleMascotaBinding
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.util.aplicarMargenesDelSistema

/**
 * Pantalla de detalle, solo en teléfono. Contiene DetalleMascotaFragment.
 * En la tableta el mismo fragmento se muestra dentro de MainActivity.
 *
 * El id de la mascota viaja en el Intent; Android lo conserva al rotar y
 * al recrear la actividad, así que no hace falta guardarlo a mano.
 */
class DetalleMascotaActivity : AppCompatActivity(), DetalleMascotaFragment.Listener {

    private lateinit var binding: ActivityDetalleMascotaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetalleMascotaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.main.aplicarMargenesDelSistema()

        // La barra superior funciona como ActionBar para que el fragmento agregue sus acciones
        setSupportActionBar(binding.barraSuperior)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeActionContentDescription(R.string.accion_regresar)
        title = ""

        // Solo la primera vez: al rotar, Android restaura el fragmento que ya estaba
        if (savedInstanceState == null) {
            val mascotaId = intent.getLongExtra(EXTRA_MASCOTA_ID, 0L)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.contenedor_fragmento, DetalleMascotaFragment.newInstance(mascotaId))
            }
        }
    }

    /** Flecha de regresar de la barra superior. */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onMascotaCargada(mascota: Mascota) {
        title = mascota.nombre
    }

    /** Si la mascota se eliminó, se regresa a la lista. */
    override fun onMascotaEliminada() {
        finish()
    }

    companion object {
        private const val EXTRA_MASCOTA_ID = "mascota_id"

        fun crearIntent(context: Context, mascotaId: Long): Intent =
            Intent(context, DetalleMascotaActivity::class.java).putExtra(EXTRA_MASCOTA_ID, mascotaId)
    }
}
