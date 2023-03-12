package com.ionnier.pdma.ui.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ionnier.pdma.R
import com.ionnier.pdma.Utils
import com.ionnier.pdma.databinding.ListViewItemBinding
import timber.log.Timber
import java.util.*

class LanguageAdapter(var wifiList: List<String>, val onClickList: (String) -> Unit) : RecyclerView.Adapter<LanguageAdapter.ListViewHolder>() {

    class ListViewHolder(val binding: ListViewItemBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        // create view holder to hold reference
        return ListViewHolder(ListViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        //set values
        val context = holder.binding.root.context
        val countryCode = wifiList[position]
        val flagResource = Utils.getResource(countryCode, context) ?: R.drawable.unknown_flag
        holder.binding.imageFlag.setImageDrawable(ContextCompat.getDrawable(context, flagResource))
        holder.binding.ssid.text = Locale(Locale.getDefault().language, countryCode).displayCountry
        holder.binding.selectButton.setOnClickListener {
            onClickList(countryCode)
        }
    }

    override fun getItemCount(): Int {
        return wifiList.size
    }
}
