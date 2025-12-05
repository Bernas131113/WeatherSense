package pt.ipt.weathersense.models

data class ForecastItem(
    val dayOfWeek: String,
    val temp: String,
    val iconCode: String
)