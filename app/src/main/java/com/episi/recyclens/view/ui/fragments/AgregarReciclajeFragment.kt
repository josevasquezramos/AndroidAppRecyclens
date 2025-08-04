package com.episi.recyclens.view.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.episi.recyclens.databinding.FragmentAgregarReciclajeBinding
import com.episi.recyclens.model.Reciclaje
import com.episi.recyclens.viewmodel.ReciclajeViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File

class AgregarReciclajeFragment : Fragment() {

    private var _binding: FragmentAgregarReciclajeBinding? = null
    private val binding get() = _binding!!
    private val reciclajeViewModel: ReciclajeViewModel by viewModels()
    private var ubicacionActual: Location? = null
    private var fotoUri: Uri? = null

    private val tomarFotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            fotoUri?.let {
                binding.imageViewFoto.setImageURI(it)
                binding.imageViewFoto.visibility = View.VISIBLE
            }
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            obtenerUbicacion()
        } else {
            Toast.makeText(
                requireContext(),
                "Permiso de ubicación requerido para registrar el reciclaje",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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

        // Verificación moderna de permisos
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                obtenerUbicacion()
            }
            else -> {
                locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        binding.btnTomarFoto.setOnClickListener {
            tomarFoto()
        }

        binding.btnGuardar.setOnClickListener {
            validarYGuardarReciclaje()
        }
    }

    private fun tomarFoto() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val fotoFile = File.createTempFile(
                "JPEG_${System.currentTimeMillis()}_",
                ".jpg",
                requireContext().externalCacheDir
            )
            fotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                fotoFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
            tomarFotoLauncher.launch(intent)
        } else {
            cameraPermissionRequest.launch(Manifest.permission.CAMERA)
        }
    }

    private val cameraPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tomarFoto()
        } else {
            Toast.makeText(
                requireContext(),
                "Permiso de cámara requerido para tomar fotos",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun validarYGuardarReciclaje() {
        // Bloquear controles al inicio
        bloquearControles(true)

        val cantidadStr = binding.editCantidad.text.toString()
        val tipo = binding.spinnerTipo.selectedItem.toString()

        when {
            cantidadStr.isBlank() -> {
                Toast.makeText(requireContext(), "Ingresa la cantidad", Toast.LENGTH_SHORT).show()
                bloquearControles(false)
                return
            }
            ubicacionActual == null -> {
                Toast.makeText(requireContext(), "Obteniendo ubicación...", Toast.LENGTH_SHORT).show()
                bloquearControles(false)
                return
            }
        }

        val cantidad = cantidadStr.toDoubleOrNull()?.takeIf { it > 0 } ?: run {
            Toast.makeText(requireContext(), "Cantidad inválida", Toast.LENGTH_SHORT).show()
            bloquearControles(false)
            return
        }

        val reciclaje = Reciclaje(
            tipo = tipo.lowercase(),
            cantidadKg = cantidad,
            latitud = ubicacionActual!!.latitude,
            longitud = ubicacionActual!!.longitude,
            estado = "pendiente"
        )

        fotoUri?.let { uri ->
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    reciclajeViewModel.agregarReciclajeConFoto(requireContext(), reciclaje, uri) { success, errorMsg ->
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                bloquearControles(false) // Restaurar controles
                                if (success) {
                                    Toast.makeText(requireContext(), "Reciclaje guardado con foto", Toast.LENGTH_SHORT).show()
                                    findNavController().popBackStack()
                                } else {
                                    Toast.makeText(requireContext(), errorMsg ?: "Error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            bloquearControles(false)
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } ?: run {
            reciclajeViewModel.agregarReciclaje(reciclaje) { success, errorMsg ->
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        bloquearControles(false) // Restaurar controles
                        if (success) {
                            Toast.makeText(requireContext(), "Reciclaje guardado", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        } else {
                            Toast.makeText(requireContext(), errorMsg ?: "Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun bloquearControles(bloquear: Boolean) {
        with(binding) {
            btnGuardar.isEnabled = !bloquear
            btnTomarFoto.isEnabled = !bloquear
            spinnerTipo.isEnabled = !bloquear
            editCantidad.isEnabled = !bloquear

            if (bloquear) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun obtenerUbicacion() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                ubicacionActual = location ?: run {
                    Toast.makeText(requireContext(), "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show()
                    null
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al obtener ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}