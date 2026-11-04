package com.example.patitasaldia.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Nombres de la tabla "mascotas" y sus columnas. */
object TablaMascotas {
    const val NOMBRE = "mascotas"
    const val ID = "id"
    const val NOMBRE_MASCOTA = "nombre"
    const val ESPECIE = "especie"
    const val RAZA = "raza"
    const val SEXO = "sexo"
    const val FECHA_NACIMIENTO = "fecha_nacimiento"
    const val PESO = "peso"
    const val FOTO_RUTA = "foto_ruta"
    const val VET_NOMBRE = "vet_nombre"
    const val VET_TELEFONO = "vet_telefono"
    const val VET_CORREO = "vet_correo"
}

/** Nombres de la tabla "vacunas" y sus columnas. */
object TablaVacunas {
    const val NOMBRE = "vacunas"
    const val ID = "id"
    const val MASCOTA_ID = "mascota_id"
    const val NOMBRE_VACUNA = "nombre"
    const val FECHA_APLICACION = "fecha_aplicacion"
    const val PROXIMA_DOSIS = "proxima_dosis"
    const val NOTAS = "notas"
}

/** Nombres de la tabla "citas" y sus columnas. */
object TablaCitas {
    const val NOMBRE = "citas"
    const val ID = "id"
    const val MASCOTA_ID = "mascota_id"
    const val FECHA = "fecha"
    const val HORA = "hora"
    const val MOTIVO = "motivo"
    const val NOTAS = "notas"
}

/**
 * Crea y abre la base de datos SQLite de la app (archivo patitas.db).
 *
 * En la app se usa una sola instancia mediante [obtenerInstancia]. El
 * constructor queda público para que las pruebas puedan crear una base en
 * memoria (nombreBd = null) sin datos de prueba.
 */
class DbHelper(
    context: Context,
    nombreBd: String? = NOMBRE_BD,
    private val insertarDatosPrueba: Boolean = esDepuracion(context)
) : SQLiteOpenHelper(context, nombreBd, null, VERSION_BD) {

    /** Se llama cada vez que se abre la base, antes de onCreate/onUpgrade. */
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Sin esto SQLite ignora las FOREIGN KEY y el ON DELETE CASCADE
        db.setForeignKeyConstraintsEnabled(true)
    }

    /** Se llama una sola vez, cuando el archivo de la base todavía no existe. */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${TablaMascotas.NOMBRE} (
                ${TablaMascotas.ID}               INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TablaMascotas.NOMBRE_MASCOTA}   TEXT    NOT NULL,
                ${TablaMascotas.ESPECIE}          TEXT    NOT NULL,
                ${TablaMascotas.RAZA}             TEXT,
                ${TablaMascotas.SEXO}             TEXT,
                ${TablaMascotas.FECHA_NACIMIENTO} TEXT,
                ${TablaMascotas.PESO}             REAL,
                ${TablaMascotas.FOTO_RUTA}        TEXT,
                ${TablaMascotas.VET_NOMBRE}       TEXT,
                ${TablaMascotas.VET_TELEFONO}     TEXT,
                ${TablaMascotas.VET_CORREO}       TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${TablaVacunas.NOMBRE} (
                ${TablaVacunas.ID}               INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TablaVacunas.MASCOTA_ID}       INTEGER NOT NULL,
                ${TablaVacunas.NOMBRE_VACUNA}    TEXT    NOT NULL,
                ${TablaVacunas.FECHA_APLICACION} TEXT    NOT NULL,
                ${TablaVacunas.PROXIMA_DOSIS}    TEXT,
                ${TablaVacunas.NOTAS}            TEXT,
                FOREIGN KEY (${TablaVacunas.MASCOTA_ID})
                    REFERENCES ${TablaMascotas.NOMBRE}(${TablaMascotas.ID}) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${TablaCitas.NOMBRE} (
                ${TablaCitas.ID}         INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TablaCitas.MASCOTA_ID} INTEGER NOT NULL,
                ${TablaCitas.FECHA}      TEXT    NOT NULL,
                ${TablaCitas.HORA}       TEXT,
                ${TablaCitas.MOTIVO}     TEXT    NOT NULL,
                ${TablaCitas.NOTAS}      TEXT,
                FOREIGN KEY (${TablaCitas.MASCOTA_ID})
                    REFERENCES ${TablaMascotas.NOMBRE}(${TablaMascotas.ID}) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        if (insertarDatosPrueba) {
            DatosPrueba.insertar(db)
        }
    }

    /**
     * Se llama cuando VERSION_BD es mayor que la versión guardada en el teléfono.
     * Como la app aún no se ha publicado, basta con borrar las tablas y recrearlas.
     * Si algún día hay datos reales que conservar, aquí irían los ALTER TABLE.
     */
    override fun onUpgrade(db: SQLiteDatabase, versionAnterior: Int, versionNueva: Int) {
        // Primero las tablas hijas, luego la tabla padre
        db.execSQL("DROP TABLE IF EXISTS ${TablaCitas.NOMBRE}")
        db.execSQL("DROP TABLE IF EXISTS ${TablaVacunas.NOMBRE}")
        db.execSQL("DROP TABLE IF EXISTS ${TablaMascotas.NOMBRE}")
        onCreate(db)
    }

    companion object {
        const val NOMBRE_BD = "patitas.db"
        const val VERSION_BD = 1

        @Volatile
        private var instancia: DbHelper? = null

        /** Devuelve la única instancia de DbHelper de la app (patrón singleton). */
        fun obtenerInstancia(context: Context): DbHelper =
            instancia ?: synchronized(this) {
                instancia ?: DbHelper(context.applicationContext).also { instancia = it }
            }

        /** Indica si la app se compiló en modo depuración (debug). */
        private fun esDepuracion(context: Context): Boolean =
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}
