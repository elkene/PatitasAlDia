package com.example.patitasaldia.model

/**
 * Representa una fila de la tabla "vacunas". Cada vacuna pertenece a una mascota.
 */
data class Vacuna(
    val id: Long = 0,
    val mascotaId: Long,
    val nombre: String,
    val fechaAplicacion: String,      // yyyy-MM-dd
    val proximaDosis: String? = null, // yyyy-MM-dd
    val notas: String? = null
)
