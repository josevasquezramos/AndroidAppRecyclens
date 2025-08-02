package com.episi.recyclens.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.episi.recyclens.databinding.FragmentReciclajeDetailBinding
import com.episi.recyclens.utils.FeedbackUtils
import com.episi.recyclens.viewmodel.ReciclajeDetailViewModel
import java.text.SimpleDateFormat

class ReciclajeDetailFragment : Fragment() {

    private var _binding: FragmentReciclajeDetailBinding? = null
    private val binding get() = _binding!!
    private val args: ReciclajeDetailFragmentArgs by navArgs()
    private val viewModel: ReciclajeDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReciclajeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observarReciclaje(args.reciclajeId)

        viewModel.reciclaje.observe(viewLifecycleOwner) { reciclaje ->
            if (reciclaje != null) {
                binding.textTipo.text = "Tipo: ${reciclaje.tipo}"
                binding.textCantidad.text = "Cantidad: ${reciclaje.cantidadKg} kg"
                binding.textEstado.text = "Estado: ${reciclaje.estado}"
                binding.textUbicacion.text = "Ubicación: (${reciclaje.latitud}, ${reciclaje.longitud})"
                binding.textFecha.text = "Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm").format(reciclaje.timestamp)}"

                binding.btnCanjear.apply {
                    text = when (reciclaje.estado) {
                        "pendiente" -> "Pendiente"
                        "canjeable" -> "Canjear"
                        "canjeado" -> "Canjeado"
                        else -> "No canjeable"
                    }
                    isEnabled = reciclaje.estado == "canjeable"
                    setOnClickListener {
                        if (reciclaje.estado == "canjeable") {
                            viewModel.marcarComoCanjeado(reciclaje.id, reciclaje.cantidadKg) { success, message ->
                                if (!isAdded) return@marcarComoCanjeado

                                if (success) {
                                    darFeedbackExito()
                                    Toast.makeText(requireContext(), "¡Reciclaje canjeado! Felicidades 🎉", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                if (isAdded) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @RequiresPermission(android.Manifest.permission.VIBRATE)
    private fun darFeedbackExito() {
        context?.let {
            FeedbackUtils.vibrar(it)
            FeedbackUtils.reproducirSonidoCanjeo(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}