package com.example.patitasaldia.util

import android.content.res.ColorStateList
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.core.widget.ImageViewCompat
import coil.dispose
import coil.load
import com.google.android.material.color.MaterialColors
import java.io.File

/**
 * Muestra la foto de la mascota en un ImageView.
 *
 * - Si hay foto, la carga con Coil en un hilo de fondo (sin congelar la interfaz).
 * - Si no hay foto, muestra el ícono de la especie, con color y un relleno
 *   para que no ocupe todo el círculo.
 *
 * @param rellenoIcono relleno que se deja alrededor del ícono (depende del tamaño del ImageView).
 */
fun ImageView.mostrarFotoMascota(fotoRuta: String?, especie: String?, @DimenRes rellenoIcono: Int) {
    val archivo = fotoRuta?.let { File(it) }?.takeIf { it.exists() }
    if (archivo != null) {
        ImageViewCompat.setImageTintList(this, null)
        setPadding(0, 0, 0, 0)
        load(archivo) { crossfade(true) }
    } else {
        // Cancela una carga pendiente (por ejemplo, si la fila de la lista se reutilizó)
        dispose()
        val relleno = resources.getDimensionPixelSize(rellenoIcono)
        setPadding(relleno, relleno, relleno, relleno)
        val color = MaterialColors.getColor(
            this, com.google.android.material.R.attr.colorOnPrimaryContainer
        )
        ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))
        setImageResource(iconoEspecie(especie))
    }
}
