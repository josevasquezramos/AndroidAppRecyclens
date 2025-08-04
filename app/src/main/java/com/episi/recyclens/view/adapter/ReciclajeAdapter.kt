package com.episi.recyclens.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.episi.recyclens.R
import com.episi.recyclens.model.Reciclaje
import com.episi.recyclens.view.ui.fragments.ReciclajeListFragmentDirections

class ReciclajeAdapter(
    private val onCanjearClick: (Reciclaje) -> Unit,
    private val onEditarClick: (Reciclaje) -> Unit,
    private val onEliminarClick: (Reciclaje) -> Unit
) : ListAdapter<Reciclaje, ReciclajeAdapter.ReciclajeViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReciclajeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reciclaje, parent, false)
        return ReciclajeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReciclajeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReciclajeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tipoText: TextView = view.findViewById(R.id.textTipo)
        private val cantidadText: TextView = view.findViewById(R.id.textCantidad)
        private val estadoText: TextView = view.findViewById(R.id.textEstado)
        private val btnCanjear: Button = view.findViewById(R.id.btnCanjear)

        fun bind(item: Reciclaje) {
            tipoText.text = "Tipo: ${item.tipo}"
            cantidadText.text = "Cantidad: ${item.cantidadKg} kg"
            estadoText.text = "Estado: ${item.estado}"

            btnCanjear.text = when (item.estado) {
                "pendiente" -> "Pendiente"
                "canjeable" -> "Canjear"
                "canjeado" -> "Canjeado"
                else -> "No canjeable"
            }
            btnCanjear.isEnabled = item.estado == "canjeable"

            // Aquí añadimos el click listener para el item completo
            itemView.setOnClickListener {
                // Necesitamos acceso al NavController, lo obtenemos a través de la vista
                val navController = findNavController(itemView)
                val action = ReciclajeListFragmentDirections
                    .actionReciclajeListFragmentToReciclajeDetailFragment(item.id)
                navController.navigate(action)
            }

            btnCanjear.setOnClickListener {
                if (item.estado == "canjeable") {
                    onCanjearClick(item)
                }
            }

            itemView.setOnLongClickListener {
                if (item.estado == "pendiente") {
                    mostrarMenuContextual(item)
                } else {
                    Toast.makeText(itemView.context,
                        "Opciones disponibles solo para reciclajes pendientes",
                        Toast.LENGTH_SHORT).show()
                }
                true
            }
        }

        private fun mostrarMenuContextual(reciclaje: Reciclaje) {
            PopupMenu(itemView.context, itemView).apply {
                menuInflater.inflate(R.menu.menu_reciclaje, menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_editar -> {
                            onEditarClick(reciclaje)
                            true
                        }
                        R.id.menu_eliminar -> {
                            onEliminarClick(reciclaje) // Delegamos al Fragment
                            true
                        }
                        else -> false
                    }
                }
                show()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Reciclaje>() {
        override fun areItemsTheSame(oldItem: Reciclaje, newItem: Reciclaje): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reciclaje, newItem: Reciclaje): Boolean {
            return oldItem == newItem
        }
    }
}
