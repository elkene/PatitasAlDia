package com.example.patitasaldia.ui.detalle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.patitasaldia.R
import com.example.patitasaldia.databinding.ItemCitaBinding
import com.example.patitasaldia.model.Cita
import com.example.patitasaldia.util.FechaUtils

/**
 * Lista de citas. El detalle usa dos: una para las próximas y otra para las
 * pasadas (que se muestran atenuadas).
 *
 * @param esDePasadas si es true, las filas se ven más tenues.
 * @param alTocar abre la cita para editarla.
 * @param alEliminar pide confirmación para eliminarla (botón de basura o pulsación larga).
 */
class CitaAdapter(
    private val esDePasadas: Boolean,
    private val alTocar: (Cita) -> Unit,
    private val alEliminar: (Cita) -> Unit
) : ListAdapter<Cita, CitaAdapter.CitaViewHolder>(ComparadorCitas) {

    class CitaViewHolder(val binding: ItemCitaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder =
        CitaViewHolder(ItemCitaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = getItem(position)
        val contexto = holder.itemView.context
        with(holder.binding) {
            val fecha = FechaUtils.bdAPantalla(cita.fecha) ?: cita.fecha
            txtFechaHora.text = if (cita.hora != null) {
                contexto.getString(R.string.cita_fecha_hora, fecha, cita.hora)
            } else {
                fecha
            }
            txtMotivo.text = cita.motivo
            txtNotas.isVisible = !cita.notas.isNullOrBlank()
            txtNotas.text = cita.notas
            root.alpha = if (esDePasadas) ALFA_PASADAS else 1f

            root.setOnClickListener { alTocar(cita) }
            root.setOnLongClickListener {
                alEliminar(cita)
                true
            }
            btnEliminar.setOnClickListener { alEliminar(cita) }
        }
    }

    private object ComparadorCitas : DiffUtil.ItemCallback<Cita>() {
        override fun areItemsTheSame(anterior: Cita, nueva: Cita) = anterior.id == nueva.id
        override fun areContentsTheSame(anterior: Cita, nueva: Cita) = anterior == nueva
    }

    private companion object {
        const val ALFA_PASADAS = 0.6f
    }
}
