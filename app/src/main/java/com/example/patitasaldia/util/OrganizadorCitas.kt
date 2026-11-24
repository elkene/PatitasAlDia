package com.example.patitasaldia.util

import com.example.patitasaldia.model.Cita

/** Citas de una mascota separadas en próximas y pasadas. */
data class CitasSeparadas(
    /** De la más cercana a la más lejana. */
    val proximas: List<Cita>,
    /** De la más reciente a la más antigua. */
    val pasadas: List<Cita>
)

object OrganizadorCitas {

    /**
     * Separa las citas en próximas y pasadas.
     *
     * Una cita es próxima si su fecha es posterior a hoy, o si es hoy y su hora
     * todavía no llega. Una cita de hoy sin hora se considera próxima todo el día.
     *
     * @param citas ordenadas por fecha y hora, como las regresa CitaDao.
     * @param hoyBd y [horaActual] se pueden cambiar en las pruebas.
     */
    fun separar(
        citas: List<Cita>,
        hoyBd: String = FechaUtils.hoy(),
        horaActual: String = FechaUtils.horaActual()
    ): CitasSeparadas {
        val (proximas, pasadas) = citas.partition { cita ->
            cita.fecha > hoyBd || (cita.fecha == hoyBd && (cita.hora == null || cita.hora >= horaActual))
        }
        return CitasSeparadas(proximas, pasadas.reversed())
    }
}
