package com.episi.recyclens.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import com.episi.recyclens.R
import com.episi.recyclens.databinding.FragmentHomeBinding
import com.episi.recyclens.view.adapter.CarouselAdapter
import com.episi.recyclens.viewmodel.HomeViewModel
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val carouselItems = listOf(
            CarouselAdapter.CarouselItem(R.drawable.logo_scrollable_first, "♻\uFE0F Reciclar es cuidar el planeta."),
            CarouselAdapter.CarouselItem(R.drawable.logo_scrollable_second, "\uD83C\uDF0D Tu basura puede tener una segunda vida."),
            CarouselAdapter.CarouselItem(R.drawable.logo_scrollable_third, "\uD83D\uDDD1\uFE0F Separar hoy, salvar mañana."),
            CarouselAdapter.CarouselItem(R.drawable.papel, "Recicla el papel para salvar árboles"),
            CarouselAdapter.CarouselItem(R.drawable.metal, "El metal reciclado ahorra energía"),
            CarouselAdapter.CarouselItem(R.drawable.plastico, "El plástico tarda siglos en degradarse"),
            CarouselAdapter.CarouselItem(R.drawable.organico, "Los residuos orgánicos son compostables")
        )

        val adapter = CarouselAdapter(carouselItems)
        binding.viewPager.adapter = adapter

        binding.startButton.setOnClickListener {

            findNavController().navigate(R.id.reciclajeListFragment)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}