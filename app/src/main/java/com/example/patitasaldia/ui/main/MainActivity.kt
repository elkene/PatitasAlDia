package com.example.patitasaldia.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.example.patitasaldia.R
import com.example.patitasaldia.databinding.ActivityMainBinding
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.ui.detalle.DetalleMascotaActivity
import com.example.patitasaldia.ui.detalle.DetalleMascotaFragment
import com.example.patitasaldia.ui.form.FormMascotaActivity
import com.example.patitasaldia.util.aplicarMargenesDelSistema
import com.example.patitasaldia.viewmodel.MascotasViewModel

/**
 * Pantalla principal. Contiene ListaMascotasFragment y el botón flotante
 * para agregar una mascota.
 *
 * Teléfono o tableta (requisito 3): Android elige layout/activity_main.xml o
 * layout-sw600dp/activity_main.xml según el tamaño de la pantalla. Solo el de
 * tableta tiene el contenedor del detalle, así que basta con revisar si existe.
 */
class MainActivity :
    AppCompatActivity(),
    ListaMascotasFragment.AlSeleccionarMascota,
    DetalleMascotaFragment.Listener {

    private lateinit var binding: ActivityMainBinding

    // El mismo ViewModel que usa ListaMascotasFragment
    private val viewModel: MascotasViewModel by viewModels()

    /** true en tableta: hay un panel derecho para el detalle. */
    private val hayDosPaneles: Boolean get() = binding.contenedorDetalle != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.main.aplicarMargenesDelSistema()

        // La barra superior funciona como ActionBar para que el detalle agregue Editar y Eliminar
        setSupportActionBar(binding.barraSuperior)

        binding.fabAgregar.setOnClickListener {
            startActivity(FormMascotaActivity.crearIntent(this))
        }

        if (hayDosPaneles) {
            restaurarDetalleEnTableta()
        } else {
            quitarDetalleDeTableta()
        }
    }

    /**
     * Tableta: si había una mascota seleccionada pero su fragmento no existe
     * (por ejemplo, Android cerró la app), se vuelve a mostrar.
     */
    private fun restaurarDetalleEnTableta() {
        val id = viewModel.seleccionadaId.value
        if (id != null && fragmentoDetalle() == null) {
            mostrarDetalle(id)
        }
        actualizarMensajeSeleccion()
    }

    /**
     * Teléfono: si la pantalla venía de la versión de tableta (pantalla
     * dividida, cambio de tamaño), Android intentaría restaurar el detalle en
     * un contenedor que ya no existe. Se quita antes de que eso pase.
     */
    private fun quitarDetalleDeTableta() {
        fragmentoDetalle()?.let { detalle ->
            supportFragmentManager.commitNow { remove(detalle) }
        }
        viewModel.seleccionar(null)
    }

    override fun onMascotaSeleccionada(mascota: Mascota) {
        if (hayDosPaneles) {
            viewModel.seleccionar(mascota.id)
            mostrarDetalle(mascota.id)
        } else {
            startActivity(DetalleMascotaActivity.crearIntent(this, mascota.id))
        }
    }

    /** Pone el detalle de la mascota en el panel derecho (solo tableta). */
    private fun mostrarDetalle(mascotaId: Long) {
        if (fragmentoDetalle()?.mascotaId == mascotaId) return
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.contenedor_detalle, DetalleMascotaFragment.newInstance(mascotaId), ETIQUETA_DETALLE)
        }
        binding.txtSeleccionaMascota?.isVisible = false
    }

    /** La mascota del panel derecho se eliminó: se limpia el panel para no mostrar datos viejos. */
    override fun onMascotaEliminada() {
        fragmentoDetalle()?.let { detalle -> supportFragmentManager.commit { remove(detalle) } }
        viewModel.seleccionar(null)
        viewModel.cargarMascotas()
        binding.txtSeleccionaMascota?.isVisible = true
    }

    private fun actualizarMensajeSeleccion() {
        binding.txtSeleccionaMascota?.isVisible = viewModel.seleccionadaId.value == null
    }

    private fun fragmentoDetalle(): DetalleMascotaFragment? =
        supportFragmentManager.findFragmentByTag(ETIQUETA_DETALLE) as? DetalleMascotaFragment

    private companion object {
        const val ETIQUETA_DETALLE = "detalle_mascota"
    }
}
