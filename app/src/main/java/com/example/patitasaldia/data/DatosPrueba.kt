package com.example.patitasaldia.data

import android.database.sqlite.SQLiteDatabase
import com.example.patitasaldia.model.Cita
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.model.Vacuna
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Datos de ejemplo que se insertan al crear la base, solo en modo depuración.
 *
 * Las fechas se calculan a partir del día de hoy para que siempre existan
 * casos de prueba: una vacuna vencida, una próxima a vencer (menos de 30 días),
 * una al corriente, citas pasadas y citas futuras.
 *
 * Recibe el SQLiteDatabase que entrega onCreate en lugar de usar los DAOs,
 * porque los DAOs piden la base al DbHelper y eso causaría una llamada
 * recursiva mientras la base todavía se está creando.
 */
internal object DatosPrueba {

    fun insertar(db: SQLiteDatabase) {
        val firulais = db.insert(
            TablaMascotas.NOMBRE, null, MascotaDao.aContentValues(
                Mascota(
                    nombre = "Firulais",
                    especie = "Perro",
                    raza = "Labrador",
                    sexo = "Macho",
                    fechaNacimiento = "2021-03-15",
                    peso = 28.5,
                    vetNombre = "Dra. Laura Méndez",
                    vetTelefono = "6641234567",
                    vetCorreo = "laura.mendez@veterinaria.com"
                )
            )
        )
        val michi = db.insert(
            TablaMascotas.NOMBRE, null, MascotaDao.aContentValues(
                Mascota(
                    nombre = "Michi",
                    especie = "Gato",
                    raza = "Siamés",
                    sexo = "Hembra",
                    fechaNacimiento = "2022-08-02",
                    peso = 4.2,
                    vetNombre = "Dr. Carlos Ruiz",
                    vetTelefono = "6649876543",
                    vetCorreo = "carlos.ruiz@veterinaria.com"
                )
            )
        )
        // Sin datos del veterinario, para probar que los botones se deshabilitan
        val kiwi = db.insert(
            TablaMascotas.NOMBRE, null, MascotaDao.aContentValues(
                Mascota(
                    nombre = "Kiwi",
                    especie = "Ave",
                    raza = "Periquito",
                    peso = 0.04
                )
            )
        )

        val vacunas = listOf(
            // Próxima dosis vencida
            Vacuna(
                mascotaId = firulais, nombre = "Rabia",
                fechaAplicacion = fechaRelativa(-380), proximaDosis = fechaRelativa(-15),
                notas = "Refuerzo anual"
            ),
            // Próxima dosis en menos de 30 días
            Vacuna(
                mascotaId = firulais, nombre = "Parvovirus",
                fechaAplicacion = fechaRelativa(-345), proximaDosis = fechaRelativa(20)
            ),
            // Al corriente
            Vacuna(
                mascotaId = michi, nombre = "Triple felina",
                fechaAplicacion = fechaRelativa(-60), proximaDosis = fechaRelativa(305)
            ),
            // Sin próxima dosis
            Vacuna(
                mascotaId = michi, nombre = "Leucemia felina",
                fechaAplicacion = fechaRelativa(-200), notas = "Dosis única"
            )
        )
        vacunas.forEach { db.insert(TablaVacunas.NOMBRE, null, VacunaDao.aContentValues(it)) }

        val citas = listOf(
            Cita(
                mascotaId = firulais, fecha = fechaRelativa(-30), hora = "10:00",
                motivo = "Revisión general"
            ),
            Cita(
                mascotaId = firulais, fecha = fechaRelativa(7), hora = "16:30",
                motivo = "Vacuna de rabia", notas = "Llevar cartilla"
            ),
            Cita(
                mascotaId = michi, fecha = fechaRelativa(14), hora = "11:15",
                motivo = "Limpieza dental"
            ),
            Cita(
                mascotaId = kiwi, fecha = fechaRelativa(-5),
                motivo = "Corte de uñas"
            )
        )
        citas.forEach { db.insert(TablaCitas.NOMBRE, null, CitaDao.aContentValues(it)) }
    }

    /** Regresa la fecha de hoy más (o menos) los días indicados, en formato yyyy-MM-dd. */
    private fun fechaRelativa(dias: Int): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_YEAR, dias)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendario.time)
    }
}
