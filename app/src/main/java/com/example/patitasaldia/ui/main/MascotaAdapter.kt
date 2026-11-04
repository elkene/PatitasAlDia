package com.example.patitasaldia.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.patitasaldia.R
import com.example.patitasaldia.databinding.ItemMascotaBinding
import com.example.patitasaldia.model.Mascota

/**
 * Adaptador de la lista de mascotas.
 *
 * ListAdapter calcula con DiffUtil qué filas cambiaron entre la lista anterior
 * y la nueva, y solo redibuja esas (con animación), en lugar de toda la lista.
 *
 * @param alTocar se llama con la mascota que el usuario tocó.
 */
class MascotaAdapter(
    private val alTocar: (Mascota) -> Unit
) : ListAdapter<Mascota, MascotaAdapter.MascotaViewHolder>(ComparadorMascotas) {

    /** Guarda las vistas de una fila para no buscarlas cada vez que se reutiliza. */
    class MascotaViewHolder(val binding: ItemMascotaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val binding = ItemMascotaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MascotaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = getItem(position)
        with(holder.binding) {
            txtNombre.text = mascota.nombre
            txtEspecie.text = mascota.especie
            imgFoto.contentDescription =
                root.context.getString(R.string.foto_de_mascota, mascota.nombre)
            // En la fase 7 aquí se cargará la foto con Coil o el ícono según la especie
            root.setOnClickListener { alTocar(mascota) }
        }
    }

    /** Le indica a DiffUtil cómo comparar dos mascotas. */
    private object ComparadorMascotas : DiffUtil.ItemCallback<Mascota>() {
        // ¿Es la misma mascota? (mismo registro en la base)
        override fun areItemsTheSame(anterior: Mascota, nueva: Mascota) = anterior.id == nueva.id

        // ¿Cambió algún dato? Como Mascota es data class, == compara todos los campos
        override fun areContentsTheSame(anterior: Mascota, nueva: Mascota) = anterior == nueva
    }
}
