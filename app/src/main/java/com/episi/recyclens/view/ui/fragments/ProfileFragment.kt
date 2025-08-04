// ProfileFragment.kt
package com.episi.recyclens.view.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.episi.recyclens.databinding.FragmentProfileBinding
import com.episi.recyclens.view.ui.activities.AuthActivity
import com.episi.recyclens.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            binding.tvUserName.text = user.displayName
            binding.tvUserEmail.text = user.email
        }

        viewModel.reciclajeStats.observe(viewLifecycleOwner) { stats ->
            binding.tvCantidadPapel.text = "${stats["papel"] ?: 0.0} kg"
            binding.tvCantidadPlastico.text = "${stats["plastico"] ?: 0.0} kg"
            binding.tvCantidadMetal.text = "${stats["metal"] ?: 0.0} kg"
            binding.tvCantidadOrganico.text = "${stats["organico"] ?: 0.0} kg"
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                // Mostrar error al usuario
            }
        }

        viewModel.logoutEvent.observe(viewLifecycleOwner) { loggedOut ->
            if (loggedOut == true) {
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun setupListeners() {
        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}