package com.example.patitasaldia.util

import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Locale

/**
 * Muestra los selectores de fecha y hora de Material.
 *
 * Los selectores son fragmentos: al rotar, Android los vuelve a crear solo,
 * pero pierde la función que se ejecuta al presionar "Aceptar". Por eso cada
 * pantalla llama a reconectarFecha / reconectarHora al crearse.
 */
object Selectores {

    /**
     * @param fechaBd fecha preseleccionada (yyyy-MM-dd) o null para hoy.
     * @param soloPasado si es true no deja elegir fechas futuras (por ejemplo, el nacimiento).
     * @param alElegir recibe la fecha elegida en formato yyyy-MM-dd.
     */
    fun mostrarFecha(
        fragmentManager: FragmentManager,
        etiqueta: String,
        @StringRes titulo: Int,
        fechaBd: String?,
        soloPasado: Boolean,
        alElegir: (String) -> Unit
    ) {
        // Evita abrir dos selectores si el usuario toca dos veces seguidas
        if (fragmentManager.findFragmentByTag(etiqueta) != null) return

        val constructor = MaterialDatePicker.Builder.datePicker()
            .setTitleText(titulo)
            .setSelection(
                FechaUtils.bdAMilisUtc(fechaBd) ?: MaterialDatePicker.todayInUtcMilliseconds()
            )
        if (soloPasado) {
            constructor.setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                    .build()
            )
        }
        val selector = constructor.build()
        selector.addOnPositiveButtonClickListener { alElegir(FechaUtils.milisUtcABd(it)) }
        selector.show(fragmentManager, etiqueta)
    }

    /** Vuelve a conectar la acción de un selector de fecha que se recreó al rotar. */
    fun reconectarFecha(
        fragmentManager: FragmentManager,
        etiqueta: String,
        alElegir: (String) -> Unit
    ) {
        @Suppress("UNCHECKED_CAST")
        val selector = fragmentManager.findFragmentByTag(etiqueta) as? MaterialDatePicker<Long>
        selector?.addOnPositiveButtonClickListener { alElegir(FechaUtils.milisUtcABd(it)) }
    }

    /**
     * @param horaActual hora preseleccionada (HH:mm) o null para la hora actual.
     * @param alElegir recibe la hora elegida en formato HH:mm.
     */
    fun mostrarHora(
        fragmentManager: FragmentManager,
        etiqueta: String,
        @StringRes titulo: Int,
        horaActual: String?,
        alElegir: (String) -> Unit
    ) {
        if (fragmentManager.findFragmentByTag(etiqueta) != null) return

        val (hora, minuto) = (horaActual ?: FechaUtils.horaActual())
            .split(":").map { it.toIntOrNull() ?: 0 }
        val selector = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hora)
            .setMinute(minuto)
            .setTitleText(titulo)
            .build()
        selector.addOnPositiveButtonClickListener {
            alElegir(formatearHora(selector.hour, selector.minute))
        }
        selector.show(fragmentManager, etiqueta)
    }

    /** Vuelve a conectar la acción de un selector de hora que se recreó al rotar. */
    fun reconectarHora(
        fragmentManager: FragmentManager,
        etiqueta: String,
        alElegir: (String) -> Unit
    ) {
        val selector = fragmentManager.findFragmentByTag(etiqueta) as? MaterialTimePicker
        selector?.addOnPositiveButtonClickListener {
            alElegir(formatearHora(selector.hour, selector.minute))
        }
    }

    private fun formatearHora(hora: Int, minuto: Int) =
        String.format(Locale.US, "%02d:%02d", hora, minuto)
}
