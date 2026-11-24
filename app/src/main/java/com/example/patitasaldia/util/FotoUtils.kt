package com.example.patitasaldia.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/**
 * Manejo de los archivos de foto de las mascotas.
 *
 * Las fotos se guardan en el almacenamiento interno de la app (filesDir/fotos),
 * que no necesita permisos y se borra al desinstalar la app. En la base de
 * datos solo se guarda la ruta del archivo.
 */
object FotoUtils {

    private const val CARPETA = "fotos"
    private const val PREFIJO = "mascota_"
    private const val EXTENSION = ".jpg"

    private fun carpeta(context: Context): File =
        File(context.filesDir, CARPETA).apply { mkdirs() }

    /** Crea un archivo vacío con nombre único, por ejemplo mascota_123456.jpg. */
    fun crearArchivo(context: Context): File =
        File.createTempFile(PREFIJO, EXTENSION, carpeta(context))

    /**
     * Uri content:// para compartir el archivo con la app de cámara. No se puede
     * pasar la ruta directa (file://) porque otra app no tiene acceso a ella;
     * el FileProvider concede ese acceso de forma temporal.
     */
    fun uriParaCompartir(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    /**
     * Copia la imagen elegida en la galería a la carpeta de la app, porque el
     * Uri de la galería puede dejar de ser válido más adelante.
     * Debe llamarse en un hilo de fondo. Regresa null si no se pudo copiar.
     */
    fun copiarDesde(context: Context, uri: Uri): File? {
        val destino = crearArchivo(context)
        return try {
            val entrada = context.contentResolver.openInputStream(uri)
                ?: throw IOException("No se pudo abrir la imagen")
            entrada.use { origen -> destino.outputStream().use { origen.copyTo(it) } }
            destino
        } catch (e: IOException) {
            destino.delete()
            null
        } catch (e: SecurityException) {
            destino.delete()
            null
        }
    }

    /** Borra el archivo de foto, si existe. */
    fun borrar(ruta: String?) {
        if (ruta != null) File(ruta).delete()
    }
}
