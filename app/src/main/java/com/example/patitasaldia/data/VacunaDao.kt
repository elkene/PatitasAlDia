package com.example.patitasaldia.data

import android.content.ContentValues
import android.database.Cursor
import com.example.patitasaldia.model.Vacuna

/**
 * Acceso a la tabla "vacunas". Sus métodos no deben llamarse en el hilo principal.
 */
class VacunaDao(private val dbHelper: DbHelper) {

    /** Inserta la vacuna y regresa el id que le asignó SQLite (-1 si falló). */
    fun insertar(vacuna: Vacuna): Long =
        dbHelper.writableDatabase.insert(TablaVacunas.NOMBRE, null, aContentValues(vacuna))

    /** Actualiza la vacuna con el mismo id. Regresa cuántas filas cambiaron. */
    fun actualizar(vacuna: Vacuna): Int =
        dbHelper.writableDatabase.update(
            TablaVacunas.NOMBRE,
            aContentValues(vacuna),
            "${TablaVacunas.ID} = ?",
            arrayOf(vacuna.id.toString())
        )

    /** Elimina la vacuna con ese id. Regresa cuántas filas se borraron. */
    fun eliminar(id: Long): Int =
        dbHelper.writableDatabase.delete(
            TablaVacunas.NOMBRE,
            "${TablaVacunas.ID} = ?",
            arrayOf(id.toString())
        )

    /**
     * Regresa las vacunas de una mascota, de la más reciente a la más antigua.
     * Como las fechas están en formato yyyy-MM-dd, ordenarlas como texto
     * da el mismo resultado que ordenarlas como fechas.
     */
    fun obtenerPorMascota(mascotaId: Long): List<Vacuna> =
        dbHelper.readableDatabase.query(
            TablaVacunas.NOMBRE,
            null,
            "${TablaVacunas.MASCOTA_ID} = ?",
            arrayOf(mascotaId.toString()),
            null, null,
            "${TablaVacunas.FECHA_APLICACION} DESC"
        ).use { cursor ->
            val lista = mutableListOf<Vacuna>()
            while (cursor.moveToNext()) {
                lista.add(cursor.aVacuna())
            }
            lista
        }

    /** Convierte la fila actual del cursor en una Vacuna. */
    private fun Cursor.aVacuna() = Vacuna(
        id = leerLong(TablaVacunas.ID),
        mascotaId = leerLong(TablaVacunas.MASCOTA_ID),
        nombre = leerString(TablaVacunas.NOMBRE_VACUNA),
        fechaAplicacion = leerString(TablaVacunas.FECHA_APLICACION),
        proximaDosis = leerStringOrNull(TablaVacunas.PROXIMA_DOSIS),
        notas = leerStringOrNull(TablaVacunas.NOTAS)
    )

    companion object {
        /**
         * Convierte una Vacuna en los valores de una fila. El id no se incluye:
         * lo maneja SQLite. Es "internal" para que DatosPrueba también lo use.
         */
        internal fun aContentValues(vacuna: Vacuna) = ContentValues().apply {
            put(TablaVacunas.MASCOTA_ID, vacuna.mascotaId)
            put(TablaVacunas.NOMBRE_VACUNA, vacuna.nombre)
            put(TablaVacunas.FECHA_APLICACION, vacuna.fechaAplicacion)
            put(TablaVacunas.PROXIMA_DOSIS, vacuna.proximaDosis)
            put(TablaVacunas.NOTAS, vacuna.notas)
        }
    }
}
