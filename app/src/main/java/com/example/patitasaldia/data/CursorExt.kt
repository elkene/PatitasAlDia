package com.example.patitasaldia.data

import android.database.Cursor

/*
 * Funciones auxiliares para leer columnas de un Cursor por su nombre.
 * Las versiones "OrNull" regresan null cuando la columna tiene NULL en SQLite.
 */

internal fun Cursor.leerLong(columna: String): Long =
    getLong(getColumnIndexOrThrow(columna))

internal fun Cursor.leerString(columna: String): String =
    getString(getColumnIndexOrThrow(columna))

internal fun Cursor.leerStringOrNull(columna: String): String? {
    val indice = getColumnIndexOrThrow(columna)
    return if (isNull(indice)) null else getString(indice)
}

internal fun Cursor.leerDoubleOrNull(columna: String): Double? {
    val indice = getColumnIndexOrThrow(columna)
    return if (isNull(indice)) null else getDouble(indice)
}
