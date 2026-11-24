package com.example.patitasaldia.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.patitasaldia.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Diálogo de confirmación antes de eliminar una mascota, una vacuna o una cita.
 *
 * Es un DialogFragment (y no un AlertDialog suelto) para que siga abierto
 * después de rotar la pantalla. La respuesta se entrega con la Fragment Result
 * API: el fragmento que lo abrió escucha CLAVE_RESULTADO y recibe el tipo y el id.
 */
class ConfirmarEliminarDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val argumentos = requireArguments()
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.titulo_confirmar_eliminar)
            .setMessage(argumentos.getString(ARG_MENSAJE))
            .setPositiveButton(R.string.accion_eliminar) { _, _ ->
                setFragmentResult(
                    CLAVE_RESULTADO,
                    bundleOf(
                        RESULTADO_TIPO to argumentos.getString(ARG_TIPO),
                        RESULTADO_ID to argumentos.getLong(ARG_ID)
                    )
                )
            }
            .setNegativeButton(R.string.accion_cancelar, null)
            .create()
    }

    companion object {
        const val ETIQUETA = "confirmar_eliminar"
        const val CLAVE_RESULTADO = "resultado_confirmar_eliminar"
        const val RESULTADO_TIPO = "tipo"
        const val RESULTADO_ID = "id"

        const val TIPO_MASCOTA = "mascota"
        const val TIPO_VACUNA = "vacuna"
        const val TIPO_CITA = "cita"

        private const val ARG_MENSAJE = "mensaje"
        private const val ARG_TIPO = "tipo"
        private const val ARG_ID = "id"

        fun nuevo(mensaje: String, tipo: String, id: Long) = ConfirmarEliminarDialog().apply {
            arguments = bundleOf(ARG_MENSAJE to mensaje, ARG_TIPO to tipo, ARG_ID to id)
        }
    }
}
