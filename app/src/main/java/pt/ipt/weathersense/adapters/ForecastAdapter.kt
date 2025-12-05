package pt.ipt.weathersense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipt.weathersense.R
import pt.ipt.weathersense.models.ForecastItem

class ForecastAdapter(private val forecastList: List<ForecastItem>) :
    RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tvDay)
        val tvTemp: TextView = view.findViewById(R.id.tvForecastTemp)
        val ivIcon: ImageView = view.findViewById(R.id.ivForecastIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = forecastList[position]
        holder.tvDay.text = item.dayOfWeek
        holder.tvTemp.text = item.temp

        val iconUrl = "https://openweathermap.org/img/wn/${item.iconCode}@2x.png"
        Glide.with(holder.itemView.context)
            .load(iconUrl)
            .into(holder.ivIcon)
    }

    override fun getItemCount() = forecastList.size
}