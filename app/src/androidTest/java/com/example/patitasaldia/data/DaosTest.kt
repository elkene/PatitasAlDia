package com.example.patitasaldia.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.patitasaldia.model.Cita
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.model.Vacuna
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de DbHelper y de los DAOs. Cada prueba usa una base nueva en memoria
 * (nombreBd = null) y sin datos de prueba, así que no toca patitas.db.
 */
@RunWith(AndroidJUnit4::class)
class DaosTest {

    private lateinit var dbHelper: DbHelper
    private lateinit var mascotaDao: MascotaDao
    private lateinit var vacunaDao: VacunaDao
    private lateinit var citaDao: CitaDao

    @Before
    fun preparar() {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        dbHelper = DbHelper(contexto, nombreBd = null, insertarDatosPrueba = false)
        mascotaDao = MascotaDao(dbHelper)
        vacunaDao = VacunaDao(dbHelper)
        citaDao = CitaDao(dbHelper)
    }

    @After
    fun cerrar() {
        dbHelper.close()
    }

    @Test
    fun insertarYObtenerMascota_conservaTodosLosCampos() {
        val original = Mascota(
            nombre = "Firulais", especie = "Perro", raza = "Labrador", sexo = "Macho",
            fechaNacimiento = "2021-03-15", peso = 28.5, fotoRuta = "/fotos/a.jpg",
            vetNombre = "Dra. Méndez", vetTelefono = "6641234567", vetCorreo = "vet@correo.com"
        )
        val id = mascotaDao.insertar(original)

        assertTrue(id > 0)
        assertEquals(original.copy(id = id), mascotaDao.obtenerPorId(id))
    }

    @Test
    fun insertarMascotaSinOpcionales_regresaNulos() {
        val id = mascotaDao.insertar(Mascota(nombre = "Kiwi", especie = "Ave"))
        val leida = mascotaDao.obtenerPorId(id)!!

        assertNull(leida.raza)
        assertNull(leida.peso)
        assertNull(leida.vetTelefono)
    }

    @Test
    fun actualizarMascota_cambiaLosDatos() {
        val id = mascotaDao.insertar(Mascota(nombre = "Michi", especie = "Gato"))
        val modificada = Mascota(id = id, nombre = "Michi", especie = "Gato", peso = 4.2)

        assertEquals(1, mascotaDao.actualizar(modificada))
        assertEquals(modificada, mascotaDao.obtenerPorId(id))
    }

    @Test
    fun obtenerTodas_ordenaPorNombreSinImportarMayusculas() {
        mascotaDao.insertar(Mascota(nombre = "rocky", especie = "Perro"))
        mascotaDao.insertar(Mascota(nombre = "Bola", especie = "Gato"))
        mascotaDao.insertar(Mascota(nombre = "Michi", especie = "Gato"))

        val nombres = mascotaDao.obtenerTodas().map { it.nombre }
        assertEquals(listOf("Bola", "Michi", "rocky"), nombres)
    }

    @Test
    fun eliminarMascota_borraSusVacunasYCitasEnCascada() {
        val id = mascotaDao.insertar(Mascota(nombre = "Firulais", especie = "Perro"))
        vacunaDao.insertar(Vacuna(mascotaId = id, nombre = "Rabia", fechaAplicacion = "2026-01-10"))
        citaDao.insertar(Cita(mascotaId = id, fecha = "2026-12-01", motivo = "Revisión"))

        assertEquals(1, mascotaDao.eliminar(id))
        assertNull(mascotaDao.obtenerPorId(id))
        assertTrue(vacunaDao.obtenerPorMascota(id).isEmpty())
        assertTrue(citaDao.obtenerPorMascota(id).isEmpty())
    }

    @Test
    fun insertarVacunaDeMascotaInexistente_fallaPorLlaveForanea() {
        // insert() atrapa la excepción de SQLite y regresa -1
        val id = vacunaDao.insertar(
            Vacuna(mascotaId = 999, nombre = "Rabia", fechaAplicacion = "2026-01-10")
        )
        assertEquals(-1L, id)
    }

    @Test
    fun vacunas_seOrdenanDeLaMasRecienteALaMasAntiguaYSeActualizan() {
        val mascota = mascotaDao.insertar(Mascota(nombre = "Firulais", especie = "Perro"))
        vacunaDao.insertar(Vacuna(mascotaId = mascota, nombre = "A", fechaAplicacion = "2025-05-01"))
        val idB = vacunaDao.insertar(
            Vacuna(mascotaId = mascota, nombre = "B", fechaAplicacion = "2026-02-01")
        )
        vacunaDao.insertar(Vacuna(mascotaId = mascota, nombre = "C", fechaAplicacion = "2025-11-20"))

        assertEquals(listOf("B", "C", "A"), vacunaDao.obtenerPorMascota(mascota).map { it.nombre })

        val editada = Vacuna(
            id = idB, mascotaId = mascota, nombre = "B", fechaAplicacion = "2026-02-01",
            proximaDosis = "2027-02-01", notas = "Refuerzo"
        )
        assertEquals(1, vacunaDao.actualizar(editada))
        assertEquals(editada, vacunaDao.obtenerPorMascota(mascota).first())

        assertEquals(1, vacunaDao.eliminar(idB))
        assertEquals(2, vacunaDao.obtenerPorMascota(mascota).size)
    }

    @Test
    fun citas_seOrdenanPorFechaYHoraYSoloDeLaMascotaPedida() {
        val firulais = mascotaDao.insertar(Mascota(nombre = "Firulais", especie = "Perro"))
        val michi = mascotaDao.insertar(Mascota(nombre = "Michi", especie = "Gato"))
        citaDao.insertar(Cita(mascotaId = firulais, fecha = "2026-12-01", hora = "16:00", motivo = "Tarde"))
        citaDao.insertar(Cita(mascotaId = firulais, fecha = "2026-12-01", hora = "09:00", motivo = "Mañana"))
        citaDao.insertar(Cita(mascotaId = firulais, fecha = "2026-11-15", hora = "12:00", motivo = "Antes"))
        citaDao.insertar(Cita(mascotaId = michi, fecha = "2026-10-01", motivo = "De Michi"))

        assertEquals(
            listOf("Antes", "Mañana", "Tarde"),
            citaDao.obtenerPorMascota(firulais).map { it.motivo }
        )
    }
}
