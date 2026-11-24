package com.example.patitasaldia.model

import com.example.patitasaldia.util.FechaUtils

/**
 * Situación de una vacuna según la fecha de su próxima dosis.
 * Se usa para resaltar en el detalle las vacunas vencidas o por vencer.
 */
enum class EstadoVacuna {
    /** La próxima dosis ya pasó. */
    VENCIDA,

    /** La próxima dosis es hoy o en menos de 30 días. */
    PROXIMA,

    /** Faltan 30 días o más para la próxima dosis. */
    AL_DIA,

    /** La vacuna no tiene próxima dosis registrada. */
    SIN_PROXIMA_DOSIS;

    companion object {
        const val DIAS_AVISO = 30

        /** @param hoyBd fecha de hoy (yyyy-MM-dd); se puede cambiar en las pruebas. */
        fun calcular(proximaDosis: String?, hoyBd: String = FechaUtils.hoy()): EstadoVacuna {
            if (proximaDosis == null) return SIN_PROXIMA_DOSIS
            val dias = FechaUtils.diasDesde(hoyBd, proximaDosis) ?: return SIN_PROXIMA_DOSIS
            return when {
                dias < 0 -> VENCIDA
                dias < DIAS_AVISO -> PROXIMA
                else -> AL_DIA
            }
        }
    }
}
