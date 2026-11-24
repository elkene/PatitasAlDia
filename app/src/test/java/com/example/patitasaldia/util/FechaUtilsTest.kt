package com.example.patitasaldia.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas de las conversiones y cálculos de fechas. */
class FechaUtilsTest {

    @Test
    fun bdAPantalla_convierteElFormato() {
        assertEquals("15/03/2021", FechaUtils.bdAPantalla("2021-03-15"))
    }

    @Test
    fun bdAPantalla_regresaNullSiNoHayFechaOEsInvalida() {
        assertNull(FechaUtils.bdAPantalla(null))
        assertNull(FechaUtils.bdAPantalla(""))
        assertNull(FechaUtils.bdAPantalla("2021-02-31"))
    }

    @Test
    fun pantallaABd_convierteElFormato() {
        assertEquals("2021-03-15", FechaUtils.pantallaABd("15/03/2021"))
        assertNull(FechaUtils.pantallaABd("31/02/2021"))
        assertNull(FechaUtils.pantallaABd("  "))
    }

    @Test
    fun milisUtc_idaYVueltaNoRecorreElDia() {
        val milis = FechaUtils.bdAMilisUtc("2026-11-23")!!
        assertEquals("2026-11-23", FechaUtils.milisUtcABd(milis))
    }

    @Test
    fun diasDesde_cuentaDiasEnteros() {
        assertEquals(0L, FechaUtils.diasDesde("2026-11-23", "2026-11-23"))
        assertEquals(7L, FechaUtils.diasDesde("2026-11-23", "2026-11-30"))
        assertEquals(-15L, FechaUtils.diasDesde("2026-11-23", "2026-11-08"))
        // Cruza el cambio de año
        assertEquals(10L, FechaUtils.diasDesde("2026-12-27", "2027-01-06"))
    }

    @Test
    fun mesesCumplidos_soloCuentaMesesCompletos() {
        assertEquals(68, FechaUtils.mesesCumplidos("2021-03-15", "2026-11-23"))
        assertEquals(0, FechaUtils.mesesCumplidos("2026-11-01", "2026-11-23"))
        // El 14 todavía no cumple el mes que empezó el 15
        assertEquals(11, FechaUtils.mesesCumplidos("2025-11-15", "2026-11-14"))
        assertEquals(12, FechaUtils.mesesCumplidos("2025-11-15", "2026-11-15"))
        assertNull(FechaUtils.mesesCumplidos("2027-01-01", "2026-11-23"))
    }
}
