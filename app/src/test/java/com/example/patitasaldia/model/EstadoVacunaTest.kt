package com.example.patitasaldia.model

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pruebas de la regla que resalta las vacunas vencidas o por vencer. */
class EstadoVacunaTest {

    private val hoy = "2026-11-23"

    @Test
    fun sinProximaDosis() {
        assertEquals(EstadoVacuna.SIN_PROXIMA_DOSIS, EstadoVacuna.calcular(null, hoy))
    }

    @Test
    fun vencida_siLaFechaYaPaso() {
        assertEquals(EstadoVacuna.VENCIDA, EstadoVacuna.calcular("2026-11-22", hoy))
        assertEquals(EstadoVacuna.VENCIDA, EstadoVacuna.calcular("2025-01-01", hoy))
    }

    @Test
    fun proxima_desdeHoyHastaMenosDe30Dias() {
        assertEquals(EstadoVacuna.PROXIMA, EstadoVacuna.calcular("2026-11-23", hoy))
        assertEquals(EstadoVacuna.PROXIMA, EstadoVacuna.calcular("2026-12-22", hoy)) // 29 días
    }

    @Test
    fun alDia_conTreintaDiasOMas() {
        assertEquals(EstadoVacuna.AL_DIA, EstadoVacuna.calcular("2026-12-23", hoy)) // 30 días
        assertEquals(EstadoVacuna.AL_DIA, EstadoVacuna.calcular("2027-06-01", hoy))
    }
}
