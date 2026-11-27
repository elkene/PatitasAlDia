package com.example.patitasaldia.ui.detalle

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.patitasaldia.R
import com.example.patitasaldia.databinding.ItemVacunaBinding
import com.example.patitasaldia.model.EstadoVacuna
import com.example.patitasaldia.model.Vacuna
import com.example.patitasaldia.util.FechaUtils
import com.google.android.material.color.MaterialColors
import kotlin.math.abs

/**
 * Lista de vacunas del detalle. Resalta con color las vacunas cuya próxima
 * dosis ya venció (rojo) o vence en menos de 30 días (ámbar).
 *
 * @param alTocar abre la vacuna para editarla.
 * @param alEliminar pide confirmación para eliminarla (botón de basura o pulsación larga).
 */
class VacunaAdapter(
    private val alTocar: (Vacuna) -> Unit,
    private val alEliminar: (Vacuna) -> Unit
) : ListAdapter<Vacuna, VacunaAdapter.VacunaViewHolder>(ComparadorVacunas) {

    class VacunaViewHolder(val binding: ItemVacunaBinding) : RecyclerView.ViewHolder(binding.root) {
        // Borde original de la tarjeta, para restaurarlo cuando la fila se reutiliza
        val bordeNormal: ColorStateList? = binding.tarjeta.strokeColorStateList
        val anchoBordeNormal: Int = binding.tarjeta.strokeWidth
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacunaViewHolder =
        VacunaViewHolder(ItemVacunaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VacunaViewHolder, position: Int) {
        val vacuna = getItem(position)
        val contexto = holder.itemView.context
        with(holder.binding) {
            txtNombre.text = vacuna.nombre
            txtAplicacion.text = contexto.getString(
                R.string.vacuna_aplicada, FechaUtils.bdAPantalla(vacuna.fechaAplicacion)
            )
            val proxima = FechaUtils.bdAPantalla(vacuna.proximaDosis)
            txtProxima.isVisible = proxima != null
            txtProxima.text = proxima?.let { contexto.getString(R.string.vacuna_proxima, it) }
            txtNotas.isVisible = !vacuna.notas.isNullOrBlank()
            txtNotas.text = vacuna.notas

            // Estado y color según la fecha de la próxima dosis
            val dias = vacuna.proximaDosis?.let { FechaUtils.diasDesde(FechaUtils.hoy(), it) }
            val resources = contexto.resources
            val estado = EstadoVacuna.calcular(vacuna.proximaDosis)
            val (textoEstado, color) = when (estado) {
                EstadoVacuna.VENCIDA -> {
                    val diasVencida = abs(dias ?: 0).toInt()
                    resources.getQuantityString(R.plurals.estado_vencida, diasVencida, diasVencida) to
                        ContextCompat.getColor(contexto, R.color.aviso_vencida)
                }
                EstadoVacuna.PROXIMA -> {
                    val diasFaltan = (dias ?: 0).toInt()
                    val texto = if (diasFaltan == 0) {
                        contexto.getString(R.string.estado_vence_hoy)
                    } else {
                        resources.getQuantityString(R.plurals.estado_vence_en, diasFaltan, diasFaltan)
                    }
                    texto to ContextCompat.getColor(contexto, R.color.aviso_proxima)
                }
                EstadoVacuna.AL_DIA -> contexto.getString(R.string.estado_al_dia) to
                    MaterialColors.getColor(root, com.google.android.material.R.attr.colorSecondary)
                EstadoVacuna.SIN_PROXIMA_DOSIS -> null to null
            }
            txtEstado.isVisible = textoEstado != null
            txtEstado.text = textoEstado
            color?.let { txtEstado.setTextColor(it) }

            // Solo las vencidas y las próximas llevan el borde de color
            val resaltar = estado == EstadoVacuna.VENCIDA || estado == EstadoVacuna.PROXIMA
            if (resaltar && color != null) {
                tarjeta.setStrokeColor(ColorStateList.valueOf(color))
                tarjeta.strokeWidth = resources.getDimensionPixelSize(R.dimen.borde_aviso)
            } else {
                tarjeta.setStrokeColor(holder.bordeNormal)
                tarjeta.strokeWidth = holder.anchoBordeNormal
            }

            root.setOnClickListener { alTocar(vacuna) }
            root.setOnLongClickListener {
                alEliminar(vacuna)
                true
            }
            btnEliminar.setOnClickListener { alEliminar(vacuna) }
        }
    }

    private object ComparadorVacunas : DiffUtil.ItemCallback<Vacuna>() {
        override fun areItemsTheSame(anterior: Vacuna, nueva: Vacuna) = anterior.id == nueva.id
        override fun areContentsTheSame(anterior: Vacuna, nueva: Vacuna) = anterior == nueva
    }
}
