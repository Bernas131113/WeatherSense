package pt.ipt.weathersense

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ipt.weathersense.adapters.FavoritesAdapter
import pt.ipt.weathersense.models.AddFavoriteRequest
import pt.ipt.weathersense.network.RetrofitClient
import android.app.AlertDialog
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.util.Calendar
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.constraintlayout.widget.Group
import pt.ipt.weathersense.BuildConfig
import pt.ipt.weathersense.adapters.ForecastAdapter
import pt.ipt.weathersense.models.ForecastItem

import java.util.Date
class MainActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var tvTemperature: TextView
    private lateinit var tvFeelsLike: TextView
    private lateinit var tvWind: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var ivWeatherIcon: ImageView
    private lateinit var tvLocalTime: TextView
    private lateinit var tvCity: TextView
    private lateinit var searchLocation: Button
    private var isCardExpanded = true
    private lateinit var groupCardContent: androidx.constraintlayout.widget.Group
    private lateinit var btnToggleCard: ImageButton
    private lateinit var btnFavAction: Button
    private var favoriteCitiesList: MutableList<String> = ArrayList()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tvTemperature = findViewById(R.id.tvTemperature)
        tvFeelsLike = findViewById(R.id.tvFeelsLike)
        tvWind = findViewById(R.id.tvWind)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon)
        tvLocalTime = findViewById(R.id.tvLocalTime)
        tvCity = findViewById(R.id.tvCity)
        searchLocation = findViewById(R.id.searchLocation)
        button = findViewById(R.id.button)
        groupCardContent = findViewById(R.id.groupCardContent)
        btnToggleCard = findViewById(R.id.btnToggleCard)
        btnFavAction = findViewById(R.id.btnFavAction)


        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        button.setOnClickListener {
            checkLocationPermission()
        }
        val btnLogin = findViewById<Button>(R.id.btnGoToLogin)

        searchLocation.setOnClickListener {
            showSearchDialog()
        }


        btnLogin.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)
        }

        btnToggleCard.setOnClickListener {
            toggleCardVisibility()
        }

        setupFavoritesGrid()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request location permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions already granted, fetch location
            fetchLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch location
                fetchLocation()
            } else {
                // Permission denied, show message
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$API_KEY"

                    // Debugging: Log URL
                    println("Weather API URL: $weatherUrl")

                    fetchWeatherData(weatherUrl)

                } else {
                    tvTemperature.text = "Could not get location."
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchWeatherData(url: String) {
        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)

                    val main = jsonResponse.getJSONObject("main")
                    val temp = main.getDouble("temp").toInt().toString()
                    val feelsLike = main.getDouble("feels_like").toInt().toString()


                    val city = jsonResponse.getString("name")

                    val windObj = jsonResponse.getJSONObject("wind")
                    val windSpeed = windObj.getString("speed")

                    val weatherArray = jsonResponse.getJSONArray("weather")
                    val weatherObj = weatherArray.getJSONObject(0)
                    val iconCode = weatherObj.getString("icon")

                    // Calcular horas
                    // Obter o "shift" em segundos (ex: 3600)
                    val timezoneOffset = jsonResponse.getLong("timezone")
                    // Obter a hora atual em UTC
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    // Somar o desvio da cidade
                    calendar.add(Calendar.SECOND, timezoneOffset.toInt())



                    // Construir o URL da imagem (4x para ficar com melhor qualidade)
                    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@4x.png"

                    // Formatar as horas
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    // Garantir que não soma o fuso do telemóvel
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    val localTime = sdf.format(calendar.time)


                    // Usar as variaveis globais
                    tvCity.text = city
                    tvTemperature.text = "${temp}°C "
                    tvFeelsLike.text = "Sensação: ${feelsLike}°C"
                    tvWind.text = "Vento: ${windSpeed} m/s"
                    tvLocalTime.text = "Hora local: ${localTime}"

                    // Carregar a imagem com o Glide
                    Glide.with(this)
                        .load(iconUrl)
                        .into(ivWeatherIcon)

                    updateFavoriteButtonState(city)

                    val coord = jsonResponse.getJSONObject("coord")
                    val lat = coord.getDouble("lat")
                    val lon = coord.getDouble("lon")


                    fetchForecast(lat,lon)

                } catch (e: Exception) {
                    // Podes usar o tvDescription para mostrar erro se quiseres
                    tvTemperature.text = "Erro ao ler dados"
                    e.printStackTrace()
                }
            },
            { error ->
                tvTemperature.text = "Erro de ligação!"
                error.printStackTrace()
            })

        queue.add(request)
    }
    companion object {
        val API_KEY = BuildConfig.API_KEY
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    private fun fetchForecast(latitude: Double, longitude: Double) {
        val url = "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&units=metric&appid=$API_KEY"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)

                    // Obter o desvio de fuso horário da cidade (em segundos)
                    val cityObj = jsonResponse.getJSONObject("city")
                    val timezoneOffset = cityObj.getLong("timezone")

                    val list = jsonResponse.getJSONArray("list")
                    val forecastItems = ArrayList<ForecastItem>()

                    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()) // Ex: "Seg"

                    for (i in 0 until list.length()) {
                        val itemObj = list.getJSONObject(i)

                        // Obter o timestamp UTC da previsão (segundos)
                        val dt = itemObj.getLong("dt")

                        // Calcular a hora LOCAL nessa cidade (0-23)
                        // (dt + timezone) dá-nos a hora local em segundos Unix.
                        // Dividimos por 3600 para ter horas totais, e % 24 para ter a hora do dia.
                        val localHour = ((dt + timezoneOffset) / 3600) % 24


                        // Escolher previsões que sejam entre as 11h e as 13h locais.
                        // Como os dados vêm de 3 em 3 horas (ex: 10, 13, 16 ou 11, 14, 17),
                        // este intervalo garante que apanhamos sempre o "meio-dia" local.
                        if (localHour in 11..13) {

                            val main = itemObj.getJSONObject("main")
                            val temp = main.getDouble("temp").toInt().toString() + "°C"

                            val weatherArray = itemObj.getJSONArray("weather")
                            val icon = weatherArray.getJSONObject(0).getString("icon")

                            // Formatar data para obter o dia da semana
                            val dtTxt = itemObj.getString("dt_txt")
                            val date = inputFormat.parse(dtTxt)
                            val dayName = dayFormat.format(date!!)

                            forecastItems.add(ForecastItem(dayName, temp, icon))
                        }
                    }

                    val rvForecast = findViewById<RecyclerView>(R.id.rvForecast)
                    rvForecast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    rvForecast.adapter = ForecastAdapter(forecastItems)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            })

        Volley.newRequestQueue(this).add(request)
    }

    override fun onResume() {
        super.onResume()

        //checking if user is logged in
        val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
        val userEmail = sharedPref.getString("USER_EMAIL", "User")

        val btnLogin = findViewById<Button>(R.id.btnGoToLogin)

        val rvFavorites = findViewById<RecyclerView>(R.id.rvFavorites)

        //showing or hiding button based on login state
        if (isLoggedIn) {
            btnLogin.text = "Logout"

            tvUserEmail.text = "$userEmail" // Podes personalizar a mensagem
            tvUserEmail.visibility = View.VISIBLE

            btnLogin.setOnClickListener {
                //clear the session
                sharedPref.edit().clear().apply()

                Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()
                favoriteCitiesList.clear()
                // run it back
                onResume()
            }

            rvFavorites.visibility = View.VISIBLE

            // Carregar os dados da grelha
            setupFavoritesGrid()


        } else {
            btnLogin.text = "Login"
            tvUserEmail.visibility = View.GONE
            btnFavAction.visibility = View.GONE

            btnLogin.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            rvFavorites.visibility = View.GONE
        }



    }

    private fun setupFavoritesGrid() {
        val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", null)

        if (userId != null) {
            // User is logged in, fetch data
            fetchFavorites(userId)
        } else {
            // User is logged out, clear the grid (optional)
        }
    }

    private fun fetchFavorites(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getFavorites(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val cities = response.body()!!.favorites
                        favoriteCitiesList = cities.toMutableList()
                        val rvFavorites = findViewById<RecyclerView>(R.id.rvFavorites)

                        rvFavorites.layoutManager = GridLayoutManager(this@MainActivity, 1)
                        rvFavorites.adapter = FavoritesAdapter(
                            cities,
                            onCityClick = { clickedCityName ->
                                // 1. Ver Tempo
                                //Toast.makeText(this@MainActivity, "Loading $clickedCityName...", Toast.LENGTH_SHORT).show()
                                val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=$clickedCityName&units=metric&appid=$API_KEY"
                                fetchWeatherData(weatherUrl)
                            },

                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
    private fun testAddFavorite(userId: String, city: String) {
        CoroutineScope(Dispatchers.IO).launch {
            RetrofitClient.instance.addFavorite(AddFavoriteRequest(userId, city))
            // After adding, refresh the list
            fetchFavorites(userId)
        }
    }

    private fun showAddCityDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Favorite City")

        // Set up the input
        val input = EditText(this)
        input.hint = "Enter city name (e.g. Tokyo)"
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("Add") { dialog, which ->
            val cityName = input.text.toString()
            if (cityName.isNotEmpty()) {
                // Get User ID
                val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
                val userId = sharedPref.getString("USER_ID", null)

                if (userId != null) {
                    saveCityToBackend(userId, cityName)
                } else {
                    Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun showSearchDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pesquisar Localização")

        // configurar o input
        val input = EditText(this)
        input.hint = "Introduza o nome da cidade (ex: Porto)"
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // botões
        builder.setPositiveButton("Pesquisar") { dialog, which ->
            val cityName = input.text.toString().trim()
            if (cityName.isNotEmpty()) {

                // construir o URL da API para essa cidade
                val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&units=metric&appid=$API_KEY"


                // funcao que ja temos
                fetchWeatherData(weatherUrl)
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun saveCityToBackend(userId: String, cityName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.addFavorite(AddFavoriteRequest(userId, cityName))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "$cityName added!", Toast.LENGTH_SHORT).show()
                        // refresh grid
                        fetchFavorites(userId)
                        favoriteCitiesList.add(cityName)
                        updateFavoriteButtonState(cityName)
                    } else {
                        Toast.makeText(this@MainActivity, "Error adding city", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun deleteFavoriteCity(userId: String, cityName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Reutilizamos AddFavoriteRequest pq os dados são os mesmos (ID e Cidade)
                val response = RetrofitClient.instance.removeFavorite(AddFavoriteRequest(userId, cityName))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "$cityName removed!", Toast.LENGTH_SHORT).show()
                        // Atualiza a grelha
                        fetchFavorites(userId)
                        favoriteCitiesList.remove(cityName)
                        updateFavoriteButtonState(cityName)
                    } else {
                        Toast.makeText(this@MainActivity, "Error removing city", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun toggleCardVisibility() {
        isCardExpanded = !isCardExpanded

        if (isCardExpanded) {

            groupCardContent.visibility = View.VISIBLE
            btnToggleCard.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
            if (isLoggedIn) {
                btnFavAction.visibility = View.VISIBLE
            } else {
                btnFavAction.visibility = View.GONE
            }
        } else {
            groupCardContent.visibility = View.GONE
            btnFavAction.visibility = View.GONE
            btnToggleCard.setImageResource(android.R.drawable.ic_input_add)
        }
    }

    private fun updateFavoriteButtonState(currentCity: String) {
        val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
        val userId = sharedPref.getString("USER_ID", null)

        if (isLoggedIn && userId != null) {
            btnFavAction.visibility = View.VISIBLE
            val isFavorite = favoriteCitiesList.any { it.equals(currentCity, ignoreCase = true) }

            if (favoriteCitiesList.contains(currentCity)) {
                btnFavAction.text = "Remover dos Favoritos"

                btnFavAction.setOnClickListener {
                    deleteFavoriteCity(userId, currentCity)
                }
            } else {

                btnFavAction.text = "Adicionar aos Favoritos"
                btnFavAction.setOnClickListener {
                    saveCityToBackend(userId, currentCity)
                }
            }
        } else {
            // Se não tiver login, esconde o botão
            btnFavAction.visibility = View.GONE
        }
    }

}