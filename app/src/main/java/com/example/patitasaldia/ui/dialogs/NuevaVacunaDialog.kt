package com.example.patitasaldia.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.patitasaldia.R
import com.example.patitasaldia.databinding.DialogVacunaBinding
import com.example.patitasaldia.model.Vacuna
import com.example.patitasaldia.util.FechaUtils
import com.example.patitasaldia.util.Selectores
import com.example.patitasaldia.util.mostrarBorrarSoloConTexto
import com.example.patitasaldia.viewmodel.DetalleMascotaViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Diálogo para agregar (sin vacuna) o editar (con vacuna) una vacuna.
 *
 * Se muestra con show(childFragmentManager, ETIQUETA) desde DetalleMascotaFragment,
 * así que Android lo vuelve a crear solo al rotar. Los campos tienen id, por lo
 * que su texto también se conserva. Para guardar usa el ViewModel del fragmento padre.
 */
class NuevaVacunaDialog : DialogFragment() {

    private val viewModel: DetalleMascotaViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private var _binding: DialogVacunaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogVacunaBinding.inflate(layoutInflater)
        val argumentos = requireArguments()
        val esEdicion = argumentos.getLong(ARG_ID) != 0L

        if (savedInstanceState == null) {
            binding.etNombre.setText(argumentos.getString(ARG_NOMBRE))
            binding.etFechaAplicacion.setText(
                FechaUtils.bdAPantalla(argumentos.getString(ARG_FECHA_APLICACION) ?: FechaUtils.hoy())
            )
            binding.etProximaDosis.setText(FechaUtils.bdAPantalla(argumentos.getString(ARG_PROXIMA_DOSIS)))
            binding.etNotas.setText(argumentos.getString(ARG_NOTAS))
        }
        configurarFechas()

        val dialogo = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (esEdicion) R.string.titulo_editar_vacuna else R.string.titulo_nueva_vacuna)
            .setView(binding.root)
            // El listener se pone después para que el diálogo no se cierre si hay errores
            .setPositiveButton(R.string.accion_guardar, null)
            .setNegativeButton(R.string.accion_cancelar, null)
            .create()
        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (guardar()) dismiss()
            }
        }
        return dialogo
    }

    private fun configurarFechas() {
        val elegirAplicacion = {
            Selectores.mostrarFecha(
                childFragmentManager, ETIQUETA_APLICACION, R.string.campo_fecha_aplicacion,
                FechaUtils.pantallaABd(binding.etFechaAplicacion.text?.toString()),
                soloPasado = true, alElegir = ::ponerFechaAplicacion
            )
        }
        binding.etFechaAplicacion.setOnClickListener { elegirAplicacion() }
        binding.tilFechaAplicacion.setStartIconOnClickListener { elegirAplicacion() }

        val elegirProxima = {
            Selectores.mostrarFecha(
                childFragmentManager, ETIQUETA_PROXIMA, R.string.campo_proxima_dosis,
                FechaUtils.pantallaABd(binding.etProximaDosis.text?.toString()),
                soloPasado = false, alElegir = ::ponerProximaDosis
            )
        }
        binding.etProximaDosis.setOnClickListener { elegirProxima() }
        binding.tilProximaDosis.setStartIconOnClickListener { elegirProxima() }
        binding.tilProximaDosis.setEndIconOnClickListener { binding.etProximaDosis.setText("") }
        binding.tilProximaDosis.mostrarBorrarSoloConTexto()

        Selectores.reconectarFecha(childFragmentManager, ETIQUETA_APLICACION, ::ponerFechaAplicacion)
        Selectores.reconectarFecha(childFragmentManager, ETIQUETA_PROXIMA, ::ponerProximaDosis)
    }

    private fun ponerFechaAplicacion(fechaBd: String) {
        _binding?.etFechaAplicacion?.setText(FechaUtils.bdAPantalla(fechaBd))
    }

    private fun ponerProximaDosis(fechaBd: String) {
        _binding?.etProximaDosis?.setText(FechaUtils.bdAPantalla(fechaBd))
    }

    /** Valida y guarda. Regresa false si hay errores (el diálogo sigue abierto). */
    private fun guardar(): Boolean {
        binding.tilNombre.error = null
        binding.tilFechaAplicacion.error = null
        binding.tilProximaDosis.error = null

        val nombre = binding.etNombre.text.toString().trim()
        val aplicacion = FechaUtils.pantallaABd(binding.etFechaAplicacion.text?.toString())
        val proxima = FechaUtils.pantallaABd(binding.etProximaDosis.text?.toString())
        var valido = true

        if (nombre.isEmpty()) {
            binding.tilNombre.error = getString(R.string.error_vacuna_nombre)
            valido = false
        }
        if (aplicacion == null) {
            binding.tilFechaAplicacion.error = getString(R.string.error_fecha_obligatoria)
            valido = false
        }
        // Las fechas yyyy-MM-dd se pueden comparar como texto
        if (aplicacion != null && proxima != null && proxima <= aplicacion) {
            binding.tilProximaDosis.error = getString(R.string.error_proxima_dosis)
            valido = false
        }
        if (!valido || aplicacion == null) return false

        val argumentos = requireArguments()
        viewModel.guardarVacuna(
            Vacuna(
                id = argumentos.getLong(ARG_ID),
                mascotaId = argumentos.getLong(ARG_MASCOTA_ID),
                nombre = nombre,
                fechaAplicacion = aplicacion,
                proximaDosis = proxima,
                notas = binding.etNotas.text.toString().trim().ifEmpty { null }
            )
        )
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val ETIQUETA = "dialogo_vacuna"
        private const val ETIQUETA_APLICACION = "selector_fecha_aplicacion"
        private const val ETIQUETA_PROXIMA = "selector_proxima_dosis"

        private const val ARG_ID = "id"
        private const val ARG_MASCOTA_ID = "mascota_id"
        private const val ARG_NOMBRE = "nombre"
        private const val ARG_FECHA_APLICACION = "fecha_aplicacion"
        private const val ARG_PROXIMA_DOSIS = "proxima_dosis"
        private const val ARG_NOTAS = "notas"

        /** Diálogo para agregar una vacuna nueva a la mascota. */
        fun nueva(mascotaId: Long) = NuevaVacunaDialog().apply {
            arguments = bundleOf(ARG_ID to 0L, ARG_MASCOTA_ID to mascotaId)
        }

        /** Diálogo para editar una vacuna existente. */
        fun editar(vacuna: Vacuna) = NuevaVacunaDialog().apply {
            arguments = bundleOf(
                ARG_ID to vacuna.id,
                ARG_MASCOTA_ID to vacuna.mascotaId,
                ARG_NOMBRE to vacuna.nombre,
                ARG_FECHA_APLICACION to vacuna.fechaAplicacion,
                ARG_PROXIMA_DOSIS to vacuna.proximaDosis,
                ARG_NOTAS to vacuna.notas
            )
        }
    }
}
