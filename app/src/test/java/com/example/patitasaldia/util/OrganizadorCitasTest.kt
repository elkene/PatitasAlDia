package com.example.patitasaldia.util

import com.example.patitasaldia.model.Cita
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pruebas de la separación de citas en próximas y pasadas. */
class OrganizadorCitasTest {

    private fun cita(id: Long, fecha: String, hora: String?) =
        Cita(id = id, mascotaId = 1, fecha = fecha, hora = hora, motivo = "Cita $id")

    @Test
    fun separaPorFechaYHora() {
        // Ordenadas como las regresa CitaDao (fecha y hora ascendentes)
        val citas = listOf(
            cita(1, "2026-11-01", "10:00"),
            cita(2, "2026-11-20", null),
            cita(3, "2026-11-23", "09:00"), // hoy, ya pasó la hora
            cita(4, "2026-11-23", null),    // hoy sin hora: cuenta como próxima
            cita(5, "2026-11-23", "18:00"), // hoy, más tarde
            cita(6, "2026-12-01", "16:30")
        )

        val resultado = OrganizadorCitas.separar(citas, hoyBd = "2026-11-23", horaActual = "12:00")

        assertEquals(listOf(4L, 5L, 6L), resultado.proximas.map { it.id })
        // Las pasadas van de la más reciente a la más antigua
        assertEquals(listOf(3L, 2L, 1L), resultado.pasadas.map { it.id })
    }

    @Test
    fun listaVacia() {
        val resultado = OrganizadorCitas.separar(emptyList(), "2026-11-23", "12:00")
        assertEquals(0, resultado.proximas.size + resultado.pasadas.size)
    }
}
