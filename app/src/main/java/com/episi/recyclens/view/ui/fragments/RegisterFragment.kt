package com.episi.recyclens.view.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.episi.recyclens.view.ui.activities.MainActivity
import com.episi.recyclens.databinding.FragmentRegisterBinding
import com.episi.recyclens.model.User
import com.episi.recyclens.network.Callback
import com.episi.recyclens.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.registerButton.setOnClickListener {
            val displayName = binding.displayNameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Correo y contraseña son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.registerProgress.visibility = View.VISIBLE

            viewModel.register(email, password, displayName, object : Callback<User> {
                override fun onSuccess(result: User?) {
                    binding.registerProgress.visibility = View.GONE
                    Toast.makeText(requireContext(), "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }

                override fun onFailed(exception: Exception) {
                    binding.registerProgress.visibility = View.GONE
                    Toast.makeText(requireContext(), exception.message ?: "Error desconocido", Toast.LENGTH_LONG).show()
                }
            })
        }

        binding.backToLoginButton.setOnClickListener {
            findNavController().navigate(com.episi.recyclens.R.id.action_registerFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
