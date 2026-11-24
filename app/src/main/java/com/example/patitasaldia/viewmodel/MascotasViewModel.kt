package com.example.patitasaldia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.patitasaldia.data.DbHelper
import com.example.patitasaldia.data.MascotaDao
import com.example.patitasaldia.model.Mascota
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla principal. Lo comparten MainActivity y
 * ListaMascotasFragment.
 *
 * - La lista de mascotas se conserva al rotar porque el ViewModel sobrevive.
 * - El id de la mascota seleccionada (tableta) se guarda en SavedStateHandle,
 *   que además sobrevive si Android cierra la app en segundo plano.
 */
class MascotasViewModel(
    application: Application,
    private val estado: SavedStateHandle
) : AndroidViewModel(application) {

    private val mascotaDao = MascotaDao(DbHelper.obtenerInstancia(application))

    // Solo el ViewModel puede modificar la lista; la interfaz solo la observa
    private val _mascotas = MutableLiveData<List<Mascota>>()
    val mascotas: LiveData<List<Mascota>> = _mascotas

    /** Id de la mascota que se muestra en el panel derecho de la tableta, o null. */
    val seleccionadaId: LiveData<Long?> = estado.getLiveData(CLAVE_SELECCIONADA, null)

    /**
     * Lee las mascotas de la base en un hilo de fondo y publica el resultado.
     * postValue se usa en lugar de "value" porque se llama fuera del hilo principal.
     */
    fun cargarMascotas() {
        viewModelScope.launch(Dispatchers.IO) {
            _mascotas.postValue(mascotaDao.obtenerTodas())
        }
    }

    /** Marca la mascota seleccionada en la tableta (null para quitar la selección). */
    fun seleccionar(id: Long?) {
        estado[CLAVE_SELECCIONADA] = id
    }

    private companion object {
        const val CLAVE_SELECCIONADA = "mascota_seleccionada"
    }
}
