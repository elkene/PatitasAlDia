package com.example.patitasaldia.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Conversión y cálculo de fechas.
 *
 * - En la base de datos las fechas se guardan como yyyy-MM-dd y las horas como HH:mm.
 * - En pantalla las fechas se muestran como dd/MM/yyyy.
 *
 * Se crea un SimpleDateFormat nuevo en cada llamada porque esa clase no es
 * segura para usarse desde varios hilos a la vez.
 */
object FechaUtils {

    private const val FORMATO_BD = "yyyy-MM-dd"
    private const val FORMATO_PANTALLA = "dd/MM/yyyy"
    private const val FORMATO_HORA = "HH:mm"
    private const val MILIS_POR_DIA = 24L * 60 * 60 * 1000

    private fun formato(patron: String, utc: Boolean = false) =
        SimpleDateFormat(patron, Locale.US).apply {
            isLenient = false // rechaza fechas imposibles como 31/02/2026
            if (utc) timeZone = TimeZone.getTimeZone("UTC")
        }

    /** yyyy-MM-dd -> dd/MM/yyyy. Regresa null si la fecha es nula o no es válida. */
    fun bdAPantalla(fechaBd: String?): String? {
        if (fechaBd.isNullOrBlank()) return null
        return try {
            formato(FORMATO_PANTALLA).format(formato(FORMATO_BD).parse(fechaBd)!!)
        } catch (e: ParseException) {
            null
        }
    }

    /** dd/MM/yyyy -> yyyy-MM-dd. Regresa null si el texto está vacío o no es una fecha válida. */
    fun pantallaABd(fechaPantalla: String?): String? {
        if (fechaPantalla.isNullOrBlank()) return null
        return try {
            formato(FORMATO_BD).format(formato(FORMATO_PANTALLA).parse(fechaPantalla.trim())!!)
        } catch (e: ParseException) {
            null
        }
    }

    /** Fecha de hoy en formato yyyy-MM-dd. */
    fun hoy(): String = formato(FORMATO_BD).format(Date())

    /** Hora actual en formato HH:mm. */
    fun horaActual(): String = formato(FORMATO_HORA).format(Date())

    /**
     * MaterialDatePicker trabaja con milisegundos en UTC. Se convierten usando
     * también UTC para que la fecha no se recorra un día por la zona horaria.
     */
    fun milisUtcABd(milis: Long): String = formato(FORMATO_BD, utc = true).format(Date(milis))

    /** yyyy-MM-dd -> milisegundos UTC (para preseleccionar el calendario). */
    fun bdAMilisUtc(fechaBd: String?): Long? {
        if (fechaBd.isNullOrBlank()) return null
        return try {
            formato(FORMATO_BD, utc = true).parse(fechaBd)!!.time
        } catch (e: ParseException) {
            null
        }
    }

    /**
     * Días que faltan desde [hoyBd] hasta [fechaBd]. Es negativo si la fecha ya pasó
     * y 0 si es hoy. Ambas fechas se interpretan en UTC, así que la resta es exacta.
     */
    fun diasDesde(hoyBd: String, fechaBd: String): Long? {
        val fecha = bdAMilisUtc(fechaBd) ?: return null
        val hoy = bdAMilisUtc(hoyBd) ?: return null
        return (fecha - hoy) / MILIS_POR_DIA
    }

    /**
     * Meses completos entre dos fechas yyyy-MM-dd (por ejemplo, para calcular la edad).
     * Regresa null si alguna fecha no es válida o si [desdeBd] es posterior a [hastaBd].
     */
    fun mesesCumplidos(desdeBd: String, hastaBd: String): Int? {
        if (bdAMilisUtc(desdeBd) == null || bdAMilisUtc(hastaBd) == null) return null
        val (anio1, mes1, dia1) = desdeBd.split("-").map { it.toInt() }
        val (anio2, mes2, dia2) = hastaBd.split("-").map { it.toInt() }
        var meses = (anio2 - anio1) * 12 + (mes2 - mes1)
        if (dia2 < dia1) meses-- // todavía no se cumple el mes en curso
        return meses.takeIf { it >= 0 }
    }
}
