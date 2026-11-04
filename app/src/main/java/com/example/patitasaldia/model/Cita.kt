package com.example.patitasaldia.model

/**
 * Representa una fila de la tabla "citas". Cada cita pertenece a una mascota.
 */
data class Cita(
    val id: Long = 0,
    val mascotaId: Long,
    val fecha: String,                // yyyy-MM-dd
    val hora: String? = null,         // HH:mm
    val motivo: String,
    val notas: String? = null
)
