package com.example.patitasaldia.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de las reglas de validación de los formularios. */
class ValidacionesTest {

    @Test
    fun telefono_aceptaFormatosComunes() {
        assertTrue(Validaciones.telefonoValido("6641234567"))
        assertTrue(Validaciones.telefonoValido("(664) 123-4567"))
        assertTrue(Validaciones.telefonoValido("+52 664 123 4567"))
    }

    @Test
    fun telefono_rechazaTextoONumerosMuyCortos() {
        assertFalse(Validaciones.telefonoValido("123"))
        assertFalse(Validaciones.telefonoValido("telefono"))
        assertFalse(Validaciones.telefonoValido("12345678901234567"))
    }

    @Test
    fun limpiarTelefono_quitaEspaciosGuionesYParentesis() {
        assertEquals("6641234567", Validaciones.limpiarTelefono("(664) 123-4567"))
    }

    @Test
    fun correo_validaElFormato() {
        assertTrue(Validaciones.correoValido("laura.mendez@veterinaria.com"))
        assertTrue(Validaciones.correoValido(" vet@uabc.edu.mx "))
        assertFalse(Validaciones.correoValido("laura@"))
        assertFalse(Validaciones.correoValido("veterinaria.com"))
        assertFalse(Validaciones.correoValido("vet@correo"))
    }

    @Test
    fun peso_aceptaPuntoOComaDecimal() {
        assertEquals(28.5, Validaciones.leerPeso("28.5")!!, 0.0001)
        assertEquals(4.2, Validaciones.leerPeso("4,2")!!, 0.0001)
        assertNull(Validaciones.leerPeso("abc"))
    }

    @Test
    fun peso_debeSerPositivoYRazonable() {
        assertTrue(Validaciones.pesoValido(0.04))
        assertFalse(Validaciones.pesoValido(0.0))
        assertFalse(Validaciones.pesoValido(-3.0))
        assertFalse(Validaciones.pesoValido(5000.0))
    }
}
