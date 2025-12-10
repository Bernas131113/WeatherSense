package pt.ipt.weathersense.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.weathersense.R

import android.app.AlertDialog
import android.widget.Toast

class FavoritesAdapter(private val cityList: List<String>,
                       private val onCityClick: (String) -> Unit) :
    RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCityName: TextView = view.findViewById(R.id.tvCityName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_city, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = cityList[position]
        holder.tvCityName.text = city

        holder.itemView.setOnClickListener {
            onCityClick(city) // Pass the city name back to MainActivity
        }

    }

    override fun getItemCount() = cityList.size
}