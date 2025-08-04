package com.episi.recyclens.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.episi.recyclens.databinding.FragmentEditarReciclajeBinding
import com.episi.recyclens.model.Reciclaje
import com.episi.recyclens.viewmodel.ReciclajeViewModel
import com.google.firebase.firestore.ListenerRegistration

class EditarReciclajeFragment : Fragment() {
    private var _binding: FragmentEditarReciclajeBinding? = null
    private val binding get() = _binding!!
    private val reciclajeViewModel: ReciclajeViewModel by viewModels()
    private lateinit var reciclajeActual: Reciclaje
    private val args: EditarReciclajeFragmentArgs by navArgs()
    private val tipos = listOf("Papel", "Plástico", "Metal", "Orgánico")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditarReciclajeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configurarSpinner()
        cargarReciclaje(args.reciclajeId)
        configurarBotonActualizar()
    }

    private fun configurarSpinner() {
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tipos
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipo.adapter = spinnerAdapter
    }

    private fun cargarReciclaje(reciclajeId: String) {
        reciclajeViewModel.obtenerReciclajePorId(
            reciclajeId,
            onSuccess = { reciclaje ->
                reciclajeActual = reciclaje
                binding.spinnerTipo.setSelection(tipos.indexOf(reciclaje.tipo.capitalize()))
                binding.editCantidad.setText(reciclaje.cantidadKg.toString())

                // Mostrar foto si existe
                reciclaje.fotoUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    binding.imageViewFoto.visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(url)
                        .into(binding.imageViewFoto)
                }

                // Deshabilitar edición si no está pendiente
                if (reciclaje.estado != "pendiente") {
                    binding.editCantidad.isEnabled = false
                    binding.spinnerTipo.isEnabled = false
                    binding.btnActualizar.visibility = View.GONE
                }
            },
            onError = { error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        )
    }

    private fun configurarBotonActualizar() {
        binding.btnActualizar.setOnClickListener {
            actualizarReciclaje()
        }
    }

    private fun actualizarReciclaje() {
        val cantidad = binding.editCantidad.text.toString().toDoubleOrNull() ?: 0.0
        val tipo = binding.spinnerTipo.selectedItem.toString()

        if (cantidad <= 0) {
            Toast.makeText(requireContext(), "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        val reciclajeActualizado = reciclajeActual.copy(
            tipo = tipo.lowercase(),
            cantidadKg = cantidad
        )

        reciclajeViewModel.editarReciclaje(reciclajeActualizado) { success, errorMsg ->
            if (success) {
                Toast.makeText(requireContext(), "Reciclaje actualizado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), errorMsg ?: "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}