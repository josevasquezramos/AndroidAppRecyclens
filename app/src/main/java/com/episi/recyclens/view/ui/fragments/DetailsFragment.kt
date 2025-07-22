package com.episi.recyclens.view.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.episi.recyclens.databinding.FragmentDetailsBinding
import com.episi.recyclens.model.FraseMotivadora
import com.episi.recyclens.model.Reciclaje
import com.episi.recyclens.viewmodel.DetailsViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var detailsViewModel: DetailsViewModel

    private lateinit var pieChart: PieChart
    private lateinit var fraseTextView: TextView

    private lateinit var textTipoUltimoReciclaje: TextView
    private lateinit var textCantidadUltimoReciclaje: TextView
    private lateinit var textFechaUltimoReciclaje: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        detailsViewModel = ViewModelProvider(this).get(DetailsViewModel::class.java)
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inicializar la vista
        pieChart = binding.pieChart
        fraseTextView = binding.textFraseDelDia

        textTipoUltimoReciclaje = binding.textTipoUltimoReciclaje
        textCantidadUltimoReciclaje = binding.textCantidadUltimoReciclaje
        textFechaUltimoReciclaje = binding.textFechaUltimoReciclaje

        // Observar los datos del ViewModel
        detailsViewModel.reciclajesCanjeados.observe(viewLifecycleOwner) { reciclajes ->
            if (reciclajes.isNotEmpty()) {
                mostrarGraficoPieChart(reciclajes)
                mostrarUltimoReciclaje(reciclajes)
            } else {
                // Puedes mostrar un mensaje si no hay reciclajes canjeados
                Log.d("DetailsFragment", "No hay reciclajes canjeados")
            }
        }

        detailsViewModel.fraseDelDia.observe(viewLifecycleOwner) { frase ->
            if (frase != null) {
                mostrarFraseDelDia(frase)
            } else {
                // Si no hay frase, mostrar un mensaje
                fraseTextView.text = "No hay frase disponible"
                Log.d("DetailsFragment", "Frase del día no disponible")
            }
        }

        // Cargar datos
        detailsViewModel.obtenerReciclajesCanjeados()
        detailsViewModel.obtenerFraseDelDia()

        return root
    }

    private fun mostrarGraficoPieChart(reciclajes: List<Reciclaje>) {
        val tipoBasura = mutableMapOf<String, Double>()

        // Agrupar reciclajes por tipo de basura
        reciclajes.forEach { reciclaje ->
            tipoBasura[reciclaje.tipo] = tipoBasura.getOrDefault(reciclaje.tipo, 0.0) + reciclaje.cantidadKg
        }

        // Verifica que tipoBasura no esté vacío
        if (tipoBasura.isEmpty()) {
            Log.d("DetailsFragment", "No hay tipos de basura para mostrar")
        }

        // Crear entradas para el gráfico
        val entries = tipoBasura.map { PieEntry(it.value.toFloat(), it.key) }

        if (entries.isNotEmpty()) {
            val dataSet = PieDataSet(entries, "") // Título vacío para eliminar "Description label"
            dataSet.colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW) // Colores personalizados
            dataSet.valueTextColor = Color.BLACK // Color de los valores
            dataSet.valueTextSize = 12f // Tamaño de los valores
            dataSet.setDrawValues(true) // Mostrar los valores en las partes del gráfico

            // Personalizar las características del gráfico
            val data = PieData(dataSet)

            // Configuración del gráfico
            pieChart.data = data
            pieChart.setUsePercentValues(true) // Mostrar en porcentajes
            pieChart.setDrawSliceText(false) // No mostrar el texto dentro de cada sección
            pieChart.description.isEnabled = false // Deshabilitar el texto descriptivo ("Description label")
            pieChart.isDrawHoleEnabled = true // Hacer un agujero en el centro (para un "donut" chart)
            pieChart.holeRadius = 58f // Tamaño del agujero
            pieChart.setHoleColor(Color.TRANSPARENT) // Hacer el agujero transparente
            pieChart.transparentCircleRadius = 61f // Tamaño del círculo transparente alrededor del agujero
            pieChart.setRotationAngle(0f) // Inicializar sin rotación
            pieChart.animateY(1400, Easing.EaseInOutQuad) // Animación suave

            // Configuración de la leyenda
            pieChart.legend.isEnabled = true // Habilitar la leyenda
            pieChart.legend.form = Legend.LegendForm.CIRCLE // Formato de las leyendas (círculo)
            pieChart.legend.formSize = 10f // Tamaño de los círculos en la leyenda
            pieChart.legend.textColor = Color.BLACK // Color del texto de la leyenda

            // Refrescar el gráfico
            pieChart.invalidate() // Actualiza el gráfico
        } else {
            Log.d("DetailsFragment", "No se pudo generar el gráfico, no hay datos")
        }
    }

    private fun mostrarFraseDelDia(frase: FraseMotivadora) {
        fraseTextView.text = frase.texto
    }

    private fun mostrarUltimoReciclaje(reciclajes: List<Reciclaje>) {
        // Ordenar por timestamp descendente y tomar el primero
        val ultimoReciclaje = reciclajes.sortedByDescending { it.timestamp }.firstOrNull()

        ultimoReciclaje?.let {
            textTipoUltimoReciclaje.text = "Tipo: ${it.tipo}"
            textCantidadUltimoReciclaje.text = "Cantidad: ${it.cantidadKg} kg"
            textFechaUltimoReciclaje.text = "Fecha: ${formatDate(it.timestamp)}"
        } ?: run {
            textTipoUltimoReciclaje.text = "Tipo: -"
            textCantidadUltimoReciclaje.text = "Cantidad: - kg"
            textFechaUltimoReciclaje.text = "Fecha: -"
        }
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Fecha desconocida"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
