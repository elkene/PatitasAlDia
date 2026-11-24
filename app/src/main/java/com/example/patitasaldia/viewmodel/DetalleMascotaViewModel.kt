package com.example.patitasaldia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.patitasaldia.data.CitaDao
import com.example.patitasaldia.data.DbHelper
import com.example.patitasaldia.data.MascotaDao
import com.example.patitasaldia.data.VacunaDao
import com.example.patitasaldia.model.Cita
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.model.Vacuna
import com.example.patitasaldia.util.FotoUtils
import com.example.patitasaldia.util.OrganizadorCitas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel del detalle de una mascota: sus datos, vacunas y citas.
 *
 * Pertenece a DetalleMascotaFragment. El id de la mascota llega por los
 * arguments del fragmento, que el SavedStateHandle recibe automáticamente.
 */
class DetalleMascotaViewModel(
    application: Application,
    estado: SavedStateHandle
) : AndroidViewModel(application) {

    private val dbHelper = DbHelper.obtenerInstancia(application)
    private val mascotaDao = MascotaDao(dbHelper)
    private val vacunaDao = VacunaDao(dbHelper)
    private val citaDao = CitaDao(dbHelper)

    val mascotaId: Long = requireNotNull(estado.get<Long>(ARG_MASCOTA_ID)) {
        "DetalleMascotaViewModel necesita el id de la mascota"
    }

    private val _mascota = MutableLiveData<Mascota>()
    val mascota: LiveData<Mascota> = _mascota

    private val _vacunas = MutableLiveData<List<Vacuna>>()
    val vacunas: LiveData<List<Vacuna>> = _vacunas

    private val _citasProximas = MutableLiveData<List<Cita>>()
    val citasProximas: LiveData<List<Cita>> = _citasProximas

    private val _citasPasadas = MutableLiveData<List<Cita>>()
    val citasPasadas: LiveData<List<Cita>> = _citasPasadas

    private val _eliminada = MutableLiveData(false)
    /** Cambia a true si la mascota se eliminó o ya no existe. */
    val eliminada: LiveData<Boolean> = _eliminada

    /** Lee la mascota, sus vacunas y sus citas en un hilo de fondo. */
    fun cargar() {
        viewModelScope.launch(Dispatchers.IO) { cargarDatos() }
    }

    /** Debe llamarse ya dentro de un hilo de fondo. */
    private fun cargarDatos() {
        val mascota = mascotaDao.obtenerPorId(mascotaId)
        if (mascota == null) {
            _eliminada.postValue(true)
            return
        }
        val citas = OrganizadorCitas.separar(citaDao.obtenerPorMascota(mascotaId))
        _mascota.postValue(mascota)
        _vacunas.postValue(vacunaDao.obtenerPorMascota(mascotaId))
        _citasProximas.postValue(citas.proximas)
        _citasPasadas.postValue(citas.pasadas)
    }

    /** Inserta la vacuna si es nueva (id 0) o la actualiza, y recarga. */
    fun guardarVacuna(vacuna: Vacuna) {
        viewModelScope.launch(Dispatchers.IO) {
            if (vacuna.id == 0L) vacunaDao.insertar(vacuna) else vacunaDao.actualizar(vacuna)
            cargarDatos()
        }
    }

    fun eliminarVacuna(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            vacunaDao.eliminar(id)
            cargarDatos()
        }
    }

    /** Inserta la cita si es nueva (id 0) o la actualiza, y recarga. */
    fun guardarCita(cita: Cita) {
        viewModelScope.launch(Dispatchers.IO) {
            if (cita.id == 0L) citaDao.insertar(cita) else citaDao.actualizar(cita)
            cargarDatos()
        }
    }

    fun eliminarCita(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            citaDao.eliminar(id)
            cargarDatos()
        }
    }

    /**
     * Elimina la mascota (sus vacunas y citas se borran por el ON DELETE CASCADE)
     * y también el archivo de su foto.
     */
    fun eliminarMascota() {
        viewModelScope.launch(Dispatchers.IO) {
            val fotoRuta = mascotaDao.obtenerPorId(mascotaId)?.fotoRuta
            mascotaDao.eliminar(mascotaId)
            FotoUtils.borrar(fotoRuta)
            _eliminada.postValue(true)
        }
    }

    companion object {
        const val ARG_MASCOTA_ID = "mascota_id"
    }
}
