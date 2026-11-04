package com.example.patitasaldia.data

import android.content.ContentValues
import android.database.Cursor
import com.example.patitasaldia.model.Mascota

/**
 * Acceso a la tabla "mascotas". Es el único lugar donde se escribe SQL
 * para las mascotas. Sus métodos no deben llamarse en el hilo principal.
 */
class MascotaDao(private val dbHelper: DbHelper) {

    /** Inserta la mascota y regresa el id que le asignó SQLite (-1 si falló). */
    fun insertar(mascota: Mascota): Long =
        dbHelper.writableDatabase.insert(TablaMascotas.NOMBRE, null, aContentValues(mascota))

    /** Actualiza la mascota con el mismo id. Regresa cuántas filas cambiaron. */
    fun actualizar(mascota: Mascota): Int =
        dbHelper.writableDatabase.update(
            TablaMascotas.NOMBRE,
            aContentValues(mascota),
            "${TablaMascotas.ID} = ?",
            arrayOf(mascota.id.toString())
        )

    /**
     * Elimina la mascota. Sus vacunas y citas se borran solas por el
     * ON DELETE CASCADE. Regresa cuántas filas se borraron.
     */
    fun eliminar(id: Long): Int =
        dbHelper.writableDatabase.delete(
            TablaMascotas.NOMBRE,
            "${TablaMascotas.ID} = ?",
            arrayOf(id.toString())
        )

    /** Regresa todas las mascotas ordenadas por nombre (sin distinguir mayúsculas). */
    fun obtenerTodas(): List<Mascota> =
        dbHelper.readableDatabase.query(
            TablaMascotas.NOMBRE,
            null, null, null, null, null,
            "${TablaMascotas.NOMBRE_MASCOTA} COLLATE NOCASE ASC"
        ).use { cursor ->
            val lista = mutableListOf<Mascota>()
            while (cursor.moveToNext()) {
                lista.add(cursor.aMascota())
            }
            lista
        }

    /** Regresa la mascota con ese id, o null si no existe. */
    fun obtenerPorId(id: Long): Mascota? =
        dbHelper.readableDatabase.query(
            TablaMascotas.NOMBRE,
            null,
            "${TablaMascotas.ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.aMascota() else null
        }

    /** Convierte la fila actual del cursor en una Mascota. */
    private fun Cursor.aMascota() = Mascota(
        id = leerLong(TablaMascotas.ID),
        nombre = leerString(TablaMascotas.NOMBRE_MASCOTA),
        especie = leerString(TablaMascotas.ESPECIE),
        raza = leerStringOrNull(TablaMascotas.RAZA),
        sexo = leerStringOrNull(TablaMascotas.SEXO),
        fechaNacimiento = leerStringOrNull(TablaMascotas.FECHA_NACIMIENTO),
        peso = leerDoubleOrNull(TablaMascotas.PESO),
        fotoRuta = leerStringOrNull(TablaMascotas.FOTO_RUTA),
        vetNombre = leerStringOrNull(TablaMascotas.VET_NOMBRE),
        vetTelefono = leerStringOrNull(TablaMascotas.VET_TELEFONO),
        vetCorreo = leerStringOrNull(TablaMascotas.VET_CORREO)
    )

    companion object {
        /**
         * Convierte una Mascota en los valores de una fila. El id no se incluye:
         * lo maneja SQLite. Es "internal" para que DatosPrueba también lo use.
         */
        internal fun aContentValues(mascota: Mascota) = ContentValues().apply {
            put(TablaMascotas.NOMBRE_MASCOTA, mascota.nombre)
            put(TablaMascotas.ESPECIE, mascota.especie)
            put(TablaMascotas.RAZA, mascota.raza)
            put(TablaMascotas.SEXO, mascota.sexo)
            put(TablaMascotas.FECHA_NACIMIENTO, mascota.fechaNacimiento)
            put(TablaMascotas.PESO, mascota.peso)
            put(TablaMascotas.FOTO_RUTA, mascota.fotoRuta)
            put(TablaMascotas.VET_NOMBRE, mascota.vetNombre)
            put(TablaMascotas.VET_TELEFONO, mascota.vetTelefono)
            put(TablaMascotas.VET_CORREO, mascota.vetCorreo)
        }
    }
}
