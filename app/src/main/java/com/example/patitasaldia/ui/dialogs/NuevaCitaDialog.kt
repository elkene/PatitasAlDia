package com.example.patitasaldia.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.patitasaldia.R
import com.example.patitasaldia.databinding.DialogCitaBinding
import com.example.patitasaldia.model.Cita
import com.example.patitasaldia.util.FechaUtils
import com.example.patitasaldia.util.Selectores
import com.example.patitasaldia.util.mostrarBorrarSoloConTexto
import com.example.patitasaldia.viewmodel.DetalleMascotaViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Diálogo para agregar (sin cita) o editar (con cita) una cita al veterinario.
 * Funciona igual que NuevaVacunaDialog.
 */
class NuevaCitaDialog : DialogFragment() {

    private val viewModel: DetalleMascotaViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private var _binding: DialogCitaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCitaBinding.inflate(layoutInflater)
        val argumentos = requireArguments()
        val esEdicion = argumentos.getLong(ARG_ID) != 0L

        if (savedInstanceState == null) {
            binding.etFecha.setText(
                FechaUtils.bdAPantalla(argumentos.getString(ARG_FECHA) ?: FechaUtils.hoy())
            )
            binding.etHora.setText(argumentos.getString(ARG_HORA))
            binding.etMotivo.setText(argumentos.getString(ARG_MOTIVO))
            binding.etNotas.setText(argumentos.getString(ARG_NOTAS))
        }
        configurarSelectores()

        val dialogo = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (esEdicion) R.string.titulo_editar_cita else R.string.titulo_nueva_cita)
            .setView(binding.root)
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

    private fun configurarSelectores() {
        val elegirFecha = {
            Selectores.mostrarFecha(
                childFragmentManager, ETIQUETA_FECHA, R.string.campo_fecha,
                FechaUtils.pantallaABd(binding.etFecha.text?.toString()),
                soloPasado = false, alElegir = ::ponerFecha
            )
        }
        binding.etFecha.setOnClickListener { elegirFecha() }
        binding.tilFecha.setStartIconOnClickListener { elegirFecha() }

        val elegirHora = {
            Selectores.mostrarHora(
                childFragmentManager, ETIQUETA_HORA, R.string.campo_hora,
                binding.etHora.text?.toString()?.ifBlank { null }, alElegir = ::ponerHora
            )
        }
        binding.etHora.setOnClickListener { elegirHora() }
        binding.tilHora.setStartIconOnClickListener { elegirHora() }
        binding.tilHora.setEndIconOnClickListener { binding.etHora.setText("") }
        binding.tilHora.mostrarBorrarSoloConTexto()

        Selectores.reconectarFecha(childFragmentManager, ETIQUETA_FECHA, ::ponerFecha)
        Selectores.reconectarHora(childFragmentManager, ETIQUETA_HORA, ::ponerHora)
    }

    private fun ponerFecha(fechaBd: String) {
        _binding?.etFecha?.setText(FechaUtils.bdAPantalla(fechaBd))
    }

    private fun ponerHora(hora: String) {
        _binding?.etHora?.setText(hora)
    }

    /** Valida y guarda. Regresa false si hay errores (el diálogo sigue abierto). */
    private fun guardar(): Boolean {
        binding.tilFecha.error = null
        binding.tilMotivo.error = null

        val fecha = FechaUtils.pantallaABd(binding.etFecha.text?.toString())
        val motivo = binding.etMotivo.text.toString().trim()
        var valido = true

        if (fecha == null) {
            binding.tilFecha.error = getString(R.string.error_fecha_obligatoria)
            valido = false
        }
        if (motivo.isEmpty()) {
            binding.tilMotivo.error = getString(R.string.error_motivo)
            valido = false
        }
        if (!valido || fecha == null) return false

        val argumentos = requireArguments()
        viewModel.guardarCita(
            Cita(
                id = argumentos.getLong(ARG_ID),
                mascotaId = argumentos.getLong(ARG_MASCOTA_ID),
                fecha = fecha,
                hora = binding.etHora.text?.toString()?.ifBlank { null },
                motivo = motivo,
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
        const val ETIQUETA = "dialogo_cita"
        private const val ETIQUETA_FECHA = "selector_fecha_cita"
        private const val ETIQUETA_HORA = "selector_hora_cita"

        private const val ARG_ID = "id"
        private const val ARG_MASCOTA_ID = "mascota_id"
        private const val ARG_FECHA = "fecha"
        private const val ARG_HORA = "hora"
        private const val ARG_MOTIVO = "motivo"
        private const val ARG_NOTAS = "notas"

        /** Diálogo para agregar una cita nueva a la mascota. */
        fun nueva(mascotaId: Long) = NuevaCitaDialog().apply {
            arguments = bundleOf(ARG_ID to 0L, ARG_MASCOTA_ID to mascotaId)
        }

        /** Diálogo para editar una cita existente. */
        fun editar(cita: Cita) = NuevaCitaDialog().apply {
            arguments = bundleOf(
                ARG_ID to cita.id,
                ARG_MASCOTA_ID to cita.mascotaId,
                ARG_FECHA to cita.fecha,
                ARG_HORA to cita.hora,
                ARG_MOTIVO to cita.motivo,
                ARG_NOTAS to cita.notas
            )
        }
    }
}
