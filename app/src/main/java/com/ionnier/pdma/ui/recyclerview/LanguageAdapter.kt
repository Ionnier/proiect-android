package com.ionnier.pdma.ui.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
        holder.binding.ssid.text = Locale(Locale.getDefault().language, wifiList[position]).displayCountry
        holder.binding.selectButton.setOnClickListener {
            onClickList(wifiList[position])
        }
    }

    override fun getItemCount(): Int {
        return wifiList.size
    }
}
