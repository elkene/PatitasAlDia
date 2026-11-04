package com.example.patitasaldia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.patitasaldia.data.DbHelper
import com.example.patitasaldia.data.MascotaDao
import com.example.patitasaldia.model.Mascota
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla principal. Conserva la lista de mascotas mientras
 * la actividad se destruye y se vuelve a crear (por ejemplo, al rotar).
 *
 * Hereda de AndroidViewModel para tener acceso al contexto de la aplicación,
 * que se necesita para abrir la base de datos.
 */
class MascotasViewModel(application: Application) : AndroidViewModel(application) {

    private val mascotaDao = MascotaDao(DbHelper.obtenerInstancia(application))

    // Solo el ViewModel puede modificar la lista; la interfaz solo la observa
    private val _mascotas = MutableLiveData<List<Mascota>>()
    val mascotas: LiveData<List<Mascota>> = _mascotas

    /**
     * Lee las mascotas de la base en un hilo de fondo y publica el resultado.
     * postValue se usa en lugar de "value" porque se llama fuera del hilo principal.
     */
    fun cargarMascotas() {
        viewModelScope.launch(Dispatchers.IO) {
            _mascotas.postValue(mascotaDao.obtenerTodas())
        }
    }
}
