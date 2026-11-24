package com.example.patitasaldia.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Agrega relleno a la vista para que el contenido no quede debajo de la barra
 * de estado, la barra de navegación ni el recorte de la cámara (la app se
 * dibuja de borde a borde con enableEdgeToEdge).
 *
 * @param incluirTeclado si es true, también deja espacio para el teclado,
 * para que los campos de un formulario no queden tapados al escribir.
 */
fun View.aplicarMargenesDelSistema(incluirTeclado: Boolean = false) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { vista, insets ->
        val barras = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val abajo = if (incluirTeclado) {
            maxOf(barras.bottom, insets.getInsets(WindowInsetsCompat.Type.ime()).bottom)
        } else {
            barras.bottom
        }
        vista.setPadding(barras.left, barras.top, barras.right, abajo)
        insets
    }
}
