package com.example.patitasaldia.util

import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout

/**
 * Muestra el ícono final del campo (la X para borrar) solo cuando el campo
 * tiene texto. Se usa en los campos de fecha y hora opcionales.
 */
fun TextInputLayout.mostrarBorrarSoloConTexto() {
    val campo = editText ?: return
    isEndIconVisible = !campo.text.isNullOrEmpty()
    campo.doAfterTextChanged { texto -> isEndIconVisible = !texto.isNullOrEmpty() }
}
