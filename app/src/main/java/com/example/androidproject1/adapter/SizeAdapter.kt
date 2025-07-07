package com.example.androidproject1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject1.R

class SizeAdapter(
    sizes: List<String>?,
    private val onSizeSelected: (String) -> Unit
) : RecyclerView.Adapter<SizeAdapter.SizeViewHolder>() {

    private val safeSizes = sizes?.filterNotNull()?.filter { it.isNotBlank() } ?: emptyList()
    private var selectedPosition = -1

    class SizeViewHolder(
        itemView: View,
        private val onSizeSelected: (String) -> Unit,
        private val adapter: SizeAdapter
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvSize: TextView = itemView.findViewById(R.id.tv_size)

        fun bind(size: String, isSelected: Boolean) {
            tvSize.text = size

            if (isSelected) {
                tvSize.setBackgroundResource(R.drawable.size_selector_selected)
                tvSize.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
            } else {
                tvSize.setBackgroundResource(R.drawable.size_selector)
                tvSize.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
            }

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val previousPosition = adapter.selectedPosition
                    adapter.selectedPosition = position

                    if (previousPosition != -1) {
                        adapter.notifyItemChanged(previousPosition)
                    }
                    adapter.notifyItemChanged(adapter.selectedPosition)

                    onSizeSelected(adapter.safeSizes[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_size, parent, false)
        return SizeViewHolder(view, onSizeSelected, this)
    }

    override fun onBindViewHolder(holder: SizeViewHolder, position: Int) {
        val size = safeSizes[position]
        val isSelected = position == selectedPosition
        holder.bind(size, isSelected)
    }

    override fun getItemCount(): Int = safeSizes.size

    fun getSelectedSize(): String? {
        return if (selectedPosition != -1) safeSizes[selectedPosition] else null
    }

    fun setSelectedSize(size: String) {
        val position = safeSizes.indexOf(size)
        if (position != -1) {
            val previousPosition = selectedPosition
            selectedPosition = position

            if (previousPosition != -1) {
                notifyItemChanged(previousPosition)
            }
            notifyItemChanged(selectedPosition)
        }
    }

    fun clearSelection() {
        val previousPosition = selectedPosition
        selectedPosition = -1
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition)
        }
    }
}
