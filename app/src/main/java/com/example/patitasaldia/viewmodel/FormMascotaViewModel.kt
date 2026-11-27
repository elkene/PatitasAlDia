package com.example.patitasaldia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.patitasaldia.data.DbHelper
import com.example.patitasaldia.data.MascotaDao
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.util.FotoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ViewModel del formulario de mascota (alta y edición).
 *
 * Usa SavedStateHandle para las rutas de las fotos: a diferencia de las
 * variables normales del ViewModel, lo que se guarda ahí sobrevive aunque
 * Android cierre la app en segundo plano (requisito 5). El id de la mascota
 * también llega aquí solo: SavedStateHandle recibe los extras del Intent.
 */
class FormMascotaViewModel(
    application: Application,
    private val estado: SavedStateHandle
) : AndroidViewModel(application) {

    private val mascotaDao = MascotaDao(DbHelper.obtenerInstancia(application))

    /** Id de la mascota que se edita, o null en modo alta. */
    val mascotaId: Long? = estado.get<Long>(EXTRA_MASCOTA_ID)?.takeIf { it > 0 }

    val esEdicion: Boolean get() = mascotaId != null

    /** Ruta de la foto que se muestra en el formulario (puede ser nueva o la original). */
    val fotoRuta: LiveData<String?> = estado.getLiveData(CLAVE_FOTO, null)

    /** Foto con la que se abrió el formulario. Solo se borra si el usuario guarda otra. */
    private val fotoOriginal: String? get() = estado[CLAVE_FOTO_ORIGINAL]

    private val _mascotaParaEditar = MutableLiveData<Mascota?>()
    /** Mascota cargada para llenar los campos en modo edición (se consume una sola vez). */
    val mascotaParaEditar: LiveData<Mascota?> = _mascotaParaEditar

    private val _guardado = MutableLiveData(false)
    /** Cambia a true cuando la mascota se guardó; la actividad entonces se cierra. */
    val guardado: LiveData<Boolean> = _guardado

    private val _errorFoto = MutableLiveData(false)
    /** Cambia a true si no se pudo copiar la imagen de la galería. */
    val errorFoto: LiveData<Boolean> = _errorFoto

    /** Lee la mascota de la base para llenar el formulario en modo edición. */
    fun cargarParaEditar() {
        val id = mascotaId ?: return
        viewModelScope.launch {
            val mascota = withContext(Dispatchers.IO) { mascotaDao.obtenerPorId(id) } ?: return@launch
            // De vuelta en el hilo principal: SavedStateHandle actualiza LiveData
            estado[CLAVE_FOTO_ORIGINAL] = mascota.fotoRuta
            estado[CLAVE_FOTO] = mascota.fotoRuta
            _mascotaParaEditar.value = mascota
        }
    }

    /** La actividad llama a esto después de llenar los campos, para no volver a llenarlos. */
    fun mascotaParaEditarMostrada() {
        _mascotaParaEditar.value = null
    }

    /**
     * Crea el archivo donde la cámara guardará la foto y regresa su Uri.
     * La ruta se guarda en SavedStateHandle por si Android cierra la app
     * mientras la cámara está abierta.
     */
    fun prepararArchivoCamara(): Uri {
        val archivo = FotoUtils.crearArchivo(getApplication())
        estado[CLAVE_FOTO_PENDIENTE] = archivo.absolutePath
        return FotoUtils.uriParaCompartir(getApplication(), archivo)
    }

    /** Resultado de la cámara: si se tomó la foto se usa; si se canceló se borra el archivo vacío. */
    fun resultadoCamara(exito: Boolean) {
        val pendiente = estado.remove<String>(CLAVE_FOTO_PENDIENTE) ?: return
        if (exito && File(pendiente).length() > 0) {
            reemplazarFoto(pendiente)
        } else {
            FotoUtils.borrar(pendiente)
        }
    }

    /** Copia la imagen de la galería a la carpeta de la app (en un hilo de fondo). */
    fun importarDeGaleria(uri: Uri) {
        viewModelScope.launch {
            val archivo = withContext(Dispatchers.IO) {
                FotoUtils.copiarDesde(getApplication(), uri)
            }
            if (archivo != null) reemplazarFoto(archivo.absolutePath) else _errorFoto.value = true
        }
    }

    fun errorFotoMostrado() {
        _errorFoto.value = false
    }

    fun quitarFoto() = reemplazarFoto(null)

    /**
     * Cambia la foto del formulario. Si la foto anterior era nueva (tomada en
     * este formulario), se borra porque ya no se va a usar. La original no se
     * borra hasta que el usuario guarde.
     */
    private fun reemplazarFoto(nuevaRuta: String?) {
        val actual = fotoRuta.value
        if (actual != null && actual != fotoOriginal && actual != nuevaRuta) {
            FotoUtils.borrar(actual)
        }
        estado[CLAVE_FOTO] = nuevaRuta
    }

    /** Inserta o actualiza la mascota en un hilo de fondo. */
    fun guardar(mascota: Mascota) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val id = mascotaId
                if (id == null) {
                    mascotaDao.insertar(mascota)
                } else {
                    mascotaDao.actualizar(mascota.copy(id = id))
                }
                // Si se cambió o quitó la foto, la original ya no se usa
                val original = fotoOriginal
                if (original != null && original != mascota.fotoRuta) FotoUtils.borrar(original)
            }
            _guardado.value = true
        }
    }

    /**
     * Se llama cuando el usuario sale del formulario sin guardar: borra las
     * fotos nuevas que se tomaron o eligieron, porque ninguna mascota las usa.
     */
    fun descartarCambios() {
        if (_guardado.value == true) return
        val actual = fotoRuta.value
        if (actual != null && actual != fotoOriginal) FotoUtils.borrar(actual)
        estado.remove<String>(CLAVE_FOTO_PENDIENTE)?.let { FotoUtils.borrar(it) }
    }

    companion object {
        /** Debe coincidir con el extra que pone FormMascotaActivity.crearIntent. */
        const val EXTRA_MASCOTA_ID = "mascota_id"

        private const val CLAVE_FOTO = "foto_ruta"
        private const val CLAVE_FOTO_ORIGINAL = "foto_original"
        private const val CLAVE_FOTO_PENDIENTE = "foto_pendiente"
    }
}
