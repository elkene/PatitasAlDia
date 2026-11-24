package com.example.patitasaldia.util

/**
 * Reglas para validar los campos de los formularios.
 *
 * Se usan expresiones regulares propias (y no android.util.Patterns) para que
 * estas reglas se puedan probar con pruebas unitarias en la computadora.
 */
object Validaciones {

    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Un "+" opcional y de 7 a 15 dígitos (el máximo internacional)
    private val REGEX_TELEFONO = Regex("^\\+?[0-9]{7,15}$")

    private const val PESO_MAXIMO_KG = 1000.0

    /** Quita espacios, guiones y paréntesis: "(664) 123-4567" -> "6641234567". */
    fun limpiarTelefono(telefono: String): String = telefono.replace(Regex("[\\s()-]"), "")

    fun telefonoValido(telefono: String): Boolean =
        REGEX_TELEFONO.matches(limpiarTelefono(telefono))

    fun correoValido(correo: String): Boolean = REGEX_CORREO.matches(correo.trim())

    /** Convierte el texto del peso a número. Acepta punto o coma decimal. */
    fun leerPeso(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

    fun pesoValido(peso: Double): Boolean = peso > 0 && peso < PESO_MAXIMO_KG
}
