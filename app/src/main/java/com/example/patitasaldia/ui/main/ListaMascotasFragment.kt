package com.example.patitasaldia.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.patitasaldia.databinding.FragmentListaMascotasBinding
import com.example.patitasaldia.model.Mascota
import com.example.patitasaldia.viewmodel.MascotasViewModel

/**
 * Muestra la lista de mascotas. Cuando el usuario toca una, avisa a la
 * actividad que lo contiene, que decide si abre el detalle en otra pantalla
 * (teléfono) o en el panel derecho (tableta).
 */
class ListaMascotasFragment : Fragment() {

    /** Lo implementa la actividad que contiene este fragmento. */
    interface AlSeleccionarMascota {
        fun onMascotaSeleccionada(mascota: Mascota)
    }

    // El binding solo existe mientras la vista existe (entre onCreateView y onDestroyView)
    private var _binding: FragmentListaMascotasBinding? = null
    private val binding get() = _binding!!

    // activityViewModels: el mismo ViewModel que usa MainActivity
    private val viewModel: MascotasViewModel by activityViewModels()

    private val adaptador = MascotaAdapter { mascota ->
        (activity as? AlSeleccionarMascota)?.onMascotaSeleccionada(mascota)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaMascotasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listaMascotas.adapter = adaptador

        viewModel.mascotas.observe(viewLifecycleOwner) { mascotas ->
            adaptador.submitList(mascotas)
            binding.txtSinMascotas.isVisible = mascotas.isEmpty()
        }
    }

    /**
     * Se recarga cada vez que el fragmento vuelve a primer plano, para que la
     * lista refleje los cambios hechos en el formulario o en el detalle.
     */
    override fun onResume() {
        super.onResume()
        viewModel.cargarMascotas()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Evita conservar referencias a vistas que ya se destruyeron
        _binding = null
    }
}
