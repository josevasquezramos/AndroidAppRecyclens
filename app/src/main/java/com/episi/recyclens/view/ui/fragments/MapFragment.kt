package com.episi.recyclens.view.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.episi.recyclens.databinding.FragmentMapBinding
import com.episi.recyclens.network.PuntoReciclajeRepository
import com.episi.recyclens.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.episi.recyclens.R

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MapViewModel
    private lateinit var googleMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var isFirstLocationUpdate = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        // Inicialización del repositorio y ViewModel
        val repository = PuntoReciclajeRepository()
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapViewModel(repository) as T
            }
        }).get(MapViewModel::class.java)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnBuscar.setOnClickListener {
            val tipoSeleccionado = binding.spinnerTipo.selectedItem.toString()
            viewModel.userMarker.value?.position?.let { userPosition ->
                viewModel.buscarPuntosPorTipo(tipoSeleccionado, userPosition)
            } ?: run {
                viewModel.buscarPuntosPorTipo(tipoSeleccionado, null)
            }
        }

        return binding.root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setInfoWindowAdapter(this)

        setupObservers()
        getCurrentLocation()
    }

    private fun setupObservers() {
        viewModel.recyclingMarkers.observe(viewLifecycleOwner) { markers ->
            googleMap.clear()

            viewModel.userMarker.value?.let { userMarker ->
                googleMap.addMarker(userMarker)
            }

            markers?.forEach { marker ->
                googleMap.addMarker(
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
            }
        }

        viewModel.userMarker.observe(viewLifecycleOwner) { marker ->
            marker?.let {
                val markerOnMap = googleMap.addMarker(it)
                if (isFirstLocationUpdate) {
                    markerOnMap?.let {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it.position, 15f))
                        isFirstLocationUpdate = false
                    }
                }
            }
        }

        viewModel.mapCenter.observe(viewLifecycleOwner) { bounds ->
            try {
                // Mover cámara para mostrar todos los marcadores con padding
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        100 // padding en pixels
                    )
                )
            } catch (e: Exception) {
                // Si hay error (generalmente por bounds muy pequeños), hacer zoom a un nivel predeterminado
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        bounds.center,
                        12f
                    )
                )
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.takeIf { it.isNotEmpty() }?.let {
                android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getInfoContents(marker: Marker): View? = null

    override fun getInfoWindow(marker: Marker): View {
        val view = layoutInflater.inflate(R.layout.custom_info_window, null)
        val tvTitle = view.findViewById<TextView>(R.id.info_window_title)
        val tvSnippet = view.findViewById<TextView>(R.id.info_window_snippet)
        tvTitle.text = marker.title
        tvSnippet.text = marker.snippet
        return view
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        val fusedLocationClient = com.google.android.gms.location.LocationServices
            .getFusedLocationProviderClient(requireActivity())

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                viewModel.setUserPosition(currentLatLng)

                // Centrar mapa en la ubicación actual al inicio
                if (isFirstLocationUpdate) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    isFirstLocationUpdate = false
                }
            }
        }.addOnFailureListener { e ->
            val defaultLocation = LatLng(-9.119075735215851, -78.51513684557689)
            viewModel.setUserPosition(defaultLocation)
            if (isFirstLocationUpdate) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
                isFirstLocationUpdate = false
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    val defaultLocation = LatLng(-9.119075735215851, -78.51513684557689)
                    viewModel.setUserPosition(defaultLocation)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}