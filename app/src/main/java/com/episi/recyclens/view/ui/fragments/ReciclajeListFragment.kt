package com.episi.recyclens.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.episi.recyclens.databinding.FragmentReciclajeListBinding
import com.episi.recyclens.utils.FeedbackUtils
import com.episi.recyclens.view.adapter.ReciclajeAdapter
import com.episi.recyclens.viewmodel.ReciclajeViewModel

class ReciclajeListFragment : Fragment() {

    private var _binding: FragmentReciclajeListBinding? = null
    private val binding get() = _binding!!

    private val reciclajeViewModel: ReciclajeViewModel by viewModels()
    private lateinit var adapter: ReciclajeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReciclajeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ReciclajeAdapter { reciclaje ->
            if (reciclaje.estado == "canjeable") {
                reciclajeViewModel.marcarComoCanjeado(reciclaje.id, reciclaje.cantidadKg) { success, message ->
                    // Verificar si el fragmento sigue montado antes de usar context o view
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

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.fab.setOnClickListener {
            findNavController().navigate(
                ReciclajeListFragmentDirections.actionReciclajeListFragmentToAgregarReciclajeFragment()
            )
        }

        reciclajeViewModel.reciclajes.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        reciclajeViewModel.error.observe(viewLifecycleOwner, Observer { errorMsg ->
            errorMsg?.let {
                if (isAdded) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        })

        reciclajeViewModel.cargarReciclajes()
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