package com.episi.recyclens.view.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.episi.recyclens.databinding.FragmentAgregarReciclajeBinding
import com.episi.recyclens.model.Reciclaje
import com.episi.recyclens.viewmodel.ReciclajeViewModel
import com.google.android.gms.location.LocationServices

class AgregarReciclajeFragment : Fragment() {

    private var _binding: FragmentAgregarReciclajeBinding? = null
    private val binding get() = _binding!!

    private val reciclajeViewModel: ReciclajeViewModel by viewModels()

    private var ubicacionActual: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgregarReciclajeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tipos = listOf("Papel", "Plástico", "Metal", "Orgánico")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipos)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipo.adapter = spinnerAdapter

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        } else {
            obtenerUbicacion()
        }

        obtenerUbicacion()

        binding.btnGuardar.setOnClickListener {
            val cantidadStr = binding.editCantidad.text.toString()
            val tipo = binding.spinnerTipo.selectedItem.toString()

            if (cantidadStr.isBlank() || ubicacionActual == null) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cantidad = cantidadStr.toDoubleOrNull()
            if (cantidad == null || cantidad <= 0) {
                Toast.makeText(requireContext(), "Cantidad inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reciclaje = Reciclaje(
                tipo = tipo.lowercase(),
                cantidadKg = cantidad,
                latitud = ubicacionActual!!.latitude,
                longitud = ubicacionActual!!.longitude,
                estado = "pendiente"
            )

            reciclajeViewModel.agregarReciclaje(reciclaje) { success, errorMsg ->
                if (success) {
                    Toast.makeText(requireContext(), "Reciclaje guardado", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), errorMsg ?: "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun obtenerUbicacion() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Permiso de ubicación no otorgado", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    ubicacionActual = location
                } else {
                    Toast.makeText(requireContext(), "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion()
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
