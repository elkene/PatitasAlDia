package com.example.patitasaldia.util

import android.content.Context
import androidx.annotation.DrawableRes
import com.example.patitasaldia.R
import com.example.patitasaldia.model.Especie
import com.example.patitasaldia.model.Sexo

/*
 * Traduce los valores guardados en la base ("Perro", "Macho", etc.) al texto
 * de strings.xml y elige el ícono de cada especie.
 */

fun Context.textoEspecie(especie: String): String = when (especie) {
    Especie.PERRO -> getString(R.string.especie_perro)
    Especie.GATO -> getString(R.string.especie_gato)
    Especie.AVE -> getString(R.string.especie_ave)
    Especie.OTRO -> getString(R.string.especie_otro)
    else -> especie
}

fun Context.textoSexo(sexo: String): String = when (sexo) {
    Sexo.MACHO -> getString(R.string.sexo_macho)
    Sexo.HEMBRA -> getString(R.string.sexo_hembra)
    else -> sexo
}

/** Ícono que se muestra cuando la mascota no tiene foto. */
@DrawableRes
fun iconoEspecie(especie: String?): Int = when (especie) {
    Especie.PERRO -> R.drawable.ic_especie_perro
    Especie.GATO -> R.drawable.ic_especie_gato
    Especie.AVE -> R.drawable.ic_especie_ave
    else -> R.drawable.ic_huella
}
