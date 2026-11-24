package com.example.patitasaldia.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.patitasaldia.R
import com.example.patitasaldia.databinding.ItemMascotaBinding
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.util.mostrarFotoMascota
import com.example.patitasaldia.util.textoEspecie

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

    /** Mascota resaltada en la tableta (la que se ve en el panel derecho). */
    private var idSeleccionado: Long? = null

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
            txtEspecie.text = root.context.textoEspecie(mascota.especie)
            imgFoto.mostrarFotoMascota(mascota.fotoRuta, mascota.especie, R.dimen.relleno_icono_lista)
            imgFoto.contentDescription =
                root.context.getString(R.string.foto_de_mascota, mascota.nombre)
            // "activated" activa el color de fondo definido en fondo_item_lista.xml
            root.isActivated = mascota.id == idSeleccionado
            root.setOnClickListener { alTocar(mascota) }
        }
    }

    /** Cambia la mascota resaltada y redibuja solo las dos filas afectadas. */
    fun seleccionar(id: Long?) {
        if (id == idSeleccionado) return
        val anterior = idSeleccionado
        idSeleccionado = id
        redibujar(anterior)
        redibujar(id)
    }

    private fun redibujar(id: Long?) {
        val posicion = currentList.indexOfFirst { it.id == id }
        if (posicion >= 0) notifyItemChanged(posicion)
    }

    /** Le indica a DiffUtil cómo comparar dos mascotas. */
    private object ComparadorMascotas : DiffUtil.ItemCallback<Mascota>() {
        // ¿Es la misma mascota? (mismo registro en la base)
        override fun areItemsTheSame(anterior: Mascota, nueva: Mascota) = anterior.id == nueva.id

        // ¿Cambió algún dato? Como Mascota es data class, == compara todos los campos
        override fun areContentsTheSame(anterior: Mascota, nueva: Mascota) = anterior == nueva
    }
}
