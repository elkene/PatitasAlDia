package com.example.patitasaldia.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.model.Vacuna
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba del texto del correo con el historial de vacunas. Necesita un
 * dispositivo porque los textos salen de strings.xml.
 */
@RunWith(AndroidJUnit4::class)
class ComunicacionUtilsTest {

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun historial_incluyeDatosDeLaMascotaYVacunasConFechas() {
        val mascota = Mascota(
            nombre = "Firulais", especie = "Perro", raza = "Labrador",
            fechaNacimiento = "2021-03-15", peso = 28.5
        )
        val vacunas = listOf(
            Vacuna(
                mascotaId = 1, nombre = "Rabia", fechaAplicacion = "2025-11-08",
                proximaDosis = "2026-11-08", notas = "Refuerzo anual"
            ),
            Vacuna(mascotaId = 1, nombre = "Parvovirus", fechaAplicacion = "2025-12-13")
        )

        val texto = ComunicacionUtils.armarHistorial(contexto, mascota, vacunas)

        assertTrue(texto.contains("Nombre: Firulais"))
        assertTrue(texto.contains("Raza: Labrador"))
        assertTrue(texto.contains("Fecha de nacimiento: 15/03/2021"))
        assertTrue(texto.contains("Peso: 28.5 kg"))
        assertTrue(
            texto.contains("- Rabia: aplicada el 08/11/2025, próxima dosis el 08/11/2026. Notas: Refuerzo anual")
        )
        assertTrue(texto.contains("- Parvovirus: aplicada el 13/12/2025"))
        assertFalse(texto.contains("Sexo:")) // no se registró
    }

    @Test
    fun historial_sinVacunas() {
        val texto = ComunicacionUtils.armarHistorial(
            contexto, Mascota(nombre = "Kiwi", especie = "Ave"), emptyList()
        )
        assertTrue(texto.contains("Sin vacunas registradas."))
    }
}
