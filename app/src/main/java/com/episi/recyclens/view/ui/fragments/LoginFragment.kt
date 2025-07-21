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
import com.episi.recyclens.databinding.FragmentLoginBinding
import com.episi.recyclens.model.User
import com.episi.recyclens.network.Callback
import com.episi.recyclens.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Correo y contraseña son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.loginProgress.visibility = View.VISIBLE

            viewModel.login(email, password, object : Callback<User> {
                override fun onSuccess(result: User?) {
                    binding.loginProgress.visibility = View.GONE
                    Toast.makeText(requireContext(), "¡Bienvenido ${result?.displayName ?: ""}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }

                override fun onFailed(exception: Exception) {
                    binding.loginProgress.visibility = View.GONE
                    Toast.makeText(requireContext(), exception.message ?: "Error desconocido", Toast.LENGTH_LONG).show()
                }
            })
        }

        binding.resetButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.sendResetPassword(email, object : Callback<String> {
                    override fun onSuccess(result: String?) {
                        Toast.makeText(requireContext(), result ?: "Correo enviado", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailed(exception: Exception) {
                        Toast.makeText(requireContext(), exception.message ?: "Error al enviar el correo", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                Toast.makeText(requireContext(), "Ingresa tu correo", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(com.episi.recyclens.R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
