package com.example.patitasaldia.ui.form

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.patitasaldia.R
import com.example.patitasaldia.databinding.ActivityFormMascotaBinding
import com.example.patitasaldia.model.Especie
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.model.Sexo
import com.example.patitasaldia.util.FechaUtils
import com.example.patitasaldia.util.Selectores
import com.example.patitasaldia.util.Validaciones
import com.example.patitasaldia.util.aplicarMargenesDelSistema
import com.example.patitasaldia.util.mostrarBorrarSoloConTexto
import com.example.patitasaldia.util.mostrarFotoMascota
import com.example.patitasaldia.viewmodel.FormMascotaViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formulario para agregar (sin id) o editar (con id) una mascota.
 *
 * Guardar el estado (requisito 5):
 * - Los campos de texto y los chips tienen id, así que Android conserva su
 *   contenido solo al rotar o si cierra la app en segundo plano.
 * - La foto se guarda en el SavedStateHandle del ViewModel.
 * - La fecha se guarda como el texto del campo (dd/MM/yyyy).
 */
class FormMascotaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormMascotaBinding
    private val viewModel: FormMascotaViewModel by viewModels()

    // Relación entre cada chip y el valor que se guarda en la base
    private val chipsEspecie by lazy {
        mapOf(
            R.id.chip_perro to Especie.PERRO,
            R.id.chip_gato to Especie.GATO,
            R.id.chip_ave to Especie.AVE,
            R.id.chip_otro to Especie.OTRO
        )
    }
    private val chipsSexo by lazy {
        mapOf(R.id.chip_macho to Sexo.MACHO, R.id.chip_hembra to Sexo.HEMBRA)
    }

    // Los "launchers" deben registrarse al crear la actividad, antes de que inicie
    private val tomarFoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        viewModel.resultadoCamara(exito)
    }
    private val elegirFoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.importarDeGaleria(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFormMascotaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.main.aplicarMargenesDelSistema(incluirTeclado = true)

        configurarBarraSuperior()
        configurarFoto()
        configurarCampos()
        observarViewModel()
        // Dibuja el ícono por defecto desde el inicio, aunque todavía no haya foto
        mostrarFoto(viewModel.fotoRuta.value)

        // Solo la primera vez: después, Android restaura lo que el usuario escribió
        if (savedInstanceState == null && viewModel.esEdicion) {
            viewModel.cargarParaEditar()
        }
    }

    private fun configurarBarraSuperior() {
        binding.barraSuperior.setTitle(
            if (viewModel.esEdicion) R.string.titulo_editar_mascota else R.string.titulo_nueva_mascota
        )
        binding.barraSuperior.setNavigationOnClickListener { finish() }
        binding.barraSuperior.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.accion_guardar) {
                guardar()
                true
            } else {
                false
            }
        }
    }

    private fun configurarFoto() {
        val foto = binding.foto
        // En dispositivos sin cámara (algunas tabletas) se oculta el botón
        foto.btnCamara.isVisible = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        foto.btnCamara.setOnClickListener { abrirCamara() }
        foto.btnGaleria.setOnClickListener {
            // El selector de fotos del sistema no necesita permisos
            elegirFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        foto.btnQuitarFoto.setOnClickListener { viewModel.quitarFoto() }
    }

    private fun abrirCamara() {
        val uri = viewModel.prepararArchivoCamara()
        try {
            tomarFoto.launch(uri)
        } catch (e: ActivityNotFoundException) {
            viewModel.resultadoCamara(false)
            Toast.makeText(this, R.string.error_sin_camara, Toast.LENGTH_LONG).show()
        }
    }

    private fun configurarCampos() {
        val campos = binding.campos
        val abrirCalendario = {
            Selectores.mostrarFecha(
                supportFragmentManager, ETIQUETA_FECHA, R.string.campo_fecha_nacimiento,
                FechaUtils.pantallaABd(campos.etFechaNacimiento.text?.toString()),
                soloPasado = true, alElegir = ::ponerFechaNacimiento
            )
        }
        campos.etFechaNacimiento.setOnClickListener { abrirCalendario() }
        campos.tilFechaNacimiento.setStartIconOnClickListener { abrirCalendario() }
        campos.tilFechaNacimiento.setEndIconOnClickListener { campos.etFechaNacimiento.setText("") }
        campos.tilFechaNacimiento.mostrarBorrarSoloConTexto()
        Selectores.reconectarFecha(supportFragmentManager, ETIQUETA_FECHA, ::ponerFechaNacimiento)

        // El ícono por defecto depende de la especie elegida
        campos.grupoEspecie.setOnCheckedStateChangeListener { _, _ ->
            campos.txtErrorEspecie.isVisible = false
            mostrarFoto(viewModel.fotoRuta.value)
        }
    }

    private fun ponerFechaNacimiento(fechaBd: String) {
        binding.campos.etFechaNacimiento.setText(FechaUtils.bdAPantalla(fechaBd))
    }

    private fun observarViewModel() {
        viewModel.fotoRuta.observe(this) { ruta -> mostrarFoto(ruta) }

        viewModel.mascotaParaEditar.observe(this) { mascota ->
            if (mascota != null) {
                llenarCampos(mascota)
                viewModel.mascotaParaEditarMostrada()
            }
        }

        viewModel.errorFoto.observe(this) { hayError ->
            if (hayError) {
                Toast.makeText(this, R.string.error_foto, Toast.LENGTH_LONG).show()
                viewModel.errorFotoMostrado()
            }
        }

        viewModel.guardado.observe(this) { guardado ->
            if (guardado) {
                Toast.makeText(this, R.string.mascota_guardada, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun mostrarFoto(ruta: String?) {
        binding.foto.imgFoto.mostrarFotoMascota(
            ruta, especieSeleccionada(), R.dimen.relleno_icono_formulario
        )
        binding.foto.btnQuitarFoto.isVisible = ruta != null
    }

    private fun especieSeleccionada(): String? =
        chipsEspecie[binding.campos.grupoEspecie.checkedChipId]

    private fun sexoSeleccionado(): String? = chipsSexo[binding.campos.grupoSexo.checkedChipId]

    /** Llena el formulario con los datos de la mascota que se va a editar. */
    private fun llenarCampos(mascota: Mascota) = with(binding.campos) {
        etNombre.setText(mascota.nombre)
        chipsEspecie.entries.firstOrNull { it.value == mascota.especie }?.let { grupoEspecie.check(it.key) }
        etRaza.setText(mascota.raza)
        chipsSexo.entries.firstOrNull { it.value == mascota.sexo }?.let { grupoSexo.check(it.key) }
        etFechaNacimiento.setText(FechaUtils.bdAPantalla(mascota.fechaNacimiento))
        // Se usa punto decimal porque es lo que acepta el teclado numérico
        etPeso.setText(mascota.peso?.let { FORMATO_PESO.format(it) })
        etVetNombre.setText(mascota.vetNombre)
        etVetTelefono.setText(mascota.vetTelefono)
        etVetCorreo.setText(mascota.vetCorreo)
    }

    /** Valida los campos y, si todo está bien, le pide al ViewModel que guarde. */
    private fun guardar() = with(binding.campos) {
        listOf(tilNombre, tilPeso, tilVetTelefono, tilVetCorreo).forEach { it.error = null }
        txtErrorEspecie.isVisible = false
        var valido = true

        val nombre = etNombre.text.toString().trim()
        if (nombre.isEmpty()) {
            tilNombre.error = getString(R.string.error_nombre_obligatorio)
            valido = false
        }

        val especie = especieSeleccionada()
        if (especie == null) {
            txtErrorEspecie.isVisible = true
            valido = false
        }

        val textoPeso = etPeso.text.toString().trim()
        val peso = if (textoPeso.isEmpty()) null else Validaciones.leerPeso(textoPeso)
        if (textoPeso.isNotEmpty() && (peso == null || !Validaciones.pesoValido(peso))) {
            tilPeso.error = getString(R.string.error_peso)
            valido = false
        }

        val telefono = etVetTelefono.text.toString().trim()
        if (telefono.isNotEmpty() && !Validaciones.telefonoValido(telefono)) {
            tilVetTelefono.error = getString(R.string.error_telefono)
            valido = false
        }

        val correo = etVetCorreo.text.toString().trim()
        if (correo.isNotEmpty() && !Validaciones.correoValido(correo)) {
            tilVetCorreo.error = getString(R.string.error_correo)
            valido = false
        }

        if (!valido || especie == null) return@with

        viewModel.guardar(
            Mascota(
                nombre = nombre,
                especie = especie,
                raza = etRaza.text.toString().trim().ifEmpty { null },
                sexo = sexoSeleccionado(),
                fechaNacimiento = FechaUtils.pantallaABd(etFechaNacimiento.text?.toString()),
                peso = peso,
                fotoRuta = viewModel.fotoRuta.value,
                vetNombre = etVetNombre.text.toString().trim().ifEmpty { null },
                vetTelefono = telefono.ifEmpty { null }?.let { Validaciones.limpiarTelefono(it) },
                vetCorreo = correo.ifEmpty { null }
            )
        )
    }

    override fun onDestroy() {
        // isFinishing distingue "el usuario salió" de "Android recreó la pantalla"
        if (isFinishing) viewModel.descartarCambios()
        super.onDestroy()
    }

    companion object {
        private const val ETIQUETA_FECHA = "selector_fecha_nacimiento"
        private val FORMATO_PESO = DecimalFormat("0.##", DecimalFormatSymbols(Locale.US))

        /**
         * Crea el Intent para abrir el formulario. Sin mascotaId abre el modo
         * alta; con mascotaId abre el modo edición de esa mascota.
         */
        fun crearIntent(context: Context, mascotaId: Long? = null): Intent =
            Intent(context, FormMascotaActivity::class.java).apply {
                if (mascotaId != null) putExtra(FormMascotaViewModel.EXTRA_MASCOTA_ID, mascotaId)
            }
    }
}
