package com.example.patitasaldia.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import com.example.patitasaldia.R
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.model.Vacuna
import java.text.DecimalFormat

/**
 * Comunicación con el veterinario mediante Intents implícitos (requisito 8).
 *
 * Un Intent implícito no dice qué app abrir, solo qué se quiere hacer
 * (marcar, mandar mensaje, mandar correo); Android elige la app. Ninguna de
 * estas acciones necesita permisos porque el usuario termina la acción en la
 * otra app.
 */
object ComunicacionUtils {

    private val FORMATO_PESO = DecimalFormat("0.##")

    /** Abre el marcador con el número escrito. No llama directamente. */
    fun llamar(context: Context, telefono: String) {
        lanzar(context, Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", telefono, null)))
    }

    /** Abre la app de mensajes con un texto para pedir una cita. */
    fun enviarSms(context: Context, telefono: String, nombreMascota: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", telefono, null))
            .putExtra("sms_body", context.getString(R.string.sms_texto, nombreMascota))
        lanzar(context, intent)
    }

    /** Abre el cliente de correo con el historial de vacunas en el cuerpo del mensaje. */
    fun enviarHistorial(context: Context, correo: String, mascota: Mascota, vacunas: List<Vacuna>) {
        // "mailto:" hace que solo aparezcan apps de correo
        val intent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri())
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(correo))
            .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.correo_asunto, mascota.nombre))
            .putExtra(Intent.EXTRA_TEXT, armarHistorial(context, mascota, vacunas))
        lanzar(context, intent)
    }

    /** Texto del correo: datos de la mascota y lista de vacunas con sus fechas. */
    fun armarHistorial(context: Context, mascota: Mascota, vacunas: List<Vacuna>): String =
        buildString {
            appendLine(context.getString(R.string.correo_saludo))
            appendLine()
            appendLine(context.getString(R.string.correo_nombre, mascota.nombre))
            appendLine(context.getString(R.string.correo_especie, context.textoEspecie(mascota.especie)))
            mascota.raza?.let { appendLine(context.getString(R.string.correo_raza, it)) }
            mascota.sexo?.let { appendLine(context.getString(R.string.correo_sexo, context.textoSexo(it))) }
            FechaUtils.bdAPantalla(mascota.fechaNacimiento)?.let {
                appendLine(context.getString(R.string.correo_nacimiento, it))
            }
            mascota.peso?.let {
                appendLine(context.getString(R.string.correo_peso, FORMATO_PESO.format(it)))
            }
            appendLine()
            appendLine(context.getString(R.string.correo_titulo_vacunas))
            if (vacunas.isEmpty()) {
                appendLine(context.getString(R.string.correo_sin_vacunas))
            }
            vacunas.forEach { vacuna ->
                append(
                    context.getString(
                        R.string.correo_vacuna,
                        vacuna.nombre,
                        FechaUtils.bdAPantalla(vacuna.fechaAplicacion) ?: vacuna.fechaAplicacion
                    )
                )
                FechaUtils.bdAPantalla(vacuna.proximaDosis)?.let {
                    append(context.getString(R.string.correo_vacuna_proxima, it))
                }
                vacuna.notas?.takeIf { it.isNotBlank() }?.let {
                    append(context.getString(R.string.correo_vacuna_notas, it))
                }
                appendLine()
            }
            appendLine()
            append(context.getString(R.string.correo_firma))
        }

    /**
     * Abre la otra app. Si no hay ninguna que pueda hacerlo (por ejemplo, una
     * tableta sin SMS), muestra un aviso en lugar de cerrarse con error.
     */
    private fun lanzar(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.error_sin_app, Toast.LENGTH_LONG).show()
        }
    }
}
