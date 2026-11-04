package com.example.patitasaldia.model

/**
 * Representa una fila de la tabla "mascotas".
 *
 * El id vale 0 mientras la mascota no se ha guardado; SQLite asigna el id
 * real al insertarla. Los campos opcionales son nulos si el usuario no los llenó.
 */
data class Mascota(
    val id: Long = 0,
    val nombre: String,
    val especie: String,              // Perro, Gato, Ave, Otro
    val raza: String? = null,
    val sexo: String? = null,         // Macho, Hembra
    val fechaNacimiento: String? = null, // yyyy-MM-dd
    val peso: Double? = null,         // kg
    val fotoRuta: String? = null,     // ruta del archivo en el almacenamiento interno
    val vetNombre: String? = null,
    val vetTelefono: String? = null,
    val vetCorreo: String? = null
)
