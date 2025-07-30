package com.episi.recyclens.view.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.episi.recyclens.R
import com.episi.recyclens.databinding.FragmentMapBinding
import com.episi.recyclens.model.PuntoReciclaje
import com.episi.recyclens.viewmodel.MapViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: MapViewModel
    private var currentLocation: Location? = null

    private val tipos = listOf("papel", "metal", "plástico", "orgánico")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupSpinner()
        setupMap()

        binding.btnBuscar.setOnClickListener {
            val tipoSeleccionado = binding.spinnerTipo.selectedItem.toString()
            buscarPuntos(tipoSeleccionado)
        }

        return binding.root
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipo.adapter = adapter
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        pedirPermisoUbicacion()
    }

    private fun pedirPermisoUbicacion() {
        if (tienePermisoUbicacion()) {
            obtenerUbicacion()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }

    private fun tienePermisoUbicacion(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun obtenerUbicacion() {
        if (!tienePermisoUbicacion()) {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    val latLng = LatLng(location.latitude, location.longitude)
                    try {
                        map.isMyLocationEnabled = true
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                } else {
                    Toast.makeText(requireContext(), "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun buscarPuntos(tipo: String) {
        viewModel.obtenerPuntosPorTipo(
            tipo = tipo,
            onResult = { puntos -> mostrarPuntos(puntos) },
            onError = { e ->
                Toast.makeText(requireContext(), "Error al obtener puntos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun mostrarPuntos(puntos: List<PuntoReciclaje>) {
        map.clear()
        currentLocation?.let { ubicacionActual ->

            val masCercano = puntos.minByOrNull { punto ->
                val coord = punto.coordenadas
                if (coord != null) {
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        ubicacionActual.latitude, ubicacionActual.longitude,
                        coord.latitude, coord.longitude, results
                    )
                    results[0]
                } else {
                    Float.MAX_VALUE
                }
            }

            puntos.forEach { punto ->
                val coord = punto.coordenadas
                if (coord != null) {
                    val latLng = LatLng(coord.latitude, coord.longitude)
                    map.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(punto.nombreLugar)
                            .snippet(punto.direccion)
                    )
                }
            }

            masCercano?.coordenadas?.let { destino ->
                val destinoLatLng = LatLng(destino.latitude, destino.longitude)
                val origenLatLng = LatLng(ubicacionActual.latitude, ubicacionActual.longitude)
                trazarRuta(origenLatLng, destinoLatLng)
            }
        }
    }

    private fun trazarRuta(origen: LatLng, destino: LatLng) {
        val apiKey = try {
            val applicationInfo = requireContext().packageManager
                .getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
            applicationInfo.metaData.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            null
        }

        if (apiKey.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "API Key no encontrada", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origen.latitude},${origen.longitude}" +
                "&destination=${destino.latitude},${destino.longitude}" +
                "&mode=walking" +
                "&key=$apiKey"

        Thread {
            try {
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Error al obtener la ruta", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val body = response.body?.string()
                if (body.isNullOrEmpty()) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val jsonObject = org.json.JSONObject(body)
                val routes = jsonObject.optJSONArray("routes")

                if (routes != null && routes.length() > 0) {
                    val overviewPolyline = routes.getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")

                    val decodedPath = decodePolyline(overviewPolyline)

                    requireActivity().runOnUiThread {
                        map.addPolyline(
                            PolylineOptions()
                                .addAll(decodedPath)
                                .color(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark))
                                .width(8f)
                        )
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(destino, 14f))
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "No se encontró una ruta", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error de red o parsing", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }


    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return poly
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
