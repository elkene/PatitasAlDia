package com.example.patitasaldia.data

import android.content.ContentValues
import android.database.Cursor
import com.example.patitasaldia.model.Cita

/**
 * Acceso a la tabla "citas". Sus métodos no deben llamarse en el hilo principal.
 */
class CitaDao(private val dbHelper: DbHelper) {

    /** Inserta la cita y regresa el id que le asignó SQLite (-1 si falló). */
    fun insertar(cita: Cita): Long =
        dbHelper.writableDatabase.insert(TablaCitas.NOMBRE, null, aContentValues(cita))

    /** Actualiza la cita con el mismo id. Regresa cuántas filas cambiaron. */
    fun actualizar(cita: Cita): Int =
        dbHelper.writableDatabase.update(
            TablaCitas.NOMBRE,
            aContentValues(cita),
            "${TablaCitas.ID} = ?",
            arrayOf(cita.id.toString())
        )

    /** Elimina la cita con ese id. Regresa cuántas filas se borraron. */
    fun eliminar(id: Long): Int =
        dbHelper.writableDatabase.delete(
            TablaCitas.NOMBRE,
            "${TablaCitas.ID} = ?",
            arrayOf(id.toString())
        )

    /**
     * Regresa las citas de una mascota en orden cronológico (fecha y hora).
     * La separación entre próximas y pasadas se hace después, en el ViewModel.
     */
    fun obtenerPorMascota(mascotaId: Long): List<Cita> =
        dbHelper.readableDatabase.query(
            TablaCitas.NOMBRE,
            null,
            "${TablaCitas.MASCOTA_ID} = ?",
            arrayOf(mascotaId.toString()),
            null, null,
            "${TablaCitas.FECHA} ASC, ${TablaCitas.HORA} ASC"
        ).use { cursor ->
            val lista = mutableListOf<Cita>()
            while (cursor.moveToNext()) {
                lista.add(cursor.aCita())
            }
            lista
        }

    /** Convierte la fila actual del cursor en una Cita. */
    private fun Cursor.aCita() = Cita(
        id = leerLong(TablaCitas.ID),
        mascotaId = leerLong(TablaCitas.MASCOTA_ID),
        fecha = leerString(TablaCitas.FECHA),
        hora = leerStringOrNull(TablaCitas.HORA),
        motivo = leerString(TablaCitas.MOTIVO),
        notas = leerStringOrNull(TablaCitas.NOTAS)
    )

    companion object {
        /**
         * Convierte una Cita en los valores de una fila. El id no se incluye:
         * lo maneja SQLite. Es "internal" para que DatosPrueba también lo use.
         */
        internal fun aContentValues(cita: Cita) = ContentValues().apply {
            put(TablaCitas.MASCOTA_ID, cita.mascotaId)
            put(TablaCitas.FECHA, cita.fecha)
            put(TablaCitas.HORA, cita.hora)
            put(TablaCitas.MOTIVO, cita.motivo)
            put(TablaCitas.NOTAS, cita.notas)
        }
    }
}
