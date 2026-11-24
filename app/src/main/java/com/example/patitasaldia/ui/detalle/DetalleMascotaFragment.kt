package com.example.patitasaldia.ui.detalle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.example.patitasaldia.R
import com.example.patitasaldia.databinding.FragmentDetalleMascotaBinding
import com.example.patitasaldia.model.Cita
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.model.Vacuna
import com.example.patitasaldia.ui.dialogs.ConfirmarEliminarDialog
import com.example.patitasaldia.ui.dialogs.NuevaCitaDialog
import com.example.patitasaldia.ui.dialogs.NuevaVacunaDialog
import com.example.patitasaldia.ui.form.FormMascotaActivity
import com.example.patitasaldia.util.ComunicacionUtils
import com.example.patitasaldia.util.FechaUtils
import com.example.patitasaldia.util.mostrarFotoMascota
import com.example.patitasaldia.util.textoEspecie
import com.example.patitasaldia.util.textoSexo
import com.example.patitasaldia.viewmodel.DetalleMascotaViewModel
import java.text.DecimalFormat

/**
 * Detalle de una mascota: foto, datos, botones de comunicación, vacunas y citas.
 *
 * Se usa en dos lugares:
 * - En teléfono, dentro de DetalleMascotaActivity.
 * - En tableta, en el panel derecho de MainActivity.
 * Por eso no conoce a la actividad: le avisa lo importante con la interfaz Listener.
 */
class DetalleMascotaFragment : Fragment() {

    /** Lo implementa la actividad que contiene este fragmento. */
    interface Listener {
        /** Se llama cada vez que se cargan los datos (por ejemplo, para poner el título). */
        fun onMascotaCargada(mascota: Mascota) {}

        /** Se llama cuando la mascota se eliminó o ya no existe. */
        fun onMascotaEliminada()
    }

    private var _binding: FragmentDetalleMascotaBinding? = null
    private val binding get() = _binding!!

    // ViewModel propio del fragmento; recibe el id por los arguments
    private val viewModel: DetalleMascotaViewModel by viewModels()

    /** Id de la mascota que muestra este fragmento. */
    val mascotaId: Long get() = requireArguments().getLong(DetalleMascotaViewModel.ARG_MASCOTA_ID)

    private val listener: Listener? get() = activity as? Listener

    private val vacunaAdapter = VacunaAdapter(
        alTocar = { vacuna -> mostrarDialogo(NuevaVacunaDialog.editar(vacuna), NuevaVacunaDialog.ETIQUETA) },
        alEliminar = { vacuna ->
            confirmarEliminar(
                getString(R.string.confirmar_eliminar_vacuna, vacuna.nombre),
                ConfirmarEliminarDialog.TIPO_VACUNA, vacuna.id
            )
        }
    )

    private val alTocarCita = { cita: Cita ->
        mostrarDialogo(NuevaCitaDialog.editar(cita), NuevaCitaDialog.ETIQUETA)
    }
    private val alEliminarCita = { cita: Cita ->
        confirmarEliminar(
            getString(R.string.confirmar_eliminar_cita, FechaUtils.bdAPantalla(cita.fecha) ?: cita.fecha),
            ConfirmarEliminarDialog.TIPO_CITA, cita.id
        )
    }
    private val citasProximasAdapter = CitaAdapter(esDePasadas = false, alTocarCita, alEliminarCita)
    private val citasPasadasAdapter = CitaAdapter(esDePasadas = true, alTocarCita, alEliminarCita)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Respuesta del diálogo de confirmación (también funciona después de rotar)
        childFragmentManager.setFragmentResultListener(
            ConfirmarEliminarDialog.CLAVE_RESULTADO, this
        ) { _, resultado ->
            val id = resultado.getLong(ConfirmarEliminarDialog.RESULTADO_ID)
            when (resultado.getString(ConfirmarEliminarDialog.RESULTADO_TIPO)) {
                ConfirmarEliminarDialog.TIPO_MASCOTA -> viewModel.eliminarMascota()
                ConfirmarEliminarDialog.TIPO_VACUNA -> viewModel.eliminarVacuna(id)
                ConfirmarEliminarDialog.TIPO_CITA -> viewModel.eliminarCita(id)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleMascotaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vacunas.listaVacunas.adapter = vacunaAdapter
        binding.citas.listaCitasProximas.adapter = citasProximasAdapter
        binding.citas.listaCitasPasadas.adapter = citasPasadasAdapter

        binding.vacunas.btnAgregarVacuna.setOnClickListener {
            mostrarDialogo(NuevaVacunaDialog.nueva(mascotaId), NuevaVacunaDialog.ETIQUETA)
        }
        binding.citas.btnAgregarCita.setOnClickListener {
            mostrarDialogo(NuevaCitaDialog.nueva(mascotaId), NuevaCitaDialog.ETIQUETA)
        }

        agregarAccionesDeLaBarra()
        observarViewModel()
    }

    /** Recarga cada vez que vuelve a primer plano (por ejemplo, después de editar la mascota). */
    override fun onResume() {
        super.onResume()
        viewModel.cargar()
    }

    /**
     * Agrega Editar y Eliminar a la barra superior de la actividad que contiene
     * este fragmento. Desaparecen solas cuando la vista del fragmento se destruye.
     */
    private fun agregarAccionesDeLaBarra() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_detalle_mascota, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.accion_editar -> {
                    startActivity(FormMascotaActivity.crearIntent(requireContext(), mascotaId))
                    true
                }
                R.id.accion_eliminar -> {
                    val nombre = viewModel.mascota.value?.nombre.orEmpty()
                    confirmarEliminar(
                        getString(R.string.confirmar_eliminar_mascota, nombre),
                        ConfirmarEliminarDialog.TIPO_MASCOTA, mascotaId
                    )
                    true
                }
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observarViewModel() {
        viewModel.mascota.observe(viewLifecycleOwner) { mascota ->
            mostrarMascota(mascota)
            listener?.onMascotaCargada(mascota)
        }

        viewModel.vacunas.observe(viewLifecycleOwner) { vacunas ->
            vacunaAdapter.submitList(vacunas)
            binding.vacunas.txtSinVacunas.isVisible = vacunas.isEmpty()
        }

        viewModel.citasProximas.observe(viewLifecycleOwner) { citas ->
            citasProximasAdapter.submitList(citas)
            actualizarSeccionCitas()
        }

        viewModel.citasPasadas.observe(viewLifecycleOwner) { citas ->
            citasPasadasAdapter.submitList(citas)
            actualizarSeccionCitas()
        }

        viewModel.eliminada.observe(viewLifecycleOwner) { eliminada ->
            if (eliminada) listener?.onMascotaEliminada()
        }
    }

    /** Oculta los subtítulos vacíos y muestra "Sin citas" si no hay ninguna. */
    private fun actualizarSeccionCitas() {
        val proximas = viewModel.citasProximas.value.orEmpty()
        val pasadas = viewModel.citasPasadas.value.orEmpty()
        with(binding.citas) {
            txtTituloProximas.isVisible = proximas.isNotEmpty()
            txtTituloPasadas.isVisible = pasadas.isNotEmpty()
            txtSinCitas.isVisible = proximas.isEmpty() && pasadas.isEmpty()
        }
    }

    private fun mostrarMascota(mascota: Mascota) {
        val contexto = requireContext()
        val info = binding.info
        val datos = info.datos

        info.imgFoto.mostrarFotoMascota(mascota.fotoRuta, mascota.especie, R.dimen.relleno_icono_detalle)
        info.imgFoto.contentDescription = getString(R.string.foto_de_mascota, mascota.nombre)
        info.txtNombre.text = mascota.nombre
        val especie = contexto.textoEspecie(mascota.especie)
        info.txtEspecieRaza.text = mascota.raza?.let {
            getString(R.string.detalle_especie_raza, especie, it)
        } ?: especie

        datos.txtSexo.isVisible = mascota.sexo != null
        datos.txtSexo.text = mascota.sexo?.let { getString(R.string.detalle_sexo, contexto.textoSexo(it)) }

        val nacimiento = FechaUtils.bdAPantalla(mascota.fechaNacimiento)
        datos.txtNacimiento.isVisible = nacimiento != null
        datos.txtNacimiento.text = nacimiento?.let { fecha ->
            val edad = textoEdad(mascota.fechaNacimiento!!)
            if (edad != null) {
                getString(R.string.detalle_nacimiento, fecha, edad)
            } else {
                getString(R.string.detalle_nacimiento_sin_edad, fecha)
            }
        }

        datos.txtPeso.isVisible = mascota.peso != null
        datos.txtPeso.text = mascota.peso?.let { getString(R.string.detalle_peso, FORMATO_PESO.format(it)) }

        // Veterinario
        datos.txtVetNombre.isVisible = mascota.vetNombre != null
        datos.txtVetNombre.text = mascota.vetNombre
        datos.txtVetTelefono.isVisible = mascota.vetTelefono != null
        datos.txtVetTelefono.text = mascota.vetTelefono?.let { getString(R.string.detalle_vet_telefono, it) }
        datos.txtVetCorreo.isVisible = mascota.vetCorreo != null
        datos.txtVetCorreo.text = mascota.vetCorreo?.let { getString(R.string.detalle_vet_correo, it) }
        datos.txtSinVet.isVisible =
            mascota.vetNombre == null && mascota.vetTelefono == null && mascota.vetCorreo == null

        configurarBotonesComunicacion(mascota)
    }

    /**
     * Botones para comunicarse con el veterinario (requisito 8). Se deshabilitan
     * si falta el teléfono o el correo.
     */
    private fun configurarBotonesComunicacion(mascota: Mascota) = with(binding.info.botones) {
        val telefono = mascota.vetTelefono
        val correo = mascota.vetCorreo
        btnLlamar.isEnabled = telefono != null
        btnSms.isEnabled = telefono != null
        btnCorreo.isEnabled = correo != null

        btnLlamar.setOnClickListener {
            telefono?.let { ComunicacionUtils.llamar(requireContext(), it) }
        }
        btnSms.setOnClickListener {
            telefono?.let { ComunicacionUtils.enviarSms(requireContext(), it, mascota.nombre) }
        }
        btnCorreo.setOnClickListener {
            correo?.let {
                val vacunas: List<Vacuna> = viewModel.vacunas.value.orEmpty()
                ComunicacionUtils.enviarHistorial(requireContext(), it, mascota, vacunas)
            }
        }
    }

    /** Edad legible: "5 años", "8 meses" o "menos de un mes". */
    private fun textoEdad(fechaNacimiento: String): String? {
        val meses = FechaUtils.mesesCumplidos(fechaNacimiento, FechaUtils.hoy()) ?: return null
        return when {
            meses >= MESES_POR_ANIO -> {
                val anios = meses / MESES_POR_ANIO
                resources.getQuantityString(R.plurals.edad_anios, anios, anios)
            }
            meses > 0 -> resources.getQuantityString(R.plurals.edad_meses, meses, meses)
            else -> getString(R.string.edad_menos_de_un_mes)
        }
    }

    /** Muestra un diálogo si no hay otro igual abierto (evita duplicados por doble toque). */
    private fun mostrarDialogo(dialogo: DialogFragment, etiqueta: String) {
        if (childFragmentManager.findFragmentByTag(etiqueta) == null) {
            dialogo.show(childFragmentManager, etiqueta)
        }
    }

    private fun confirmarEliminar(mensaje: String, tipo: String, id: Long) {
        mostrarDialogo(ConfirmarEliminarDialog.nuevo(mensaje, tipo, id), ConfirmarEliminarDialog.ETIQUETA)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val MESES_POR_ANIO = 12
        private val FORMATO_PESO = DecimalFormat("0.##")

        /** Crea el fragmento para la mascota con ese id. */
        fun newInstance(mascotaId: Long) = DetalleMascotaFragment().apply {
            arguments = bundleOf(DetalleMascotaViewModel.ARG_MASCOTA_ID to mascotaId)
        }
    }
}
